package com.aol.cyclops.closures.mutable;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import java.util.function.*;
import java.util.stream.Stream;

import org.junit.Test;
public class MutableIntTest {

	@Test
	public void testMutate(){
		MutableInt num = MutableInt.of(20);
		    
		Stream.of(1,2,3,4).map(i->i*10).peek(i-> num.mutate(n->n+i)).forEach(System.out::println);
		    
		assertThat(num.getAsInt(),is(120));
	}
	@Test
	public void inClosure(){
		MutableInt myInt = new MutableInt(0);
		
	  Function<Integer,Function<Integer,MutableInt>> fn = ((Integer i)-> (Integer j)-> myInt.set(i*j));
	  fn.apply(10).apply(20);
		
		assertThat(myInt.getAsInt(),
				is(200));
	}
	@Test
	public void inClosure2(){
		MutableInt myInt = new MutableInt(0);
		
		BiFunction<Integer,Integer,MutableInt> fn = (i,j)-> myInt.set(i*j);
		fn.apply(10,20);
		
		assertThat(myInt.getAsInt(),
				is(200));
	}

	@Test
	public void testSet() {
		assertThat(new MutableInt().set(1000).getAsInt(),is(1000));
	}

	@Test
	public void testClosedVar() {
		assertThat(new MutableInt(10).getAsInt(),equalTo(10));
	}
	@Test
	public void testClosedVarEquals() {
		assertThat(new MutableInt(10),equalTo(new MutableInt(10)));
	}
	@Test
	public void testClosedVarEqualsFalse() {
		assertThat(new MutableInt(10),not(equalTo(new MutableInt(20))));
	}
	@Test
	public void testClosedVarHashCode() {
		assertThat(new MutableInt(10).hashCode(),equalTo(new MutableInt(10).hashCode()));
	}
	@Test
	public void testClosedVarHashCodeFalse() {
		assertThat(new MutableInt(10).hashCode(),not(equalTo(new MutableInt(20).hashCode())));
	}
}
