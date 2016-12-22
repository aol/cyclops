package com.aol.cyclops.react.lazy.futures;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.stream.Collectors;

import cyclops.stream.FutureStream;
import org.junit.Test;

public class SkipTest {
	@Test
	public void testSkipLast(){
		assertThat(FutureStream.of(1,2,3,4,5)
							.actOnFutures()
							.skipLast(2)
							.collect(Collectors.toList()),equalTo(Arrays.asList(1,2,3)));
	}
	@Test
	public void testSkipLastEmpty(){
		assertThat(FutureStream.of()
							.actOnFutures()
							.skipLast(2)
							.collect(Collectors.toList()),equalTo(Arrays.asList()));
	}
	@Test
	public void testLimitLast(){
		assertThat(FutureStream.of(1,2,3,4,5)
							.actOnFutures()
							.limitLast(2)
							.collect(Collectors.toList()),equalTo(Arrays.asList(4,5)));
	}
	@Test
	public void testLimitLastEmpty(){
		assertThat(FutureStream.of()
							.actOnFutures()
							.limitLast(2)
							.collect(Collectors.toList()),equalTo(Arrays.asList()));
	}
}
