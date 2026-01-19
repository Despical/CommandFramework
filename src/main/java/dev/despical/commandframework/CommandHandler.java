/*
 * Command Framework - Annotation based command framework
 * Copyright (C) 2026  Berke Akçen
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

import dev.despical.commandframework.annotations.Command;
import dev.despical.commandframework.annotations.Completer;
import dev.despical.commandframework.annotations.Flag;
import dev.despical.commandframework.annotations.Option;
import dev.despical.commandframework.exceptions.CooldownException;
import dev.despical.commandframework.internal.CommandRegistry;
import dev.despical.commandframework.internal.FrameworkContext;
import dev.despical.commandframework.internal.ParameterHandler;
import dev.despical.commandframework.options.FrameworkOption;
import dev.despical.commandframework.parser.OptionParser;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

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

    protected final CommandRegistry registry;
    protected final ParameterHandler parameterHandler;

    public CommandHandler() {
        this.registry = FrameworkContext.getInstance().getRegistry();
        this.parameterHandler = new ParameterHandler();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command cmd, @NotNull String label, String[] args) {
        var member = registry.getCommandMatcher().getMatch(cmd.getName(), args);

        if (member == null || member.method() == null) {
            return false;
        }

        Command command = member.annotation();
        String permission = command.permission();

        String[] nameParts = command.name().split("\\.");
        String[] newArgs = Arrays.copyOfRange(args, nameParts.length - 1, args.length);
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
        FrameworkContext context = FrameworkContext.getInstance();

        Method method = member.method();

        if (context.checkConfirmation(sender, command, method)) {
            return true;
        }

        if (!commandFramework.options().isEnabled(FrameworkOption.CUSTOM_COOLDOWN_CHECKER) &&
            context.getCooldownManager().hasCooldown(arguments, command, method)
        ) {
            return true;
        }

        boolean parseOptions = method.getAnnotationsByType(Option.class).length > 0 ||
            method.getAnnotationsByType(Flag.class).length > 0;

        if (parseOptions) {
            OptionParser optionParser = new OptionParser(newArgs, method);
            arguments.setParsedOptions(optionParser.parseOptions());
            arguments.setParsedFlags(optionParser.parseFlags());
        }

        Runnable invocation = () -> {
            try {
                Object[] params = parameterHandler.getParameterArray(method, arguments);
                params = combine(member.instance(), params);

                member.handle().invokeWithArguments(params);
            } catch (Throwable throwable) {
                if (throwable instanceof CooldownException || throwable.getCause() instanceof CooldownException) {
                    return;
                }

                logErrorMessage(throwable, cmd.getLabel(), args, sender.getName());
            }
        };

        if (command.async()) {
            Bukkit.getScheduler().runTaskAsynchronously(commandFramework.getPlugin(), invocation);
        } else {
            invocation.run();
        }

        return true;
    }

    private Object[] combine(Object instance, Object[] params) {
        Object[] combined = new Object[params.length + 1];
        combined[0] = instance;

        System.arraycopy(params, 0, combined, 1, params.length);
        return combined;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command cmd, @NotNull String label, String[] args) {
        var member = registry.getCommandMatcher().getCompleterMatch(cmd.getName(), args);

        if (member == null) {
            return null;
        }

        Completer completer = member.annotation();
        String permission = completer.permission();

        if (!permission.isEmpty() && !sender.hasPermission(permission)) {
            return null;
        }

        try {
            String[] nameParts = completer.name().split("\\.");
            String[] newArgs = Arrays.copyOfRange(args, nameParts.length - 1, args.length);

            CommandArguments arguments = new CommandArguments(sender, cmd, null, label, newArgs);
            Object[] params = parameterHandler.getParameterArray(member.method(), arguments);
            params = combine(member.instance(), params);

            Object result = member.handle().invokeWithArguments(params);

            return (List<String>) result;
        } catch (Throwable throwable) {
            logErrorMessage(throwable, cmd.getLabel(), args, sender.getName());
        }

        return null;
    }

    private void logErrorMessage(Throwable throwable, String label, String[] args, String senderName) {
        CommandFramework.getInstance().getLogger().log(
            Level.SEVERE,
            throwable,
            () -> "Error executing command: /%s %s (sender=%s)".formatted(label, String.join(" ", args), senderName)
        );
    }
}
