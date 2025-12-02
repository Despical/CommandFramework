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

package me.despical.commandframework.utils;

import me.despical.commandframework.CommandArguments;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

/**
 * @author Despical
 * <p>
 * Created at 2.12.2025
 */
public final class CompleterHelper {

    private final CommandArguments arguments;

    public CompleterHelper(CommandArguments arguments) {
        this.arguments = arguments;
    }

    /**
     * Retrieves the names of all currently online players on the server.
     * <p>
     * This method is useful for tab completion suggestions where a list of
     * player names is required.
     *
     * @return An unmodifiable list containing the names of online players.
     * Returns an empty list if no players are online. Never returns null.
     */
    @NotNull
    @Contract(pure = true)
    public List<String> playerNames() {
        return Bukkit.getServer().getOnlinePlayers().stream().map(Player::getName).toList();
    }

    /**
     * Copies all elements from the iterable collection of originals to the
     * collection provided.
     *
     * @param <T> the collection of strings
     * @param index Argument index to search for
     * @param originals An iterable collection of strings to filter.
     * @param collection The collection to add matches to
     * @return the collection provided that would have the elements copied
     *     into
     * @throws UnsupportedOperationException if the collection is immutable
     *     and originals contains a string which starts with the specified
     *     search string.
     * @throws IllegalArgumentException if any parameter is null
     * @throws IllegalArgumentException if originals contains a null element.
     *     <b>Note: the collection may be modified before this is thrown</b>
     */
    @NotNull
    @Contract(pure = true)
    public <T extends Collection<String>> T copyMatches(
        final int index,
        @NotNull final Iterable<String> originals,
        @NotNull final T collection
    ) {
        String argument = arguments.getArgument(index);

        if (argument == null) {
            return collection;
        }

        return StringUtil.copyPartialMatches(argument, originals, collection);
    }

    /**
     * Copies all elements from the iterable collection of originals to the
     * collection provided.
     *
     * @param <T> the collection of strings
     * @param token String to search for
     * @param originals An iterable collection of strings to filter.
     * @param collection The collection to add matches to
     * @return the collection provided that would have the elements copied
     *     into
     * @throws UnsupportedOperationException if the collection is immutable
     *     and originals contains a string which starts with the specified
     *     search string.
     * @throws IllegalArgumentException if any parameter is null
     * @throws IllegalArgumentException if originals contains a null element.
     *     <b>Note: the collection may be modified before this is thrown</b>
     */
    @NotNull
    @Contract(pure = true)
    public <T extends Collection<String>> T copyMatches(
        @NotNull final String token,
        @NotNull final Iterable<String> originals,
        @NotNull final T collection
    ) {
        return StringUtil.copyPartialMatches(token, originals, collection);
    }
}
