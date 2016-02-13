package com.aol.cyclops.javaslang;

import com.aol.simple.react.stream.traits.LazyFutureStream;

public class FromSimpleReact {
	public static <T> javaslang.collection.LazyStream<T> fromSimpleReact(
			LazyFutureStream<T> s) {
		return javaslang.collection.LazyStream.ofAll(()->s.iterator());
	}
	

}
