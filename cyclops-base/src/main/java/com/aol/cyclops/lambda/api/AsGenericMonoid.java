package com.aol.cyclops.lambda.api;

import java.util.Arrays;
import java.util.function.BiFunction;

import lombok.AllArgsConstructor;

import com.aol.cyclops.lambda.monads.Monoid;

public class AsGenericMonoid {
	
	public <A> Monoid<A> asMonoid(Object o){
		return new WrappedMonoid(o);
	}
	@AllArgsConstructor
	public static class WrappedMonoid<A> implements Monoid<A>{
		private final Object o;
		private final InvokeDynamic invokeDynamic = new InvokeDynamic();
		
		public A zero(){
			return (A)invokeDynamic.execute("zero",o).get();
		}
		public BiFunction<A,A,A> combiner(){
			return (a,b) -> (A)invokeDynamic.execute(Arrays.asList("sum","combine"),a,b).get();
		}
		
	}
	
}
