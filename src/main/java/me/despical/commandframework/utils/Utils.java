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

import me.despical.commandframework.annotations.Command;
import org.jetbrains.annotations.ApiStatus;

import java.lang.annotation.Annotation;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class is a part of Despical's Commons library.
 * <p>
 * Created at 30.05.2020
 *
 * @author Despical
 */
@ApiStatus.Internal
public final class Utils {

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
		try {
			return Integer.parseInt(string);
		} catch (Exception ignored) {
			return 0;
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
		try {
			return Double.parseDouble(string);
		} catch (Exception ignored) {
			return 0d;
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
		try {
			return Long.parseLong(string);
		} catch (Exception ignored) {
			return 0L;
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
		try {
			return Float.parseFloat(string);
		} catch (Exception ignored) {
			return 0F;
		}
	}

	/**
	 * Returns a {@link Map.Entry} containing the given key and value.
	 *
	 * @param a   new key to be stored in this entry.
	 * @param b   new value to be stored in this entry.
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
	 * @param a   key to be stored in this map.
	 * @param b   value to be stored in this map.
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
	 * @param a   Array of given entries to be stored in this map.
	 * @param <K> key type to be stored in this map.
	 * @param <V> value type to be stored in this map.
	 * @return Returns a mutable map containing an arbitrary number of elements.
	 */
	@SafeVarargs
	public static <K, V> Map<K, V> mapOf(Map.Entry<K, V>... a) {
		return Arrays.stream(a).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (b, c) -> c));
	}

	public static void handleExceptions(Exception exception) {
		final Throwable cause = exception.getCause();

		if (cause == null) {
			exception.printStackTrace();
			return;
		}

		cause.printStackTrace();
	}

	public static Command createCommand(final Command command, final String commandName) {
		return new Command() {

			@Override
			public String name() {
				return commandName;
			}

			@Override
			public String fallbackPrefix() {
				return command.fallbackPrefix();
			}

			@Override
			public String permission() {
				return command.permission();
			}

			@Override
			public String[] aliases() {
				return new String[0];
			}

			@Override
			public String desc() {
				return command.desc();
			}

			@Override
			public String usage() {
				return command.usage();
			}

			@Override
			public int min() {
				return command.min();
			}

			@Override
			public int max() {
				return command.max();
			}

			@Override
			public boolean onlyOp() {
				return command.onlyOp();
			}

			@Override
			public boolean async() {
				return command.async();
			}

			@Override
			public SenderType senderType() {
				return command.senderType();
			}

			@Override
			public Class<? extends Annotation> annotationType() {
				return command.annotationType();
			}
		};
	}
}
