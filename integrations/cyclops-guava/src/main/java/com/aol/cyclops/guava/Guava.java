
package com.aol.cyclops.guava;

import com.aol.cyclops.monad.AnyM;
import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;

public class Guava {
	/**
	 * <pre>
	 * {@code
	 * Guava.anyM(Optional.of("hello world"))
				.map(String::toUpperCase)
				.toSequence()
				.toList()
	 * }
	 * //[HELLO WORLD]
	 * </pre>
	 * 
	 * @param optionM to construct AnyM from
	 * @return AnyM
	 */
	public static <T> AnyM<T> anyM(Optional<T> optionM){
		return  AnyM.ofMonad(optionM);
	}
	/**
	 * <pre>
	 * {@code
	 * Guava.anyM(FluentIterable.of(new String[]{"hello world"}))
				.map(String::toUpperCase)
				.flatMap(i->AnyMonads.anyM(java.util.stream.Stream.of(i)))
				.toSequence()
				.toList()
	 * }
	 *  //[HELLO WORLD]
	 * </pre>
	 * 
	 * @param streamM to construct AnyM from
	 * @return AnyM
	 */
	public static <T> AnyM<T> anyM(FluentIterable<T> streamM){
		return  AnyM.ofMonad(streamM);
	}
}
