package com.aol.cyclops.javaslang;

import com.aol.simple.react.stream.traits.LazyFutureStream;

public class FromSimpleReact {
	public static <T> javaslang.collection.Stream<T> fromSimpleReact(
			LazyFutureStream<T> s) {
		return javaslang.collection.Stream.ofAll(()->s.iterator());
	}
	

}
