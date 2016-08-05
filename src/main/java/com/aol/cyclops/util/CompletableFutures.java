package com.aol.cyclops.util;

import static com.aol.cyclops.control.AnyM.fromCompletableFuture;
import static java.util.concurrent.CompletableFuture.completedFuture;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.reactivestreams.Publisher;

import com.aol.cyclops.Reducer;
import com.aol.cyclops.Semigroup;
import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.FutureW;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.data.collections.extensions.CollectionX;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.types.Value;

public class CompletableFutures {
    
	public static <T> CompletableFuture<ListX<T>> sequence(CollectionX<CompletableFuture<T>> fts){
	    return sequence(fts.stream()).thenApply(s->s.toListX());
	}
	
	public static <T> CompletableFuture<ReactiveSeq<T>> sequence(Stream<CompletableFuture<T>> fts){
	    return AnyM.sequence(fts.map(f->fromCompletableFuture(f)),
                ()->AnyM.fromCompletableFuture(completedFuture(Stream.<T>empty())))
	                .map(s->ReactiveSeq.fromStream(s))
	                .unwrap();
       
    }
	public static <T,R> CompletableFuture<R> accumulateSuccess(CollectionX<CompletableFuture<T>> fts,Reducer<R> reducer){
        
	    CompletableFuture<ListX<T>> sequenced =  AnyM.sequence(fts.map(f->AnyM.fromCompletableFuture(f))).unwrap();
        return sequenced.thenApply(s->s.mapReduce(reducer));
    }
	public static <T,R> CompletableFuture<R> accumulate(CollectionX<CompletableFuture<T>> fts,Reducer<R> reducer){
		return sequence(fts).thenApply(s->s.mapReduce(reducer));
	}
	public static <T,R> CompletableFuture<R> accumulate(CollectionX<CompletableFuture<T>> fts,Function<? super T, R> mapper,Semigroup<R> reducer){
		return sequence(fts).thenApply(s->s.map(mapper).reduce(reducer.reducer()).get());
	}

    public static <T> CompletableFuture<T> schedule(String cron, ScheduledExecutorService ex, Supplier<T> t){
        return FutureW.schedule(cron, ex, t).getFuture();
    }
    public static <T> CompletableFuture<T> schedule(long delay, ScheduledExecutorService ex, Supplier<T> t){
        return FutureW.schedule(delay, ex, t).getFuture();
    }
    
    public static <T1,T2,R> CompletableFuture<R> combine(CompletableFuture<? extends T1> f, Value<? extends T2> v, BiFunction<? super T1,? super T2,? extends R> fn){
        return narrow(FutureW.of(f).combine(v, fn).getFuture());
    }
    public static <T1,T2,R> CompletableFuture<R> zip(CompletableFuture<? extends T1> f, Iterable<? extends T2> v, BiFunction<? super T1,? super T2,? extends R> fn){
        return narrow(FutureW.of(f).zip(v, fn).getFuture());
    }
    public static <T1,T2,R> CompletableFuture<R> zip(Publisher<? extends T2> p,CompletableFuture<? extends T1> f,  BiFunction<? super T1,? super T2,? extends R> fn){
        return narrow(FutureW.of(f).zip(fn,p).getFuture());
    }

    public static <T> CompletableFuture<T> narrow(CompletableFuture<? extends T> f){
        return (CompletableFuture<T>)f;
    }
}
