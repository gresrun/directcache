package net.greghaines.directcache;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

public interface DirectCache<K,V extends Serializable>
{
	V put(K key, V value);
	
	V putIfAbsent(K key, V value);
	
	void putAll(Map<? extends K,? extends V> map);
	
	V get(Object key);
	
	V remove(Object key);
	
	void clear();
	
	boolean isEmpty();
	
	int size();
	
	boolean containsKey(Object key);
	
	Set<K> keySet();
}
