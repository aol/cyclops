package com.aol.cyclops.comprehensions.comprehenders;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class StreamComprehender implements Comprehender<Stream> {

	@Override
	public Object filter(Stream t, Predicate p) {
		return t.filter(p);
	}

	@Override
	public Object map(Stream t, Function fn) {
		return t.map(fn);
	}

	@Override
	public Stream flatMap(Stream t, Function fn) {
		return t.flatMap(fn);
	}

}
