package com.aol.cyclops2.streams.streamable;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import cyclops.stream.Streamable;

public class ReverseTest {
	@Test
	public void limitRange() throws InterruptedException{
		
		assertThat(Streamable.range(0,Integer.MAX_VALUE)
				 .limit(100)
				 .count(),equalTo(100L));
	}
	@Test
	public void limitList() throws InterruptedException{
		
		List<Integer> list= new ArrayList<>();
		for(int i=0;i<1000;i++)
			list.add(i);
		assertThat(Streamable.fromList(list)
				 .limit(100)
				 .count(),equalTo(100L));
		
	}
	@Test
	public void limitArray() throws InterruptedException{
		
		List<Integer> list= new ArrayList<>();
		for(int i=0;i<1000;i++)
			list.add(i);
		assertThat(Streamable.of(list.toArray())
				 .limit(100)
				 .count(),equalTo(100L));
		
	}
	@Test
	public void skipArray() throws InterruptedException{
		
		List<Integer> list= new ArrayList<>();
		for(int i=0;i<1000;i++)
			list.add(i);
		assertThat(Streamable.of(list.toArray())
				 .skip(100)
				 .count(),equalTo(900L));
		
	}
	@Test
	public void skipRange() throws InterruptedException{
		
		assertThat(Streamable.range(0,1000)
				 .skip(100)
				 .count(),equalTo(900L));
	}
	@Test
	public void skipRangeReversed() throws InterruptedException{
		
		assertThat(Streamable.range(0,1000)
				 .skip(100).reverse()
				 .count(),equalTo(900L));
	}
	@Test
	public void skipList() throws InterruptedException{
		
		List<Integer> list= new ArrayList<>();
		for(int i=0;i<1000;i++)
			list.add(i);
		assertThat(Streamable.fromList(list)
				 .skip(100)
				 .count(),equalTo(900L));
		
	}
	@Test
	public void reversedOfArray() throws InterruptedException{
		List<Integer> list= new ArrayList<>();
		list.add(1);
		list.add(2);
		
		assertThat(Streamable.reversedOf(1,2)
							.toList(),
							equalTo(Arrays.asList(2,1)));
		
	}
	@Test
	public void reversedOfList() throws InterruptedException{
		List<Integer> list= new ArrayList<>();
		list.add(1);
		list.add(2);
		
		assertThat(Streamable.reversedListOf(list)
							.toList(),
							equalTo(Arrays.asList(2,1)));
		
	}
	
}
