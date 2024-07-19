package me.despical.commandframework.utils;

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