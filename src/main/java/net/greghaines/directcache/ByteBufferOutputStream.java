package net.greghaines.directcache;

import java.io.OutputStream;
import java.nio.ByteBuffer;

import java.util.ArrayList;
import java.util.List;

public class ByteBufferOutputStream extends OutputStream
{
	private final DirectBufferSource bufferSource;
	private final List<ByteBuffer> buffers = new ArrayList<ByteBuffer>(1);

	public ByteBufferOutputStream(final DirectBufferSource bufferSource)
	{
		if (bufferSource == null)
		{
			throw new IllegalArgumentException("bufferSource must not be null");
		}
		this.bufferSource = bufferSource;
		this.buffers.add(this.bufferSource.get());
	}

	/**
	 * <strong>WARNING: Once this method is called, this <code>ByteBufferOutputStream</code> 
	 * is no longer able to handle writes!</strong>
	 * 
	 * @return the flipped ByteBuffers that were written to
	 */
	public List<ByteBuffer> getBuffers()
	{
		for (final ByteBuffer buffer : this.buffers)
		{
			buffer.flip();
		}
		return this.buffers;
	}

	@Override
	public void write(final int b)
	{
		ByteBuffer buffer = this.buffers.get(this.buffers.size() - 1);
		if (buffer.remaining() < 1)
		{
			buffer = this.bufferSource.get();
			this.buffers.add(buffer);
		}
		buffer.put((byte) b);
	}

	@Override
	public void write(final byte[] b, final int off, final int len)
	{
		if (b == null)
		{
			throw new IllegalArgumentException("b must not be null");
		}
		if (off < 0)
		{
			throw new IndexOutOfBoundsException("off must not be negative: " + off);
		}
		if (len < 0)
		{
			throw new IndexOutOfBoundsException("len must not be negative: " + len);
		}
		if (off + len > b.length)
		{
			throw new IndexOutOfBoundsException("off + len must not be greater than b.length: " + 
				off + "," + len + "," + b.length);
		}
		ByteBuffer buffer = this.buffers.get(this.buffers.size() - 1);
		int remaining = buffer.remaining();
		int tmpOff = off;
		int tmpLen = len;
		while (tmpLen > remaining)
		{
			buffer.put(b, tmpOff, remaining);
			tmpLen -= remaining;
			tmpOff += remaining;
			buffer = this.bufferSource.get();
			this.buffers.add(buffer);
			remaining = buffer.remaining();
		}
		buffer.put(b, tmpOff, tmpLen);
	}
}
