package com.aol.simple.react.collectors.lazy;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface LazyResultConsumer<T> extends Consumer<CompletableFuture<T>>{

	public LazyResultConsumer<T> withResults(Collection<T> t);

	public Collection<T> getResults();
}
