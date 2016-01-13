package com.aol.cyclops.streams.reactivestreams;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.reactivestreams.Subscription;

import com.aol.cyclops.sequence.SequenceM;
import com.aol.cyclops.sequence.reactivestreams.ReactiveStreamsTerminalOperations;
import com.aol.cyclops.streams.StreamUtils;

public class ForEachSequenceMTest {
	boolean complete =false;
	@Before
	public void setup(){
		error= null;
		complete =false;
	}

	@Test
	public void forEachX(){
		Subscription s = SequenceM.of(1,2,3).forEachX( 2, System.out::println);
		System.out.println("first batch");
		s.request(1);
	}
	@Test
	public void forEachXTest(){
		List<Integer> list = new ArrayList<>();
		Subscription s = SequenceM.of(1,2,3).forEachX( 2,  i->list.add(i));
		assertThat(list,hasItems(1,2));
		assertThat(list.size(),equalTo(2));
		s.request(1);
		assertThat(list,hasItems(1,2,3));
		assertThat(list.size(),equalTo(3));
	}
	@Test
	public void forEachXTestIsComplete(){
		List<Integer> list = new ArrayList<>();
		Subscription s = SequenceM.of(1,2,3).forEachX( 2,  i->list.add(i));
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
							.forEachXWithError( 2, i->list.add(i),
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
		
		Subscription s = SequenceM.of(()->1,()->2,()->3,(Supplier<Integer>)()->{ throw new RuntimeException();}).map(Supplier::get)
						.forEachXEvents( 2, i->list.add(i),
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
		SequenceM.of(()->1,()->2,()->3,(Supplier<Integer>)()->{ throw new RuntimeException();}).map(Supplier::get)
							.forEachWithError(  i->list.add(i),
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
		SequenceM.of(()->1,()->2,()->3,(Supplier<Integer>)()->{ throw new RuntimeException();})
				.map(Supplier::get)
				 .forEachEvent( i->list.add(i),e->error=e,()->complete=true);
		
		
		
		assertThat(list,hasItems(1,2,3));
		assertThat(list.size(),equalTo(3));
		
		
		assertThat(error,instanceOf(RuntimeException.class));
		
		assertTrue(complete);
	}
}
