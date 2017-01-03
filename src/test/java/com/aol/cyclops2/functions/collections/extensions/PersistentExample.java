package com.aol.cyclops2.functions.collections.extensions;

import org.junit.Test;

import cyclops.collections.immutable.PBagX;
import cyclops.collections.immutable.POrderedSetX;
import cyclops.collections.immutable.PQueueX;
import cyclops.collections.immutable.PSetX;
import cyclops.collections.immutable.PStackX;
import cyclops.collections.immutable.PVectorX;

public class PersistentExample {

	@Test
	public void list(){
		PVectorX.of(1,2,3)
			.map(i->i+2)
			.plus(5)
			.map(i->"hello" + i)
			.forEach(System.out::println);
			
	}
	@Test
	public void stack(){
		PStackX.of(1,2,3)
			.map(i->i+2)
			.plus(5)
			.map(i->"hello" + i)
			.forEach(System.out::println);
			
	}
	
	@Test
	public void set(){
		PSetX.of(1,2,3)
			  .map(i->i+2)
		      .plus(5)
		      .map(i->"hello" + i)
		      .forEach(System.out::println);
	}
	@Test
	public void bag(){
		PBagX.of(1,2,3)
			  .map(i->i+2)
		      .plus(5)
		      .map(i->"hello" + i)
		      .forEach(System.out::println);
	}
	@Test
	public void orderedSet(){
		POrderedSetX.of(1,2,3)
			  .map(i->i+2)
		      .plus(5)
		      .map(i->"hello" + i)
		      .forEach(System.out::println);
	}
	@Test
	public void queue(){
		PQueueX.of(1,2,3)
			  .map(i->i+2)
		      .plus(5)
		      .map(i->"hello" + i)
		      .forEach(System.out::println);
	}
}
