package net.greghaines.directcache;

import java.nio.ByteBuffer;

public interface DirectBufferSource
{
	void offer(ByteBuffer buffer);
	
	ByteBuffer get();
}
