package com.aol.cyclops.lambda.monads;

import java.util.function.Function;

import lombok.AllArgsConstructor;
import lombok.experimental.Delegate;



@AllArgsConstructor
public class SimplexImpl<X> implements Simplex<X>{
	
		@Delegate(excludes=FlatMap.class)
		private final Monad<Object,X> m ;//= (Monad)Monad.this;

		public  <R1, NT> Monad<R1, NT> flatMap(Function<X, R1> fn) {
			return m.flatMap(fn);
		}
		
		public <R> R monad(){
			return (R)unwrap();
		}
		static interface FlatMap<U>{
			public  <R1, NT> Monad<R1, NT> flatMap(Function<U, R1> fn);
		}
}
