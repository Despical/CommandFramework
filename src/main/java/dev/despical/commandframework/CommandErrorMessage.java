/*
 * Command Framework - Annotation based command framework
 * Copyright (C) 2026  Berke Akcen
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

package dev.despical.commandframework;

import dev.despical.commandframework.annotations.Command;
import dev.despical.commandframework.internal.MessageHelper;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Built-in framework error and validation messages.
 *
 * @author Despical
 * @since 1.6.4
 */
public enum CommandErrorMessage {

    SHORT_ARG_SIZE("<red>Required argument length is less than needed!", true),
    LONG_ARG_SIZE("<red>Required argument length greater than needed!", true),
    ONLY_BY_PLAYERS("<red>This command is only executable by players!"),
    ONLY_BY_CONSOLE("<red>This command is only executable by console!"),
    NO_PERMISSION("<red>You don't have enough permission to execute this command!"),
    MUST_HAVE_OP("<red>You must have OP to execute this command!"),
    WAIT_BEFORE_USING_AGAIN("<red>You have to wait before using this command again!"),
    UNKNOWN_SUBCOMMAND((command, arguments) -> {
        String commandPath = MessageHelper.getCommandPath(command, arguments);
        List<String> visibleSubcommands = MessageHelper.getDirectSubcommands(command).stream()
            .filter(subcommand -> !subcommand.usage().isEmpty())
            .map(MessageHelper::getSubcommandName)
            .toList();
        String subcommands = String.join(" | ", visibleSubcommands);

        if (subcommands.isEmpty()) {
            arguments.sendMessage("<red>This command cannot be used directly.");
            return true;
        }

        arguments.sendMessage("<red>This command cannot be used directly. Try /{0} <{1}>", commandPath, subcommands);
        return true;
    });

    private final BiFunction<Command, CommandArguments, Boolean> defaultHandler;
    private BiFunction<Command, CommandArguments, Boolean> handler;

    CommandErrorMessage(String message) {
        this(message, false);
    }

    CommandErrorMessage(String message, boolean sendUsage) {
        this((command, arguments) -> {
            if (sendUsage && !MessageHelper.SEND_USAGE.apply(command, arguments)) {
                return true;
            }

            arguments.sendMessage(message);
            return true;
        });
    }

    CommandErrorMessage(BiFunction<Command, CommandArguments, Boolean> handler) {
        this.defaultHandler = handler;
        this.handler = handler;
    }

    public static void setMessageFormatter(@NotNull Function<String, Component> messageFormatter) {
        MessageHelper.setMessageFormatter(messageFormatter);
    }

    public void setHandler(@NotNull BiFunction<Command, CommandArguments, Boolean> handler) {
        this.handler = handler;
    }

    public void resetHandler() {
        this.handler = this.defaultHandler;
    }

    @ApiStatus.Internal
    boolean sendMessage(Command command, CommandArguments arguments) {
        return this.handler.apply(command, arguments);
    }
}
