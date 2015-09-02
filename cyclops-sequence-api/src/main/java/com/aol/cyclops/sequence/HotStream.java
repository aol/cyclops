package com.aol.cyclops.sequence;

import java.util.function.Function;
import java.util.stream.Stream;

public interface HotStream<T> {

	public SequenceM<T> connect();
	public <R extends Stream<T>> R connectTo(Function<SequenceM<T>,R> to);
}
