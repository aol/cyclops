package com.aol.simple.react.lazy.futures;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.junit.Test;

import com.aol.simple.react.stream.traits.LazyFutureStream;

public class SkipTest {
	@Test
	public void testSkipLast(){
		assertThat(LazyFutureStream.of(1,2,3,4,5)
							.actOnFutures()
							.skipLast(2)
							.collect(Collectors.toList()),equalTo(Arrays.asList(1,2,3)));
	}
	@Test
	public void testSkipLastEmpty(){
		assertThat(LazyFutureStream.of()
							.actOnFutures()
							.skipLast(2)
							.collect(Collectors.toList()),equalTo(Arrays.asList()));
	}
	@Test
	public void testLimitLast(){
		assertThat(LazyFutureStream.of(1,2,3,4,5)
							.actOnFutures()
							.limitLast(2)
							.collect(Collectors.toList()),equalTo(Arrays.asList(4,5)));
	}
	@Test
	public void testLimitLastEmpty(){
		assertThat(LazyFutureStream.of()
							.actOnFutures()
							.limitLast(2)
							.collect(Collectors.toList()),equalTo(Arrays.asList()));
	}
}
