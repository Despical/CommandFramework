package me.despical.commandframework.utils;

import org.jetbrains.annotations.ApiStatus;

import java.util.Map;

/**
 * @author Despical
 * <p>
 * Created at 6.02.2024
 */
@ApiStatus.Internal
public interface SelfExpiringMap<K, V> extends Map<K, V> {

	V put(K k, V v, long lifeTimeMs);
}