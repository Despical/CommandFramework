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

import org.bukkit.command.CommandSender;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

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
     * Do not try to cast objects except subclasses of {@link CommandSender}
     * otherwise {@link ClassCastException} will occur. Also casting for {@link Player}
     * {@link org.bukkit.command.ConsoleCommandSender} isn't needed.
     *
     * @return sender of command as Player or CommandSender
     */
    public <T extends CommandSender> T getSender() {
        return (T) commandSender;
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

    /**
     * @param i index
     * @return indexed element or null if index out of bounds
     */
    public String getArgument(int i) {
        return arguments.length > i && i > 0 ? arguments[i] : null;
    }
}