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
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
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

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY_SECTION_SERIALIZER = LegacyComponentSerializer.legacySection();

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

    /**
     * For instance, can be used to translate Minecraft color and Hex color codes.
     *
     * @param colorFormatter the function that will be applied to the strings to colorize
     */
    public static void setColorFormatter(@NotNull Function<String, String> colorFormatter) {
        MessageHelper.messageFormatter = text -> LEGACY_SECTION_SERIALIZER.deserialize(colorFormatter.apply(text));
    }

    public static void setMessageFormatter(@NotNull Function<String, Component> messageFormatter) {
        MessageHelper.messageFormatter = messageFormatter;
    }

    public static Component formatMessage(@NotNull String string) {
        return messageFormatter.apply(string);
    }

    public static String applyColorFormatter(@NotNull String string) {
        return LEGACY_SECTION_SERIALIZER.serialize(formatMessage(string));
    }
}
