package com.aol.cyclops.lambda.functions;

import static com.aol.cyclops.functions.Memoise.*;
import static org.junit.Assert.fail;

import java.util.concurrent.Callable;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
public class MemoiseTest {

	int called= 0;
	@Before
	public void setup(){
		called = 0;
	}
	@Test
	public void testMemoiseSupplier() {
		Supplier<Integer> s = memoiseSupplier(()->++called);
		assertThat(s.get(),equalTo(1));
		assertThat(s.get(),equalTo(1));
		assertThat(s.get(),equalTo(1));
		assertThat(s.get(),equalTo(1));
	}

	@Test
	public void testMemoiseCallable() throws Exception {
		Callable<Integer> s = memoiseCallable(()->++called);
		assertThat(s.call(),equalTo(1));
		assertThat(s.call(),equalTo(1));
		assertThat(s.call(),equalTo(1));
		assertThat(s.call(),equalTo(1));
	}

	@Test
	public void testMemoiseFunction() {
		Function<Integer,Integer> s = memoiseFunction( a->a + ++called);
		assertThat(s.apply(0),equalTo(1));
		assertThat(s.apply(0),equalTo(1));
		assertThat(s.apply(0),equalTo(1));
		assertThat(s.apply(1),equalTo(3));
		assertThat(s.apply(1),equalTo(3));
	}

	@Test
	public void testMemoiseBiFunction() {
		BiFunction<Integer,Integer,Integer> s = memoiseBiFunction( (a,b)->a + ++called);
		assertThat(s.apply(0,1),equalTo(1));
		assertThat(s.apply(0,1),equalTo(1));
		assertThat(s.apply(0,1),equalTo(1));
		assertThat(s.apply(1,1),equalTo(3));
		assertThat(s.apply(1,1),equalTo(3));
	}

	@Test
	public void testMemoisePredicate() {
		Predicate<Integer> s = memoisePredicate( a-> a==++called);
		assertThat(s.test(0),equalTo(false));
		assertThat(s.test(0),equalTo(false));
		assertThat(s.test(2),equalTo(true));
		assertThat(s.test(2),equalTo(true));
		
	}

}
