package com.aol.cyclops.comprehensions.comprehenders;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.DoubleStream;
import java.util.stream.LongStream;

import com.aol.cyclops.lambda.api.Comprehender;

public class DoubleStreamComprehender implements Comprehender<DoubleStream> {
	
	@Override
	public Object filter(DoubleStream t, Predicate p) {
		return t.filter(test->p.test(test));
	}

	@Override
	public Object map(DoubleStream t, Function fn) {
		return t.map(i->(double)fn.apply(i));
	}

	@Override
	public DoubleStream flatMap(DoubleStream t, Function fn) {
		return t.flatMap( i-> (DoubleStream)fn.apply(i));
	}

	@Override
	public boolean instanceOfT(Object apply) {
		return apply instanceof DoubleStream;
	}

	@Override
	public DoubleStream of(Object o) {
		return DoubleStream.of((double)o);
	}

	@Override
	public DoubleStream of() {
		return DoubleStream.of();
	}

}
