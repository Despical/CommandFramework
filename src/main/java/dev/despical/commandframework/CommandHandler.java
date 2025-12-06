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

package dev.despical.commandframework;

import dev.despical.commandframework.annotations.Flag;
import dev.despical.commandframework.annotations.Option;
import dev.despical.commandframework.annotations.Command;
import dev.despical.commandframework.annotations.Completer;
import dev.despical.commandframework.exceptions.CooldownException;
import dev.despical.commandframework.options.FrameworkOption;
import dev.despical.commandframework.parser.OptionParser;
import dev.despical.commandframework.utils.Utils;
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
		Map.Entry<Command, Map.Entry<Method, Object>> entry = registry.getCommandMatcher().getAssociatedCommand(cmd.getName(), args);

		if (entry == null) return false;

		Method method = entry.getValue().getKey();

		if (method == null) {
			return false;
		}

		Command command = entry.getKey();
		String permission = command.permission();
		String[] split = command.name().split("\\.");
		String[] newArgs = Arrays.copyOfRange(args, split.length - 1, args.length);
		CommandArguments arguments = new CommandArguments(sender, cmd, command, label, newArgs);

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

		CommandFramework commandFramework = CommandFramework.getInstance();

		if (commandFramework.checkConfirmation(sender, command, method)) {
			return true;
		}

		if (!commandFramework.options().isEnabled(FrameworkOption.CUSTOM_COOLDOWN_CHECKER) && commandFramework.getCooldownManager().hasCooldown(arguments, command, method)) {
			return true;
		}

		boolean parseOptions = method.getAnnotationsByType(Option.class).length + method.getAnnotationsByType(Flag.class).length > 0;

		if (parseOptions) {
			OptionParser optionParser = new OptionParser(newArgs, method);

			arguments.setParsedOptions(optionParser.parseOptions());
			arguments.setParsedFlags(optionParser.parseFlags());
		}

		Runnable invocation = () -> {
			try {
				Object instance = entry.getValue().getValue();

				method.invoke(instance, parameterHandler.getParameterArray(method, arguments));
			} catch (Exception exception) {
				if (exception.getCause() instanceof CooldownException) {
					return;
				}

				Utils.handleExceptions(exception);
			}
		};

		if (command.async()) {
			Plugin plugin = commandFramework.plugin;

			plugin.getServer().getScheduler().runTaskAsynchronously(plugin, invocation);
		} else {
			invocation.run();
		}

		return true;
	}

	@Override
	public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command cmd, @NotNull String label, String[] args) {
		Map.Entry<Completer, Map.Entry<Method, Object>> entry = this.registry.getCommandMatcher().getAssociatedCompleter(cmd.getName(), args);

		if (entry == null)
			return null;

		String permission = entry.getKey().permission();

		if (!permission.isEmpty() && !sender.hasPermission(permission))
			return null;

		try {
			Method method = entry.getValue().getKey();
			Object instance = entry.getValue().getValue();
			String[] splitName = entry.getKey().name().split("\\.");
			String[] newArgs = Arrays.copyOfRange(args, splitName.length - 1, args.length);
			Object completer = method.invoke(instance, parameterHandler.getParameterArray(method, new CommandArguments(sender, cmd, null, label, newArgs)));

			return (List<String>) completer;
		} catch (Exception exception) {
			Utils.handleExceptions(exception);
		}

		return null;
	}
}
