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

import java.lang.annotation.*;

/**
 * A utility class in framework to create argument completions
 * for commands.
 *
 * @author Despical
 * @since 1.0.0
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Completer {

	/**
	 * The name of the command. If command would be a sub command then
	 * sub command's name must be separated by dot. For example like the
	 * {@code "command.subcommand"}
	 *
	 * @return name of the command or subcommand
	 */
	String name();

	/**
	 * The permission that sender must have to receive tab complete.
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
}