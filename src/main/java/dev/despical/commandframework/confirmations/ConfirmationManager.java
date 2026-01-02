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

package dev.despical.commandframework.confirmations;

import dev.despical.commandframework.annotations.Command;
import dev.despical.commandframework.annotations.Confirmation;
import dev.despical.commandframework.internal.MessageHelper;
import dev.despical.commandframework.options.FrameworkOption;
import dev.despical.commandframework.utils.SelfExpiringHashMap;
import dev.despical.commandframework.utils.SelfExpiringMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.jetbrains.annotations.ApiStatus;

import java.lang.reflect.Method;

/**
 * This class handles the confirmations for commands and subcommands.
 *
 * <p>This is an internal class and should not be instantiated by any
 * external class.
 *
 * @author Despical
 * @since 1.4.8
 * <p>
 * Created at 18.07.2024
 *
 * @see FrameworkOption#CONFIRMATIONS
 * @see Confirmation
 */
@ApiStatus.Internal
public final class ConfirmationManager {

	private final SelfExpiringMap<CommandSender, Command> confirmations;

	public ConfirmationManager() {
		this.confirmations = new SelfExpiringHashMap<>();
	}

	public boolean checkConfirmations(final CommandSender sender, final Command command, final Method method) {
		if (method == null) return false;
		if (!method.isAnnotationPresent(Confirmation.class)) return false;

		final Confirmation confirmation = method.getAnnotation(Confirmation.class);

		if (confirmation.expireAfter() <= 0) return false;

		final boolean isConsoleSender = sender instanceof ConsoleCommandSender;

		if (isConsoleSender && !confirmation.overrideConsole()) return false;

        final String bypassPerm = confirmation.bypassPerm();

		if (!isConsoleSender && !bypassPerm.isEmpty() && sender.hasPermission(bypassPerm))
			return false;

		if (confirmations.containsKey(sender)) {
			confirmations.remove(sender);
			return false;
		}

		confirmations.put(sender, command, confirmation.timeUnit().toMillis(confirmation.expireAfter()));

		sender.sendMessage(MessageHelper.applyColorFormatter(confirmation.message()));
		return true;
	}
}
