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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Despical
 * <p>
 * Created at 29.03.2024
 */
@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Param {

	/**
	 * The unique id of the parameter.
	 * <p>
	 * If a {@link Class} name is used as an id then no need for
	 * this annotation to be used. The parameters can directly be
	 * used from method's parameter list.
	 * </p>
	 *
	 * @return the unique id to call annotated parameter
	 */
	String value();
}
