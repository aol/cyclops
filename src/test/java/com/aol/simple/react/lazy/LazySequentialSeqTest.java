package com.aol.simple.react.lazy;

import com.aol.simple.react.base.BaseSequentialSeqTest;
import com.aol.simple.react.stream.lazy.LazyFutureStream;
import com.aol.simple.react.stream.traits.FutureStream;

public class LazySequentialSeqTest extends BaseSequentialSeqTest {

	@Override
	protected <U> FutureStream<U> of(U... array) {
		return LazyFutureStream.sequentialBuilder().of(array);
	}

}
