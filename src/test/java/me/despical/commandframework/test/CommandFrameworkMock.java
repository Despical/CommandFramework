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

package me.despical.commandframework.test;

import me.despical.commandframework.CommandFramework;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * Mocked class of {@link CommandFramework}.
 *
 * @author gamerover98
 * @author Despical
 */
public class CommandFrameworkMock extends CommandFramework {

	public CommandFrameworkMock(@NotNull Plugin plugin) {
		super(plugin);

		/*
		 * There is no SimplePluginManager class in MockBukkit,
		 * so the commandMap can be easily obtained from the ServerMock instance.
		 */
		super.setCommandMap(plugin.getServer().getCommandMap());
	}
}
