package com.aol.cyclops.javaslang.forcomprehensions;


import static com.aol.cyclops.lambda.api.AsAnyM.notTypeSafeAnyM;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.function.BiFunction;

import org.junit.Test;

import com.aol.cyclops.monad.AnyM;

import fj.data.Option;

public class DoFJOptionTest {
	@Test
	public void optionTest(){
		AnyM<Integer> one = notTypeSafeAnyM(Option.some(1));
		AnyM<Integer> empty = notTypeSafeAnyM(Option.none());
		BiFunction<Integer,Integer,Integer> f2 = (a,b) -> a *b; 
		
		Option result =  Do.add(one)
							.add(empty)
							.yield(  a -> b -> f2.apply(a,b)).unwrap();
		
		System.out.println(result);
		assertTrue(result.isNone());

	}
	@Test
	public void optionTestWith(){
		AnyM<Integer> one = notTypeSafeAnyM(Option.some(1));
		AnyM<Integer> empty = notTypeSafeAnyM(Option.none());
		BiFunction<Integer,Integer,Integer> f2 = (a,b) -> a *b; 
		
		Option result =  Do.add(one)
							.withAnyM( i-> empty)
							.yield(  a -> b -> f2.apply(a,b)).unwrap();
		
		System.out.println(result);
		assertTrue(result.isNone());

	}
	@Test
	public void optionPositiveTest(){
		AnyM<Integer> one = notTypeSafeAnyM(Option.some(1));
		AnyM<Integer> empty = notTypeSafeAnyM(Option.some(3));
		BiFunction<Integer,Integer,Integer> f2 = (a,b) -> a *b; 
		
		Option result =  Do.add(one)
							.add(empty)
							.yield(  a -> b -> f2.apply(a,b)).unwrap();
		
		System.out.println(result);
		assertEquals(result.some(),3);

	}
	
}
