package me.despical.commandframework;

import org.bukkit.ChatColor;
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
            pluginCommand.setDescription(command.desc());
            pluginCommand.setAliases(Arrays.asList(command.aliases()));

            commandMap.register(splittedCommand, pluginCommand);
        } catch(Exception exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args) {
        for (Map.Entry<Command, Map.Entry<Method, Object>> entry : commands.entrySet()) {
            Command command = entry.getKey();
            String[] splitted = command.name().split("\\.");
            String allArgs = String.join(".", Arrays.copyOfRange(args, 0, splitted.length - 1));

            if (isValidTrigger(command, command.name().contains(".") ? splitted[0] + "." + (args.length > 0 ? allArgs : "") : cmd.getName())) {
                if (!sender.hasPermission(command.permission())) {
                    sender.sendMessage(ChatColor.RED + "You don't have enough permission to execute this command!");
                    return true;
                }

                if (command.senderType() == Command.SenderType.PLAYER && !(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "This command is only executable by players!");
                    return false;
                }

                if (command.senderType() == Command.SenderType.CONSOLE && sender instanceof Player) {
                    sender.sendMessage(ChatColor.RED + "This command is only executable by console!");
                    return false;
                }

                String[] newArgs = Arrays.copyOfRange(args, splitted.length - 1, args.length);

                if (args.length >= command.min() + splitted.length - 1 && newArgs.length <= (command.max() == -1 ? newArgs.length + 1 : command.max())) {
                    try {
                        entry.getValue().getKey().invoke(entry.getValue().getValue(), new CommandArguments(sender, cmd, label, newArgs));
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "Required argument length is less or greater than needed!");
                }

                return true;
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

    public final boolean isValidTrigger(Command cmd, String name) {
        if (cmd.name().equalsIgnoreCase(name)) {
            return true;
        }

       for (String alias : cmd.aliases()) {
            if (alias.equalsIgnoreCase(name)) {
                return true;
            }
       }

       return false;
    }

    /**
     * Get list of registered commands.
     *
     * @return list of commands.
     */
    public List<Command> getCommands() {
        return new ArrayList<>(commands.keySet());
    }
}