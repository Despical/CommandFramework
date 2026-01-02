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

import dev.despical.commandframework.internal.FrameworkContext;
import dev.despical.commandframework.internal.MessageHelper;
import dev.despical.commandframework.utils.Utils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * A utility class to use command arguments without external
 * Bukkit parameters and includes some useful methods to improve
 * code quality and performance.
 *
 * @author Despical
 * @since 1.0.0
 */
public final class CommandArguments {

	private Set<String> parsedFlags;
	private Map<String, List<String>> parsedOptions;

	private final dev.despical.commandframework.annotations.Command command;
	private final CommandSender commandSender;
	private final Command bukkitCommand;
	private final String label;
	private final String [] arguments;

	CommandArguments(
        CommandSender commandSender,
		Command bukkitCommand,
		dev.despical.commandframework.annotations.Command command,
		String label,
		String... arguments
    ) {
		this.commandSender = commandSender;
		this.bukkitCommand = bukkitCommand;
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
    @Contract(pure = true)
	public <T extends CommandSender> T getSender() {
		return (T) commandSender;
	}

	/**
	 * Retrieves the base command associated with this object.
	 *
	 * @return the base command.
	 * @since 1.4.8
	 */
	@Nullable
    @Contract(pure = true)
	public dev.despical.commandframework.annotations.Command getCommand() {
		return this.command;
	}

	/**
	 * Retrieves the Bukkit command associated with this object.
	 *
	 * @return the base command as a Bukkit command.
	 */
	@NotNull
    @Contract(pure = true)
	public Command getBukkitCommand() {
		return bukkitCommand;
	}

	/**
	 * Retrieves the label associated with this object.
	 *
	 * @return label of the command.
	 */
	@NotNull
    @Contract(pure = true)
	public String getLabel() {
		return label;
	}

	/**
	 * Retrieves the array of arguments associated with this object.
	 *
	 * @return arguments of the command.
	 */
	@NotNull
    @Contract(pure = true)
	public String[] getArguments() {
		return arguments;
	}

	// SOME GETTER METHODS FOR COMMON PRIMITIVE TYPES //

	/**
	 * Retrieves the argument at the specified index.
	 *
	 * @param index the index of desired argument.
	 * @return indexed element or null if index out of bounds
	 */
	@Nullable
    @Contract(pure = true)
	public String getArgument(int index) {
		return arguments.length > index && index >= 0 ? arguments[index] : null;
	}

    /**
     * Gets the first argument.
     *
     * @return The first argument, or {@code null} if no arguments exist.
     */
    @Nullable
    @Contract(pure = true)
    public String getFirst() {
        return this.getArgument(0);
    }

    /**
     * Gets the last argument.
     *
     * @return The last argument, or {@code null} if no arguments exist.
     */
    @Nullable
    @Contract(pure = true)
    public String getLast() {
        return this.getArgument(arguments.length - 1);
    }

	/**
	 * Returns the indexed element from the arguments array, or the {@code defaultValue}
	 * if and only if index is out the bounds.
	 *
	 * @param index        the index of desired argument.
	 * @param defaultValue the default value to return if the index is out of bounds.
	 * @return the argument at the specified index, or the default value if the index is out of bounds.
	 */
	@NotNull
    @Contract(pure = true)
	public String getArgument(int index, String defaultValue) {
		return arguments.length > index && index >= 0 ? arguments[index] : defaultValue;
	}

	/**
	 * Returns the integer value of the indexed element from the arguments array.
	 *
	 * @param index the index of desired argument.
	 * @return Integer if indexed element is primitive type of int
	 * or 0 if element is null.
	 */
    @Contract(pure = true)
	public int getArgumentAsInt(int index) {
		return Utils.getInt(this.getArgument(index));
	}

	/**
	 * Returns the double value of the indexed element from the arguments array.
	 *
	 * @param index the index of desired argument.
	 * @return Double if indexed element is primitive type of double
	 * or 0 if element is null.
	 */
    @Contract(pure = true)
	public double getArgumentAsDouble(int index) {
		return Utils.getDouble(this.getArgument(index));
	}

	/**
	 * Returns the float value of the indexed element from the arguments array.
	 *
	 * @param index the index of desired argument.
	 * @return Float if indexed element is primitive type of float
	 * or 0 if element is null.
	 */
    @Contract(pure = true)
	public float getArgumentAsFloat(int index) {
		return Utils.getFloat(this.getArgument(index));
	}

	/**
	 * Returns the long value of the indexed element from the arguments array.
	 *
	 * @param index the index of desired argument.
	 * @return Long if indexed element is primitive type of long
	 * or 0 if element is null.
	 */
    @Contract(pure = true)
	public long getArgumentAsLong(int index) {
		return Utils.getLong(this.getArgument(index));
	}

	/**
	 * Returns the boolean value of the indexed element from the arguments array.
	 *
	 * @param index the index of desired argument.
	 * @return Boolean if indexed element is primitive type of boolean
	 * or 0 if element is null.
	 */
    @Contract(pure = true)
	public boolean getArgumentAsBoolean(int index) {
		return "true".equalsIgnoreCase(this.getArgument(index));
	}

	// ---------------------------------------------- //

	/**
	 * Returns {@code true} if the arguments array is empty, otherwise {@code false}.
	 *
	 * @return true if arguments are empty, otherwise false.
	 */
    @Contract(pure = true)
	public boolean isArgumentsEmpty() {
		return arguments.length == 0;
	}

	/**
	 * Sends message to sender without receiving the command sender.
	 *
	 * @param message the message will be sent to sender.
	 */
	public void sendMessage(String message) {
		if (message == null)
			return;
		commandSender.sendMessage(MessageHelper.applyColorFormatter(message));
	}

	/**
	 * Sends message to sender without receiving command
	 * sender with the given parameters.
	 *
	 * @param message the message will be sent to sender.
	 * @param params  the parameters to format the message.
	 */
	public void sendMessage(String message, Object... params) {
		if (message == null)
			return;
		commandSender.sendMessage(MessageHelper.applyColorFormatter(MessageFormat.format(message, params)));
	}

	/**
	 * Sends the specified {@link Message} to command sender associated with this object.
	 *
	 * @param  message the {@link Message} object to be sent.
	 * @see    #getSender()
	 */
	public boolean sendMessage(Message message) {
		return message.sendMessage(command, this);
	}

    /**
     * Sends the specified {@link Component} to command sender associated with this object.
     *
     * @param  component the {@link Component} object to be sent.
     */
    public void sendMessage(Component component) {
        commandSender.sendMessage(component);
    }

	/**
	 * Returns {@code true} if, and only if, command sender is console.
	 *
	 * @return {@code true} if, and only if, command sender is console, otherwise
	 * {@code false}.
	 */
    @Contract(pure = true)
	public boolean isSenderConsole() {
		return !isSenderPlayer();
	}

	/**
	 * Returns {@code true} if, and only if, command sender is player.
	 *
	 * @return {@code true} if, and only if, command sender is player, otherwise
	 * {@code false}.
	 */
    @Contract(pure = true)
	public boolean isSenderPlayer() {
		return commandSender instanceof Player;
	}

	/**
	 * Returns {@code true} if the command sender has required {@code permission} or, if
	 * {@code permission} is empty.
	 *
	 * @param permission the permission to check.
	 * @return {@code true} if the command sender has required {@code permission} or, if
	 * {@code permission} is empty, otherwise {@code false}.
	 */
    @Contract(pure = true)
	public boolean hasPermission(String permission) {
		return permission.isEmpty() || commandSender.hasPermission(permission);
	}

	/**
	 * Returns the number of arguments passed to the command.
	 *
	 * @return length of the arguments array.
	 */
    @Contract(pure = true)
	public int getLength() {
		return arguments.length;
	}

	/**
	 * Gets player object from the server with given {@code name}.
	 *
	 * @param name the name of player.
	 * @return player with the given name if online, otherwise
	 * empty optional.
	 * @throws IllegalArgumentException if the {@code name} is null.
	 * @see Optional#empty()
	 * @since 1.3.6
	 */
    @Contract(pure = true)
	public Optional<Player> getPlayer(String name) {
		return Optional.ofNullable(Bukkit.getPlayer(name));
	}

	/**
	 * Gets player object from the server with given argument.
	 *
	 * @param index the index of desired argument.
	 * @return player with the given name if online, otherwise
	 * empty optional.
	 * @throws IllegalArgumentException if given index is out
	 *                                  of bounds.
	 * @see Optional#empty()
	 * @since 1.3.6
	 */
    @Contract(pure = true)
	public Optional<Player> getPlayer(int index) {
		return this.getPlayer(this.getArgument(index));
	}

	/**
	 * Concatenates all arguments into a single {@code String}
	 * object.
	 *
	 * @return all arguments as a single String object.
	 * @since 1.3.8
	 */
    @Contract(pure = true)
	public String concatArguments() {
		return String.join(" ", arguments);
	}

	/**
	 * Concatenates a range of elements from the specified array
	 * into a single string, using a space as the delimiter.
	 *
	 * @param from the starting index (inclusive) of the range.
	 * @param to   the ending index (exclusive) of the range.
	 * @return a string containing the concatenated elements within
	 * the specified range, separated by a space.
	 * @throws ArrayIndexOutOfBoundsException if {@code from} is negative,
	 *                                        {@code to} is greater than the length of the array,
	 *                                        or {@code from} is greater than {@code to}.
	 * @since 1.3.8
	 */
    @Contract(pure = true)
	public String concatRangeOf(int from, int to) {
		return String.join(" ", Arrays.copyOfRange(arguments, from, to));
	}

	/**
	 * Checks if the value obtained from the argument at the specified index is numeric,
	 * i.e., if it contains only digit characters (0-9).
	 *
	 * @param index The index of the argument from which the value is retrieved.
	 * @return {@code true} if the value at the specified argument index is numeric, {@code false} otherwise.
	 * Returns {@code false} for null or empty values obtained from the argument.
	 */
    @Contract(pure = true)
	public boolean isNumeric(int index) {
		return this.isNumeric(this.getArgument(index));
	}

	/**
	 * Checks if the given string is numeric, i.e., if it contains only digit characters (0-9).
	 *
	 * @param string The input string to be checked for numeric content.
	 * @return {@code true} if the input string is numeric, {@code false} otherwise.
	 * Returns {@code false} for null or empty strings.
	 */
    @Contract(pure = true)
	public boolean isNumeric(String string) {
		if (string == null || string.isEmpty())
			return false;

		return string.chars().allMatch(Character::isDigit);
	}

	/**
	 * Checks if the value obtained from the argument at the specified index can be successfully
	 * parsed into an integer using {@code Integer.parseInt}.
	 *
	 * @param index The index of the argument from which the value is retrieved.
	 * @return {@code true} if the value at the specified argument index can be parsed into an integer,
	 * {@code false} otherwise. Returns {@code false} for null or empty values obtained from the argument.
	 * Also returns {@code false} for values that cannot be parsed into an integer.
	 */
    @Contract(pure = true)
	public boolean isInteger(int index) {
		return this.isInteger(this.getArgument(index));
	}

	/**
	 * Checks if the given string can be successfully parsed into an integer using {@code Integer.parseInt}.
	 *
	 * @param string The input string to be checked for its ability to be parsed into an integer.
	 * @return {@code true} if the string can be parsed into an integer, {@code false} otherwise.
	 * Returns {@code false} for null strings or strings that cannot be parsed into an integer.
	 */
    @Contract(pure = true)
	public boolean isInteger(String string) {
		try {
			Integer.parseInt(string);
			return true;
		} catch (Exception exception) {
			return false;
		}
	}

	/**
	 * Checks if the value obtained from the argument at the specified index can be successfully
	 * parsed into a floating-point decimal using {@code Double.parseDouble}.
	 *
	 * @param index The index of the argument from which the value is retrieved.
	 * @return {@code true} if the value at the specified argument index can be parsed into a floating-point decimal,
	 * {@code false} otherwise. Returns {@code false} for null or empty values obtained from the argument.
	 * Also returns {@code false} for values that cannot be parsed into a floating-point decimal.
	 */
    @Contract(pure = true)
	public boolean isFloatingDecimal(int index) {
		return this.isFloatingDecimal(this.getArgument(index));
	}

	/**
	 * Checks if the given string can be successfully parsed into a floating decimal using {@code Double.parseDouble}.
	 * Supports primitive types such as {@code Integer}, {@code Float}, {@code Double}, {@code Long}, etc.
	 *
	 * @param string The input string to be checked for its ability to be parsed into a decimal.
	 * @return {@code true} if the string can be parsed into a decimal, {@code false} otherwise.
	 * Returns {@code false} for null strings or strings that cannot be parsed into a decimal.
	 */
    @Contract(pure = true)
	public boolean isFloatingDecimal(String string) {
		try {
			Double.parseDouble(string);
			return true;
		} catch (Exception exception) {
			return false;
		}
	}

	/**
	 * This method checks if the current command sender has a cooldown on the command that
	 * is associated with current {@link CommandArguments} object.
	 *
	 * <blockquote>For example,
	 * <pre>{@code
	 *     @Command(name = "test")
	 *     @Cooldown(cooldown = 5)
	 *     public void testCommand(CommandArguments args) {
	 *         if (args.getLength() != 1) {
	 *           	// if sender has cooldown, execution will be end here
	 *           	args.checkCooldown();
	 *         }
	 *
	 *         args.sendMessage("Test command successfully executed.");
	 *     }
	 * }</pre></blockquote>
	 *
	 * Note that execution will be stopped if this method returns {@code true}.
	 *
	 * @return {@code true} if the sender has a cooldown on this command
	 */
	public boolean checkCooldown() {
		return FrameworkContext.getInstance().getCooldownManager().hasCooldown(this);
	}

	void setParsedOptions(Map<String, List<String>> parsedOptions) {
		this.parsedOptions = parsedOptions;
	}

	@Nullable
    @Contract(pure = true)
	public List<String> getOption(final @NotNull String option) {
		return this.parsedOptions.get(option);
	}

    @Contract(pure = true)
	public Optional<List<String>> findOption(final @NotNull String option) {
		return Optional.ofNullable(this.getOption(option));
	}

	void setParsedFlags(Set<String> parsedFlags) {
		this.parsedFlags = parsedFlags;
	}

    @Contract(pure = true)
	public boolean isFlagPresent(final @NotNull String flag) {
		return this.parsedFlags.contains(flag);
	}
}
