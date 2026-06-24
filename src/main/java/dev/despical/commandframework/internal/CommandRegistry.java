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

package dev.despical.commandframework.internal;

import com.google.common.reflect.ClassPath;
import dev.despical.commandframework.CommandAttributes;
import dev.despical.commandframework.CommandFramework;
import dev.despical.commandframework.annotations.Command;
import dev.despical.commandframework.annotations.Completer;
import dev.despical.commandframework.debug.Debug;
import dev.despical.commandframework.exceptions.CommandException;
import dev.despical.commandframework.options.FrameworkOption;
import dev.despical.commandframework.utils.CommandNameValidator;
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
import java.util.function.Consumer;
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
        boolean debugEnabled = framework.options().isEnabled(FrameworkOption.DEBUG);

        Class<?> clazz = instance.getClass();

        if (!debugEnabled && clazz.isAnnotationPresent(Debug.class)) {
            return;
        }

        for (Method method : clazz.getMethods()) {
            if (!debugEnabled && method.isAnnotationPresent(Debug.class)) {
                continue;
            }

            Command command = method.getAnnotation(Command.class);

            if (command != null) {
                this.registerCommand(command, method, instance);
                continue;
            }

            if (method.isAnnotationPresent(Completer.class)) {
                this.registerCompleter(instance, method);
            }
        }

        this.verifySubCommandHierarchy();
    }

    public void registerAllInPackage(@NotNull String packageName) {
        var framework = CommandFramework.getInstance();
        boolean debugEnabled = framework.options().isEnabled(FrameworkOption.DEBUG);
        Plugin plugin = framework.getPlugin();

        try {
            ClassPath cp = ClassPath.from(plugin.getClass().getClassLoader());

            for (var info : cp.getTopLevelClassesRecursive(packageName)) {
                Class<?> clazz = info.load();

                if (clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers())) {
                    continue;
                }

                if (!debugEnabled && clazz.isAnnotationPresent(Debug.class)) {
                    continue;
                }

                try {
                    Object instance = clazz.getDeclaredConstructor().newInstance();

                    for (Method method : clazz.getDeclaredMethods()) {
                        if (!debugEnabled && method.isAnnotationPresent(Debug.class)) {
                            continue;
                        }

                        Command command = method.getAnnotation(Command.class);

                        if (command != null) {
                            registerCommand(command, method, instance);
                            continue;
                        }

                        if (method.isAnnotationPresent(Completer.class)) {
                            registerCompleter(instance, method);
                        }
                    }
                } catch (Exception ignored) {}
            }

            this.verifySubCommandHierarchy();
        } catch (IOException exception) {
            plugin.getLogger().log(Level.SEVERE, "Package scanning failed", exception);
        }
    }

    public void registerCommand(Command command, Method method, Object instance) {
        validateCommandMetadata(command, method);

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
        if (label.contains(".")) {
            return;
        }

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
        } catch (Exception exception) {
            CommandFramework.getInstance().getLogger().log(Level.SEVERE, "Command registration failed for: " + label, exception);
        }
    }

    private void registerCompleter(Object instance, Method method) {
        if (!List.class.isAssignableFrom(method.getReturnType())) return;

        Completer completer = method.getAnnotation(Completer.class);
        validateCompleterMetadata(completer, method);
        innerRegisterCompleter(completer.name(), completer, method, instance);

        for (String alias : completer.aliases()) {
            innerRegisterCompleter(alias, completer, method, instance);
        }
    }

    private void innerRegisterCompleter(String name, Completer completer, Method method, Object instance) {
        String[] parts = name.split("\\.");
        CommandNode<Completer> node = completionTree.computeIfAbsent(parts[0], k -> new CommandNode<>());

        for (int i = 1; i < parts.length; i++) {
            node = node.getChildren().computeIfAbsent(parts[i], k -> new CommandNode<>());
        }

        try {
            MethodHandle handle = MethodHandles.lookup().unreflect(method);
            node.setMember(new RegisteredMember<>(instance, method, handle, completer));
        } catch (IllegalAccessException exception) {
            CommandFramework.getInstance().getLogger().log(Level.SEVERE, "Failed to register completer: " + name, exception);
        }
    }

    public void unregisterCommand(@NotNull String commandName) {
        String rootLabel = commandName.split("\\.")[0];

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
    public List<Command> getDirectChildCommands(@NotNull String commandName) {
        CommandNode<Command> node = findCommandNode(normalizeCommandPath(commandName));

        if (node == null || node.getChildren().isEmpty()) {
            return List.of();
        }

        return node.getChildren().entrySet().stream()
            .sorted(Map.Entry.comparingByKey(String.CASE_INSENSITIVE_ORDER))
            .map(Map.Entry::getValue)
            .map(CommandNode::getMember)
            .filter(Objects::nonNull)
            .map(RegisteredMember::annotation)
            .toList();
    }

    public boolean updateCommandAttributes(
        @NotNull String commandName,
        @NotNull Consumer<CommandAttributes.Builder> updater
    ) {
        Objects.requireNonNull(updater, "updater");

        CommandNode<Command> node = findCommandNode(normalizeCommandPath(commandName));

        if (node == null || node.getMember() == null) {
            return false;
        }

        CommandAttributes.Builder builder = CommandAttributes.builder(node.getMember().annotation());
        updater.accept(builder);

        return setCommandAttributes(commandName, builder.build());
    }

    public boolean setCommandAttributes(@NotNull String commandName, @NotNull CommandAttributes attributes) {
        String currentName = normalizeCommandPath(commandName);
        Objects.requireNonNull(attributes, "attributes");

        CommandNode<Command> node = findCommandNode(currentName);

        if (node == null || node.getMember() == null) {
            return false;
        }

        RegisteredMember<Command> originalMember = node.getMember();
        Command originalCommand = originalMember.annotation();
        Command newCommand = attributes.toCommand();
        String newName = newCommand.name();

        ensureCommandPathAvailable(newName, currentName, originalMember);
        ensureAliasesAvailable(newCommand, currentName, originalMember);
        ensureBukkitRootAvailable(newName, currentName, originalMember);
        ensureAliasBukkitRootsAvailable(newCommand, currentName, originalMember);

        if (!currentName.equals(newName)) {
            ensureCommandCanMove(currentName, newName, node);
            ensureCompletionPathAvailable(currentName, newName);
        }

        removeAliases(originalCommand, originalMember);

        if (!currentName.equals(newName)) {
            detachCommandNode(currentName);
            insertCommandNode(newName, node);
            replaceCommandPrefix(node, currentName, newName);
            moveCompletionNode(currentName, newName);
        }

        CommandNode<Command> updatedNode = findCommandNode(newName);
        replaceCommand(updatedNode, newCommand);
        ensureParentCommands(newName);
        syncRootCommand(newName);

        RegisteredMember<Command> updatedMember = updatedNode.getMember();
        registerAliases(newCommand, updatedMember);

        return true;
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

    private void replaceCommandPrefix(CommandNode<Command> node, String oldPrefix, String newPrefix) {
        RegisteredMember<Command> member = node.getMember();

        if (member != null) {
            Command command = member.annotation();
            String commandName = command.name();

            if (commandName.equals(oldPrefix) || commandName.startsWith(oldPrefix + ".")) {
                Command renamedCommand = CommandAttributes.builder(command)
                    .name(newPrefix + commandName.substring(oldPrefix.length()))
                    .build()
                    .toCommand();

                replaceCommand(node, renamedCommand);
            }
        }

        for (CommandNode<Command> child : node.getChildren().values()) {
            replaceCommandPrefix(child, oldPrefix, newPrefix);
        }
    }

    private void validateCommandMetadata(Command command, Method method) {
        try {
            String normalizedName = CommandNameValidator.normalizeName(command.name(), "name");
            CommandNameValidator.normalizeAliases(command.aliases(), normalizedName);
        } catch (IllegalArgumentException exception) {
            throw new CommandException(
                "Invalid @Command metadata in method ''{0}#{1}'': {2}",
                method.getDeclaringClass().getSimpleName(), method.getName(), exception.getMessage()
            );
        }
    }

    private void validateCompleterMetadata(Completer completer, Method method) {
        try {
            String normalizedName = CommandNameValidator.normalizeName(completer.name(), "name");
            CommandNameValidator.normalizeAliases(completer.aliases(), normalizedName);
        } catch (IllegalArgumentException exception) {
            throw new CommandException(
                "Invalid @Completer metadata in method ''{0}#{1}'': {2}",
                method.getDeclaringClass().getSimpleName(), method.getName(), exception.getMessage()
            );
        }
    }

    private void replaceCommand(CommandNode<Command> node, Command command) {
        RegisteredMember<Command> member = node.getMember();

        if (member != null) {
            removeFromCaches(member.annotation());
            node.setMember(member.withAnnotation(command));
        } else {
            node.setMember(RegisteredMember.dummyCommand(command));
        }

        addToCache(command);
    }

    private void registerAliases(Command command, RegisteredMember<Command> sourceMember) {
        for (String alias : command.aliases()) {
            Command aliasCommand = Utils.createDummy(command, alias);
            CommandNode<Command> aliasNode = new CommandNode<>();
            aliasNode.setMember(sourceMember.withAnnotation(aliasCommand));

            insertCommandNode(alias, aliasNode);
            addToCache(aliasCommand);
            ensureParentCommands(alias);
            syncRootCommand(alias);
        }
    }

    private void removeAliases(Command command, RegisteredMember<Command> ownerMember) {
        for (String alias : command.aliases()) {
            CommandNode<Command> aliasNode = findCommandNode(alias);

            if (aliasNode == null || !isSameRegisteredCommand(aliasNode.getMember(), ownerMember)) {
                continue;
            }

            CommandNode<Command> removed = detachCommandNode(alias);

            if (removed != null) {
                removeNodeFromCaches(removed);
            }
        }
    }

    private void ensureAliasesAvailable(Command command, String currentName, RegisteredMember<Command> ownerMember) {
        for (String alias : command.aliases()) {
            ensureCommandPathAvailable(alias, currentName, ownerMember);
        }
    }

    private void ensureAliasBukkitRootsAvailable(Command command, String currentName, RegisteredMember<Command> ownerMember) {
        for (String alias : command.aliases()) {
            ensureBukkitRootAvailable(alias, currentName, ownerMember);
        }
    }

    private void ensureCommandPathAvailable(
        String commandName,
        String currentName,
        RegisteredMember<Command> ownerMember
    ) {
        CommandNode<Command> existing = findCommandNode(commandName);

        if (existing == null || commandName.equals(currentName) || isSameRegisteredCommand(existing.getMember(), ownerMember)) {
            return;
        }

        throw new CommandException("Cannot update command ''{0}'' because the target name ''{1}'' is already registered!",
            currentName, commandName);
    }

    private void ensureCommandCanMove(String currentName, String newName, CommandNode<Command> node) {
        if (newName.startsWith(currentName + ".") && !node.getChildren().isEmpty()) {
            throw new CommandException("Cannot move command ''{0}'' inside its own sub-command tree!", currentName);
        }
    }

    private void ensureCompletionPathAvailable(String currentName, String newName) {
        if (findNode(completionTree, currentName) == null || findNode(completionTree, newName) == null) {
            return;
        }

        throw new CommandException("Cannot move completer from ''{0}'' to ''{1}'' because the target already exists!",
            currentName, newName);
    }

    private void ensureBukkitRootAvailable(
        String commandName,
        String currentName,
        RegisteredMember<Command> ownerMember
    ) {
        if (commandName.contains(".")) {
            return;
        }

        PluginCommand pluginCommand = Bukkit.getPluginCommand(commandName);

        if (pluginCommand == null) {
            return;
        }

        CommandNode<Command> existing = findCommandNode(commandName);
        boolean ownedByThisUpdate = commandName.equals(currentName) ||
            (existing != null && isSameRegisteredCommand(existing.getMember(), ownerMember));

        if (pluginCommand.getPlugin().equals(CommandFramework.getInstance().getPlugin()) && ownedByThisUpdate) {
            return;
        }

        throw new CommandException("Cannot update command ''{0}'' because Bukkit command ''{1}'' is already registered!",
            currentName, commandName);
    }

    private boolean isSameRegisteredCommand(RegisteredMember<Command> first, RegisteredMember<Command> second) {
        return first != null && second != null &&
            Objects.equals(first.instance(), second.instance()) &&
            Objects.equals(first.method(), second.method());
    }

    private CommandNode<Command> findCommandNode(String commandName) {
        return findNode(commandTree, commandName);
    }

    private <T extends Annotation> CommandNode<T> findNode(Map<String, CommandNode<T>> tree, String commandName) {
        String[] parts = commandName.split("\\.");
        CommandNode<T> node = tree.get(parts[0]);

        for (int i = 1; node != null && i < parts.length; i++) {
            node = node.getChildren().get(parts[i]);
        }

        return node;
    }

    private CommandNode<Command> detachCommandNode(String commandName) {
        String[] parts = commandName.split("\\.");
        CommandNode<Command> removed;

        if (parts.length == 1) {
            removed = commandTree.remove(parts[0]);
            unregisterBukkitCommand(parts[0]);
        } else {
            CommandNode<Command> parent = findCommandNode(String.join(".", Arrays.copyOf(parts, parts.length - 1)));
            removed = parent == null ? null : parent.getChildren().remove(parts[parts.length - 1]);
        }

        removeEmptyDummyParents(parts);
        return removed;
    }

    private void insertCommandNode(String commandName, CommandNode<Command> node) {
        String[] parts = commandName.split("\\.");

        if (parts.length == 1) {
            commandTree.put(parts[0], node);
            return;
        }

        CommandNode<Command> parent = commandTree.computeIfAbsent(parts[0], key -> new CommandNode<>());

        for (int i = 1; i < parts.length - 1; i++) {
            parent = parent.getChildren().computeIfAbsent(parts[i], key -> new CommandNode<>());
        }

        parent.getChildren().put(parts[parts.length - 1], node);
    }

    private void ensureParentCommands(String commandName) {
        String[] parts = commandName.split("\\.");

        if (parts.length == 1) {
            return;
        }

        StringBuilder path = new StringBuilder(parts[0]);
        CommandNode<Command> node = commandTree.get(parts[0]);

        for (int i = 0; i < parts.length - 1; i++) {
            if (node.getMember() == null) {
                Command dummy = Utils.createDummy(path.toString());
                node.setMember(RegisteredMember.dummyCommand(dummy));
                addToCache(dummy);

                if (i == 0) {
                    registerToBukkitSafely(dummy, path.toString());
                }
            }

            if (i + 1 < parts.length - 1) {
                path.append('.').append(parts[i + 1]);
                node = node.getChildren().get(parts[i + 1]);
            }
        }
    }

    private void removeEmptyDummyParents(String[] removedParts) {
        for (int i = removedParts.length - 1; i >= 1; i--) {
            String parentName = String.join(".", Arrays.copyOf(removedParts, i));
            CommandNode<Command> parent = findCommandNode(parentName);

            if (parent == null || !parent.getChildren().isEmpty()) {
                break;
            }

            RegisteredMember<Command> member = parent.getMember();

            if (member != null && member.method() != null) {
                break;
            }

            if (member != null) {
                removeFromCaches(member.annotation());
            }

            detachCommandNode(parentName);
        }
    }

    private void removeNodeFromCaches(CommandNode<Command> node) {
        RegisteredMember<Command> member = node.getMember();

        if (member != null) {
            removeFromCaches(member.annotation());
        }

        for (CommandNode<Command> child : node.getChildren().values()) {
            removeNodeFromCaches(child);
        }
    }

    private void addToCache(Command command) {
        if (command.name().contains(".")) {
            subCommandCache.add(command);
            return;
        }

        commandCache.add(command);
    }

    private void removeFromCaches(Command command) {
        commandCache.remove(command);
        subCommandCache.remove(command);
    }

    private void syncRootCommand(String commandName) {
        if (commandName.contains(".")) {
            return;
        }

        CommandNode<Command> node = findCommandNode(commandName);

        if (node == null || node.getMember() == null) {
            return;
        }

        PluginCommand pluginCommand = Bukkit.getPluginCommand(commandName);

        if (pluginCommand == null) {
            registerToBukkitSafely(node.getMember().annotation(), commandName);
            return;
        }

        if (!pluginCommand.getPlugin().equals(CommandFramework.getInstance().getPlugin())) {
            throw new CommandException("Cannot update Bukkit command ''{0}'' because it belongs to another plugin!",
                commandName);
        }

        Command command = node.getMember().annotation();
        pluginCommand.setUsage(command.usage());
        pluginCommand.setPermission(command.permission().isEmpty() ? null : command.permission());
        pluginCommand.setDescription(command.desc());
    }

    private void unregisterBukkitCommand(String label) {
        try {
            PluginCommand command = Bukkit.getPluginCommand(label);

            if (command == null || !command.getPlugin().equals(CommandFramework.getInstance().getPlugin())) {
                return;
            }

            command.unregister(commandMap);

            var knownCommands = (Map<String, org.bukkit.command.Command>) KNOWN_COMMANDS_FIELD.get(commandMap);
            knownCommands.remove(label);
        } catch (Exception exception) {
            CommandFramework.getInstance().getLogger().log(Level.SEVERE, "Error unregistering: " + label, exception);
        }
    }

    private void moveCompletionNode(String currentName, String newName) {
        CommandNode<Completer> completionNode = findNode(completionTree, currentName);

        if (completionNode == null) {
            return;
        }

        detachNode(completionTree, currentName);
        insertNode(completionTree, newName, completionNode);
    }

    private <T extends Annotation> CommandNode<T> detachNode(Map<String, CommandNode<T>> tree, String commandName) {
        String[] parts = commandName.split("\\.");

        if (parts.length == 1) {
            return tree.remove(parts[0]);
        }

        CommandNode<T> parent = findNode(tree, String.join(".", Arrays.copyOf(parts, parts.length - 1)));
        return parent == null ? null : parent.getChildren().remove(parts[parts.length - 1]);
    }

    private <T extends Annotation> void insertNode(Map<String, CommandNode<T>> tree, String commandName, CommandNode<T> node) {
        String[] parts = commandName.split("\\.");

        if (parts.length == 1) {
            tree.put(parts[0], node);
            return;
        }

        CommandNode<T> parent = tree.computeIfAbsent(parts[0], key -> new CommandNode<>());

        for (int i = 1; i < parts.length - 1; i++) {
            parent = parent.getChildren().computeIfAbsent(parts[i], key -> new CommandNode<>());
        }

        parent.getChildren().put(parts[parts.length - 1], node);
    }

    private String normalizeCommandPath(String commandName) {
        Objects.requireNonNull(commandName, "commandName");
        String normalized = commandName.trim();

        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Command name cannot be empty.");
        }

        if (normalized.startsWith(".") || normalized.endsWith(".") || normalized.contains("..")) {
            throw new IllegalArgumentException("Command name contains an empty path segment.");
        }

        return normalized;
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
