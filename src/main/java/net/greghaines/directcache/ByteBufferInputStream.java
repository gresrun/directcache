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

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;

public class ByteBufferInputStream extends InputStream
{
	private final List<ByteBuffer> buffers;
	private int current = 0;

	public ByteBufferInputStream(final List<ByteBuffer> buffers)
	{
		if (buffers == null)
		{
			throw new IllegalArgumentException("buffers must not be null");
		}
		this.buffers = buffers;
	}

	public void rewind()
	{
		this.current = 0;
		for (final ByteBuffer buffer : this.buffers)
		{
			buffer.rewind();
		}
	}

	@Override
	public int read()
	throws IOException
	{
		return (int)(getNextBuffer().get() & 0xFF);
	}

	@Override
	public int read(final byte[] b, final int off, final int len)
	throws IOException
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
		if (len == 0)
		{
			return 0;
		}
		final ByteBuffer buffer = getNextBuffer();
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

	private ByteBuffer getNextBuffer()
	throws EOFException
	{
		while (this.current < this.buffers.size())
		{
			final ByteBuffer buffer = this.buffers.get(this.current);
			if (buffer.hasRemaining())
			{
				return buffer;
			}
			this.current++;
		}
		throw new EOFException();
	}
}
