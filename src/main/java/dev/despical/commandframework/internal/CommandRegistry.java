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

package dev.despical.commandframework.internal;

import com.google.common.reflect.ClassPath;
import dev.despical.commandframework.CommandFramework;
import dev.despical.commandframework.annotations.Command;
import dev.despical.commandframework.annotations.Completer;
import dev.despical.commandframework.debug.Debug;
import dev.despical.commandframework.options.FrameworkOption;
import dev.despical.commandframework.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.SimplePluginManager;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.logging.Level;

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
public final class CommandRegistry {

    private static final Constructor<PluginCommand> PLUGIN_COMMAND_CONSTRUCTOR;
    private static final Field KNOWN_COMMANDS_FIELD;
    private static final Field COMMAND_MAP_FIELD;

    private CommandMap commandMap;

    private final CommandMatcher commandMatcher = new CommandMatcher();

    private final Set<Command> commandCache = new LinkedHashSet<>();
    private final Set<Command> subCommandCache = new LinkedHashSet<>();

    private final Map<String, CommandNode<Command>> commandTree = new HashMap<>();
    private final Map<String, CommandNode<Completer>> completionTree = new HashMap<>();

    public CommandRegistry() {
        var pluginManager = Bukkit.getServer().getPluginManager();

        if (pluginManager instanceof SimplePluginManager manager) {
            try {
                this.commandMap = (CommandMap) COMMAND_MAP_FIELD.get(manager);
            } catch (IllegalAccessException ignored) { }
        }
    }

    public void registerCommands(@NotNull Object instance) {
        var framework = CommandFramework.getInstance();
        boolean notDebug = !framework.options().isEnabled(FrameworkOption.DEBUG);

        for (Method method : instance.getClass().getMethods()) {
            if (notDebug && method.isAnnotationPresent(Debug.class)) continue;

            Command command = method.getAnnotation(Command.class);

            if (command != null) {
                this.registerCommand(command, method, instance);
            } else if (method.isAnnotationPresent(Completer.class)) {
                this.registerCompleter(instance, method);
            }
        }

        this.verifySubCommandHierarchy();
    }

