package com.aol.cyclops.lambda.utils;

import static com.aol.cyclops.lambda.utils.Lambda.λ2;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import java.util.function.BiFunction;

import org.junit.Test;
public class ClosedVarTest {

	@Test
	public void inClosure(){
		Mutable<Integer> myInt = new Mutable<>(0);
		
		λ2((Integer i)-> (Integer j)-> myInt.set(i*j)).apply(10).apply(20);
		
		assertThat(myInt.get(),
				is(200));
	}
	@Test
	public void inClosure2(){
		Mutable<Integer> myInt = new Mutable<>(0);
		
		BiFunction<Integer,Integer,Mutable<Integer>> fn = (i,j)-> myInt.set(i*j);
		fn.apply(10,20);
		
		assertThat(myInt.get(),
				is(200));
	}

	@Test
	public void testSet() {
		assertThat(new Mutable().set("hello").get(),is("hello"));
	}

	@Test
	public void testClosedVar() {
		assertThat(new Mutable(10).get(),equalTo(10));
	}
	@Test
	public void testClosedVarEquals() {
		assertThat(new Mutable(10),equalTo(new Mutable(10)));
	}
	@Test
	public void testClosedVarEqualsFalse() {
		assertThat(new Mutable(10),not(equalTo(new Mutable(20))));
	}
	@Test
	public void testClosedVarHashCode() {
		assertThat(new Mutable(10).hashCode(),equalTo(new Mutable(10).hashCode()));
	}
	@Test
	public void testClosedVarHashCodeFalse() {
		assertThat(new Mutable(10).hashCode(),not(equalTo(new Mutable(20).hashCode())));
	}
}
