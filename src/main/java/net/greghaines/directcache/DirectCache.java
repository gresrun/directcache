/*
 * Copyright 2011 Greg Haines
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
