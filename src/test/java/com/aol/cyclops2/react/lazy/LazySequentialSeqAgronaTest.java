package com.aol.cyclops2.react.lazy;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.jooq.lambda.tuple.Tuple.tuple;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cyclops.stream.FutureStream;
import org.jooq.lambda.tuple.Tuple2;
import org.junit.Test;

import cyclops.async.LazyReact;
import com.aol.cyclops2.react.base.BaseSequentialSeqTest;

public class LazySequentialSeqAgronaTest extends BaseSequentialSeqTest {

	@Override
	protected <U> FutureStream<U> of(U... array) {
		return FutureStream.of(array).boundedWaitFree(1000);
	}
	@Override
	protected <U> FutureStream<U> ofThread(U... array) {
		return FutureStream.freeThread(array).boundedWaitFree(1000);
	}

	@Override
	protected <U> FutureStream<U> react(Supplier<U>... array) {
		return LazyReact.sequentialBuilder().ofAsync(array).boundedWaitFree(1000);
	}

	@Test
	public void concatStreams(){
	List<String> result = 	of(1,2,3).concat(of(100,200,300))
			.map(it ->it+"!!").collect(Collectors.toList());

		assertThat(result,containsInAnyOrder("1!!","2!!","100!!","200!!","3!!","300!!"));
	}
	@Test
	public void concat(){
	List<String> result = 	of(1,2,3).concat(100,200,300)
			.map(it ->it+"!!").collect(Collectors.toList());

		assertThat(result,containsInAnyOrder("1!!","2!!","100!!","200!!","3!!","300!!"));
	}
	
	@Test
	public void merge(){
	List<String> result = 	of(1,2,3).mergeLatest(of(100,200,300))
			.map(it ->it+"!!").collect(Collectors.toList());

		assertThat(result,equalTo(Arrays.asList("1!!","2!!","100!!","200!!","3!!","300!!")));
	}
	@Test
	public void batchSinceLastReadIterator() throws InterruptedException{
		Iterator<Collection<Integer>> it = of(1,2,3,4,5,6).chunkLastReadIterator();
	
		Thread.sleep(10);
		Collection one = it.next();
		
		Collection two = it.next();
		
		assertThat(one.size(),greaterThan(0));
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
		
		assertThat( FutureStream.iterate(1, n -> n+1)
				.flatMap(i -> Arrays.asList(i, 0, -i).stream())
				.limit(10).block(),
				equalTo(Arrays.asList(1, 0, -1, 2, 0, -2, 3, 0, -3, 4)));
	}
	
	
	

	
	@Test
	public void testZipWithFutures(){
		Stream stream = of("a","b");
		List<Tuple2<Integer,String>> result = of(1,2).actOnFutures().zip(stream).block();
		
		assertThat(result,is(asList(tuple(1,"a"),tuple(2,"b"))));
	}
	

	@Test
	public void testZipFuturesWithIndex(){
		
		List<Tuple2<String,Long>> result = of("a","b").actOnFutures().zipWithIndex().block();
		
		assertThat(result,is(asList(tuple("a",0l),tuple("b",1l))));
	}
	@Test
	public void duplicateFutures(){
		List<String> list = of("a","b").actOnFutures().duplicate().v1.block();
		assertThat(list,is(asList("a","b")));
	}
	@Test
	public void duplicateFutures2(){
		List<String> list = of("a","b").actOnFutures().duplicate().v2.block();
		assertThat(list,is(asList("a","b")));
	}
}
