package com.aol.cyclops.comprehensions.comprehenders;

import java.util.function.Function;

import com.aol.cyclops.lambda.api.Comprehender;
import com.nurkiewicz.lazyseq.LazySeq;

public class LazySeqComprehender implements Comprehender<LazySeq> {

	@Override
	public Object map(LazySeq t, Function fn) {
		return t.map(fn);
	}

	@Override
	public LazySeq flatMap(LazySeq t, Function fn) {
		return t.flatMap(fn);
	}

	@Override
	public LazySeq of(Object o) {
		return LazySeq.of(o);
	}

	@Override
	public LazySeq empty() {
		return LazySeq.of();
	}

	@Override
	public Class getTargetClass() {
		return LazySeq.class;
	}

}
