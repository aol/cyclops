package com.aol.cyclops.trampoline;

import java.util.stream.Stream;

/**
 * simple Trampoline implementation : inspired by excellent TotallyLazy Java 8 impl 
 * and Mario Fusco presentation
 * 
 * @author johnmcclean
 *
 * @param <T> Return type
 */
public interface Trampoline<T> {

	/**
	 * @return next stage in Trampolining
	 */
	default Trampoline<T> bounce(){
		return this;
	}

	/**
	 * @return The result of Trampoline execution
	 */
	T result();
	
	/**
	 * @return true if complete
	 * 
	 */
	default boolean complete() {
		return true;
	}

	
	/**
	 * Created a completed Trampoline
	 * 
	 * @param result Completed result
	 * @return Completed Trampoline
	 */
	public static <T> Trampoline<T> done(T result) {
		return () -> result;
	}

	/**
	 * Create a Trampoline that has more work to do
	 * 
	 * @param trampoline Next stage in Trampoline
	 * @return Trampoline with more work
	 */
	public static <T> Trampoline<T> more(Trampoline<Trampoline<T>> trampoline) {
		return new Trampoline<T>() {
			
			@Override
			public boolean complete() {
				return false;
			}

			@Override
			public Trampoline<T> bounce() {
				return trampoline.result();
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
