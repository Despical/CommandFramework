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

package dev.despical.commandframework.internal;

import dev.despical.commandframework.CommandFramework;
import dev.despical.commandframework.annotations.Command;
import dev.despical.commandframework.confirmations.ConfirmationManager;
import dev.despical.commandframework.cooldown.CooldownManager;
import dev.despical.commandframework.options.FrameworkOption;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.ApiStatus;

import java.lang.reflect.Method;

/**
 * @author Despical
 * <p>
 * Created at 9.12.2025
 */
@ApiStatus.Internal
public class FrameworkContext {

    private static final FrameworkContext instance = new FrameworkContext();

    private CooldownManager cooldownManager;
    private ConfirmationManager confirmationManager;

    private final CommandRegistry registry;

    private FrameworkContext() {
        this.registry = new CommandRegistry();
    }

    public CommandRegistry getRegistry() {
        return registry;
    }

    public CooldownManager getCooldownManager() {
        if (this.cooldownManager == null)
            this.cooldownManager = new CooldownManager(CommandFramework.getInstance());
        return cooldownManager;
    }

    public boolean checkConfirmation(CommandSender sender, final Command command, final Method method) {
        if (!CommandFramework.getInstance().options().isEnabled(FrameworkOption.CONFIRMATIONS)) {
            return false;
        }

        if (this.confirmationManager == null)
            this.confirmationManager = new ConfirmationManager();
        return confirmationManager.checkConfirmations(sender, command, method);
    }

    public static FrameworkContext getInstance() {
        return instance;
    }
}
