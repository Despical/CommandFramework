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
import java.util.concurrent.TimeUnit;

/**
 * @author Despical
 * <p>
 * Created at 1.02.2024
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Cooldown {

	/**
	 * The time between using command again.
	 * Use 0 (zero) or a negative value for no cooldown.
	 *
	 * @return value of time to use command again.
	 */
	long cooldown();

	/**
	 * The time unit of command cooldown.
	 *
	 * @return unit of the cooldown.
	 */
	TimeUnit timeUnit() default TimeUnit.SECONDS;

	/**
	 * Command senders that have this permission
	 * will bypass the command cooldown.
	 *
	 * @return permission that can bypass cooldown.
	 */
	String bypassPerm() default "";

	/**
	 * If option is true, console will be affected by
	 * cooldowns; otherwise, it will override the
	 * delay to use the command again.
	 *
	 * @return false if console overrides cooldowns, otherwise true
	 */
	boolean overrideConsole() default false;
}