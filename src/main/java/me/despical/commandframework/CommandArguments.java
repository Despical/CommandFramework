/*
 * Command Framework - Annotation based command framework
 * Copyright (C) 2024  Berke Akçen
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

package me.despical.commandframework;

import me.despical.commandframework.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Optional;

/**
 * A utility class to use command arguments without external
 * Bukkit parameters and includes some useful methods to improve
 * code performance and quality.
 *
 * @author Despical
 * @since 1.0.0
 */
public class CommandArguments {

	private final CommandSender commandSender;
	private final Command command;
	private final String label, arguments[];

	public CommandArguments(CommandSender commandSender, Command command, String label, String... arguments) {
		this.commandSender = commandSender;
		this.command = command;
		this.label = label;
		this.arguments = arguments;
	}

	/**
	 * Do not try to cast objects except subclasses of {@link CommandSender}
	 * otherwise {@link ClassCastException} will occur. Also casting for {@link Player}
	 * or {@link ConsoleCommandSender} isn't needed.
	 *
	 * @param <T> {@link CommandSender}
	 * @return sender of command as Player or CommandSender
	 */
	@SuppressWarnings("unchecked")
	@NotNull
	public <T extends CommandSender> T getSender() {
		return (T) commandSender;
	}

	/**
	 * @return base command.
	 */
	@NotNull
	public Command getCommand() {
		return command;
	}

	/**
	 * @return label of the command.
	 */
	@NotNull
	public String getLabel() {
		return label;
	}

	/**
	 * @return arguments of the command.
	 */
	@NotNull
	public String[] getArguments() {
		return arguments;
	}

	// SOME GETTER METHODS FOR COMMON PRIMITIVE TYPES //

	/**
	 * @param i   the index of desired argument.
	 * @return indexed element or null if index out of bounds
	 */
	@Nullable
	public String getArgument(int i) {
		return arguments.length > i && i >= 0 ? arguments[i] : null;
	}

	/**
	 * @param i   the index of desired argument.
	 * @return Integer if indexed element is primitive type of int
	 *         or 0 if element is null.
	 */
	public int getArgumentAsInt(int i) {
		return Utils.getInt(this.getArgument(i));
	}

	/**
	 * @param i   the index of desired argument.
	 * @return Double if indexed element is primitive type of double
	 *         or 0 if element is null.
	 */
	public double getArgumentAsDouble(int i) {
		return Utils.getDouble(this.getArgument(i));
	}

	/**
	 * @param i   the index of desired argument.
	 * @return Float if indexed element is primitive type of float
	 *         or 0 if element is null.
	 */
	public float getArgumentAsFloat(int i) {
		return Utils.getFloat(this.getArgument(i));
	}

	/**
	 * @param i   the index of desired argument.
	 * @return Long if indexed element is primitive type of long
	 *         or 0 if element is null.
	 */
	public long getArgumentAsLong(int i) {
		return Utils.getLong(this.getArgument(i));
	}

	/**
	 * @param i   the index of desired argument.
	 * @return Boolean if indexed element is primitive type of boolean
	 *         or 0 if element is null.
	 */
	public boolean getArgumentAsBoolean(int i) {
		return "true".equalsIgnoreCase(this.getArgument(i));
	}

	// ---------------------------------------------- //

	/**
	 * @return true if arguments are empty, otherwise false.
	 */
	public boolean isArgumentsEmpty() {
		return arguments.length == 0;
	}

	/**
	 * Sends message to sender without receiving command
	 * sender.
	 *
	 * @param message   the message will be sent to sender.
	 */
	public void sendMessage(String message) {
		if (message == null)
			return;
		commandSender.sendMessage(CommandFramework.instance.colorFormatter.apply(message));
	}

	/**
	 * Checks if command sender is console.
	 *
	 * @return true if sender is console, otherwise false
	 */
	public boolean isSenderConsole() {
		return !isSenderPlayer();
	}

	/**
	 * Checks if command sender is player.
	 *
	 * @return true if sender is player, otherwise false
	 */
	public boolean isSenderPlayer() {
		return commandSender instanceof Player;
	}

	/**
	 * Checks if command sender has specified permission.
	 *
	 * @param permission   the permission to check.
	 * @return true if sender has permission or {@code permission} is empty, otherwise false.
	 */
	public boolean hasPermission(String permission) {
		return permission.isEmpty() || commandSender.hasPermission(permission);
	}

	/**
	 * @return length of the arguments array.
	 */
	public int getLength() {
		return arguments.length;
	}

	/**
	 * Gets player object from the server with given {@code name}.
	 *
	 * @param name
	 *        the name of player.
	 *
	 * @return player with the given name if online, otherwise
	 *         empty optional.
	 *
	 * @throws IllegalArgumentException if the {@code name} is null.
	 *
	 * @see Optional#empty()
	 * @since 1.3.6
	 */
	public Optional<Player> getPlayer(String name) {
		return Optional.ofNullable(Bukkit.getPlayer(name));
	}

