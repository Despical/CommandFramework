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

package dev.despical.commandframework.utils;

import org.jetbrains.annotations.ApiStatus;

import java.util.Map;

/**
 * An implementation of Map interface with expiring
 * keys feature.
 *
 * @author Despical
 * <p>
 * Created at 6.02.2024
 */
@ApiStatus.Internal
public interface SelfExpiringMap<K, V> extends Map<K, V> {

	/**
	 * Associates the specified value with the specified key in this map.
	 * If the map previously contained a mapping for the key, the old
	 * value is replaced.
	 *
	 * @param key        key with which the specified value is to be associated
	 * @param value      value to be associated with the specified key
	 * @param lifeTimeMs how many milliseconds should the key live
	 * @return the previous value associated with {@code key}, or
	 * {@code null} if there was no mapping for {@code key}.
	 * (A {@code null} return can also indicate that the map
	 * previously associated {@code null} with {@code key}.)
	 */
	V put(K key, V value, long lifeTimeMs);
}
