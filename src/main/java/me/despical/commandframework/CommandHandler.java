package me.despical.commandframework;

import me.despical.commandframework.annotations.Flag;
import me.despical.commandframework.annotations.Option;
import me.despical.commandframework.annotations.Command;
import me.despical.commandframework.annotations.Completer;
import me.despical.commandframework.exceptions.CooldownException;
import me.despical.commandframework.parser.OptionParser;
import me.despical.commandframework.utils.Utils;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * This class handles the command executions and tab completes.
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
@ApiStatus.NonExtendable
abstract class CommandHandler implements CommandExecutor, TabCompleter {

	private CommandRegistry registry;
	protected ParameterHandler parameterHandler;

	void setRegistry(CommandFramework commandFramework) {
		this.registry = commandFramework.getRegistry();
		this.parameterHandler = new ParameterHandler();
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command cmd, @NotNull String label, String[] args) {
		final Map.Entry<Command, Map.Entry<Method, Object>> entry = registry.getCommandMatcher().getAssociatedCommand(cmd.getName(), args);

		if (entry == null) return false;

		final Command command = entry.getKey();
		final String permission = command.permission();
		final String[] splitted = command.name().split("\\.");
		final String[] newArgs = Arrays.copyOfRange(args, splitted.length - 1, args.length);
		final CommandArguments arguments = new CommandArguments(sender, cmd, command, label, newArgs);

		if (command.onlyOp() && !sender.isOp()) {
			arguments.sendMessage(Message.MUST_HAVE_OP);
			return true;
		}

		if (!permission.isEmpty() && !sender.hasPermission(permission)) {
			arguments.sendMessage(Message.NO_PERMISSION);
			return true;
		}

		if (command.senderType() == Command.SenderType.PLAYER && !(sender instanceof Player)) {
			arguments.sendMessage(Message.ONLY_BY_PLAYERS);
			return true;
		}

		if (command.senderType() == Command.SenderType.CONSOLE && sender instanceof Player) {
			arguments.sendMessage(Message.ONLY_BY_CONSOLE);
			return true;
		}

		if (newArgs.length < command.min()) {
			return arguments.sendMessage(Message.SHORT_ARG_SIZE);
		}

		if (command.max() != -1 && newArgs.length > command.max()) {
			return arguments.sendMessage(Message.LONG_ARG_SIZE);
		}

		final Method method = entry.getValue().getKey();
		final CommandFramework commandFramework = CommandFramework.getInstance();

		if (commandFramework.checkConfirmation(sender, command, method)) {
			return true;
		}

		if (!commandFramework.options().isEnabled(me.despical.commandframework.options.Option.CUSTOM_COOLDOWN_CHECKER) && commandFramework.getCooldownManager().hasCooldown(arguments, command, method)) {
			return true;
		}

		final boolean parseOptions = method.getAnnotationsByType(Option.class).length + method.getAnnotationsByType(Flag.class).length > 0;

		if (parseOptions) {
			OptionParser optionParser = new OptionParser(newArgs, method);

			arguments.setParsedOptions(optionParser.parseOptions());
			arguments.setParsedFlags(optionParser.parseFlags());
		}

		final Runnable invocation = () -> {
			try {
				final Object instance = entry.getValue().getValue();

				method.invoke(instance, parameterHandler.getParameterArray(method, arguments));
			} catch (Exception exception) {
				if (exception.getCause() instanceof CooldownException) {
					return;
				}

				Utils.handleExceptions(exception);
			}
		};

		if (command.async()) {
			final Plugin plugin = commandFramework.plugin;

			plugin.getServer().getScheduler().runTaskAsynchronously(plugin, invocation);
		} else {
			invocation.run();
		}

		return true;
	}

	@Override
	public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command cmd, @NotNull String label, String[] args) {
		final Map.Entry<Completer, Map.Entry<Method, Object>> entry = this.registry.getCommandMatcher().getAssociatedCompleter(cmd.getName(), args);

		if (entry == null)
			return null;

		final String permission = entry.getKey().permission();

		if (!permission.isEmpty() && !sender.hasPermission(permission))
			return null;

		try {
			final Method method = entry.getValue().getKey();
			final Object instance = entry.getValue().getValue();
			final String[] splitName = entry.getKey().name().split("\\.");
			final String[] newArgs = Arrays.copyOfRange(args, splitName.length - 1, args.length);
			final Object completer = method.invoke(instance, parameterHandler.getParameterArray(method, new CommandArguments(sender, cmd, null, label, newArgs)));

			return (List<String>) completer;
		} catch (Exception exception) {
			Utils.handleExceptions(exception);
		}

		return null;
	}
}
