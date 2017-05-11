package com.aol.cyclops2.streams.future;

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
import org.junit.Test;

import cyclops.stream.ReactiveSeq;
import com.aol.cyclops2.types.stream.reactive.ReactiveTask;
import org.reactivestreams.Subscription;

public class ForEachSequenceMFutureTest {
	Executor exec = Executors.newFixedThreadPool(1);
	volatile boolean complete =false;
	@Before
	public void setup(){

		complete =false;
	}

	@Test
	public void forEachX(){
		Subscription s = ReactiveSeq.of(1,2,3)
                                    .foldFuture(t->t.forEach(2,System.out::println),exec)
                                    .get();

		
		System.out.println("first batch");
		s.request(1);

	}
	@Test
	public void forEachXTest(){
		List<Integer> list = new ArrayList<>();
		Subscription s = ReactiveSeq.of(1,2,3)
                                    .foldFuture(t->t.forEach(2, i->list.add(i)),exec).get();

		while(list.size()!=2){

        }
		assertThat(list,hasItems(1,2));
		assertThat(list.size(),equalTo(2));
		s.request(1);
		
		assertThat(list,hasItems(1,2,3));
		assertThat(list.size(),equalTo(3));
	}
	

	
}