    public void registerAllInPackage(@NotNull String packageName) {
        Plugin plugin = CommandFramework.getInstance().getPlugin();

        try {
            ClassPath cp = ClassPath.from(plugin.getClass().getClassLoader());

            for (var info : cp.getTopLevelClassesRecursive(packageName)) {
                Class<?> clazz = info.load();

                if (clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers())) {
                    continue;
                }

                try {
                    Object instance = clazz.getDeclaredConstructor().newInstance();

                    for (Method method : clazz.getDeclaredMethods()) {
                        Command cmd = method.getAnnotation(Command.class);
                        if (cmd != null) registerCommand(cmd, method, instance);

                        Completer comp = method.getAnnotation(Completer.class);
                        if (comp != null) registerCompleter(instance, method);
                    }
                } catch (Exception ignored) {}
            }

            this.verifySubCommandHierarchy();
        } catch (IOException exception) {
            plugin.getLogger().log(Level.SEVERE, "Package scanning failed", exception);
        }
    }

    public void registerCommand(Command command, Method method, Object instance) {
        innerRegister(command.name(), command, method, instance);

        for (String alias : command.aliases()) {
            innerRegister(alias, Utils.createDummy(command, alias), method, instance);
        }
    }

    private void innerRegister(String name, Command command, Method method, Object instance) {
        String[] parts = name.split("\\.");
        CommandNode<Command> node = commandTree.computeIfAbsent(parts[0], k -> new CommandNode<>());

        for (int i = 1; i < parts.length; i++) {
            node = node.getChildren().computeIfAbsent(parts[i], k -> new CommandNode<>());
        }

        try {
            MethodHandle handle = method != null ? MethodHandles.lookup().unreflect(method) : null;
            node.setMember(new RegisteredMember<>(instance, method, handle, command));

            if (parts.length == 1) {
                commandCache.add(command);
                return;
            }

            subCommandCache.add(command);
        } catch (Exception exception) {
            CommandFramework.getInstance().getLogger().log(Level.SEVERE, "Trie insertion failed: " + name, exception);
        }
    }

    private void verifySubCommandHierarchy() {
        for (Map.Entry<String, CommandNode<Command>> entry : commandTree.entrySet()) {
            String label = entry.getKey();
            CommandNode<Command> node = entry.getValue();

            if (node.getMember() != null) {
                registerToBukkitSafely(node.getMember().annotation(), label);
            } else if (!node.getChildren().isEmpty()) {
                Command dummy = Utils.createDummy(label);
                node.setMember(new RegisteredMember<>(new Object(), null, null, dummy));

                registerToBukkitSafely(dummy, label);

                commandCache.add(dummy);
            }

            verifyNodeRecursive(label, node);
        }
    }

    private void registerToBukkitSafely(Command command, String label) {
        try {
            Plugin plugin = CommandFramework.getInstance().getPlugin();
            PluginCommand pc = PLUGIN_COMMAND_CONSTRUCTOR.newInstance(label, plugin);
            pc.setExecutor(CommandFramework.getInstance());
            pc.setTabCompleter(CommandFramework.getInstance());
            pc.setUsage(command.usage());
            pc.setPermission(command.permission().isEmpty() ? null : command.permission());
            pc.setDescription(command.desc());

            String prefix = command.fallbackPrefix().isEmpty() ? plugin.getName() : command.fallbackPrefix();
            commandMap.register(prefix, pc);
        } catch (Exception e) {
            CommandFramework.getInstance().getLogger().log(Level.SEVERE, "Command registration failed for: " + label, e);
        }
    }

    private void registerCompleter(Object instance, Method method) {
        if (!List.class.isAssignableFrom(method.getReturnType())) return;

        Completer completer = method.getAnnotation(Completer.class);
        String[] parts = completer.name().toLowerCase(Locale.ENGLISH).split("\\.");
        CommandNode<Completer> node = completionTree.computeIfAbsent(parts[0], k -> new CommandNode<>());

        for (int i = 1; i < parts.length; i++) {
            node = node.getChildren().computeIfAbsent(parts[i], k -> new CommandNode<>());
        }

        try {
            MethodHandle handle = MethodHandles.lookup().unreflect(method);
            node.setMember(new RegisteredMember<>(instance, method, handle, completer));
        } catch (IllegalAccessException e) {
            CommandFramework.getInstance().getLogger().log(Level.SEVERE, "Failed to register completer: " + completer.name(), e);
        }
    }

    public void unregisterCommand(@NotNull String commandName) {
        String rootLabel = commandName.split("\\.")[0].toLowerCase(Locale.ENGLISH);

        if (!commandTree.containsKey(rootLabel)) return;

        try {
            PluginCommand command = Bukkit.getPluginCommand(rootLabel);

            if (command != null && command.getPlugin().equals(CommandFramework.getInstance().getPlugin())) {
                command.unregister(commandMap);

                var knownCommands = (Map<String, org.bukkit.command.Command>) KNOWN_COMMANDS_FIELD.get(commandMap);
                knownCommands.remove(rootLabel);
            }

            commandCache.removeIf(cmd -> cmd.name().equalsIgnoreCase(rootLabel));
            subCommandCache.removeIf(cmd -> cmd.name().startsWith(rootLabel + "."));

            commandTree.remove(rootLabel);
            completionTree.remove(rootLabel);
        } catch (Exception e) {
            CommandFramework.getInstance().getLogger().log(Level.SEVERE, "Error unregistering: " + rootLabel, e);
        }
    }

    private void verifyNodeRecursive(String label, CommandNode<Command> node) {
        for (var child : node.getChildren().entrySet()) {
            String childLabel = label + "." + child.getKey();

            var value = child.getValue();

            if (value.getMember() == null && !value.getChildren().isEmpty()) {
                Command dummy = Utils.createDummy(childLabel);
                value.setMember(RegisteredMember.dummyCommand(dummy));

                subCommandCache.add(dummy);
            }

            verifyNodeRecursive(childLabel, value);
        }
    }

    public void unregisterCommands() {
        new HashSet<>(commandTree.keySet()).forEach(this::unregisterCommand);
    }

    @NotNull
    public Set<Command> getCommands() {
        return Set.copyOf(commandCache);
    }

    @NotNull
    public Set<Command> getSubCommands() {
        return Set.copyOf(subCommandCache);
    }

    @NotNull
    public CommandMatcher getCommandMatcher() {
        return commandMatcher;
    }

    public void setCommandMap(CommandMap commandMap) {
        this.commandMap = commandMap;
    }

    public final class CommandMatcher {

        @Nullable
        public RegisteredMember<Command> getMatch(@NotNull String label, @NotNull String[] args) {
            return findInTree(commandTree, label, args);
        }

        @Nullable
        public RegisteredMember<Completer> getCompleterMatch(@NotNull String label, @NotNull String[] args) {
            return findInTree(completionTree, label, args);
        }

        private <T extends Annotation> RegisteredMember<T> findInTree(
            @NotNull Map<String, CommandNode<T>> tree,
            @NotNull String label,
            @NotNull String[] args
        ) {
            CommandNode<T> node = tree.get(label);

            if (node == null) return null;

            var lastMatch = node.getMember();

            for (String arg : args) {
                node = node.getChildren().get(arg);

                if (node == null) {
                    break;
                }

                if (node.getMember() != null) {
                    lastMatch = node.getMember();
                }
            }

            return lastMatch;
        }
    }

    static {
        try {
            PLUGIN_COMMAND_CONSTRUCTOR = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            PLUGIN_COMMAND_CONSTRUCTOR.setAccessible(true);

            KNOWN_COMMANDS_FIELD = SimpleCommandMap.class.getDeclaredField("knownCommands");
            KNOWN_COMMANDS_FIELD.setAccessible(true);

            COMMAND_MAP_FIELD = SimplePluginManager.class.getDeclaredField("commandMap");
            COMMAND_MAP_FIELD.setAccessible(true);
        } catch (NoSuchMethodException | NoSuchFieldException exception) {
            throw new ExceptionInInitializerError("CommandFramework reflection failed: " + exception.getMessage());
        }
    }
}
