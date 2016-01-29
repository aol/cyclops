package com.aol.cyclops.functions.collections.extensions;

import org.junit.Test;

import com.aol.cyclops.collections.extensions.standard.DequeX;
import com.aol.cyclops.collections.extensions.standard.ListX;
import com.aol.cyclops.collections.extensions.standard.QueueX;
import com.aol.cyclops.collections.extensions.standard.SetX;
import com.aol.cyclops.collections.extensions.standard.SortedSetX;

public class ExampleTest {

	@Test
	public void list(){
		ListX.of(1,2,3)
			.map(i->i+2)
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
