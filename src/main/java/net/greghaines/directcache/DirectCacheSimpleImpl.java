package net.greghaines.directcache;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class DirectCacheSimpleImpl implements DirectCache
{
	private final DirectBufferSource bufferSource;
	private final ConcurrentMap<Object,List<ByteBuffer>> bufferMap = 
		new ConcurrentHashMap<Object,List<ByteBuffer>>();
	
	public DirectCacheSimpleImpl(final DirectBufferSource bufferSource)
	{
		if (bufferSource == null)
		{
			throw new IllegalArgumentException("bufferSource must not be null");
		}
		this.bufferSource = bufferSource;
	}

	public Serializable get(final Object key)
	{
		Serializable value = null;
		final List<ByteBuffer> bufList = this.bufferMap.get(key);
		if (bufList != null && !bufList.isEmpty())
		{
			value = deserialize(bufList);
		}
		return value;
	}

	public Serializable put(final Object key, final Serializable value)
	{
		if (key == null)
		{
			throw new IllegalArgumentException("key must not be null");
		}
		Serializable oldValue = null;
		if (value != null)
		{
			final List<ByteBuffer> oldBufList = this.bufferMap.put(key, serialize(value));
			if (oldBufList != null && !oldBufList.isEmpty())
			{
				oldValue = deserialize(oldBufList);
				for (final ByteBuffer buf : oldBufList)
				{
					this.bufferSource.offer(buf);
				}
			}
		}
		return oldValue;
	}
	
	public Serializable putIfAbsent(final Object key, final Serializable value)
	{
		if (key == null)
		{
			throw new IllegalArgumentException("key must not be null");
		}
		Serializable oldValue = null;
		if (value != null)
		{
			final List<ByteBuffer> oldBufList = this.bufferMap.putIfAbsent(key, serialize(value));
			if (oldBufList != null && !oldBufList.isEmpty())
			{
				oldValue = deserialize(oldBufList);
				for (final ByteBuffer buf : oldBufList)
				{
					this.bufferSource.offer(buf);
				}
			}
		}
		return oldValue;
	}

	public Serializable remove(final Object key)
	{
		Serializable value = null;
		final List<ByteBuffer> bufList = this.bufferMap.remove(key);
		if (bufList != null && !bufList.isEmpty())
		{
			value = deserialize(bufList);
			for (final ByteBuffer buf : bufList)
			{
				this.bufferSource.offer(buf);
			}
		}
		return value;
	}

	public void clear()
	{
		this.bufferMap.clear();
	}
	
	private List<ByteBuffer> serialize(final Serializable value)
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
		return bbos.getBufferList();
	}
	
	private static Serializable deserialize(final List<ByteBuffer> bufList)
	{
		Serializable value = null;
		ObjectInputStream ois = null;
		try
		{
			synchronized (bufList)
			{
				final ByteBufferInputStream bbis = new ByteBufferInputStream(bufList);
				try
				{
					ois = new ObjectInputStream(bbis);
					value = (Serializable) ois.readObject();
				}
				finally
				{
					bbis.rewind();
				}
			}
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
			if (ois != null)
			{
				try { ois.close(); } catch (Exception e){}
			}
		}
		return value;
	}
}
