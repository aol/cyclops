package com.aol.cyclops.lambda.api;

import java.util.stream.Stream;

/**
 * simple Trampoline implementation : inspired by excellent TotallyLazy Java 8 impl 
 * and Mario Fusco presentation
 * 
 * @author johnmcclean
 *
 * @param <T>
 */
public interface Trampoline<T> {

	/**
	 * @return next stage in Trampolining
	 */
	default Trampoline<T> bounce(){
		return this;
	}

	T result();
	
	default boolean complete() {
		return true;
	}

	
	public static <T> Trampoline<T> done(T result) {
		return () -> result;
	}

	public static <T> Trampoline<T> more(Trampoline<Trampoline<T>> next) {
		return new Trampoline<T>() {
			
			@Override
			public boolean complete() {
				return false;
			}

			@Override
			public Trampoline<T> bounce() {
				return next.result();
			}

			public T result() {
				return trampoline(this);
			}

			T trampoline(Trampoline<T> trampoline) {
				
				return Stream.iterate(trampoline,Trampoline::bounce)
							.filter(Trampoline::complete)
							.findFirst()
							.get()
							.result();
				

			}
		};
	}
}
