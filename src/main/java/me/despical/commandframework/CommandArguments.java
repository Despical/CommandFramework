package me.despical.commandframework;

import org.bukkit.command.CommandSender;
import org.bukkit.command.Command;

/**
 * An utility class to use command arguments without external
 * Bukkit parameters.
 *
 * @author  Despical
 * @since   1.0.0
 */
public class CommandArguments {

    private final CommandSender commandSender;
    private final Command command;
    private final String label;
    private final String[] arguments;

    public CommandArguments(CommandSender commandSender, Command command, String label, String... arguments) {
        this.commandSender = commandSender;
        this.command = command;
        this.label = label;
        this.arguments = arguments;
    }

    /**
     * @return sender of command
     */
    public CommandSender getSender() {
        return commandSender;
    }

    /**
     * @return base command.
     */
    public Command getCommand() {
        return command;
    }

    /**
     * @return label of the command.
     */
    public String getLabel() {
        return label;
    }

    /**
     * @return arguments of the command.
     */
    public String[] getArguments() {
        return arguments;
    }
}