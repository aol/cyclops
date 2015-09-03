package com.aol.cyclops.closures.mutable;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import java.util.function.*;
import java.util.stream.Stream;

import org.junit.Test;
public class MutableLongTest {

	@Test
	public void testMutate(){
		MutableLong num = MutableLong.of(20);
		    
		Stream.of(1,2,3,4).map(i->i*10).peek(i-> num.mutate(n->n+i)).forEach(System.out::println);
		    
		assertThat(num.getAsLong(),is(120));
	}
	@Test
	public void inClosure(){
		MutableLong myInt = new MutableLong(0);
		
	  Function<Integer,Function<Integer,MutableLong>> fn = ((Integer i)-> (Integer j)-> myInt.set(i*j));
	  fn.apply(10).apply(20);
		
		assertThat(myInt.getAsLong(),
				is(200));
	}
	@Test
	public void inClosure2(){
		MutableLong myInt = new MutableLong(0);
		
		BiFunction<Integer,Integer,MutableLong> fn = (i,j)-> myInt.set(i*j);
		fn.apply(10,20);
		
		assertThat(myInt.getAsLong(),
				is(200));
	}

	@Test
	public void testSet() {
		assertThat(new MutableLong().set(1000).getAsLong(),is(1000));
	}

	@Test
	public void testClosedVar() {
		assertThat(new MutableLong(10).getAsLong(),equalTo(10));
	}
	@Test
	public void testClosedVarEquals() {
		assertThat(new MutableLong(10),equalTo(new MutableLong(10)));
	}
	@Test
	public void testClosedVarEqualsFalse() {
		assertThat(new MutableLong(10),not(equalTo(new MutableLong(20))));
	}
	@Test
	public void testClosedVarHashCode() {
		assertThat(new MutableLong(10).hashCode(),equalTo(new MutableLong(10).hashCode()));
	}
	@Test
	public void testClosedVarHashCodeFalse() {
		assertThat(new MutableLong(10).hashCode(),not(equalTo(new MutableLong(20).hashCode())));
	}
}
