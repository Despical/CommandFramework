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

package me.despical.commandframework.options;

import me.despical.commandframework.CommandArguments;

/**
 * @author Despical
 * <p>
 * Created at 18.07.2024
 *
 * @see FrameworkOption#CUSTOM_COOLDOWN_CHECKER
 * @see FrameworkOption#CONFIRMATIONS
 * @see FrameworkOption#DEBUG
 */
public enum FrameworkOption {

	/**
	 * This option allows user to call {@link CommandArguments#checkCooldown()} method.
	 */
	CUSTOM_COOLDOWN_CHECKER,

	/**
	 * This option allows users to check for command confirmations.
	 */
	CONFIRMATIONS,

	/**
	 * This option enables the debug mode for this framework.
	 */
	DEBUG
}
