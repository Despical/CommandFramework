package me.despical.commandframework;

import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.SimplePluginManager;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Main class of the framework to register commands.
 *
 * @author  Despical
 * @since   1.0.0
 */
public class CommandFramework implements CommandExecutor, TabCompleter {

    /**
     * Main instance of framework.
     */
    private final Plugin plugin;

    /**
     * List of registered commands by framework.
     */
    private final Map<Command, Map.Entry<Method, Object>> commands = new HashMap<>();

    /**
     * List of registered tab completions by framework.
     */
    private final Map<Completer, Map.Entry<Method, Object>> completions = new HashMap<>();

    /**
     * Default command map of Bukkit.
     */
    private CommandMap commandMap;

    public CommandFramework(Plugin plugin) {
        this.plugin = plugin;

        if (plugin.getServer().getPluginManager() instanceof SimplePluginManager) {
            SimplePluginManager manager = (SimplePluginManager) plugin.getServer().getPluginManager();

            try {
                Field field = SimplePluginManager.class.getDeclaredField("commandMap");
                field.setAccessible(true);

                commandMap = (CommandMap) field.get(manager);
            } catch (IllegalArgumentException | SecurityException | IllegalAccessException | NoSuchFieldException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Register command methods in object class.
     *
     * @param instance object class
     */
    public void registerCommands(Object instance) {
        for (Method method : instance.getClass().getMethods()) {
            Command command = method.getAnnotation(Command.class);

            if (command != null) {
                if (method.getParameterTypes().length > 0 && method.getParameterTypes()[0] != CommandArguments.class) {
                    continue;
                }

                registerCommand(command, method, instance);
            } else if (method.getAnnotation(Completer.class) != null) {
                completions.put(method.getAnnotation(Completer.class), new AbstractMap.SimpleEntry<>(method, instance));
            }
        }
    }

    public void registerCommand(Command command, Method method, Object instance) {
        commands.put(command, new AbstractMap.SimpleEntry<>(method, instance));

        try {
            Constructor<PluginCommand> constructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            constructor.setAccessible(true);

            String splittedCommand = command.name().split("\\.")[0];

            PluginCommand pluginCommand = constructor.newInstance(splittedCommand, plugin);
            pluginCommand.setTabCompleter(this);
            pluginCommand.setExecutor(this);
            pluginCommand.setUsage(command.usage());
            pluginCommand.setPermission(command.permission());
            pluginCommand.setDescription(command.description());
            pluginCommand.setAliases(Arrays.asList(command.aliases()));

            commandMap.register(splittedCommand, pluginCommand);
        } catch(Exception exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        for (Map.Entry<Command, Map.Entry<Method, Object>> entry : commands.entrySet()) {
            Command cmd = entry.getKey();
            String[] arguments = cmd.name().split("\\.");

            if (isValidTrigger(cmd, command.getName())) {
                if (cmd.senderType() == Command.SenderType.PLAYER && !(sender instanceof Player)) {
                    sender.sendMessage("This command is only executable by players!");
                    return false;
                }

                if (cmd.senderType() == Command.SenderType.CONSOLE && sender instanceof Player) {
                    sender.sendMessage("This command is only executable by console!");
                    return false;
                }

                if (Arrays.equals(Arrays.copyOfRange(arguments, 1, arguments.length), args) || isValidArguments(cmd, args)) {
                    try {
                        entry.getValue().getKey().invoke(entry.getValue().getValue(), new CommandArguments(sender, command, label, args));
                        return true;
                    } catch (IllegalAccessException | InvocationTargetException exception) {
                        exception.printStackTrace();
                    }
                }
            }
        }

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        for (Map.Entry<Completer, Map.Entry<Method, Object>> entry : completions.entrySet()) {
            Completer completer = entry.getKey();

            // FIXME: not compatible with aliases
            if (command.getName().equalsIgnoreCase(completer.name())) {
                try {
                    Object instance = entry.getValue().getKey().invoke(entry.getValue().getValue(), new CommandArguments(sender, command, label, args));
                    List<String> list = (List<String>) instance;

                    if (args.length - 1 < list.size()) {
                        return list;
                    }
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }

    // TODO: replace the method with better one
    public final boolean isValidTrigger(Command command, String name) {
        String splitted = command.name().split("\\.")[0];

        if (splitted.equalsIgnoreCase(name)) {
            return true;
        }

        for (String alias : command.aliases()) {
            if (alias.equalsIgnoreCase(name)) {
                return true;
            }
        }

        return false;
    }

    // TODO: replace the method with better one
    public final boolean isValidArguments(Command command, String[] args) {
        String[] splitted = command.name().split("\\.");

        for (String alias : command.aliases()) {
            splitted[0] = alias;

            if (Arrays.equals(splitted, args)) {
                return true;
            }
        }

        return false;
    }
}