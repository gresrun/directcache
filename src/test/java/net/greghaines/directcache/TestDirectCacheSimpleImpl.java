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
	
	@Test
	public void multiPutGet()
	{
		final Double d1 = 1.11;
		final Double d2 = 2.11;
		final Double d3 = 3.11;
		final Double d4 = 4.11;
		final Double d5 = 5.11;
		cache.put("foo1", d1);
		cache.put("foo2", d2);
		cache.put("foo3", d3);
		cache.put("foo4", d4);
		cache.put("foo5", d5);
		for (int i = 0; i < 1000; i++)
		{
			try
			{
				Assert.assertEquals(d1, cache.remove("foo1"));
				Assert.assertEquals(d2, cache.remove("foo2"));
				Assert.assertEquals(d3, cache.remove("foo3"));
				Assert.assertEquals(d4, cache.remove("foo4"));
				Assert.assertEquals(d5, cache.remove("foo5"));
				cache.put("foo1", d1);
				cache.put("foo2", d2);
				cache.put("foo3", d3);
				cache.put("foo4", d4);
				cache.put("foo5", d5);
			}
			catch (RuntimeException re)
			{
				System.err.println("i=" + i);
				throw re;
			}
			catch (Error err)
			{
				System.err.println("i=" + i);
				throw err;
			}
		}
	}
}
