package com.aol.cyclops.comprehensions.donotation.typed;

//import fj.data.Option;

public class DoFJOptionTest {
/**
	@Test
	public void optionTest(){
		AnyM<Integer> one = AnyM.ofMonad(Option.some(1));
		AnyM<Integer> empty = AnyM.ofMonad(Option.none());
		BiFunction<Integer,Integer,Integer> f2 = (a,b) -> a *b; 
		
		Option result =  Do.add(one)
							.add(empty)
							.yield(  a -> b -> f2.apply(a,b)).unwrap();
		
		System.out.println(result);
		assertTrue(result.isNone());

	}
	@Test
	public void optionTestWith(){
		AnyM<Integer> one = AnyM.ofMonad(Option.some(1));
		AnyM<Integer> empty = AnyM.ofMonad(Option.none());
		BiFunction<Integer,Integer,Integer> f2 = (a,b) -> a *b; 
		
		Option result =  Do.add(one)
							.withAnyM( i-> empty)
							.yield(  a -> b -> f2.apply(a,b)).unwrap();
		
		System.out.println(result);
		assertTrue(result.isNone());

	}
	@Test
	public void optionPositiveTest(){
		AnyM<Integer> one = AnyM.ofMonad(Option.some(1));
		AnyM<Integer> empty = AnyM.ofMonad(Option.some(3));
		BiFunction<Integer,Integer,Integer> f2 = (a,b) -> a *b; 
		
		Option result =  Do.add(one)
							.add(empty)
							.yield(  a -> b -> f2.apply(a,b)).unwrap();
		
		System.out.println(result);
		assertEquals(result.some(),3);

	}
	**/
}
