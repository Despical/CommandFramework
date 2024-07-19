package me.despical.commandframework;

import me.despical.commandframework.annotations.Command;
import me.despical.commandframework.annotations.Completer;
import me.despical.commandframework.utils.Utils;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.SimplePluginManager;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Stream;

/**
 * @author Despical
 * <p>
 * Created at 18.07.2024
 */
@ApiStatus.Internal
public class CommandRegistry {

	@Nullable
	private CommandMap commandMap;

	@NotNull
	private final CommandFramework commandFramework;

	@NotNull
	private final CommandMatcher commandMatcher;

	@NotNull
	private final Map<Command, Map.Entry<Method, Object>> commands, subCommands;

	@NotNull
	private final Map<Completer, Map.Entry<Method, Object>> commandCompletions, subCommandCompletions;

	CommandRegistry(CommandFramework commandFramework) {
		this.commandFramework = commandFramework;
		this.commandMatcher = new CommandMatcher();
		this.commands = new HashMap<>();
		this.commandCompletions = new HashMap<>();
		this.subCommands = new TreeMap<>(Comparator.comparing(Command::name).reversed());
		this.subCommandCompletions = new TreeMap<>(Comparator.comparing(Completer::name).reversed());

		final PluginManager pluginManager = commandFramework.plugin.getServer().getPluginManager();

		if (pluginManager instanceof SimplePluginManager) {
			final SimplePluginManager manager = (SimplePluginManager) pluginManager;

			try {
				final Field field = SimplePluginManager.class.getDeclaredField("commandMap");
				field.setAccessible(true);

				commandMap = (CommandMap) field.get(manager);
			} catch (ReflectiveOperationException exception) {
				exception.printStackTrace();
			}
		}
	}

	public void setCommandMap(@Nullable CommandMap commandMap) {
		this.commandMap = commandMap;
	}

	/**
	 * Registers commands in given object's class.
	 *
	 * @param instance the class instance of given object.
	 */
	protected void registerCommands(@NotNull Object instance) {
		for (final Method method : instance.getClass().getMethods()) {
			final Command command = method.getAnnotation(Command.class);

			if (command != null) {
				registerCommand(command, method, instance);

				// Register all aliases as a plugin command. If it is a sub-command then register it as a sub-command.
				Stream.of(command.aliases()).forEach(alias -> registerCommand(Utils.createCommand(command, alias), method, instance));
			} else if (method.isAnnotationPresent(Completer.class)) {
				if (!List.class.isAssignableFrom(method.getReturnType())) {
					commandFramework.getLogger().log(Level.WARNING, "Skipped registration of ''{0}'' because it is not returning java.util.List type.", method.getName());
					continue;
				}

				final Completer completer = method.getAnnotation(Completer.class);

				if (completer.name().contains(".")) {
					subCommandCompletions.put(completer, Utils.mapEntry(method, instance));
				} else {
					commandCompletions.put(completer, Utils.mapEntry(method, instance));
				}
			}
		}

		subCommands.forEach((key, value) -> {
			final String splitName = key.name().split("\\.")[0];

			// Framework is going to work properly but this should not be handled that way.
			if (commands.keySet().stream().noneMatch(cmd -> cmd.name().equals(splitName))) {
				commandFramework.getLogger().log(Level.WARNING, "A sub-command (name: ''{0}'') is directly registered without a main command.", splitName);

				registerCommand(Utils.createCommand(key, splitName), null, null);
			}
		});
	}

	/**
	 * Registers the command with given parameters if there are any.
	 *
	 * @param command  the command object of registered command method.
	 * @param method   the command method which will invoked run when the
	 *                 command is executed.
	 * @param instance the class instance of the command method.
	 */
	protected void registerCommand(Command command, Method method, Object instance) {
		final String cmdName = command.name();

		if (cmdName.contains(".")) {
			subCommands.put(command, Utils.mapEntry(method, instance));
		} else {
			commands.put(command, Utils.mapEntry(method, instance));

			try {
				final Constructor<PluginCommand> constructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
				constructor.setAccessible(true);

				final PluginCommand pluginCommand = constructor.newInstance(cmdName, commandFramework.plugin);
				pluginCommand.setTabCompleter(commandFramework);
				pluginCommand.setExecutor(commandFramework);
				pluginCommand.setUsage(command.usage());
				pluginCommand.setPermission(!command.permission().isEmpty() ? null : command.permission());
				pluginCommand.setDescription(command.desc());

				commandMap.register(cmdName, pluginCommand);
			} catch (Exception exception) {
				Utils.handleExceptions(exception);
			}
		}
	}

