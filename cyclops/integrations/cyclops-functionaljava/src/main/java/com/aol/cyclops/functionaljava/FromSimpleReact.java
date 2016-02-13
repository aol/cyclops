package com.aol.cyclops.functionaljava;

import com.aol.cyclops.react.stream.traits.LazyFutureStream;

public class FromSimpleReact {
	public static <T> fj.data.Stream<T> fromSimpleReact(
			LazyFutureStream<T> s) {
		return fj.data.Stream.stream(s.iterator());
	}

}
