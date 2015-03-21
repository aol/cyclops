package com.aol.simple.react.eager;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.jooq.lambda.tuple.Tuple.tuple;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;
import org.junit.Test;

import com.aol.simple.react.base.BaseSeqTest;
import com.aol.simple.react.stream.eager.EagerFutureStream;
import com.aol.simple.react.stream.lazy.LazyFutureStream;
import com.aol.simple.react.stream.traits.FutureStream;

public class EagerSeqTest extends BaseSeqTest {
 
	@Override
	protected <U> EagerFutureStream<U> of(U... array) {
		return EagerFutureStream.parallel(array);
	}
	
	@Override
	protected <U> FutureStream<U> react(Supplier<U>... array) {
		return EagerFutureStream.parallelBuilder().react(array);
		
	}

	@Test
	public void testOfType() {
		assertTrue(
				of(1, "a", 2, "b", 3, null).ofType(Integer.class).toList().containsAll(asList(1, 2, 3)));
		assertTrue( of(1, "a", 2, "b", 3, null)
				.ofType(Serializable.class).toList().containsAll(asList(1, "a", 2, "b", 3)));
	}

	@Test
	public void testCastPast() {
		assertTrue(
				of(1, "a", 2, "b", 3, null).capture(e -> e.printStackTrace())
						.cast(Serializable.class).toList().containsAll(
								asList(1, "a", 2, "b", 3, null)));

	}
	
	@Test
	public void testLimitFutures(){
		assertThat(of(1,2,3,4,5).limitFutures(2).collect(Collectors.toList()).size(),is(2));
		
	}	
	@Test
	public void testSkipFutures(){
		assertThat(of(1,2,3,4,5).skipFutures(2).collect(Collectors.toList()).size(),is(3));
	}
	@Test
	public void testSliceFutures(){
		assertThat(of(1,2,3,4,5).sliceFutures(3,4).collect(Collectors.toList()).size(),is(1));
	}
	@Test
	public void testSplitFuturesAt(){
		assertThat(of(1,2,3,4,5).splitAtFuturesFutureStream(2).v1.block().size(),is(asList(1,2).size()));
	}
	@Test
	public void testSplitFuturesAt2(){
		assertThat(sortedList(of(1,2,3,4,5).splitAtFuturesFutureStream(2)
											.v2
											.collect(Collectors.toList())).size(),
											is(asList(3,4,5).size()));
	}

	
	@Test
	public void testZipWithFutures(){
		Stream stream = of("a","b");
		Seq<Tuple2<CompletableFuture<Integer>,String>> seq = of(1,2).zipFutures(stream);
		List<Tuple2<Integer,String>> result = seq.map(tuple -> Tuple.tuple(tuple.v1.join(),tuple.v2)).collect(Collectors.toList());
		assertThat(result.size(),is(asList(tuple(1,"a"),tuple(2,"b")).size()));
	}
	

	@Test
	public void testZipFuturesWithIndex(){
		
		Seq<Tuple2<CompletableFuture<String>,Long>> seq = of("a","b").zipFuturesWithIndex();
		List<Tuple2<String,Long>> result = seq.map(tuple -> Tuple.tuple(tuple.v1.join(),tuple.v2)).collect(Collectors.toList());
		assertThat(result.size(),is(asList(tuple("a",0l),tuple("b",1l)).size()));
	}
	@Test
	public void duplicateFutures(){
		List<String> list = of("a","b").duplicateFuturesFutureStream().v1.block();
		assertThat(sortedList(list),is(asList("a","b")));
	}
	private <T> List<T> sortedList(List<T> list) {
		return list.stream().sorted().collect(Collectors.toList());
	}

	@Test
	public void duplicateFutures2(){
		List<String> list = of("a","b").duplicateFuturesFutureStream().v2.block();
		assertThat(sortedList(list),is(asList("a","b")));
	}
	

	/*
	 * Default behaviour is to operate on the outputs rather than the inputs where that can be done in a non-blocking way
	 */
	
	
}
