package com.aol.simple.react.eager;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import org.junit.Test;

import com.aol.simple.react.stream.eager.EagerReact;

public class FlatMapTest {

	@Test
	public void flatMapCf(){
		assertThat( new EagerReact()
										.of(1,2,3)
										.flatMapToCompletableFuture(i->CompletableFuture.completedFuture(i))
										.block(),equalTo(Arrays.asList(1,2,3)));
	}
	@Test
	public void flatMapCfSync(){
		assertThat( new EagerReact()
										.of(1,2,3)
										.sync()
										.flatMapToCompletableFuture(i->CompletableFuture.completedFuture(i))
										.block(),equalTo(Arrays.asList(1,2,3)));
	}
	@Test
	public void flatMapCfSync2(){
		assertThat( new EagerReact()
										.of(1,2,3)
										.flatMapToCompletableFutureSync(i->CompletableFuture.completedFuture(i))
										.block(),equalTo(Arrays.asList(1,2,3)));
	}
}
