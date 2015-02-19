package com.aol.simple.react;

import static com.aol.simple.react.ReactStream.*;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

import org.jooq.lambda.tuple.Tuple2;
import org.junit.Test;

import com.aol.simple.react.async.Queue;
import com.aol.simple.react.async.QueueFactories;

public class SeqTest {
	

	@Test
	public void zip(){
		List<Tuple2<Integer,Integer>> list =  eager(1,2,3,4,5,6).zip(eager(100,200,300,400)).peek(it -> System.out.println(it)).collect(Collectors.toList());
		
		List<Integer> right = list.stream().map(t -> t.v2).collect(Collectors.toList());
		assertThat(right,hasItem(100));
		assertThat(right,hasItem(200));
		assertThat(right,hasItem(300));
		assertThat(right,hasItem(400));
		
		List<Integer> left = list.stream().map(t -> t.v1).collect(Collectors.toList());
		assertThat(Arrays.asList(1,2,3,4,5,6),hasItem(left.get(0)));
		
		
	}
	@Test
	public void zipInOrder(){
		List<Tuple2<Integer,Integer>> list =  ReactStream.eager(1,2,3,4,5,6).sorted()
													.zip( ReactStream.eager(100,200,300,400).sorted())
													.collect(Collectors.toList());
		
		assertThat(list.get(0).v1,is(1));
		assertThat(list.get(0).v2,is(100));
		
		
	}
	@Test
	public void zipFastSlow(){
		Queue q = new Queue();
		ReactStream.lazy().reactInfinitely(()->sleep(100)).then(it -> q.add("100")).run(new ForkJoinPool(1));
		ReactStream.eager(1,2,3,4,5,6).zip(q.stream()).peek(it -> System.out.println(it)).collect(Collectors.toList());
		
		
	}
	
	@Test 
	public void testBackPressureWhenZippingUnevenStreams(){
	
		Queue fast = lazy().reactInfinitely(()-> "100").withQueueFactory(QueueFactories.boundedQueue(10))
					.toQueue();
		
		 new Thread(() -> { lazy().react(()->1,SimpleReact.times(1000)).peek(c -> sleep(10))
		 			.zip(fast.stream())
		 			.forEach(it->{}); }).start();;
		
		 int max = fast.getSizeSignal().getDiscrete().stream().mapToInt(it->(int)it).limit(50).max().getAsInt();
		assertThat(max,is(10));
	}

	
	@Test
	public void limitWhileTest(){
		List<Integer> list =  eager(1,2,3,4,5,6).sorted().limitWhile(it -> it<4).peek(it -> System.out.println(it)).collect(Collectors.toList());
	
		assertThat(list,hasItem(1));
		assertThat(list,hasItem(2));
		assertThat(list,hasItem(3));
		
		
		
	}
	
	private Object sleep(int i) {
		try {
			Thread.currentThread().sleep(i);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return i;
	}
	
}
