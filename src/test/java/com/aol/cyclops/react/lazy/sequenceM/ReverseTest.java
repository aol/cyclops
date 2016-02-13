package com.aol.cyclops.react.lazy.sequenceM;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.aol.cyclops.control.LazyReact;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.react.stream.traits.LazyFutureStream;

public class ReverseTest {
	@Test
	public void limitRange() throws InterruptedException{
		
		assertThat(new LazyReact().range(0,Integer.MAX_VALUE)
				 .limit(100)
				 .count(),equalTo(100L));
	}
	@Test
	public void limitList() throws InterruptedException{
		
		List<Integer> list= new ArrayList<>();
		for(int i=0;i<1000;i++)
			list.add(i);
		assertThat(new LazyReact().fromIterable(list)
				 .limit(100)
				 .peek(System.out::println)
				 .count(),equalTo(100L));
		
	}
	@Test
	public void limitArray() throws InterruptedException{
		
		List<Integer> list= new ArrayList<>();
		for(int i=0;i<1000;i++)
			list.add(i);
		assertThat(LazyFutureStream.of(list.toArray())
				 .limit(100)
				 .count(),equalTo(100L));
		
	}
	@Test
	public void skipArray() throws InterruptedException{
		
		List<Integer> list= new ArrayList<>();
		for(int i=0;i<1000;i++)
			list.add(i);
		assertThat(LazyFutureStream.of(list.toArray())
				 .skip(100)
				 .count(),equalTo(900L));
		
	}
	@Test
	public void skipRange() throws InterruptedException{
		
		assertThat(new LazyReact().range(0,1000)
				 .skip(100)
				 .count(),equalTo(900L));
	}
	@Test
	public void skipList() throws InterruptedException{
		
		List<Integer> list= new ArrayList<>();
		for(int i=0;i<1000;i++)
			list.add(i);
		assertThat(new LazyReact().fromIterable(list)
				 .skip(100)
				 .count(),equalTo(900L));
		
	}
	
	
}
