package com.aol.cyclops.closures.mutable;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import java.util.function.*;
import java.util.stream.Stream;

import org.junit.Test;
public class MutableDoubleTest {

	@Test
	public void testMutate(){
		MutableDouble num = MutableDouble.of(20);
		    
		Stream.of(1,2,3,4).map(i->i*10).peek(i-> num.mutate(n->n+i)).forEach(System.out::println);
		    
		assertThat(num.getAsDouble(),is(120.0));
	}
	@Test
	public void inClosure(){
		MutableDouble myInt = new MutableDouble(0);
		
	  Function<Integer,Function<Integer,MutableDouble>> fn = ((Integer i)-> (Integer j)-> myInt.set(i*j));
	  fn.apply(10).apply(20);
		
		assertThat(myInt.getAsDouble(),
				is(200.0));
	}
	@Test
	public void inClosure2(){
		MutableDouble myInt = new MutableDouble(0);
		
		BiFunction<Integer,Integer,MutableDouble> fn = (i,j)-> myInt.set(i*j);
		fn.apply(10,20);
		
		assertThat(myInt.getAsDouble(),
				is(200.0));
	}

	@Test
	public void testSet() {
		assertThat(new MutableDouble().set(1000).getAsDouble(),is(1000.0));
	}

	@Test
	public void testClosedVar() {
		assertThat(new MutableDouble(10).getAsDouble(),equalTo(10.0));
	}
	@Test
	public void testClosedVarEquals() {
		assertThat(new MutableDouble(10),equalTo(new MutableDouble(10)));
	}
	@Test
	public void testClosedVarEqualsFalse() {
		assertThat(new MutableDouble(10),not(equalTo(new MutableDouble(20))));
	}
	@Test
	public void testClosedVarHashCode() {
		assertThat(new MutableDouble(10).hashCode(),equalTo(new MutableDouble(10).hashCode()));
	}
	@Test
	public void testClosedVarHashCodeFalse() {
		assertThat(new MutableDouble(10).hashCode(),not(equalTo(new MutableDouble(20).hashCode())));
	}
}
