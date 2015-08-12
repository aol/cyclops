package com.aol.simple.react.lazy;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.*;
import static org.jooq.lambda.tuple.Tuple.tuple;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jooq.lambda.tuple.Tuple2;
import org.junit.Ignore;
import org.junit.Test;

import com.aol.simple.react.base.BaseSequentialSeqTest;
import com.aol.simple.react.stream.lazy.LazyReact;
import com.aol.simple.react.stream.traits.FutureStream;
import com.aol.simple.react.stream.traits.LazyFutureStream;

public class LazySequentialSeqAgronaTest extends BaseSequentialSeqTest {

	@Override
	protected <U> LazyFutureStream<U> of(U... array) {
		return LazyFutureStream.of(array).boundedWaitFree(1000);
	}
	@Override
	protected <U> LazyFutureStream<U> ofThread(U... array) {
		return LazyFutureStream.ofThread(array).boundedWaitFree(1000);
	}

	@Override
	protected <U> FutureStream<U> react(Supplier<U>... array) {
		return LazyReact.sequentialBuilder().react(array).boundedWaitFree(1000);
	}

	@Test
	public void batchSinceLastReadIterator() throws InterruptedException{
		Iterator<Collection<Integer>> it = of(1,2,3,4,5,6).chunkLastReadIterator();
	
		Thread.sleep(10);
		Collection one = it.next();
		
		Collection two = it.next();
		
		assertThat(one.size(),is(1));
		assertThat(two.size(),greaterThan(0));
		
	
		
	}
	
	@Test 
	public void batchSinceLastRead() throws InterruptedException{
		List<Collection> cols = of(1,2,3,4,5,6).chunkSinceLastRead().peek(it->{sleep(50);}).collect(Collectors.toList());
		
		System.out.println(cols.get(0));
		assertThat(cols.get(0).size(),is(1));
		assertThat(cols.size(),greaterThan(1));
		
		
	
		
	}
	
	@Test
	public void shouldLazilyFlattenInfiniteStream() throws Exception {
		
		assertThat( LazyFutureStream.iterate(1,n -> n+1)
				.flatMap(i -> Arrays.asList(i, 0, -i).stream())
				.limit(10).block(),
				equalTo(Arrays.asList(1, 0, -1, 2, 0, -2, 3, 0, -3, 4)));
	}
	
	
	

	
	@Test
	public void testZipWithFutures(){
		Stream stream = of("a","b");
		List<Tuple2<Integer,String>> result = of(1,2).zipFutures(stream).block();
		
		assertThat(result,is(asList(tuple(1,"a"),tuple(2,"b"))));
	}
	

	@Test
	public void testZipFuturesWithIndex(){
		
		List<Tuple2<String,Long>> result = of("a","b").zipFuturesWithIndex().block();
		
		assertThat(result,is(asList(tuple("a",0l),tuple("b",1l))));
	}
	@Test
	public void duplicateFutures(){
		List<String> list = of("a","b").duplicateFutures().v1.block();
		assertThat(list,is(asList("a","b")));
	}
	@Test
	public void duplicateFutures2(){
		List<String> list = of("a","b").duplicateFutures().v2.block();
		assertThat(list,is(asList("a","b")));
	}
}
