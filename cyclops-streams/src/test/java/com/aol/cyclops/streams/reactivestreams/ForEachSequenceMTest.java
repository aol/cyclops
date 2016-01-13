package com.aol.cyclops.streams.reactivestreams;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.reactivestreams.Subscription;

import com.aol.cyclops.sequence.SequenceM;

public class ForEachSequenceMTest {
	Executor exec = Executors.newFixedThreadPool(1);
	volatile boolean complete =false;
	@Before
	public void setup(){
		error= null;
		complete =false;
	}

	@Test
	public void forEachX(){
		Subscription s = SequenceM.of(1,2,3)
							.futureOperations(exec)
							.forEachX( 2, System.out::println)
							.join();
		System.out.println("first batch");
		s.request(1);
	}
	@Test
	public void forEachXTest(){
		List<Integer> list = new ArrayList<>();
		Subscription s = SequenceM.of(1,2,3)
								  .futureOperations(exec)
								   .forEachX( 2,  i->list.add(i))
								   .join();
		assertThat(list,hasItems(1,2));
		assertThat(list.size(),equalTo(2));
		s.request(1);
		assertThat(list,hasItems(1,2,3));
		assertThat(list.size(),equalTo(3));
	}
	Throwable error;
	@Test
	public void forEachXWithErrors(){
	
		List<Integer> list = new ArrayList<>();
		
		Subscription s = SequenceM.of(()->1,()->2,()->3,(Supplier<Integer>)()->{ throw new RuntimeException();})
							.map(Supplier::get)
							.futureOperations(exec)
							.forEachXWithError( 2, i->list.add(i),
								e->error=e)
								.join();
		
		assertThat(list,hasItems(1,2));
		assertThat(list.size(),equalTo(2));
		System.out.println("first batch");
		s.request(1);
		assertThat(list,hasItems(1,2,3));
		assertThat(list.size(),equalTo(3));
		assertThat(error,nullValue());
		s.request(2);
		assertThat(error,instanceOf(RuntimeException.class));
	}
	@Test
	public void forEachXWithEvents(){
	
		List<Integer> list = new ArrayList<>();
		
		Subscription s = SequenceM.of(()->1,()->2,()->3,(Supplier<Integer>)()->{ throw new RuntimeException();}).map(Supplier::get)
						.futureOperations(exec)		
						.forEachXEvents( 2, i->list.add(i),
								e->error=e,()->complete=true).join()
								;
		
		assertThat(list,hasItems(1,2));
		assertThat(list.size(),equalTo(2));
		System.out.println("first batch");
		s.request(1);
		assertFalse(complete);
		assertThat(list,hasItems(1,2,3));
		assertThat(list.size(),equalTo(3));
		assertThat(error,nullValue());
		s.request(2);
		assertThat(error,instanceOf(RuntimeException.class));
		
		assertTrue(complete);
	}
	
	
	@Test 
	public void forEachWithErrors(){
	
		List<Integer> list = new ArrayList<>();
		assertThat(error,nullValue());
		SequenceM.of(()->1,()->2,()->3,(Supplier<Integer>)()->{ throw new RuntimeException();}).map(Supplier::get)
							.futureOperations(exec)
							.forEachWithError(  i->list.add(i),
								e->error=e).join();
		
		assertThat(list,hasItems(1,2,3));
		assertThat(list.size(),equalTo(3));
		
		assertThat(list,hasItems(1,2,3));
		assertThat(list.size(),equalTo(3));
		
	
		assertThat(error,instanceOf(RuntimeException.class));
	}
	@Test
	public void forEachWithEvents(){
	
		List<Integer> list = new ArrayList<>();
		assertFalse(complete);
		assertThat(error,nullValue());
		SequenceM.of(()->1,()->2,()->3,(Supplier<Integer>)()->{ throw new RuntimeException();})
				.map(Supplier::get)
				.futureOperations(exec) 
				.forEachEvent( i->list.add(i),e->error=e,()->complete=true)
				.join();
		
		
		
		assertThat(list,hasItems(1,2,3));
		assertThat(list.size(),equalTo(3));
		
		
		assertThat(error,instanceOf(RuntimeException.class));
		
		assertTrue(complete);
	}
}
