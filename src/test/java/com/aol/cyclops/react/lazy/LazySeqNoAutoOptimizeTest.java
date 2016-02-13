package com.aol.cyclops.react.lazy;

import static com.aol.cyclops.react.stream.traits.LazyFutureStream.parallel;

import java.util.function.Supplier;

import com.aol.cyclops.react.stream.lazy.LazyReact;
import com.aol.cyclops.react.stream.traits.LazyFutureStream;

public class LazySeqNoAutoOptimizeTest extends LazySeqTest {
	@Override
	protected <U> LazyFutureStream<U> of(U... array) {
		return new LazyReact()
							.of(array);
	}
	@Override
	protected <U> LazyFutureStream<U> ofThread(U... array) {
		return new LazyReact()
							.of(array);
	}

	@Override
	protected <U> LazyFutureStream<U> react(Supplier<U>... array) {
		return new LazyReact()
								.react(array);
	}
}
