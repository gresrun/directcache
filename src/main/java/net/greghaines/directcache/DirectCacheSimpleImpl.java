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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class DirectCacheSimpleImpl<K,V extends Serializable> implements DirectCache<K,V>
{
	private final DirectBufferSource bufferSource;
	private final ConcurrentMap<K,List<ByteBuffer>> bufferMap = 
		new ConcurrentHashMap<K,List<ByteBuffer>>();

	public DirectCacheSimpleImpl(final DirectBufferSource bufferSource)
	{
		if (bufferSource == null)
		{
			throw new IllegalArgumentException("bufferSource must not be null");
		}
		this.bufferSource = bufferSource;
	}

	public void clear()
	{
		final List<List<ByteBuffer>> allBuffers = new ArrayList<List<ByteBuffer>>(this.bufferMap.values());
		this.bufferMap.clear();
		for (final List<ByteBuffer> oldBufList : allBuffers)
		{
			if (oldBufList != null)
			{
				synchronized (oldBufList)
				{
					for (final ByteBuffer buf : oldBufList)
					{
						this.bufferSource.offer(buf);
					}
				}
			}
		}
	}

	public boolean isEmpty()
	{
		return this.bufferMap.isEmpty();
	}

	public int size()
	{
		return this.bufferMap.size();
	}

	public boolean containsKey(final Object key)
	{
		return this.bufferMap.containsKey(key);
	}

	public V get(final Object key)
	{
		V value = null;
		final List<ByteBuffer> bufList = this.bufferMap.get(key);
		if (bufList != null)
		{
			synchronized (bufList)
			{
				value = deserialize(bufList);
			}
		}
		return value;
	}

	public V put(final K key, final V value)
	{
		if (key == null)
		{
			throw new IllegalArgumentException("key must not be null");
		}
		V oldValue = null;
		if (value != null)
		{
			final List<ByteBuffer> oldBufList = this.bufferMap.put(key, serialize(value));
			if (oldBufList != null)
			{
				synchronized (oldBufList)
				{
					oldValue = deserialize(oldBufList);
					for (final ByteBuffer buf : oldBufList)
					{
						this.bufferSource.offer(buf);
					}
				}
			}
		}
		return oldValue;
	}

	public V putIfAbsent(final K key, final V value)
	{
		if (key == null)
		{
			throw new IllegalArgumentException("key must not be null");
		}
		V oldValue = null;
		if (value != null)
		{
			final List<ByteBuffer> newBufList = serialize(value);
			final List<ByteBuffer> oldBufList = this.bufferMap.putIfAbsent(key, newBufList);
			if (oldBufList != null)
			{
				synchronized (oldBufList)
				{ // Return the existing value
					oldValue = deserialize(oldBufList);
				}
				synchronized (newBufList)
				{ // Recycle the buffers used to create the value since they we're used
					for (final ByteBuffer buf : newBufList)
					{
						this.bufferSource.offer(buf);
					}
				}
			}
		}
		return oldValue;
	}

	public void putAll(final Map<? extends K,? extends V> map)
	{
		if (map == null)
		{
			throw new IllegalArgumentException("map must not be null");
		}
		for (final Entry<? extends K,? extends V> entry : map.entrySet())
		{
			put(entry.getKey(), entry.getValue());
		}
	}

	public V remove(final Object key)
	{
		V value = null;
		final List<ByteBuffer> bufList = this.bufferMap.remove(key);
		if (bufList != null)
		{
			synchronized (bufList)
			{
				value = deserialize(bufList);
				for (final ByteBuffer buf : bufList)
				{
					this.bufferSource.offer(buf);
				}
			}
		}
		return value;
	}

	public Set<K> keySet()
	{
		return Collections.unmodifiableSet(this.bufferMap.keySet());
	}

	private List<ByteBuffer> serialize(final V value)
	{
		final ByteBufferOutputStream bbos = new ByteBufferOutputStream(this.bufferSource);
		ObjectOutputStream oos = null;
		try
		{
			oos = new ObjectOutputStream(bbos);
			oos.writeObject(value);
		}
		catch (IOException ioe)
		{
			throw new SerializationException(ioe);
		}
		finally
		{
			if (oos != null)
			{
				try { oos.close(); } catch (Exception e){}
			}
		}
		return bbos.getBuffers();
	}

	@SuppressWarnings("unchecked")
	private V deserialize(final List<ByteBuffer> bufList)
	{
		V value = null;
		ObjectInputStream ois = null;
		final ByteBufferInputStream bbis = new ByteBufferInputStream(bufList);
		try
		{
			ois = new ObjectInputStream(bbis);
			value = (V) ois.readObject();
		}
		catch (IOException ioe)
		{
			throw new DeserializationException(ioe);
		}
		catch (ClassNotFoundException cnfe)
		{
			throw new DeserializationException(cnfe);
		}
		finally
		{
			bbis.rewind();
			if (ois != null)
			{
				try { ois.close(); } catch (Exception e){}
			}
		}
		return value;
	}
}
