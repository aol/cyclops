package com.aol.cyclops.streams.future;

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

import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.types.stream.reactive.ReactiveTask;

public class ForEachSequenceMFutureTest {
	Executor exec = Executors.newFixedThreadPool(1);
	volatile boolean complete =false;
	@Before
	public void setup(){
		error= null;
		complete =false;
	}

	@Test
	public void forEachX(){
		ReactiveTask s = ReactiveSeq.of(1,2,3)
							.futureOperations(exec)
							.forEachX( 2, System.out::println);
		
		System.out.println("first batch");
		s.request(1);
		s.block();
	}
	@Test
	public void forEachXTest(){
		List<Integer> list = new ArrayList<>();
		ReactiveTask s = ReactiveSeq.of(1,2,3)
								  .futureOperations(exec)
								   .forEachX( 2,  i->list.add(i));
		s.block();
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
		
		ReactiveTask s = ReactiveSeq.of(()->1,()->2,()->3,(Supplier<Integer>)()->{ throw new RuntimeException();})
							.map(Supplier::get)
							.futureOperations(exec)
							.forEachXWithError( 2, i->list.add(i),
								e->error=e);
		
		s.block();
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
		
		ReactiveTask s = ReactiveSeq.of(()->1,()->2,()->3,(Supplier<Integer>)()->{ throw new RuntimeException();}).map(Supplier::get)
						.futureOperations(exec)		
						.forEachXEvents( 2, i->list.add(i),
								e->error=e,()->complete=true);
		s.block();
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
		ReactiveSeq.of(()->1,()->2,()->3,(Supplier<Integer>)()->{ throw new RuntimeException();}).map(Supplier::get)
							.futureOperations(exec)
							.forEachWithError(  i->list.add(i),
								e->error=e).block();
		
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
		ReactiveSeq.of(()->1,()->2,()->3,(Supplier<Integer>)()->{ throw new RuntimeException();})
				.map(Supplier::get)
				.futureOperations(exec) 
				.forEachEvent( i->list.add(i),e->error=e,()->complete=true)
				.block();
		
		
		
		assertThat(list,hasItems(1,2,3));
		assertThat(list.size(),equalTo(3));
		
		
		assertThat(error,instanceOf(RuntimeException.class));
		
		assertTrue(complete);
	}
	
	@Test
	public void streamCompleteForEachXTest(){
		List<Integer> list = new ArrayList<>();
		ReactiveTask s = ReactiveSeq.of(1,2,3)
								  .futureOperations(exec)
								   .forEachX( 2,  i->list.add(i));
		s.block();
		assertFalse(s.isStreamComplete());
		
		s.request(2);
		
		
		assertTrue(s.isStreamComplete());
	}
	@Test
	public void streamCompleteForEachXWithErrorsTest(){
		List<Integer> list = new ArrayList<>();
		ReactiveTask s = ReactiveSeq.of(1,2,3)
								  .futureOperations(exec)
								   .forEachXWithError( 2, i->list.add(i),
											e->error=e);
		s.block();
		assertFalse(s.isStreamComplete());
		
		s.request(2);
		
		
		assertTrue(s.isStreamComplete());
	}
	@Test
	public void streamCompleteForEachXEventsTest(){
		List<Integer> list = new ArrayList<>();
		ReactiveTask s = ReactiveSeq.of(1,2,3)
								  .futureOperations(exec)
								  .forEachXEvents( 2, i->list.add(i),
										   	e->error=e,()->complete=true);
		s.block();
		assertFalse(s.isStreamComplete());
		
		s.request(2);
		s.block();
		
		
		assertTrue(s.isStreamComplete());
	}
	@Test
	public void streamCompleteForEachWithErrorsTest(){
	   
		List<Integer> list = new ArrayList<>();
		ReactiveTask s = ReactiveSeq.of(1,2,3)
								  .futureOperations(exec)
								   .forEachWithError( i->{ list.add(i); sleep(100);},
											e->error=e);
		
		assertFalse(s.isStreamComplete());
		
		s.block();
		
		
		assertTrue(s.isStreamComplete());
	    
	}
	private void sleep(int i) {
        try {
            Thread.sleep(i);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }

    @Test
	public void streamCompleteForEachEventsTest(){
		List<Integer> list = new ArrayList<>();
		ReactiveTask s = ReactiveSeq.of(1,2,3)
								  .futureOperations(exec)
								  .forEachEvent(  i->list.add(i),
										   	e->error=e,()->complete=true);
		
	//	assertFalse(s.isStreamComplete());
		
		s.block();
		
		
		assertTrue(s.isStreamComplete());
	}
	@Test
	public void forEachXAsync(){
		ReactiveTask s = ReactiveSeq.of(1,2,3)
							.futureOperations(exec)
							.forEachX( 2, System.out::println);
		
		System.out.println("first batch");
		s.requestAsync(1).block();
	}
	
	@Test
	public void forEachXRequestAllTest(){
		List<Integer> list = new ArrayList<>();
		ReactiveTask s = ReactiveSeq.of(1,2,3)
								  .futureOperations(exec)
								   .forEachX( 2,  i->list.add(i));
		s.block();
		assertThat(list,hasItems(1,2));
		assertThat(list.size(),equalTo(2));
		s.requestAll();
		
		assertThat(list,hasItems(1,2,3));
		assertThat(list.size(),equalTo(3));
	}
	@Test
	public void forEachXRequestAllAsyncTest(){
		List<Integer> list = new ArrayList<>();
		ReactiveTask s = ReactiveSeq.of(1,2,3)
								  .futureOperations(exec)
								   .forEachX( 2,  i->list.add(i));
		s.block();
		assertThat(list,hasItems(1,2));
		assertThat(list.size(),equalTo(2));
		s.requestAllAsync().block();
		
		assertThat(list,hasItems(1,2,3));
		assertThat(list.size(),equalTo(3));
	}
	
}
