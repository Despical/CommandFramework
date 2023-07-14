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

/**
 * @author Despical
 * <p>
 * Created at 30.05.2020
 *
 * This class is a part of Despical's Commons library.
 */
public class NumberUtils {

	private NumberUtils() {
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
	 * Convert a String to an double, returning zero if the conversion
	 * fails. If the string is null, zero is returned.
	 *
	 * @param string the string to convert, may be null
	 * @return the double represented by the string, or zero if conversion fails
	 */
	public static double getDouble(String string) {
		return getDouble(string, 0);
	}

	/**
	 * Convert a String to an double, returning a default value if the conversion
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
	 * Convert a String to an long, returning zero if the conversion
	 * fails. If the string is null, zero is returned.
	 *
	 * @param string the string to convert, may be null
	 * @return the long represented by the string, or zero if conversion fails
	 */
	public static long getLong(String string) {
		return getLong(string, 0);
	}

	/**
	 * Convert a String to an long, returning a default value if the conversion
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
	 * Convert a String to an float, returning zero if the conversion
	 * fails. If the string is null, zero is returned.
	 *
	 * @param string the string to convert, may be null
	 * @return the float represented by the string, or zero if conversion fails
	 */
	public static float getFloat(String string) {
		return getFloat(string, 0);
	}

	/**
	 * Convert a String to an float, returning a default value if the conversion
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
}