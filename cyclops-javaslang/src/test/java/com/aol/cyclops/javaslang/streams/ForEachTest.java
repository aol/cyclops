package com.aol.cyclops.javaslang.streams;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import javaslang.collection.Stream;

import org.junit.Before;
import org.junit.Test;
import org.reactivestreams.Subscription;




public class ForEachTest {
	boolean complete =false;
	@Before
	public void setup(){
		error= null;
		complete =false;
	}

	@Test
	public void forEachX(){
		Subscription s = StreamUtils.forEachX(Stream.of(1,2,3), 2, System.out::println);
		System.out.println("first batch");
		s.request(1);
	}
	@Test
	public void forEachXTest(){
		List<Integer> list = new ArrayList<>();
		Subscription s = StreamUtils.forEachX(Stream.of(1,2,3), 2,  i->list.add(i));
		assertThat(list,hasItems(1,2));
		assertThat(list.size(),equalTo(2));
		s.request(1);
		assertThat(list,hasItems(1,2,3));
		assertThat(list.size(),equalTo(3));
	}
	Throwable error;

	
}
