package com.aol.cyclops.lambda.functions;

import static com.aol.cyclops.util.function.Memoize.memoizeBiFunction;
import static com.aol.cyclops.util.function.Memoize.memoizeCallable;
import static com.aol.cyclops.util.function.Memoize.memoizeFunction;
import static com.aol.cyclops.util.function.Memoize.memoizePredicate;
import static com.aol.cyclops.util.function.Memoize.memoizeQuadFunction;
import static com.aol.cyclops.util.function.Memoize.memoizeSupplier;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.concurrent.Callable;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import lombok.val;

import org.junit.Before;
import org.junit.Test;

import com.aol.cyclops.util.function.Memoize;
public class MemoiseTest {

	int called= 0;
	@Before
	public void setup(){
		called = 0;
	}
	@Test
	public void testMemoiseSupplier() {
		
		Supplier<Integer> s = memoizeSupplier(()->++called);
		assertThat(s.get(),equalTo(1));
		assertThat(s.get(),equalTo(1));
		assertThat(s.get(),equalTo(1));
		assertThat(s.get(),equalTo(1));
		
	}

	@Test
	public void testMemoiseCallable() throws Exception {
		Callable<Integer> s = memoizeCallable(()->++called);
		assertThat(s.call(),equalTo(1));
		assertThat(s.call(),equalTo(1));
		assertThat(s.call(),equalTo(1));
		assertThat(s.call(),equalTo(1));
	}

	@Test
	public void testMemoiseFunction() {
		Function<Integer,Integer> s = memoizeFunction( a->a + ++called);
		assertThat(s.apply(0),equalTo(1));
		assertThat(s.apply(0),equalTo(1));
		assertThat(s.apply(0),equalTo(1));
		assertThat(s.apply(1),equalTo(3));
		assertThat(s.apply(1),equalTo(3));
	}

	@Test
	public void testMemoiseBiFunction() {
		BiFunction<Integer,Integer,Integer> s = memoizeBiFunction( (a,b)->a + ++called);
		assertThat(s.apply(0,1),equalTo(1));
		assertThat(s.apply(0,1),equalTo(1));
		assertThat(s.apply(0,1),equalTo(1));
		assertThat(s.apply(1,1),equalTo(3));
		assertThat(s.apply(1,1),equalTo(3));
	}

	@Test
	public void testMemoisePredicate() {
		Predicate<Integer> s = memoizePredicate( a-> a==++called);
		assertThat(s.test(0),equalTo(false));
		assertThat(s.test(0),equalTo(false));
		assertThat(s.test(2),equalTo(true));
		assertThat(s.test(2),equalTo(true));
		
	}
	@Test
	public void testMemoiseTriFunction(){
		val cached = Memoize.memoizeTriFunction(this::mult);
		
		assertThat(cached.apply(1,2,3),equalTo(6));
		assertThat(cached.apply(1,2,3),equalTo(6));
		assertThat(cached.apply(1,2,3),equalTo(6));
		assertThat(called,equalTo(1));
	}
	
	private int mult(int a,int b,int c){
		called++;
		return a*b*c;
	}
	@Test
	public void testMemoiseQuadFunction(){
		val cached = memoizeQuadFunction(this::addAll);
		
		assertThat(cached.apply(1,2,3,4),equalTo(10));
		assertThat(cached.apply(1,2,3,4),equalTo(10));
		assertThat(cached.apply(1,2,3,4),equalTo(10));
		assertThat(called,equalTo(1));
	}
	
	private int addAll(int a,int b,int c, int d){
		called++;
		return a+b+c+d;
	}

}
