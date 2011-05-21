package net.greghaines.directcache;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
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
		this.bufferMap.clear();
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
			final List<ByteBuffer> oldBufList = this.bufferMap.putIfAbsent(key, serialize(value));
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
			throw new SerializationException(ioe);
		}
		catch (ClassNotFoundException cnfe)
		{
			throw new SerializationException(cnfe);
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
