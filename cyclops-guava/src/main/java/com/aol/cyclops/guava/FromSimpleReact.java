package com.aol.cyclops.guava;

import com.aol.simple.react.stream.traits.FutureStream;
import com.google.common.collect.FluentIterable;

public class FromSimpleReact {
	public static <T> FluentIterable<T> fromSimpleReact(
			FutureStream<T> s) {
		return FluentIterable.from(s);
	}

}
