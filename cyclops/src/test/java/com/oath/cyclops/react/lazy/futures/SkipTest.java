package com.oath.cyclops.react.lazy.futures;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.stream.Collectors;

import cyclops.async.LazyReact;
import org.junit.Test;

public class SkipTest {
	@Test
	public void testSkipLast(){
		assertThat(LazyReact.sequentialBuilder().of(1,2,3,4,5)
							.actOnFutures()
							.skipLast(2)
							.collect(Collectors.toList()),equalTo(Arrays.asList(1,2,3)));
	}
	@Test
	public void testSkipLastEmpty(){
		assertThat(LazyReact.sequentialBuilder().of()
							.actOnFutures()
							.skipLast(2)
							.collect(Collectors.toList()),equalTo(Arrays.asList()));
	}
	@Test
	public void testLimitLast(){
		assertThat(LazyReact.sequentialBuilder().of(1,2,3,4,5)
							.actOnFutures()
							.limitLast(2)
							.collect(Collectors.toList()),equalTo(Arrays.asList(4,5)));
	}
	@Test
	public void testLimitLastEmpty(){
		assertThat(LazyReact.sequentialBuilder().of()
							.actOnFutures()
							.limitLast(2)
							.collect(Collectors.toList()),equalTo(Arrays.asList()));
	}
}
