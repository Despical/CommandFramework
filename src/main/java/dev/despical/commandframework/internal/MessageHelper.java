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

package dev.despical.commandframework.internal;

import dev.despical.commandframework.CommandArguments;
import dev.despical.commandframework.annotations.Command;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @author Despical
 * <p>
 * Created at 9.12.2025
 */
@ApiStatus.Internal
public class MessageHelper {

    public static final BiFunction<Command, CommandArguments, Boolean> SEND_USAGE = (command, arguments) -> {
        final String usage = command.usage();

        if (!usage.isEmpty()) {
            arguments.sendMessage(usage);
            return false;
        }

        return true;
    };

    /**
     * Function to apply messages that will be sent using CommandArguments#sendMessage method.
     */
    @NotNull
    private static Function<String, String> colorFormatter = (text) -> ChatColor.translateAlternateColorCodes('&', text);

    /**
     * For instance, can be used to translate Minecraft color and Hex color codes.
     *
     * @param colorFormatter the function that will be applied to the strings to colorize
     */
    public static void setColorFormatter(@NotNull Function<String, String> colorFormatter) {
        MessageHelper.colorFormatter = colorFormatter;
    }

    public static String applyColorFormatter(@NotNull String string) {
        return colorFormatter.apply(string);
    }
}
