package net.greghaines.directcache;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

public class TestDirectCacheSimpleImpl
{
	private static final DirectBufferSource bufferSource = new DirectBufferSourceQueueImpl(5, 8192);
	private static final DirectCache cache = new DirectCacheSimpleImpl(bufferSource);
	
	@After
	public void clearCache()
	{
		cache.clear();
	}
	
	@Test
	public void simplePutGet()
	{
		final Double i = 3.2;
		final Double nothing = (Double) cache.put("foo", i);
		Assert.assertNull(nothing);
		final Double j = (Double) cache.get("foo");
		Assert.assertEquals(i, j);
		final Double z = 4.1;
		final Double iPrime = (Double) cache.put("foo", z);
		Assert.assertEquals(i, iPrime);
		final Double k = (Double) cache.get("foo");
		Assert.assertEquals(z, k);
	}
	
	@Test
	public void simplePutIfAbsentGet()
	{
		final Double i = 3.2;
		final Double nothing = (Double) cache.putIfAbsent("foo", i);
		Assert.assertNull(nothing);
		final Double j = (Double) cache.get("foo");
		Assert.assertEquals(i, j);
		final Double z = 4.1;
		final Double iPrime = (Double) cache.putIfAbsent("foo", z);
		Assert.assertEquals(i, iPrime);
		final Double k = (Double) cache.get("foo");
		Assert.assertEquals(i, k);
	}
	
	@Test
	public void simplePutRemoveGet()
	{
		final Double i = 3.2;
		final Double nothing = (Double) cache.put("foo", i);
		Assert.assertNull(nothing);
		final Double j = (Double) cache.remove("foo");
		Assert.assertEquals(i, j);
		final Double z = (Double) cache.get("foo");
		Assert.assertNull(z);
	}
}
