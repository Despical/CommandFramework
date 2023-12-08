/*
 * Commons - Box of the common utilities.
 * Copyright (C) 2023 Despical
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.despical.commandframework.utils;

import org.jetbrains.annotations.ApiStatus;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Despical
 * <p>
 * Created at 30.05.2020
 *
 * This class is a part of Despical's Commons library.
 */
@ApiStatus.Internal
public class Utils {

	private Utils() {
	}

	/**
	 * Convert a String to an int, returning zero if the conversion
	 * fails. If the string is null, zero is returned.
	 *
	 * @param string the string to convert, may be null
	 * @return the int represented by the string, or zero if conversion fails
	 */
	public static int getInt(String string) {
		return getInt(string, 0);
	}

	/**
	 * Convert a String to an int, returning a default value if the conversion
	 * fails. If the string is null, the default value is returned.
	 *
	 * @param string the string to convert, may be null
	 * @param def the default value
	 * @return the int represented by the string, or the default if conversion fails
	 */
	public static int getInt(String string, int def) {
		if (string == null) return def;

		try {
			return Integer.parseInt(string);
		} catch (NumberFormatException ignored) {
			return def;
		}
	}

	/**
	 * Convert a String to a double, returning zero if the conversion
	 * fails. If the string is null, zero is returned.
	 *
	 * @param string the string to convert, may be null
	 * @return the double represented by the string, or zero if conversion fails
	 */
	public static double getDouble(String string) {
		return getDouble(string, 0);
	}

	/**
	 * Convert a String to a double, returning a default value if the conversion
	 * fails. If the string is null, the default value is returned.
	 *
	 * @param string the string to convert, may be null
	 * @param def the default value
	 * @return the double represented by the string, or the default if conversion fails
	 */
	public static double getDouble(String string, double def) {
		if (string == null) return def;

		try {
			return Double.parseDouble(string);
		} catch (NumberFormatException ignored) {
			return def;
		}
	}

	/**
	 * Convert a String to a long, returning zero if the conversion
	 * fails. If the string is null, zero is returned.
	 *
	 * @param string the string to convert, may be null
	 * @return the long represented by the string, or zero if conversion fails
	 */
	public static long getLong(String string) {
		return getLong(string, 0);
	}

	/**
	 * Convert a String to a long, returning a default value if the conversion
	 * fails. If the string is null, the default value is returned.
	 *
	 * @param string the string to convert, may be null
	 * @param def the default value
	 * @return the long represented by the string, or the default if conversion fails
	 */
	public static long getLong(String string, long def) {
		if (string == null) return def;

		try {
			return Long.parseLong(string);
		} catch (NumberFormatException ignored) {
			return def;
		}
	}

	/**
	 * Convert a String to a float, returning zero if the conversion
	 * fails. If the string is null, zero is returned.
	 *
	 * @param string the string to convert, may be null
	 * @return the float represented by the string, or zero if conversion fails
	 */
	public static float getFloat(String string) {
		return getFloat(string, 0);
	}

	/**
	 * Convert a String to a float, returning a default value if the conversion
	 * fails. If the string is null, the default value is returned.
	 *
	 * @param string the string to convert, may be null
	 * @param def the default value
	 * @return the float represented by the string, or the default if conversion fails
	 */
	public static float getFloat(String string, float def) {
		if (string == null) return def;

		try {
			return Float.parseFloat(string);
		} catch (NumberFormatException ignored) {
			return def;
		}
	}

	/**
	 * Returns a {@link Map.Entry} containing the given key and value.
	 *
	 * @param a new key to be stored in this entry.
	 * @param b new value to be stored in this entry.
	 * @param <K> new key type to be stored in this entry.
	 * @param <V> new value type to be stored in this entry.
	 * @return new {@link Map.Entry} containing the given key and value.
	 */
	public static <K, V> Map.Entry<K, V> mapEntry(K a, V b) {
		return new AbstractMap.SimpleEntry<>(a, b);
	}

	/**
	 * Returns an mutable map containing one element.
	 *
	 * @param a key to be stored in this map.
	 * @param b value to be stored in this map.
	 * @param <K> key type to be stored in this map.
	 * @param <V> value type to be stored in this map.
	 * @return Returns an mutable map containing one element.
	 */
	public static <K, V> Map<K, V> mapOf(K a, V b) {
		return mapOf(mapEntry(a, b));
	}

	/**
	 * Returns a mutable map containing an arbitrary number of elements.
	 *
	 * @param a Array of given entries to be stored in this map.
	 * @param <K> key type to be stored in this map.
	 * @param <V> value type to be stored in this map.
	 * @return Returns a mutable map containing an arbitrary number of elements.
	 */
	@SafeVarargs
	public static <K, V> Map<K, V> mapOf(Map.Entry<K, V>... a) {
		return Arrays.stream(a).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (b, c) -> c));
	}
}