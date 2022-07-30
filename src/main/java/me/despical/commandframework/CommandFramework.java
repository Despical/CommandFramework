/*
 * Copyright (c) 2021 - Despical
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package me.despical.commandframework;

import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.SimplePluginManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Main class of the framework to register commands, add tab
 * completions and implement a consumer to run if there is no
 * matched commands related this framework.
 *
 * @author Despical
 * @since 1.0.0
 */
public class CommandFramework implements CommandExecutor, TabCompleter {

    /**
     * Main instance of framework.
     */
    @NotNull
    private final Plugin plugin;

    /**
     * Map of registered commands by framework.
     */
    @NotNull
    private final Map<Command, Map.Entry<Method, Object>> commands = new HashMap<>();

    /**
     * Map of registered subcommands by framework.
     */
    @NotNull
    private final Map<Command, Map.Entry<Method, Object>> subCommands = new HashMap<>();

    /**
     * Map of registered tab completions by framework.
     */
    @NotNull
    private final Map<Completer, Map.Entry<Method, Object>> completions = new HashMap<>();

    /**
     * Map of registered command cooldowns by framework.
     */
    @NotNull
    private final Map<CommandSender, Long> cooldowns = new HashMap<>();

    /**
     * Consumer to accept if there is no matched commands related framework.
     */
    @Nullable
    private Consumer<CommandArguments> anyMatchConsumer;

    /**
     * Default command map of Bukkit.
     */
    @Nullable
    private CommandMap commandMap;

