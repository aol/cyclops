package com.aol.cyclops.monad;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public interface AnyMFunctions {
	<T,R> AnyM<List<R>> traverse(Collection<AnyM<T>> seq, Function<T,R> fn);
	<T,R> AnyM<List<R>> traverse(Stream<AnyM<T>> seq, Function<T,R> fn);
	<T1>  AnyM<Stream<T1>> sequence(Collection<AnyM<T1>> seq);
	<T1>  AnyM<Stream<T1>> sequence(Stream<AnyM<T1>> seq);
}
