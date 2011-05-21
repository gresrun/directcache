package net.greghaines.directcache;

import java.nio.ByteBuffer;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

public class DirectBufferSourceQueueImpl implements DirectBufferSource
{
	private final BlockingDeque<ByteBuffer> bufferQueue;
	private final int bufferCapacity;

	public DirectBufferSourceQueueImpl(final int queueCapacity, final int bufferCapacity)
	{
		if (queueCapacity <= 0)
		{
			throw new IllegalArgumentException("queueCapacity must be greater than zero");
		}
		if (bufferCapacity <= 0)
		{
			throw new IllegalArgumentException("bufferCapacity must be greater than zero");
		}
		this.bufferQueue = new LinkedBlockingDeque<ByteBuffer>(queueCapacity);
		this.bufferCapacity = bufferCapacity;
	}

	public void offer(final ByteBuffer buffer)
	{
		if (buffer != null && buffer.isDirect() && !buffer.isReadOnly())
		{ // Only want writable, direct buffers
			this.bufferQueue.offer(buffer);
		}
	}

	public ByteBuffer poll()
	{
		final ByteBuffer buffer = this.bufferQueue.poll();
		return (buffer == null) ? create() : buffer;
	}

	public void clear()
	{
		this.bufferQueue.clear();
	}

	protected ByteBuffer create()
	{
		return ByteBuffer.allocateDirect(this.bufferCapacity);
	}
}
