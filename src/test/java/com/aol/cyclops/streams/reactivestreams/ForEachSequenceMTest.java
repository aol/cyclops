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

import cyclops.stream.ReactiveSeq;

public class ForEachSequenceMTest {
	boolean complete =false;
	@Before
	public void setup(){
		error= null;
		complete =false;
	}
	volatile int times = 0;
	@Test
	public void emptyOnComplete(){
	    ReactiveSeq.empty().forEach(e->{}, e->{}, ()->complete=true);
	    assertTrue(complete);
	}
	@Test
	public void onComplete(){
        ReactiveSeq.of(1, 2, 3, 4, 5, 6, 7, 8, 9)
                    .map(this::load)
                    .forEach(System.out::println,
                aEx -> System.err.println(aEx + ":" + aEx.getMessage()), () -> {
                    times++;
                    System.out.println("Over");
                });
        assertThat(times,equalTo(1));
	}
	@Test
    public void onCompleteXEvents(){
        ReactiveSeq.of(1, 2, 3, 4, 5, 6, 7, 8, 9)
                    .map(this::load)
                   .forEach(Long.MAX_VALUE,System.out::println,
                aEx -> System.err.println(aEx + ":" + aEx.getMessage()), () -> {
                    times++;
                    System.out.println("Over");
                });
        assertThat(times,equalTo(1));
    }
	
	//this::load is simple
	String load(int i){
        if (i == 2) {
            throw new RuntimeException("test exception:" + i);
        } else {
            return "xx" + i;
        }
	}

	@Test
	public void forEachX(){
		Subscription s = ReactiveSeq.of(1,2,3).forEach( 2, System.out::println);
		s.request(1);
	}
	@Test
	public void forEachXTest(){
		List<Integer> list = new ArrayList<>();
		Subscription s = ReactiveSeq.of(1,2,3).forEach( 2, i->list.add(i));
		assertThat(list,hasItems(1,2));
		assertThat(list.size(),equalTo(2));
		s.request(1);
		assertThat(list,hasItems(1,2,3));
		assertThat(list.size(),equalTo(3));
	}
	@Test
	public void forEachXTestIsComplete(){
		List<Integer> list = new ArrayList<>();
		Subscription s = ReactiveSeq.of(1,2,3).forEach( 2, i->list.add(i));
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
		ReactiveSeq.of(1,2,3,4,5,6)
				.map(this::errors)
				.forEach(e->result.add(e), e->error=e);
		
		assertNotNull(error);
		System.out.println(result);
		assertThat(result,hasItems(1,3,4,5,6));
		assertThat(result,not(hasItems(2)));
	}
	
	@Test
	public void forEachWithEvents2(){
		error = null;
		 complete = false;
		List<Integer> result = new ArrayList<>();
		ReactiveSeq.of(1,2,3,4,5,6)
				.map(this::errors)
				.forEach(e->result.add(e), e->error=e,()->complete=true);
		
		assertNotNull(error);
		assertThat(result,hasItems(1,3,4,5,6));
		assertThat(result,not(hasItems(2)));
	}
	public Integer errors(Integer ints){
		if(ints ==2)
			throw new RuntimeException();
		return ints;
	}
	private String process(int process){
	    return "processed " + process;
	}
	@Test
	public void forEachXWithErrorExample(){
	    
	   
	    Subscription s = ReactiveSeq.of(10,20,30)
                                    .map(this::process)
                                    .forEach( 2,System.out::println, System.err::println);
        
        System.out.println("Completed 2");
        s.request(1);
        System.out.println("Finished all!");
        
	}
	@Test
	public void forEachXWithErrors(){
	    
	    
	
		List<Integer> list = new ArrayList<>();
		
		Subscription s = ReactiveSeq.of(()->1,()->2,()->3,(Supplier<Integer>)()->{ throw new RuntimeException();})
							.map(Supplier::get)
							.forEach( 2, i->list.add(i),
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
		
		Subscription s = ReactiveSeq.of(()->1,()->2,()->3,(Supplier<Integer>)()->{ throw new RuntimeException();}).map(Supplier::get)
						.forEach( 2, i->list.add(i),
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
		ReactiveSeq.of(()->1,()->2,()->3,(Supplier<Integer>)()->{ throw new RuntimeException();}).map(Supplier::get)
							.forEach(i->list.add(i),
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
		ReactiveSeq.of(()->1,()->2,()->3,(Supplier<Integer>)()->{ throw new RuntimeException();})
				.map(Supplier::get)
				 .forEach(i->list.add(i), e->error=e,()->complete=true);
		
		
		
		assertThat(list,hasItems(1,2,3));
		assertThat(list.size(),equalTo(3));
		
		
		assertThat(error,instanceOf(RuntimeException.class));
		
		assertTrue(complete);
	}
}
