package me.despical.commandframework;

import me.despical.commandframework.annotations.Command;
import me.despical.commandframework.annotations.Completer;
import me.despical.commandframework.exceptions.CooldownException;
import me.despical.commandframework.options.Option;
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
public abstract class CommandHandler implements CommandExecutor, TabCompleter {

	private CommandRegistry registry;
	private CommandFramework commandFramework;

	void setRegistry(CommandFramework commandFramework) {
		this.commandFramework = commandFramework;
		this.registry = commandFramework.getRegistry();
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

		if (commandFramework.checkConfirmation(sender, command, method)) {
			return true;
		}

		if (!this.commandFramework.isOptionEnabled(Option.CUSTOM_COOLDOWN_CHECKER) && commandFramework.getCooldownManager().hasCooldown(arguments, command, method)) {
			return true;
		}

		final Runnable invocation = () -> {
			try {
				final Object instance = entry.getValue().getValue();

				method.invoke(instance, commandFramework.getParameterHandler().getParameterArray(method, arguments));
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
			final Object completer = method.invoke(instance, commandFramework.getParameterHandler().getParameterArray(method, new CommandArguments(sender, cmd, null, label, newArgs)));

			return (List<String>) completer;
		} catch (Exception exception) {
			Utils.handleExceptions(exception);
		}

		return null;
	}
}
