package com.aol.cyclops.javaslang.streams;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import javaslang.collection.LazyStream;
import javaslang.collection.Stream;

import org.junit.Before;
import org.junit.Test;
import org.reactivestreams.Subscription;

import com.aol.cyclops.javaslang.reactivestreams.ReactiveStream;




public class ForEachTest {
	boolean complete =false;
	@Before
	public void setup(){
		error= null;
		complete =false;
	}

	@Test
	public void forEachX(){

		Subscription s = StreamUtils.forEachX(LazyStream.of(1,2,3), 2, System.out::println);

		System.out.println("first batch");
		s.request(1);
	}
	@Test
	public void forEachXTest(){
		List<Integer> list = new ArrayList<>();

		Subscription s = StreamUtils.forEachX(LazyStream.of(1,2,3), 2,  i->list.add(i));

		assertThat(list,hasItems(1,2));
		assertThat(list.size(),equalTo(2));
		s.request(1);
		assertThat(list,hasItems(1,2,3));
		assertThat(list.size(),equalTo(3));
	}
	Throwable error;
<<<<<<< HEAD
	@Test
	public void forEachXWithErrors(){
	
		List<Integer> list = new ArrayList<>();
		
		LazyStream<Integer> stream = LazyStream.of(()->1,()->2,()->3,(Supplier<Integer>)()->{ throw new RuntimeException();}).map(Supplier::get);

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
		
		LazyStream<Integer> stream = LazyStream.of(()->1,()->2,()->3,(Supplier<Integer>)()->{ throw new RuntimeException();}).map(Supplier::get);

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
		LazyStream<Integer> stream = LazyStream.of(()->1,()->2,()->3,(Supplier<Integer>)()->{ throw new RuntimeException();}).map(Supplier::get);

		StreamUtils.forEachWithError(stream,  i->list.add(i),
								e->error=e);
		
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

		LazyStream<Integer> stream = LazyStream.of(()->1,()->2,()->3,(Supplier<Integer>)()->{ throw new RuntimeException();}).map(Supplier::get);

		StreamUtils.forEachEvent(stream,  i->list.add(i),e->error=e,()->complete=true);
		
		
		
		assertThat(list,hasItems(1,2,3));
		assertThat(list.size(),equalTo(3));
		
		
		assertThat(error,instanceOf(RuntimeException.class));
		
		assertTrue(complete);
	}
	@Test
	public void forEachWithEvents2(){
	
		List<Integer> list = new ArrayList<>();
		assertFalse(complete);
		assertThat(error,nullValue());
		ReactiveStream<Integer> stream = ReactiveStream.of(()->1,()->2,()->3,(Supplier<Integer>)()->{ throw new RuntimeException();},()->5).map(Supplier::get);
		
		//StreamUtils.forEachWithError(Stream.of(()->1,()->2,()->3,(Supplier<Integer>)()->{ throw new RuntimeException();},()->5),
			//	System.out::println,Throwable::printStackTrace);
		stream.forEachWithError(System.out::println,Throwable::printStackTrace);
		
		StreamUtils.forEachEvent(stream,  i->list.add(i),e->error=e,()->complete=true);
		
		
		
		assertThat(list,hasItems(1,2,3,5));
		assertThat(list.size(),equalTo(4));
		
		
		assertThat(error,instanceOf(RuntimeException.class));
		
		assertTrue(complete);
	}
=======

	
>>>>>>> master
}
