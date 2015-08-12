package com.aol.simple.react.eager;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.jooq.lambda.tuple.Tuple.tuple;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;
import org.junit.Test;

import com.aol.simple.react.base.BaseSequentialSeqTest;
import com.aol.simple.react.stream.eager.EagerReact;
import com.aol.simple.react.stream.traits.EagerFutureStream;
import com.aol.simple.react.stream.traits.FutureStream;
import com.aol.simple.react.threads.ReactPool;
import com.aol.simple.react.util.SimpleTimer;

public class EagerSequentialSeqTest extends BaseSequentialSeqTest {
	
	
	@Override
	protected <U> EagerFutureStream<U> react(Supplier<U>... array) {
		
		return EagerFutureStream.sequentialCommonBuilder().react(array);
		
	}
	@Override
	protected <U> EagerFutureStream<U> ofThread(U... array) {
		return EagerFutureStream.ofThread(array);
	}
	
	@Override
	protected <U> EagerFutureStream<U> of(U... array) {
		return EagerFutureStream.sequentialCurrentBuilder().of(array);
	}
	@Test
	public void concatStreams(){
	List<String> result = 	of(1,2,3).concat(of(100,200,300))
			.map(it ->it+"!!").collect(Collectors.toList());

		assertThat(result,equalTo(Arrays.asList("1!!","2!!","3!!","100!!","200!!","300!!")));
	}
	@Test
	public void merge(){
	List<String> result = 	of(1,2,3).merge(of(100,200,300))
			.map(it ->it+"!!").collect(Collectors.toList());

		assertThat(result,equalTo(Arrays.asList("1!!","2!!","3!!","100!!","200!!","300!!")));
	}
	@Test
	public void batchByTime2(){
		for(int i=0;i<10;i++){
			
			assertThat(react(()->1,()->2,()->3,()->4,()->{sleep(100);return 5;},()->6)
							.batchByTime(60,TimeUnit.MILLISECONDS)
							.toList()
							.get(0)
							,not(hasItem(6)));
		}
	}
	@Test
	public void debounceEager(){
		SimpleTimer timer = new SimpleTimer();
	//	System.out.println(of(1,2,3,4,5,6));
	//	System.out.println(of(1,2,3,4,5,6).debounce(1000,TimeUnit.SECONDS).toList());
		for(int i=0;i<500;i++){
			System.out.println(i);
			assertThat(of(1,2,3,4,5,6).debounce(1000,TimeUnit.SECONDS).collect(Collectors.toList()).size(),is(1));
		}
	}
	@Test
	public void debounceOkEager(){
		for(int i=0;i<500;i++){
			System.out.println(i);
			assertThat(of(1,2,3,4,5,6).debounce(1,TimeUnit.NANOSECONDS).collect(Collectors.toList()).size(),is(6));
		}
		
	}
	@Test
	public void batchBySizeAndTimeTime(){
		
		for(int i=0;i<10;i++){
			List<List<Integer>> list = react(()->1,()->2,()->3,()->4,()->{sleep(150);return 5;},()-> 6)
					.batchBySizeAndTime(30,10,TimeUnit.MILLISECONDS)
					.toList();
			if(list.size()==0)
				System.out.println(i+":"+list);
			assertThat(list
							.get(0)
							,not(hasItem(6)));
		}
	}
	@Test
	public void batchSinceLastReadIterator() throws InterruptedException{
		Iterator<Collection<Integer>> it = of(1,2,3,4,5,6).chunkLastReadIterator();
	
		Thread.sleep(10);
		Collection one = it.next();
		
		
		Collection two = it.next();
		
		assertThat(one.size(),is(6));
		assertThat(two.size(),is(0));
		
	
		
	}

	@Test
	public void batchSinceLastRead() throws InterruptedException{
		List<Collection> cols = of(1,2,3,4,5,6).chunkSinceLastRead().peek(it->{sleep(150);}).collect(Collectors.toList());
		
		System.out.println(cols.get(0));
		assertThat(cols.size(),greaterThan(0)); //anything else is non-deterministic
	//	if(cols.size()>1)
	//		assertThat(cols.get(1).size(),is(0));
		
		
	
		
	}
	
	@Test
	public void testLimitFutures(){
		assertThat(of(1,2,3,4,5).limitFutures(2).collect(Collectors.toList()),is(asList(1,2)));
		
	}	
	@Test
	public void testSkipFutures(){
		assertThat(of(1,2,3,4,5).skipFutures(2).collect(Collectors.toList()),is(asList(3,4,5)));
	}
	@Test
	public void testSliceFutures(){
		assertThat(of(1,2,3,4,5).sliceFutures(3,4).collect(Collectors.toList()),is(asList(4)));
	}
	@Test
	public void testSplitFuturesAt(){
		assertThat(of(1,2,3,4,5).splitAtFutures(2).v1.block(),is(asList(1,2)));
	}
	@Test
	public void testSplitFuturesAt2(){
		assertThat(of(1,2,3,4,5).splitAtFutures(2).v2.collect(Collectors.toList()),is(asList(3,4,5)));
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
