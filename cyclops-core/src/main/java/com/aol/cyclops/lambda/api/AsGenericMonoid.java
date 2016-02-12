package com.aol.cyclops.lambda.api;

import java.util.Arrays;
import java.util.function.BiFunction;

import com.aol.cyclops.Monoid;
import com.aol.cyclops.internal.invokedynamic.InvokeDynamic;

import lombok.AllArgsConstructor;

public class AsGenericMonoid {
	
	/**
	 * Wrap supplied Monoid object in the cylops Monoid interface
	 * 
	 * Will look for sum(a,b) or combine(a,b) methods for combiner
	 * and zero() method for zero
	 * 
	 * @param o Monoid type to wrap
	 * @return Cyclopse Monoid
	 */
	public static <A> Monoid<A> asMonoid(Object o){
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
			return (a,b) -> (A)
					invokeDynamic.execute(Arrays.asList("sum","combine"),o,a,b).get();
		}
		
	}
	
}
