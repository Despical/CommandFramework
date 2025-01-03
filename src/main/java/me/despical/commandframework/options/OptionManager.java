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

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

/**
 * This class handles the options related Command Framework.
 *
 * <p>This is an internal class and should not be instantiated by any
 * external class.
 *
 * @author Despical
 * @since 1.4.8
 * <p>
 * Created at 18.07.2024
 *
 * @see FrameworkOption
 */
public final class OptionManager {

	private final Set<FrameworkOption> frameworkOptions;

	public OptionManager() {
		this.frameworkOptions = EnumSet.noneOf(FrameworkOption.class);
	}

	public void enableOption(FrameworkOption frameworkOption) {
		this.frameworkOptions.add(frameworkOption);
	}

	public void enableOptions(FrameworkOption... frameworkOptions) {
		this.frameworkOptions.addAll(Arrays.asList(frameworkOptions));
	}

	public boolean isEnabled(FrameworkOption frameworkOption) {
		return this.frameworkOptions.contains(frameworkOption);
	}
}
