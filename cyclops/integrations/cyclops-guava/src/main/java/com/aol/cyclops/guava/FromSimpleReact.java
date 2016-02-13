package com.aol.cyclops.guava;

import com.aol.cyclops.react.stream.traits.LazyFutureStream;
import com.google.common.collect.FluentIterable;

public class FromSimpleReact {
	public static <T> FluentIterable<T> fromSimpleReact(
			LazyFutureStream<T> s) {
		return FluentIterable.from(s);
	}

}
