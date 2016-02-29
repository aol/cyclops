package com.aol.cyclops.react.lazy;

import static com.aol.cyclops.types.futurestream.LazyFutureStream.parallel;

import java.util.function.Supplier;

import com.aol.cyclops.control.LazyReact;
import com.aol.cyclops.types.futurestream.LazyFutureStream;

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
								.ofAsync(array);
	}
}