	/**
	 * Unregisters command and tab completer if there is with the given name.
	 *
	 * @param commandName name of the command that's going to be removed
	 */
	protected void unregisterCommand(@NotNull String commandName) {
		if (commandName.contains(".")) commandName = commandName.split("\\.")[0];

		final Map.Entry<Command, Map.Entry<Method, Object>> entry = commandMatcher.getAssociatedCommand(commandName, new String[0]);

		if (entry == null) {
			commandFramework.plugin.getLogger().log(Level.WARNING, "Command removal is failed because there is no command named ''{0}''!", commandName);
			return;
		}

		final Command command = entry.getKey();
		final String name = command.name();
		final PluginCommand pluginCommand = commandFramework.plugin.getServer().getPluginCommand(name);

		Optional.ofNullable(pluginCommand).ifPresent(cmd -> {
			// Do not unregister if matched command is not registered from our instance plugin.
			if (!pluginCommand.getPlugin().equals(commandFramework.plugin))
				return;

			try {
				cmd.unregister(commandMap);

				Field field = SimpleCommandMap.class.getDeclaredField("knownCommands");
				field.setAccessible(true);

				Map<String, org.bukkit.command.Command> knownCommands = (Map<String, org.bukkit.command.Command>) field.get(commandMap);
				knownCommands.remove(name);
			} catch (Exception exception) {
				commandFramework.getLogger().log(Level.WARNING, "Something went wrong while trying to unregister command(name: {0}) from server!", name);
			}

			this.commands.remove(command);
			this.subCommands.entrySet().removeIf(subEntry -> subEntry.getKey().name().startsWith(name));
		});
	}

	/**
	 * Unregisters all of registered commands and tab completers created using that instance.
	 */
	protected void unregisterCommands() {
		Iterator<String> names = commands.keySet().stream().map(Command::name).iterator();

		while (names.hasNext()) {
			this.unregisterCommand(names.next());
		}
	}

	@NotNull
	protected Set<Command> getCommands() {
		return this.commands.keySet();
	}

	@NotNull
	protected Set<Command> getSubCommands() {
		return this.subCommands.keySet();
	}

	@NotNull
	protected CommandMatcher getCommandMatcher() {
		return this.commandMatcher;
	}

	protected final class CommandMatcher {

		@Nullable
		public Map.Entry<Command, Map.Entry<Method, Object>> getAssociatedCommand(@NotNull String commandName, @NotNull String[] possibleArgs) {
			Command command = null;

			// Search for the sub commands first
			for (Command cmd : subCommands.keySet()) {
				final String name = cmd.name(), cmdName = commandName + (possibleArgs.length == 0 ? "" : "." + String.join(".", Arrays.copyOfRange(possibleArgs, 0, name.split("\\.").length - 1)));
				// Checking aliases...
				if (name.equals(cmdName)) {
					command = cmd;
					break;
				}

				if (name.equalsIgnoreCase(cmdName)) {
					command = cmd;
					break;
				}
			}

			// If we found the sub command then return it, otherwise search the commands map
			if (command != null) {
				return Utils.mapEntry(command, subCommands.get(command));
			}

			// If our command is not a sub command then search for a main command
			for (Command cmd : commands.keySet()) {
				final String name = cmd.name();

				if (name.equalsIgnoreCase(commandName) || Stream.of(cmd.aliases()).anyMatch(commandName::equalsIgnoreCase)) {
					return Utils.mapEntry(cmd, commands.get(cmd));
				}
			}

			// Return null if the given command is not registered by Command Framework
			return null;
		}

		@Nullable
		public Map.Entry<Completer, Map.Entry<Method, Object>> getAssociatedCompleter(@NotNull String commandName, @NotNull String[] possibleArgs) {
			for (Completer comp : subCommandCompletions.keySet()) {
				final String name = comp.name(), cmdName = commandName + (possibleArgs.length == 0 ? "" : "." + String.join(".", Arrays.copyOfRange(possibleArgs, 0, name.split("\\.").length - 1)));

				if (name.equalsIgnoreCase(cmdName) || Stream.of(comp.aliases()).anyMatch(target -> target.equalsIgnoreCase(cmdName) || target.equalsIgnoreCase(commandName))) {
					return Utils.mapEntry(comp, subCommandCompletions.get(comp));
				}
			}

			for (Completer comp : commandCompletions.keySet()) {
				final String name = comp.name();

				if (name.equalsIgnoreCase(commandName) || Stream.of(comp.aliases()).anyMatch(commandName::equalsIgnoreCase)) {
					return Utils.mapEntry(comp, commandCompletions.get(comp));
				}
			}

			return null;
		}
	}
}