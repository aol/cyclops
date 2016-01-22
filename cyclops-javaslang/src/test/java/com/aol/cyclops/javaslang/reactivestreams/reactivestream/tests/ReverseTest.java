package com.aol.cyclops.javaslang.reactivestreams.reactivestream.tests;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.aol.cyclops.javaslang.reactivestreams.ReactiveStream;
import com.aol.cyclops.sequence.SequenceM;

public class ReverseTest {
	@Test
	public void limitRange() throws InterruptedException{
		
		assertThat(ReactiveStream.range(0,Integer.MAX_VALUE)
				 .take(100)
				 .length(),equalTo(100));
	}
	@Test
	public void limitList() throws InterruptedException{
		
		List<Integer> list= new ArrayList<>();
		for(int i=0;i<1000;i++)
			list.add(i);
		assertThat(ReactiveStream.fromIterable(list)
				 .take(100)
				 .length(),equalTo(100));
		
	}
	@Test
	public void limitArray() throws InterruptedException{
		
		List<Integer> list= new ArrayList<>();
		for(int i=0;i<1000;i++)
			list.add(i);
		assertThat(ReactiveStream.of(list.toArray())
				 .take(100)
				 .length(),equalTo(100));
		
	}
	@Test
	public void skipArray() throws InterruptedException{
		
		List<Integer> list= new ArrayList<>();
		for(int i=0;i<1000;i++)
			list.add(i);
		assertThat(ReactiveStream.of(list.toArray())
				 .drop(100)
				 .length(),equalTo(900));
		
	}
	@Test
	public void skipRange() throws InterruptedException{
		
		assertThat(ReactiveStream.range(0,1000)
				 .drop(100)
				 .length(),equalTo(900));
	}
	@Test
	public void skipRangeReversed() throws InterruptedException{
		
		assertThat(ReactiveStream.range(0,1000)
				 .drop(100).reverse()
				 .length(),equalTo(900));
	}
	@Test
	public void skipList() throws InterruptedException{
		
		List<Integer> list= new ArrayList<>();
		for(int i=0;i<1000;i++)
			list.add(i);
		assertThat(ReactiveStream.fromIterable(list)
				 .drop(100)
				 .length(),equalTo(900));
		
	}
	
	
	
}
