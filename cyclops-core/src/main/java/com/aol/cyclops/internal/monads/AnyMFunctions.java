package com.aol.cyclops.internal.monads;

import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Stream;

import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.SequenceM;

public interface AnyMFunctions {
	<T,R> AnyM<ListX<R>> traverse(Collection<? extends AnyM<T>> seq, Function<? super T,? extends R> fn);
	<T,R> AnyM<ListX<R>> traverse(Stream<? extends AnyM<T>> seq, Function<? super T,? extends R> fn);
	<T1>  AnyM<ListX<T1>> sequence(Collection<? extends AnyM<T1>> seq);
	<T1>  AnyM<SequenceM<T1>> sequence(Stream<? extends AnyM<T1>> seq);
}
