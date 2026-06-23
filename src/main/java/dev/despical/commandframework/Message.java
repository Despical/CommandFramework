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
import dev.despical.commandframework.internal.MessageHelper;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @author Despical
 * <p>
 * Created at 18.07.2024
 */
public enum Message {

	SHORT_ARG_SIZE("<red>Required argument length is less than needed!", true),
	LONG_ARG_SIZE("<red>Required argument length greater than needed!", true),
	ONLY_BY_PLAYERS("<red>This command is only executable by players!"),
	ONLY_BY_CONSOLE("<red>This command is only executable by console!"),
	NO_PERMISSION("<red>You don't have enough permission to execute this command!"),
	MUST_HAVE_OP("<red>You must have OP to execute this command!"),
	WAIT_BEFORE_USING_AGAIN("<red>You have to wait before using this command again!");

	private BiFunction<Command, CommandArguments, Boolean> message;

	Message(String message) {
		this(message, false);
	}

	Message(String message, boolean sendUsage) {
		this.message = (command, arguments) -> {
            if (sendUsage && !MessageHelper.SEND_USAGE.apply(command, arguments)) {
                return true;
            }

			arguments.sendMessage(message);
			return true;
		};
	}

	/**
	 * For instance, can be used to translate Minecraft color and Hex color codes.
	 *
	 * @param colorFormatter the function that will be applied to the strings to colorize
	 */
	public static void setColorFormatter(@NotNull Function<String, String> colorFormatter) {
		MessageHelper.setColorFormatter(colorFormatter);
	}

	/**
	 * Sets a custom component formatter for framework messages.
	 *
	 * @param messageFormatter the function that converts raw strings to components
	 */
	public static void setMessageFormatter(@NotNull Function<String, Component> messageFormatter) {
		MessageHelper.setMessageFormatter(messageFormatter);
	}

	/**
	 * Set a custom error message.
	 *
	 * @param message the custom error message.
	 */
	public void setMessage(BiFunction<Command, CommandArguments, Boolean> message) {
		this.message = message;
	}

	@ApiStatus.Internal
	boolean sendMessage(Command command, CommandArguments arguments) {
		return this.message.apply(command, arguments);
	}
}