	/**
	 * Gets player object from the server with given argument.
	 *
	 * @param i
	 *        the index of desired argument.
	 *
	 * @return player with the given name if online, otherwise
	 *         empty optional.
	 *
	 * @throws IllegalArgumentException if given index is out
	 *         of bounds.
	 *
	 * @see Optional#empty()
	 * @since 1.3.6
	 */
	public Optional<Player> getPlayer(int i) {
        return this.getPlayer(this.getArgument(i));
    }

	/**
	 * Concatenates all arguments into a single {@code String}
	 * object.
	 *
	 * @return all arguments as a single String object.
	 * @since 1.3.8
	 */
	public String concatenateArguments() {
		return String.join(" ", arguments);
	}

	/**
	 * Concatenates a range of elements from the specified array
	 * into a single string, using a space as the delimiter.
	 *
	 * @param from the starting index (inclusive) of the range.
	 * @param to   the ending index (exclusive) of the range.
	 * @return a string containing the concatenated elements within
	 *         the specified range, separated by a space.
	 * @throws ArrayIndexOutOfBoundsException if {@code from} is negative,
	 *         {@code to} is greater than the length of the array,
	 *         or {@code from} is greater than {@code to}.
	 * @since 1.3.8
	 */
	public String concatenateRangeOf(int from, int to) {
		return String.join(" ", Arrays.copyOfRange(arguments, from, to));
	}

	/**
	 * Checks if the value obtained from the argument at the specified index is numeric,
	 * i.e., if it contains only digit characters (0-9).
	 *
	 * @param i   The index of the argument from which the value is retrieved.
	 * @return {@code true} if the value at the specified argument index is numeric, {@code false} otherwise.
	 *         Returns {@code false} for null or empty values obtained from the argument.
	 */
	public boolean isNumeric(int i) {
		return this.isNumeric(this.getArgument(i));
	}

	/**
	 * Checks if the given string is numeric, i.e., if it contains only digit characters (0-9).
	 *
	 * @param string   The input string to be checked for numeric content.
	 * @return {@code true} if the input string is numeric, {@code false} otherwise.
	 *         Returns {@code false} for null or empty strings.
	 */
	public boolean isNumeric(String string) {
		if (string == null || string.isEmpty())
			return false;

		return string.chars().allMatch(Character::isDigit);
	}

	/**
	 * Checks if the value obtained from the argument at the specified index can be successfully
	 * parsed into an integer using {@code Integer.parseInt}.
	 *
	 * @param i   The index of the argument from which the value is retrieved.
	 * @return {@code true} if the value at the specified argument index can be parsed into an integer,
	 *         {@code false} otherwise. Returns {@code false} for null or empty values obtained from the argument.
	 *         Also returns {@code false} for values that cannot be parsed into an integer.
	 */
	public boolean isInteger(int i) {
		return this.isInteger(this.getArgument(i));
	}

	/**
	 * Checks if the given string can be successfully parsed into an integer using {@code Integer.parseInt}.
	 *
	 * @param string   The input string to be checked for its ability to be parsed into an integer.
	 * @return {@code true} if the string can be parsed into an integer, {@code false} otherwise.
	 *         Returns {@code false} for null strings or strings that cannot be parsed into an integer.
	 */
	public boolean isInteger(String string) {
		try {
			Integer.parseInt(string);
			return true;
		} catch (NumberFormatException | NullPointerException exception) {
			return false;
		}
	}

	/**
	 * Checks if the value obtained from the argument at the specified index can be successfully
	 * parsed into a floating-point decimal using {@code Double.parseDouble}.
	 *
	 * @param i   The index of the argument from which the value is retrieved.
	 * @return {@code true} if the value at the specified argument index can be parsed into a floating-point decimal,
	 *         {@code false} otherwise. Returns {@code false} for null or empty values obtained from the argument.
	 *         Also returns {@code false} for values that cannot be parsed into a floating-point decimal.
	 */
	public boolean isFloatingDecimal(int i) {
		return this.isFloatingDecimal(this.getArgument(i));
	}

	/**
	 * Checks if the given string can be successfully parsed into a floating decimal using {@code Double.parseDouble}.
	 * Supports primitive types such as {@code Integer}, {@code Float}, {@code Double}, {@code Long}, etc.
	 *
	 * @param string   The input string to be checked for its ability to be parsed into a decimal.
	 * @return {@code true} if the string can be parsed into a decimal, {@code false} otherwise.
	 *         Returns {@code false} for null strings or strings that cannot be parsed into a decimal.
	 */
	public boolean isFloatingDecimal(String string) {
		try {
			Double.parseDouble(string);
			return true;
		} catch (NumberFormatException | NullPointerException exception) {
			return false;
		}
	}
}