package com.aol.simple.react.eager;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.jooq.lambda.tuple.Tuple.tuple;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jooq.lambda.tuple.Tuple2;
import org.junit.Ignore;
import org.junit.Test;

import com.aol.simple.react.base.BaseSeqTest;
import com.aol.simple.react.stream.eager.EagerFutureStream;
import com.aol.simple.react.stream.traits.FutureStream;
import com.google.common.collect.Lists;

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
	public void batchSinceLastReadIterator() throws InterruptedException{
		Iterator<Collection<Object>> it = react(()->1,()->2,()->3,()->4,()->5,()->value()).chunkLastReadIterator();
		Thread.sleep(50);
		List<Collection> cols = Lists.newArrayList();
		while(it.hasNext()){
			
			cols.add(it.next());
		}
		
		assertThat(cols.get(0).size(),greaterThan(1));
		cols.remove(0);
		Collection withJello = cols.stream().filter(col -> col.contains("jello")).findFirst().get();
		
	
		
	}
	@Test
	public void batchSinceLastRead() throws InterruptedException{
		
			
			List<Collection> cols = react(()->1,()->2,()->3,()->4,()->5,()->value()).chunkSinceLastRead().peek(it->{sleep(50);}).collect(Collectors.toList());
			
			System.out.println(cols);
			assertThat(cols.size(),greaterThan(1));
			cols.remove(0);
			Collection withJello = cols.stream().filter(col -> col.contains("jello")).findFirst().get();
		
		
	
		
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
		assertThat(of(1,2,3,4,5).splitAtFutures(2).v1.block().size(),is(asList(1,2).size()));
	}
	@Test
	public void testSplitFuturesAt2(){
		assertThat(sortedList(of(1,2,3,4,5).splitAt(2)
											.v2
											.collect(Collectors.toList())).size(),
											is(asList(3,4,5).size()));
	}

	
	@Test
	public void testZipWithFutures(){
		Stream stream = of("a","b");
		List<Tuple2<Integer,String>> result = of(1,2).zipFutures(stream).block();
		
		assertThat(result.size(),is(asList(tuple(1,"a"),tuple(2,"b")).size()));
	}
	

	@Test
	public void testZipFuturesWithIndex(){
		
		List<Tuple2<String,Long>> result  = of("a","b").zipFuturesWithIndex().block();
		
		assertThat(result.size(),is(asList(tuple("a",0l),tuple("b",1l)).size()));
	}
	@Test
	public void duplicateFutures(){
		List<String> list = of("a","b").duplicateFutures().v1.block();
		assertThat(sortedList(list),is(asList("a","b")));
	}
	private <T> List<T> sortedList(List<T> list) {
		return list.stream().sorted().collect(Collectors.toList());
	}

	@Test
	public void duplicateFutures2(){
		List<String> list = of("a","b").duplicateFutures().v2.block();
		assertThat(sortedList(list),is(asList("a","b")));
	}
	

	/*
	 * Default behaviour is to operate on the outputs rather than the inputs where that can be done in a non-blocking way
	 */
	
	
}
