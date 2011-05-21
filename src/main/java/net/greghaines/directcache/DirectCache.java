package net.greghaines.directcache;

import java.io.Serializable;

public interface DirectCache
{
	Serializable put(Object key, Serializable value);
	
	Serializable putIfAbsent(Object key, Serializable value);
	
	Serializable get(Object key);
	
	Serializable remove(Object key);
	
	void clear();
}
