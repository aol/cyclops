package com.aol.cyclops.comprehensions.comprehenders;

import java.util.function.Function;

import org.jooq.lambda.Seq;

import com.aol.cyclops.lambda.api.Comprehender;

public class SeqComprehender implements Comprehender<Seq> {

	@Override
	public Object map(Seq t, Function fn) {
		return t.map(fn);
	}

	@Override
	public Seq flatMap(Seq t, Function fn) {
		return t.flatMap(fn);
	}

	@Override
	public Seq of(Object o) {
		return Seq.of(o);
	}

	@Override
	public Seq empty() {
		return Seq.of();
	}

	@Override
	public Class getTargetClass() {
		return Seq.class;
	}

}
