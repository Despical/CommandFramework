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

package me.despical.commandframework.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Main class of the framework to create commands in easy way.
 *
 * @author Despical
 * @since 1.0.0
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Command {

	/**
	 * The name of the command. If command would be a sub command then
	 * sub command's name must be separated by dot. For example like the
	 * {@code "command.subcommand"}
	 *
	 * @return name of the command or subcommand
	 */
	String name();

	/**
	 * The permission that sender has to have to execute command.
	 *
	 * @return name of the permission
	 */
	String permission() default "";

	/**
	 * An alternative name list of command. Check {@link #name()}
	 * to understand how command names work.
	 *
	 * @return aliases list of the command
	 */
	String[] aliases() default {};

	/**
	 * The description of the command that will be showed when sender executes
	 * Bukkit's help command.
	 *
	 * @return description of the command
	 */
	String desc() default "";

	/**
	 * The usage of the command that will be showed when sender executes
	 * command without required or missing arguments.
	 *
	 * @return usage of the command.
	 */
	String usage() default "";

	/**
	 * Minimum value of arguments.
	 *
	 * @return minimum value of arguments.
	 */
	int min() default 0;

	/**
	 * Maximum value of arguments. -1 for infinite.
	 *
	 * @return maximum value of arguments.
	 */
	int max() default -1;

	/**
	 * Only op players can execute this command.
	 * <p>
	 * Permissions will be ignored if this option
	 * is enabled.
	 *
	 * @return allow only op players.
	 */
	boolean onlyOp() default false;

	/**
	 * This option is now deprecated. In newer versions of framework
	 * a new API will be introduced with additional features.
	 * <p>
	 * This option makes command to execute in a separate thread
	 * but involves HIGH RISKS because the Bukkit API, except the
	 * scheduler package, is not thread safe nor guaranteed to be
	 * thread safe.
	 *
	 * @return asynchronous execution of command.
	 */
	@Deprecated
	boolean async() default false;

	/**
	 * Enum value of command sender type to define who will
	 * use the command.
	 *
	 * @return enum value of {@link SenderType}
	 */
	SenderType senderType() default SenderType.BOTH;

	/**
	 * Available command sender or senders.
	 */
	enum SenderType {
		BOTH, CONSOLE, PLAYER
	}
}