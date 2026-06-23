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

package dev.despical.commandframework.utils;

import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Shared validation rules for command and completer names.
 *
 * @author Despical
 * @since 1.6.4
 */
@ApiStatus.Internal
public final class CommandNameValidator {

    @Language("RegExp")
    public static final String NAME_REGEX = "^[a-zA-Z0-9_]+(?:\\.[a-zA-Z0-9_]+)*$";

    private CommandNameValidator() {
    }

    @NotNull
    public static String normalizeName(@NotNull String value, @NotNull String fieldName) {
        String commandName = Objects.requireNonNull(value, fieldName).trim();

        if (commandName.isEmpty()) {
            throw new IllegalArgumentException("Command " + fieldName + " cannot be empty.");
        }

        if (commandName.startsWith(".") || commandName.endsWith(".") || commandName.contains("..")) {
            throw new IllegalArgumentException("Command " + fieldName + " contains an empty path segment.");
        }

        if (!commandName.matches(NAME_REGEX)) {
            throw new IllegalArgumentException(
                "Command " + fieldName + " contains unsupported characters. Only letters, numbers, underscores, and dots are allowed."
            );
        }

        return commandName;
    }

    @NotNull
    public static String[] normalizeAliases(@NotNull String[] aliases, @NotNull String commandName) {
        Objects.requireNonNull(aliases, "aliases");

        Set<String> normalized = new LinkedHashSet<>();

        for (String alias : aliases) {
            String normalizedAlias = normalizeName(alias, "alias");

            if (normalizedAlias.equalsIgnoreCase(commandName)) {
                throw new IllegalArgumentException("Command alias cannot be equal to the command name.");
            }

            normalized.add(normalizedAlias);
        }

        return normalized.toArray(String[]::new);
    }
}
