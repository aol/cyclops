package com.aol.cyclops.monad;

import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Stream;

import com.aol.cyclops.collections.extensions.standard.ListX;
import com.aol.cyclops.sequence.SequenceM;

public interface AnyMFunctions {
	<T,R> AnyM<ListX<R>> traverse(Collection<AnyM<T>> seq, Function<? super T,? extends R> fn);
	<T,R> AnyM<ListX<R>> traverse(Stream<AnyM<T>> seq, Function<? super T,? extends R> fn);
	<T1>  AnyM<ListX<T1>> sequence(Collection<AnyM<T1>> seq);
	<T1>  AnyM<SequenceM<T1>> sequence(Stream<AnyM<T1>> seq);
}
