package com.aol.cyclops.control;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import com.aol.cyclops.Reducer;
import com.aol.cyclops.Semigroup;
import com.aol.cyclops.data.collections.extensions.CollectionX;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.monad.AnyM;

public class CompletableFutures {

	public static <T> CompletableFuture<ListX<T>> sequence(CollectionX<CompletableFuture<T>> fts){
		return AnyM.sequence(AnyM.<T>listFromCompletableFuture(fts)).unwrap();
	}
	
	public static <T,R> CompletableFuture<R> accumulate(CollectionX<CompletableFuture<T>> fts,Reducer<R> reducer){
		return sequence(fts).thenApply(s->s.mapReduce(reducer));
	}
	public static <T,R> CompletableFuture<R> accumulate(CollectionX<CompletableFuture<T>> fts,Function<? super T, R> mapper,Semigroup<R> reducer){
		return sequence(fts).thenApply(s->s.map(mapper).reduce(reducer.reducer()).get());
	}

}
