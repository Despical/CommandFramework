package me.despical.commandframework.utils;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Despical
 * <p>
 * Created at 6.02.2024
 */
@ApiStatus.Internal
public class SelfExpiringHashMap<K, V> implements SelfExpiringMap<K, V> {

	private final Map<K, Map.Entry<V, ExpiringData>> map;

	public SelfExpiringHashMap() {
		this.map = new HashMap<>();
	}

	@Override
	public V put(K key, V value, long lifeTimeMs) {
		this.map.put(key, Utils.mapEntry(value, new ExpiringData(System.currentTimeMillis(), lifeTimeMs)));
		return value;
	}

	@Override
	public V put(K key, V value) {
		return this.put(key, value, Long.MAX_VALUE);
	}

	@Override
	public boolean containsKey(Object key) {
		final Map.Entry<V, ExpiringData> entry = this.map.get(key);

		if (entry == null)
			return false;

		if (System.currentTimeMillis() - entry.getValue().start < entry.getValue().lifeTimeMs)
			return this.map.containsKey(key);

		return false;
	}

	@Override
	public V get(Object key) {
		final Map.Entry<V, ExpiringData> entry = this.map.get(key);

		if (entry == null)
			return null;

		if (System.currentTimeMillis() - entry.getValue().start < entry.getValue().lifeTimeMs)
			return entry.getKey();

		return null;
	}

	@Override
	public V remove(Object key) {
		final Map.Entry<V, ExpiringData> entry = this.map.remove(key);
		return entry.getKey();
	}

	// No need to fill these methods for this framework, at least for now.
	@Override
	public int size() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isEmpty() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean containsValue(Object value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void putAll(@NotNull Map<? extends K, ? extends V> map) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	@NotNull
	@Override
	public Set<K> keySet() {
		throw new UnsupportedOperationException();
	}

	@NotNull
	@Override
	public Collection<V> values() {
		throw new UnsupportedOperationException();
	}

	@NotNull
	@Override
	public Set<Entry<K, V>> entrySet() {
		throw new UnsupportedOperationException();
	}

	private static class ExpiringData {

		private final long start, lifeTimeMs;

		ExpiringData(long start, long end) {
			this.start = start;
			this.lifeTimeMs = end;
		}
	}
}