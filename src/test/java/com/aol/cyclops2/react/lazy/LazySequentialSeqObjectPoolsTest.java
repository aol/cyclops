package com.aol.cyclops2.react.lazy;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static cyclops.collections.tuple.Tuple.tuple;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cyclops.stream.FutureStream;
import cyclops.collections.tuple.Tuple2;
import org.junit.Test;

import cyclops.async.LazyReact;
import com.aol.cyclops2.react.ThreadPools;
import com.aol.cyclops2.react.base.BaseSequentialSeqTest;

public class LazySequentialSeqObjectPoolsTest extends BaseSequentialSeqTest {

	@Override
	protected <U> FutureStream<U> of(U... array) {
		return new LazyReact(ThreadPools.getCurrentThreadExecutor())
							.objectPoolingOn()
							.sync()
							.of(array);
	}
	@Override
	protected <U> FutureStream<U> ofThread(U... array) {
		return new LazyReact(ThreadPools.getCommonFreeThread())
							.objectPoolingOn()
							.sync()
							.of(array);
	}

	@Override
	protected <U> FutureStream<U> react(Supplier<U>... array) {
		return new LazyReact(ThreadPools.getCommonFreeThread()).objectPoolingOn()
								.sync()
								.ofAsync(array);
	}
	@Test
    public void testCycle() {
        assertEquals(asList(1, 2, 1, 2, 1, 2),of(1, 2).cycle().limit(6).toList());
        assertEquals(asList(1, 2, 3, 1, 2, 3), of(1, 2, 3).cycle().limit(6).toList());
    }
	@Test
    public void testCycleTimes() {
        assertEquals(asList(1, 2, 1, 2, 1, 2),of(1, 2).cycle(3).toList());
       
    }
	int count =0;
	@Test
    public void testCycleWhile() {
		count =0;
        assertEquals(asList(1, 2,3, 1, 2,3),of(1, 2, 3).cycleWhile(next->count++<6).toList());
       
    }
	@Test
    public void testCycleUntil() {
		count =0;
        assertEquals(asList(1, 2,3, 1, 2,3),of(1, 2, 3).cycleUntil(next->count++==6).toList());
       
    }
	@Test
	public void iteratorMap(){
			
		assertThat( of(1)
				.peek(i-> System.out.println("peek1 " +  i))
				.map(i->i*2)
				.peek(i-> System.out.println("peek2 " +  i))
				.iterator().next(),equalTo(2));
	}
	@Test
	public void duplicateMap(){
		assertThat(of(1,2,3).map(i->i*2).duplicate()._1().toList(),equalTo(Arrays.asList(2,4,6)));
	}
	@Test
	public void concatStreamsJDK(){
	List<String> result = 	of(1,2,3).concat(Stream.of(100,200,300))
			.map(it ->it+"!!").collect(Collectors.toList());

		assertThat(result,hasItems("1!!","2!!","100!!","200!!","3!!","300!!"));
	}
	@Test
	public void concatStreams(){
	List<String> result = 	of(1,2,3).concat(of(100,200,300))
			.map(it ->it+"!!").collect(Collectors.toList());

		assertThat(result,containsInAnyOrder("1!!","2!!","100!!","200!!","3!!","300!!"));
	}
	@Test
	public void concatStreamsEager(){
	List<String> result = 	of(1,2,3).concat(Stream.of(100,200,300))
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
									.map(it ->it+"!!")
									.collect(Collectors.toList());

		assertThat(result,equalTo(Arrays.asList("1!!","2!!","100!!","200!!","3!!","300!!")));
	}
	@Test
	public void combine(){
		
		assertThat(of(1,2,3,4,5,6).mergeLatest(of(3)).collect(Collectors.toList()).size(),greaterThan(5));
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
	public void batchByTime2(){
		for(int i=0;i<10;i++){
			
			assertThat(react(()->1,()->2,()->3,()->4,()->5,()->{sleep(150);return 6;})
							.groupedByTime(1,TimeUnit.MICROSECONDS)
							.toList()
							.get(0)
							,not(hasItem(6)));
		}
	}
	
	@Test 
	public void batchSinceLastRead() throws InterruptedException{
		List<Collection> cols = of(1,2,3,4,5,6).chunkSinceLastRead()
											.peek(it->{sleep(50);})
											.collect(Collectors.toList());
		
		System.out.println(cols.get(0));
		assertThat(cols.get(0).size(),is(1));
		assertThat(cols.size(),greaterThan(1));
		
		
	
		
	}
	
	@Test
	public void shouldLazilyFlattenInfiniteStream() throws Exception {
		
		assertThat( LazyReact.sequentialCommonBuilder().iterate(1, n -> n+1)
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
		List<String> list = of("a","b").actOnFutures().duplicate()._1().block();
		assertThat(list,is(asList("a","b")));
	}
	@Test
	public void duplicateFutures2(){
		List<String> list = of("a","b").actOnFutures().duplicate()._2().block();
		assertThat(list,is(asList("a","b")));
	}
}
