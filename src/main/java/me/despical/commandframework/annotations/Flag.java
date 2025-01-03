/*
 * Command Framework - Annotation based command framework
 * Copyright (C) 2025  Berke Akçen
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

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to specify flags for command arguments.
 * This annotation can be used multiple times on the same method.
 *
 * <p>The {@code value()} method holds the list of flags, and the
 * {@code prefix()} method defines the default prefix for the flags,
 * which is {@code --} by default.</p>
 *
 * <p>The {@link FlagContainer} is a container annotation that allows
 * the {@link Flag} annotation to be repeatable on methods.</p>
 *
 * @author Despical
 * <p>Created at 20.09.2024</p>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(Flag.FlagContainer.class)
public @interface Flag {

	/**
	 * Specifies the list of flag values.
	 *
	 * @return an array of flag values
	 */
	String[] value();

	/**
	 * Specifies the prefix for the flags. Default is "--".
	 *
	 * @return the flag prefix
	 */
	String prefix() default "--";

	/**
	 * Container annotation for holding multiple {@link Flag} annotations
	 * on the same method.
	 *
	 * <p>Used internally by the {@link Repeatable} annotation.</p>
	 */
	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	@interface FlagContainer {

		/**
		 * Holds an array of {@link Flag} annotations.
		 *
		 * @return an array of {@link Flag} annotations
		 */
		Flag[] value();
	}
}