    public CommandFramework(@NotNull Plugin plugin) {
        this.plugin = plugin;

        if (plugin.getServer().getPluginManager() instanceof SimplePluginManager) {
            final SimplePluginManager manager = (SimplePluginManager) plugin.getServer().getPluginManager();

            try {
                final Field field = SimplePluginManager.class.getDeclaredField("commandMap");
                field.setAccessible(true);

                commandMap = (CommandMap) field.get(manager);
            } catch (IllegalArgumentException | SecurityException | IllegalAccessException | NoSuchFieldException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Consumer to accept if there is no matched commands related framework.
     *
     * @param anyMatchConsumer to be accepted if there is no matched commands
     */
    public void setAnyMatch(@NotNull Consumer<CommandArguments> anyMatchConsumer) {
        this.anyMatchConsumer = anyMatchConsumer;
    }

    /**
     * Register command methods in object class.
     *
     * @param instance object class
     */
    public void registerCommands(@NotNull Object instance) {
        for (Method method : instance.getClass().getMethods()) {
            final Command command = method.getAnnotation(Command.class);

            if (command != null) {
                if (method.getParameterTypes().length > 0 && method.getParameterTypes()[0] != CommandArguments.class) {
                    continue;
                }

                registerCommand(command, method, instance);
            } else if (method.getAnnotation(Completer.class) != null) {
                completions.put(method.getAnnotation(Completer.class), me.despical.commons.util.Collections.mapEntry(method, instance));
            }
        }
    }

    /**
     * Register the command with given parameters.
     *
     * @param command of the main object
     * @param method that command will run
     * @param instance of the method above
     */
    private void registerCommand(Command command, Method method, Object instance) {
        if (command.name().contains(".")) {
            subCommands.put(command, me.despical.commons.util.Collections.mapEntry(method, instance));
        } else {
            commands.put(command, me.despical.commons.util.Collections.mapEntry(method, instance));
        }

        try {
            final Constructor<PluginCommand> constructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            constructor.setAccessible(true);

            final String splittedCommand = command.name().split("\\.")[0];
            final PluginCommand pluginCommand = constructor.newInstance(splittedCommand, plugin);
            pluginCommand.setTabCompleter(this);
            pluginCommand.setExecutor(this);
            pluginCommand.setUsage(command.usage());
            pluginCommand.setPermission(command.permission());
            pluginCommand.setDescription(command.desc());
            pluginCommand.setAliases(Arrays.asList(command.aliases()));

            commandMap.register(splittedCommand, pluginCommand);
        } catch(Exception exception) {
            exception.printStackTrace();
        }
    }

    @Nullable
    private Map.Entry<Command, Map.Entry<Method, Object>> getAssociatedCommand(@NotNull String commandName, @NotNull String[] possibleArgs) {
        Command command = null;

        // Search for the sub commands first
        for (Command cmd : subCommands.keySet()) {
            final String name = cmd.name(), cmdName = commandName + (possibleArgs.length == 0 ? "" : "." + String.join(".", Arrays.copyOfRange(possibleArgs, 0, name.split("\\.").length - 1)));

            if (name.equalsIgnoreCase(cmdName) || Stream.of(cmd.aliases()).anyMatch(commandName::equalsIgnoreCase)) {
                command = cmd;
                break;
            }
        }

        // If we found the sub command then return it, otherwise search the commands map
        if (command != null) {
            return me.despical.commons.util.Collections.mapEntry(command, subCommands.get(command));
        }

        // If our command is not a sub command then search for a main command
        for (Command cmd : commands.keySet()) {
            final String name = cmd.name();

            if (name.equalsIgnoreCase(commandName) || Stream.of(cmd.aliases()).anyMatch(commandName::equalsIgnoreCase)) {
                command = cmd;
                break;
            }
        }

        // If we found the command return it, otherwise return null
        if (command != null) {
            // Quick fix to accept any match consumer if defined
            if (command.min() >= possibleArgs.length) {
                return me.despical.commons.util.Collections.mapEntry(command, commands.get(command));
            }
        }

        // Return null if the given command is not registered by Command Framework
        return null;
    }

    // Error Message Handler

    public static String ONLY_BY_PLAYERS         = ChatColor.RED + "This command is only executable by players!";
    public static String ONLY_BY_CONSOLE         = ChatColor.RED + "This command is only executable by console!";
    public static String NO_PERMISSION           = ChatColor.RED + "You don't have enough permission to execute this command!";
    public static String SHORT_OR_LONG_ARG_SIZE  = ChatColor.RED + "Required argument length is less or greater than needed!";
    public static String WAIT_BEFORE_USING_AGAIN = ChatColor.RED + "You have to wait before using this command again!";

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command cmd, @NotNull String label, String[] args) {
        Map.Entry<Command, Map.Entry<Method, Object>> entry = this.getAssociatedCommand(cmd.getName(), args);

        if (entry == null) {
            if (anyMatchConsumer != null) {
                anyMatchConsumer.accept(new CommandArguments(sender, cmd, label, args));
            }

            return true;
        }

        Command command = entry.getKey();

        if (!sender.hasPermission(command.permission())) {
            sender.sendMessage(NO_PERMISSION);
            return true;
        }

        if (command.senderType() == Command.SenderType.PLAYER && !(sender instanceof Player)) {
            sender.sendMessage(ONLY_BY_PLAYERS);
            return true;
        }

        if (command.senderType() == Command.SenderType.CONSOLE && sender instanceof Player) {
            sender.sendMessage(ONLY_BY_CONSOLE);
            return true;
        }

        if (cooldowns.containsKey(sender)) {
            if (command.cooldown() > 0 && ((System.currentTimeMillis() - cooldowns.get(sender)) / 1000) % 60 <= command.cooldown()) {
                sender.sendMessage(WAIT_BEFORE_USING_AGAIN);
                return true;
            } else {
                cooldowns.remove(sender);
            }
        } else {
            cooldowns.put(sender, System.currentTimeMillis());
        }

        final String[] splitted = command.name().split("\\."), newArgs = Arrays.copyOfRange(args, splitted.length - 1, args.length);

        if (args.length >= command.min() + splitted.length - 1 && newArgs.length <= (command.max() == -1 ? newArgs.length + 1 : command.max())) {
            try {
                entry.getValue().getKey().invoke(entry.getValue().getValue(), new CommandArguments(sender, cmd, label, newArgs));
                return true;
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
                return true;
            }
        } else {
            sender.sendMessage(SHORT_OR_LONG_ARG_SIZE);
            return true;
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command command, @NotNull String label, String[] args) {
        for (Map.Entry<Completer, Map.Entry<Method, Object>> entry : completions.entrySet()) {
            final Completer completer = entry.getKey();

            if (command.getName().equalsIgnoreCase(completer.name()) || Stream.of(completer.aliases()).anyMatch(command.getName()::equalsIgnoreCase)) {
                try {
                    final Object instance = entry.getValue().getKey().invoke(entry.getValue().getValue(), new CommandArguments(sender, command, label, args));

                    return (List<String>) instance;
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }

    /**
     * Get the copied list of registered commands and subcommands.
     *
     * @return list of commands and subcommands.
     */
    @NotNull
    public List<Command> getCommands() {
        List<Command> commands = new ArrayList<>(this.commands.keySet());
        commands.addAll(subCommands.keySet());

        return commands;
    }
}