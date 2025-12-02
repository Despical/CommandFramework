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

package me.despical.commandframework;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * A utility class designed to simplify the process of handling tab completion for commands.
 * <p>
 * This helper provides methods to easily filter lists of strings (like player names or
 * command sub-arguments) based on the user's current input.
 * <p>
 *  * @author Despical
 *  * <p>
 *  * Created at 2.12.2025
 */
public final class CompleterHelper {

    private final CommandArguments arguments;

    @ApiStatus.Internal
    public CompleterHelper(CommandArguments arguments) {
        this.arguments = arguments;
    }

    /**
     * Retrieves a list of names for all players currently online on the server.
     * <p>
     * This is commonly used for tab completion where a target player name is required.
     *
     * @return An unmodifiable list containing the names of online players.
     * Returns an empty list if no players are online. Never returns null.
     */
    @NotNull
    @Contract(pure = true)
    public List<String> playerNames() {
        return Bukkit.getServer().getOnlinePlayers().stream()
            .map(Player::getName)
            .toList();
    }

    /**
     * Filters a collection of strings based on the argument at the specified index.
     * <p>
     * This is a convenience overload that creates a new {@link ArrayList} to store results.
     *
     * @param <T>       The type of the collection (inferred).
     * @param index     The index of the command argument to use as the filter token.
     * @param originals The source collection of strings to filter (e.g., all possible sub-commands).
     * @return A new {@link ArrayList} containing only the strings from {@code originals}
     * that start with the argument at the given {@code index}.
     * @see #copyMatches(int, Iterable, Collection)
     */
    @NotNull
    @Contract(pure = true)
    @SuppressWarnings("unchecked")
    public <T extends Collection<? super String>> T copyMatches(
        final int index,
        @NotNull final Iterable<String> originals
    ) {
        return copyMatches(index, originals, (T) new ArrayList<>());
    }

    /**
     * Filters a collection of strings based on the argument at the specified index
     * and adds matches to the provided collection.
     * <p>
     * If the argument at the specified index is null or empty, the original collection
     * is returned as is (or added to).
     *
     * @param <T>        The type of the collection to return.
     * @param index      The index of the command argument to use as the filter token.
     * @param originals  The source iterable of strings to filter.
     * @param collection The target collection where matches will be added.
     * @return The {@code collection} provided, now containing the filtered matches.
     * @throws IllegalArgumentException if any parameter is null.
     */
    @NotNull
    @Contract(pure = true)
    public <T extends Collection<? super String>> T copyMatches(
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
     * Filters a collection of strings based on a specific token string.
     * <p>
     * This is a convenience overload that creates a new {@link ArrayList} to store results.
     *
     * @param <T>       The type of the collection (inferred).
     * @param token     The string token to search for (starts-with match).
     * @param originals The source collection of strings to filter.
     * @return A new {@link ArrayList} containing only the strings that match the token.
     */
    @NotNull
    @Contract(pure = true)
    @SuppressWarnings("unchecked")
    public <T extends Collection<? super String>> T copyMatches(
        @NotNull final String token,
        @NotNull final Iterable<String> originals
    ) {
        return copyMatches(token, originals, (T) new ArrayList<>());
    }

    /**
     * Filters a collection of strings based on a specific token string and adds
     * matches to the provided collection.
     * <p>
     * This delegates directly to Bukkit's {@link StringUtil#copyPartialMatches(String, Iterable, Collection)}.
     *
     * @param <T>        The type of the collection to return.
     * @param token      The string token to search for (starts-with match).
     * @param originals  The source iterable of strings to filter.
     * @param collection The target collection where matches will be added.
     * @return The {@code collection} provided, populated with matches.
     * @throws IllegalArgumentException if originals contains a null element.
     */
    @NotNull
    @Contract(pure = true)
    public <T extends Collection<? super String>> T copyMatches(
        @NotNull final String token,
        @NotNull final Iterable<String> originals,
        @NotNull final T collection
    ) {
        return StringUtil.copyPartialMatches(token, originals, collection);
    }
}
