package com.aol.cyclops.javaslang;

import com.aol.simple.react.stream.traits.FutureStream;

public class FromSimpleReact {
	public static <T> javaslang.collection.Stream<T> fromSimpleReact(
			FutureStream<T> s) {
		return javaslang.collection.Stream.ofAll(s.iterator());
	}

}
