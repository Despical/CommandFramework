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

package dev.despical.commandframework.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to define options for command arguments.
 * This annotation can be used multiple times on the same method.
 *
 * <p>The {@code value()} method represents the key of the option,
 * and the {@code prefix()} method defines the default prefix for options,
 * which is {@code --} by default.</p>
 *
 * <p>Additional separators can be customized using the {@code valueSeparator()}
 * and {@code keySeparator()} methods. By default, the value separator is
 * a comma ({@code ,}), and the key separator is an equals sign ({@code =}).
 * The {@code allowSeparating()} method controls whether separating values
 * is allowed, with the default set to {@code true}.</p>
 *
 * <p>The {@link OptionContainer} is a container annotation that allows
 * the {@link Option} annotation to be repeatable on methods.</p>
 *
 * @author Despical
 * <p>Created at 20.09.2024</p>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(Option.OptionContainer.class)
public @interface Option {

	/**
	 * Specifies the key of the option.
	 *
	 * @return the option key
	 */
	String value();

	/**
	 * Specifies the prefix for the options. Default is "--".
	 *
	 * @return the option prefix
	 */
	String prefix() default "--";

	/**
	 * Specifies the separator used between values. Default is a comma ",".
	 *
	 * @return the value separator
	 */
	String valueSeparator() default ",";

	/**
	 * Specifies the separator between key and value. Default is "=".
	 *
	 * @return the key-value separator
	 */
	String keySeparator() default "=";

	/**
	 * Determines whether separating multiple values is allowed.
	 * Default is {@code true}.
	 *
	 * @return {@code true} if separating is allowed, {@code false} otherwise
	 */
	boolean allowSeparating() default true;

	/**
	 * Container annotation for holding multiple {@link Option} annotations
	 * on the same method.
	 *
	 * <p>Used internally by the {@link Repeatable} annotation.</p>
	 */
	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	@interface OptionContainer {

		/**
		 * Holds an array of {@link Option} annotations.
		 *
		 * @return an array of {@link Option} annotations
		 */
		Option[] value();
	}
}
