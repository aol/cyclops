package com.aol.cyclops.functionaljava;

import com.aol.simple.react.stream.traits.FutureStream;

public class FromSimpleReact {
	public static <T> fj.data.Stream<T> fromSimpleReact(
			FutureStream<T> s) {
		return fj.data.Stream.stream(s.iterator());
	}

}
