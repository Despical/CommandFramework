/*
 * Command Framework - Annotation based command framework
 * Copyright (C) 2025  Berke Akçen
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.despical.commandframework;

import me.despical.commandframework.annotations.Command;
import me.despical.commandframework.annotations.Completer;
import me.despical.commandframework.debug.Debug;
import me.despical.commandframework.options.FrameworkOption;
import me.despical.commandframework.utils.Utils;
import org.bukkit.Bukkit;
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
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.stream.Stream;

/**
 * This class manages the registry of commands, sub-commands and tab completers
 * associated with those commands. It also provides helper methods for matching
 * commands and their corresponding tab completers.
 *
 * <p>This is an internal class and should not be instantiated or extended by
 * any subclasses.
 *
 * @author Despical
 * @since 1.4.8
 * <p>
 * Created on 18.07.2024
 */
@ApiStatus.Internal
public class CommandRegistry {

	@Nullable
	private CommandMap commandMap;

	@NotNull
	private final CommandMatcher commandMatcher;

	@NotNull
	private final Map<Command, Map.Entry<Method, Object>> commands, subCommands;

	@NotNull
	private final Map<Completer, Map.Entry<Method, Object>> commandCompletions, subCommandCompletions;

	CommandRegistry() {
		this.commandMatcher = new CommandMatcher();
		this.commands = new HashMap<>();
		this.commandCompletions = new HashMap<>();
		this.subCommands = new TreeMap<>(Comparator.comparing(Command::name).reversed());
		this.subCommandCompletions = new TreeMap<>(Comparator.comparing(Completer::name).reversed());

		final PluginManager pluginManager = Bukkit.getServer().getPluginManager();

		if (pluginManager instanceof SimplePluginManager) {
			final SimplePluginManager manager = (SimplePluginManager) pluginManager;

			try {
				final Field field = SimplePluginManager.class.getDeclaredField("commandMap");
				field.setAccessible(true);

				this.commandMap = (CommandMap) field.get(manager);
			} catch (ReflectiveOperationException exception) {
				exception.printStackTrace();
			}
		}
	}

	/**
	 * Sets the {@link CommandMap} for this instance.
	 *
	 * @param commandMap the {@link CommandMap} to be set. Must be non-null.
	 */
	public void setCommandMap(@NotNull CommandMap commandMap) {
		this.commandMap = commandMap;
	}

	/**
	 * Registers commands from the specified instance's class.
	 * <p>
	 * This method scans the class of the provided instance and registers all commands
	 * defined within that class. The class should contain methods annotated to be recognized
	 * as commands.
	 * </p>
	 *
	 * @param instance the instance of the class from which commands will be registered. Must not be {@code null}.
	 */
	protected void registerCommands(@NotNull Object instance) {
		CommandFramework commandFramework = CommandFramework.getInstance();
		boolean notDebug = !commandFramework.options().isEnabled(FrameworkOption.DEBUG);

		for (Method method : instance.getClass().getMethods()) {
			if (notDebug && method.isAnnotationPresent(Debug.class)) {
				continue;
			}

			Command command = method.getAnnotation(Command.class);

			if (command != null) {
				registerCommand(command, method, instance);

				// Register all aliases as a plugin command. If it is a sub-command then register it as a sub-command.
				Stream.of(command.aliases()).forEach(alias -> registerCommand(Utils.createCommand(command, alias), method, instance));
			} else if (method.isAnnotationPresent(Completer.class)) {
				if (!List.class.isAssignableFrom(method.getReturnType())) {
					commandFramework.getLogger().log(Level.WARNING, "Skipped registration of ''{0}'' because it is not returning java.util.List type.", method.getName());
					continue;
				}

				Completer completer = method.getAnnotation(Completer.class);

				if (completer.name().contains(".")) {
					subCommandCompletions.put(completer, Utils.mapEntry(method, instance));
				} else {
					commandCompletions.put(completer, Utils.mapEntry(method, instance));
				}
			}
		}

		subCommands.forEach((key, value) -> {
			String splitName = key.name().split("\\.")[0];

			// Framework is going to work properly but this should not be handled that way.
			if (commands.keySet().stream().noneMatch(cmd -> cmd.name().equals(splitName))) {
				commandFramework.getLogger().log(Level.WARNING, "A sub-command (name: ''{0}'') is directly registered without a main command.", key.name());

				registerCommand(Utils.createCommand(key, splitName), null, null);
			}
		});
	}

	/**
	 * This method registers a command along with its associated method and instance.
	 * When the command is executed, the specified method will be invoked.
	 *
	 * @param command  the {@link Command} object representing the command to be registered.
	 * @param method   the {@link Method} object representing the method to be invoked when the command is executed.
	 * @param instance the instance of the class that contains the command method.
	 */
	protected void registerCommand(Command command, Method method, Object instance) {
		CommandFramework commandFramework = CommandFramework.getInstance();
		String cmdName = command.name();

		if (cmdName.contains(".")) {
			subCommands.put(command, Utils.mapEntry(method, instance));
		} else {
			commands.put(command, Utils.mapEntry(method, instance));

			try {
				Constructor<PluginCommand> constructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
				constructor.setAccessible(true);

				PluginCommand pluginCommand = constructor.newInstance(cmdName, commandFramework.plugin);
				pluginCommand.setExecutor(commandFramework);
				pluginCommand.setUsage(command.usage());
				pluginCommand.setPermission(command.permission().isEmpty() ? null : command.permission());
				pluginCommand.setDescription(command.desc());

				String fallbackPrefix = command.fallbackPrefix().isEmpty() ? commandFramework.plugin.getName() : command.fallbackPrefix();

				commandMap.register(fallbackPrefix, pluginCommand);
			} catch (Exception exception) {
				Utils.handleExceptions(exception);
			}
		}
	}

	/**
	 * Unregisters a command and its associated tab completer if they are registered with the specified name.
	 *
	 * @param commandName the name of the command to be unregistered. Must not be {@code null} or empty.
	 * @throws IllegalArgumentException if {@code commandName} is {@code null} or an empty string.
	 */
	protected void unregisterCommand(@NotNull String commandName) {
		if (commandName.contains(".")) commandName = commandName.split("\\.")[0];

		Map.Entry<Command, Map.Entry<Method, Object>> entry = commandMatcher.getAssociatedCommand(commandName, new String[0]);
		CommandFramework commandFramework = CommandFramework.getInstance();

		if (entry == null) {
			commandFramework.plugin.getLogger().log(Level.WARNING, "Command removal is failed because there is no command named ''{0}''!", commandName);
			return;
		}

		Command command = entry.getKey();
		String name = command.name();
		PluginCommand pluginCommand = commandFramework.plugin.getServer().getPluginCommand(name);

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
	 * Unregisters all commands and tab completers that were registered using the instance of this object.
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

	/**
	 * A helper class that contains methods for matching commands and their corresponding tab completers.
	 */
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
