package com.aol.cyclops.react.lazy.reactive;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.reactivestreams.Subscription;

import com.aol.cyclops.react.stream.traits.LazyFutureStream;
import com.aol.cyclops.util.ExceptionSoftener;
import com.aol.cyclops.util.stream.StreamUtils;

public class ForEachTest {
	boolean complete =false;
	@Before
	public void setup(){
		error= null;
		complete =false;
	}

	@Test
	public void forEachX(){
		Subscription s = StreamUtils.forEachX(LazyFutureStream.of(1,2,3), 2, System.out::println);
		System.out.println("first batch");
		s.request(1);
	}
	@Test
	public void forEachXTest(){
		List<Integer> list = new ArrayList<>();
		Subscription s = StreamUtils.forEachX(LazyFutureStream.of(1,2,3), 2,  i->list.add(i));
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
		
		Stream<Integer> stream = LazyFutureStream.of(()->1,()->2,()->3,(Supplier<Integer>)()->{ throw new RuntimeException();},()->4).map(Supplier::get);
		Subscription s = StreamUtils.forEachXWithError(stream, 2, i->list.add(i),
								e->error=e);
		
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
		
		Stream<Integer> stream = LazyFutureStream.of(()->1,()->2,()->3,(Supplier<Integer>)()->{ throw new RuntimeException();}).map(Supplier::get);
		Subscription s = StreamUtils.forEachXEvents(stream, 2, i->list.add(i),
								e->error=e,()->complete=true);
		
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
		Stream<Integer> stream = LazyFutureStream.of(()->1,()->2,()->3,(Supplier<Integer>)()->{ throw new RuntimeException();}).map(Supplier::get);
		StreamUtils.forEachWithError(stream,  i->list.add(i),
								e->error=e);
		
		assertThat(list,hasItems(1,2,3));
		assertThat(list.size(),equalTo(3));
		
		assertThat(list,hasItems(1,2,3));
		assertThat(list.size(),equalTo(3));
		
	
		assertThat(error,instanceOf(RuntimeException.class));
	}
	@Test
	public void forEachWithErrorsAsync(){
	
		List<Integer> list = new ArrayList<>();
		assertThat(error,nullValue());
		Stream<Integer> stream = LazyFutureStream.of(()->1,()->2,()->3,(Supplier<Integer>)()->{ throw new RuntimeException();},()->4,()->5)
													.withPublisherExecutor(new ForkJoinPool(1))
													.async()
													.map(Supplier::get);
		StreamUtils.forEachWithError(stream,  i->list.add(i),
								e->error=e);
		
		ExceptionSoftener.softenRunnable(()->Thread.sleep(100)).run();
		assertThat(list,hasItems(1,2,3,4,5));
		assertThat(list.size(),equalTo(5));
		
		assertThat(list,hasItems(1,2,3,4,5));
		assertThat(list.size(),equalTo(5));
		
	
		assertThat(error,instanceOf(RuntimeException.class));
	}
	@Test
	public void forEachWithEvents(){
	
		List<Integer> list = new ArrayList<>();
		assertFalse(complete);
		assertThat(error,nullValue());
		Stream<Integer> stream = LazyFutureStream.of(()->1,()->2,()->3,(Supplier<Integer>)()->{ throw new RuntimeException();}).map(Supplier::get);
		StreamUtils.forEachEvent(stream,  i->list.add(i),e->error=e,()->complete=true);
		
		
		
		assertThat(list,hasItems(1,2,3));
		assertThat(list.size(),equalTo(3));
		
		
		assertThat(error,instanceOf(RuntimeException.class));
		
		assertTrue(complete);
	}
}