package com.aol.cyclops.streams.reactivestreams;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.junit.Before;
import org.junit.Test;
import org.reactivestreams.Subscription;

import com.aol.cyclops.sequence.SequenceM;

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
	public void forEachWithErrors2(){
		error = null;
		List<Integer> result = new ArrayList<>();
		SequenceM.of(1,2,3,4,5,6)
				.map(this::errors)
				.forEachWithError(e->result.add(e),e->error=e);
		
		assertNotNull(error);
		assertThat(result,hasItems(1,3,4,5,6));
		assertThat(result,not(hasItems(2)));
	}
	
	@Test
	public void forEachWithEvents2(){
		error = null;
		 complete = false;
		List<Integer> result = new ArrayList<>();
		SequenceM.of(1,2,3,4,5,6)
				.map(this::errors)
				.forEachEvent(e->result.add(e),e->error=e,()->complete=true);
		
		assertNotNull(error);
		assertThat(result,hasItems(1,3,4,5,6));
		assertThat(result,not(hasItems(2)));
	}
	public Integer errors(Integer ints){
		if(ints ==2)
			throw new RuntimeException();
		return ints;
	}
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
