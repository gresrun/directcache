package net.greghaines.directcache;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;

public class ByteBufferInputStream extends InputStream
{
	private final List<ByteBuffer> bufList;
	private int current = 0;

	public ByteBufferInputStream(final List<ByteBuffer> bufList)
	{
		if (bufList == null)
		{
			throw new IllegalArgumentException("bufList must not be null");
		}
		this.bufList = bufList;
	}
	
	public void rewind()
	{
		this.current = 0;
		for (final ByteBuffer buf : this.bufList)
		{
			buf.rewind();
		}
	}

	@Override
	public int read()
	throws IOException
	{
		return (int)(getBuffer().get() & 0xFF);
	}

	@Override
	public int read(final byte[] b, final int off, final int len)
	throws IOException
	{
		if (len == 0)
		{
			return 0;
		}
		final ByteBuffer buffer = getBuffer();
		final int remaining = buffer.remaining();
		if (len > remaining)
		{
			buffer.get(b, off, remaining);
			return remaining;
		}
		else
		{
			buffer.get(b, off, len);
			return len;
		}
	}
	
	private ByteBuffer getBuffer()
	throws EOFException
	{
		while (this.current < this.bufList.size())
		{
			final ByteBuffer buffer = this.bufList.get(this.current);
			if (buffer.hasRemaining())
			{
				return buffer;
			}
			this.current++;
		}
		throw new EOFException();
	}
}
