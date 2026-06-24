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

import dev.despical.commandframework.CommandArguments;
import dev.despical.commandframework.annotations.Command;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @author Despical
 * <p>
 * Created at 9.12.2025
 */
@ApiStatus.Internal
public class MessageHelper {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

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
    private static Function<String, Component> messageFormatter = MINI_MESSAGE::deserialize;

    public static void setMessageFormatter(@NotNull Function<String, Component> messageFormatter) {
        MessageHelper.messageFormatter = messageFormatter;
    }

    @NotNull
    public static String getCommandPath(@NotNull Command command, @NotNull CommandArguments arguments) {
        String[] parts = command.name().split("\\.");

        if (parts.length == 1) {
            return arguments.getLabel();
        }

        return arguments.getLabel() + " " + String.join(" ", Arrays.copyOfRange(parts, 1, parts.length));
    }

    @NotNull
    public static List<Command> getDirectSubcommands(@NotNull Command command) {
        return FrameworkContext.getInstance().getRegistry().getDirectChildCommands(command.name());
    }

    @NotNull
    public static String getSubcommandName(@NotNull Command command) {
        String[] parts = command.name().split("\\.");
        return parts[parts.length - 1];
    }

    public static Component formatMessage(@NotNull String string) {
        return messageFormatter.apply(string);
    }
}
