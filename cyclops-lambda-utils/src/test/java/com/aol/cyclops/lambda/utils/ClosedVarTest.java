package com.aol.cyclops.lambda.utils;

import static com.aol.cyclops.comprehensions.functions.Lambda.λ2;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import java.util.function.BiFunction;

import org.junit.Test;
public class ClosedVarTest {

	@Test
	public void inClosure(){
		ClosedVar<Integer> myInt = new ClosedVar<>(0);
		
		λ2((Integer i)-> (Integer j)-> myInt.set(i*j)).apply(10).apply(20);
		
		assertThat(myInt.get(),
				is(200));
	}
	@Test
	public void inClosure2(){
		ClosedVar<Integer> myInt = new ClosedVar<>(0);
		
		BiFunction<Integer,Integer,ClosedVar<Integer>> fn = (i,j)-> myInt.set(i*j);
		fn.apply(10,20);
		
		assertThat(myInt.get(),
				is(200));
	}

	@Test
	public void testSet() {
		assertThat(new ClosedVar().set("hello").get(),is("hello"));
	}

	@Test
	public void testClosedVar() {
		assertThat(new ClosedVar(10).get(),equalTo(10));
	}
	@Test
	public void testClosedVarEquals() {
		assertThat(new ClosedVar(10),equalTo(new ClosedVar(10)));
	}
	@Test
	public void testClosedVarEqualsFalse() {
		assertThat(new ClosedVar(10),not(equalTo(new ClosedVar(20))));
	}
	@Test
	public void testClosedVarHashCode() {
		assertThat(new ClosedVar(10).hashCode(),equalTo(new ClosedVar(10).hashCode()));
	}
	@Test
	public void testClosedVarHashCodeFalse() {
		assertThat(new ClosedVar(10).hashCode(),not(equalTo(new ClosedVar(20).hashCode())));
	}
}
