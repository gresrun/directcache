package net.greghaines.directcache;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

public class TestDirectCacheSimpleImpl
{
	private static final DirectBufferSource bufferSource = 
		new DirectBufferSourceQueueImpl(5, 8192);
	private static final DirectCache<String,Double> cache = 
		new DirectCacheSimpleImpl<String,Double>(bufferSource);
	
	@After
	public void clearCache()
	{
		cache.clear();
	}
	
	@Test
	public void simplePutGet()
	{
		final Double i = 3.2;
		final Double nothing = cache.put("foo", i);
		Assert.assertNull(nothing);
		final Double j = cache.get("foo");
		Assert.assertEquals(i, j);
		final Double z = 4.1;
		final Double iPrime = cache.put("foo", z);
		Assert.assertEquals(i, iPrime);
		final Double k = cache.get("foo");
		Assert.assertEquals(z, k);
	}
	
	@Test
	public void simplePutIfAbsentGet()
	{
		final Double i = 3.2;
		final Double nothing = cache.putIfAbsent("foo", i);
		Assert.assertNull(nothing);
		final Double j = cache.get("foo");
		Assert.assertEquals(i, j);
		final Double z = 4.1;
		final Double iPrime = cache.putIfAbsent("foo", z);
		Assert.assertEquals(i, iPrime);
		final Double k = cache.get("foo");
		Assert.assertEquals(i, k);
	}
	
	@Test
	public void simplePutRemoveGet()
	{
		final Double i = 3.2;
		final Double nothing = cache.put("foo", i);
		Assert.assertNull(nothing);
		final Double j = cache.remove("foo");
		Assert.assertEquals(i, j);
		final Double z = cache.get("foo");
		Assert.assertNull(z);
	}
}
