package com.aol.simple.react.eager;

import com.aol.simple.react.base.BaseSequentialSeqTest;
import com.aol.simple.react.stream.eager.EagerFutureStream;
import com.aol.simple.react.stream.traits.FutureStream;

public class EagerSequentialSeqTest extends BaseSequentialSeqTest {

	@Override
	protected <U> FutureStream<U> of(U... array) {
		return EagerFutureStream.sequentialBuilder().of(array);
	}

}
