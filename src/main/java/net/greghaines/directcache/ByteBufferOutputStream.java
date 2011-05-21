package net.greghaines.directcache;

import java.io.OutputStream;
import java.nio.ByteBuffer;

import java.util.ArrayList;
import java.util.List;

public class ByteBufferOutputStream extends OutputStream
{
	private final DirectBufferSource bufferSource;
	private List<ByteBuffer> bufList;

	public ByteBufferOutputStream(final DirectBufferSource bufferSource)
	{
		if (bufferSource == null)
		{
			throw new IllegalArgumentException("bufferSource must not be null");
		}
		this.bufferSource = bufferSource;
		reset();
	}

	public List<ByteBuffer> getBufferList()
	{
		final List<ByteBuffer> tmpBufList = this.bufList;
		reset();
		for (final ByteBuffer buf : tmpBufList)
		{
			buf.flip();
		}
		return tmpBufList;
	}

	public void reset()
	{
		this.bufList = new ArrayList<ByteBuffer>(1);
		this.bufList.add(this.bufferSource.get());
	}

	@Override
	public void write(final int b)
	{
		ByteBuffer buffer = this.bufList.get(this.bufList.size() - 1);
		if (buffer.remaining() < 1)
		{
			buffer = this.bufferSource.get();
			this.bufList.add(buffer);
		}
		buffer.put((byte) b);
	}

	@Override
	public void write(final byte[] b, final int off, final int len)
	{
		ByteBuffer buffer = this.bufList.get(this.bufList.size() - 1);
		int remaining = buffer.remaining();
		int tmpOff = off;
		int tmpLen = len;
		while (tmpLen > remaining)
		{
			buffer.put(b, tmpOff, remaining);
			tmpLen -= remaining;
			tmpOff += remaining;
			buffer = this.bufferSource.get();
			this.bufList.add(buffer);
			remaining = buffer.remaining();
		}
		buffer.put(b, tmpOff, tmpLen);
	}
}
