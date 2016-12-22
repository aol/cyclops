package com.aol.cyclops.functions.collections.extensions;

import java.util.Arrays;

import org.junit.Test;

import cyclops.CyclopsCollectors;
import cyclops.collections.DequeX;
import cyclops.collections.ListX;
import cyclops.collections.QueueX;
import cyclops.collections.SetX;
import cyclops.collections.SortedSetX;

public class ExampleTest {

	@Test(expected=UnsupportedOperationException.class)
	public void immutable(){
	    
		 ListX.fromIterable(ListX.immutableCollector(), Arrays.asList(1,2,3,4))
		      .plus(5);
	}
	@Test
	public void list(){
		
			 
	   ListX.of(1,2,3)
			.map(i->i+2)
			.plus(5)
			.map(i->"hello" + i)
			.forEach(System.out::println);			
	}
	@Test
	public void listToSetX(){
		SetX<String> set = ListX.of(1,2,3)
								.flatMap(i->Arrays.asList(i+2,10))
								 .plus(5)
								 .map(i->"hello" + i).toSetX()
								 .collect(CyclopsCollectors.toSetX());
		
		set.printOut();
					
	}
	@Test
	public void listFlatMap(){
		ListX.of(1,2,3)
			.flatMap(i->Arrays.asList(i+2,10))
			.plus(5)
			.map(i->"hello" + i)
			.forEach(System.out::println);
			
	}
	@Test
	public void deque(){
		DequeX.of(1,2,3)
			.map(i->i+2)
			.plus(5)
			.map(i->"hello" + i)
			.forEach(System.out::println);
			
	}
	
	@Test
	public void set(){
		SetX.of(1,2,3)
			  .map(i->i+2)
		      .plus(5)
		      .map(i->"hello" + i)
		      .forEach(System.out::println);
	}
	@Test
	public void sortedSet(){
		SortedSetX.of(1,2,3)
			  .map(i->i+2)
		      .plus(5)
		      .map(i->"hello" + i)
		      .forEach(System.out::println);
	}
	@Test
	public void queue(){
		QueueX.of(1,2,3)
			  .map(i->i+2)
		      .plus(5)
		      .map(i->"hello" + i)
		      .forEach(System.out::println);
	}
}
