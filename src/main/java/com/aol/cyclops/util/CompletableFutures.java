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

import com.aol.cyclops.Monoid;
import com.aol.cyclops.Reducer;
import com.aol.cyclops.Semigroup;
import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.FutureW;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.data.collections.extensions.CollectionX;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.types.Value;

import lombok.experimental.UtilityClass;

/**
 * Utilty methods for working with JDK CompletableFutures
 * 
 * @author johnmcclean
 *
 */
@UtilityClass
public class CompletableFutures {
    /**
     * Asynchronous sequence operation that convert a Collection of Futures to a Future with a List
     * 
     * <pre>
     * {@code 
     *   CompletableFuture<ListX<Integer>> futures =CompletableFuture.sequence(ListX.of(
     *                                                          CompletableFuture.completedFuture(10),
     *                                                          CompletableFuture.completedFuture(1)));
         //ListX.of(10,1)
     * 
     * }
     * </pre>
     * 
     * 
     * @param fts Collection of Futures to Sequence into a Future with a List
     * @return Future with a List
     */
    public static <T> CompletableFuture<ListX<T>> sequence(final CollectionX<CompletableFuture<T>> fts) {
        return sequence(fts.stream()).thenApply(s -> s.toListX());
    }
    /**
     * Asynchronous sequence operation that convert a Stream of FutureWs to a Future with a Stream
     * 
     * <pre>
     * {@code 
     *   CompletableFuture<ListX<Integer>> futures =CompletableFuture.sequence(ListX.of(
     *                                                          CompletableFuture.completedFuture(10),
     *                                                          CompletableFuture.completedFuture(1)));
         //ListX.of(10,1)
     * 
     * }
     * </pre>
     * 
     * 
     * @param fts Stream of Futures to Sequence into a Future with a Stream
     * @return Future with a Stream
     */
    public static <T> CompletableFuture<ReactiveSeq<T>> sequence(final Stream<CompletableFuture<T>> fts) {
        return AnyM.sequence(fts.map(f -> fromCompletableFuture(f)), () -> AnyM.fromCompletableFuture(completedFuture(Stream.<T> empty())))
                   .map(s -> ReactiveSeq.fromStream(s))
                   .unwrap();

    }
    /**
     * 
     * Asynchronously accumulate the results only from those Futures which have completed successfully.
     * Also @see {@link CompletableFutures#accumulate(CollectionX, Reducer)} if you would like a failure to result in a CompletableFuture
     * with an error
     * <pre>
     * {@code 
     * 
     * CompletableFuture<Integer> just = CompletableFuture.completedFuture(10);
      CompletableFuture<Integer> none = FutureW.ofError(new NoSuchElementException())
                                               .getFuture();
       
     * CompletableFuture<PSetX<Integer>> futures = CompletableFutures.accumulateSuccess(ListX.of(just,none,CompletableFuture.completedFuture(1)),Reducers.toPSetX());
       
       //CompletableFuture[PSetX[10,1]]
     *  }
     *  </pre>
     * 
     * @param fts Collection of Futures to accumulate successes
     * @param reducer Reducer to accumulate results
     * @return CompletableFuture asynchronously populated with the accumulate success operation
     */
    public static <T, R> CompletableFuture<R> accumulateSuccess(final CollectionX<CompletableFuture<T>> fts, final Reducer<R> reducer) {
        CompletableFuture<R> result = new CompletableFuture<>();
        Stream<T> successes = fts.stream()
                                                    .filter(ft->!ft.isCompletedExceptionally())
                                                    .map(CompletableFuture::join);
        CompletableFuture.allOf(fts.toArray(new CompletableFuture[0]))
                        .thenRun(()-> result.complete(reducer.mapReduce(successes)))
                        .exceptionally(e->{ result.complete(reducer.mapReduce(successes)); return null;});
        
        return result;    
    }
    /**
     * Asynchronously accumulate the results only from those Futures which have completed successfully, using the supplied mapping function to
     * convert the data from each FutureW before reducing them using the supplied Monoid (a combining BiFunction/BinaryOperator and identity element that takes two
     * input values of the same type and returns the combined result) {@see com.aol.cyclops.Monoids }.
     * 
     * <pre>
     * {@code 
     * CompletableFuture<String> future = CompletableFutures.accumulate(ListX.of(CompletableFuture.completedFuture(10),CompletableFuture.completedFuture(1)),i->""+i,Monoids.stringConcat);
        //CompletableFuture["101"]
     * }
     * </pre>
     * 
     * @param fts Collection of Futures to accumulate successes
     * @param mapper Mapping function to be applied to the result of each Future
     * @param reducer Monoid to combine values from each Future
     * @return CompletableFuture asynchronously populated with the accumulate operation
     */
    public static <T, R> CompletableFuture<R> accumulateSuccess(final CollectionX<CompletableFuture<T>> fts,final Function<? super T, R> mapper,final Monoid<R> reducer) {
        CompletableFuture<R> result = new CompletableFuture<>();
        ReactiveSeq<R> successes = fts.stream()
                                      .filter(ft->!ft.isCompletedExceptionally())
                                      .map(CompletableFuture::join)
                                      .map(mapper);
        CompletableFuture.allOf(fts.toArray(new CompletableFuture[0]))
                        .thenRun(()-> result.complete(successes.reduce(reducer)))
                        .exceptionally(e->{ result.complete(successes.reduce(reducer)); return null;});
        
        return result;    
    }
    /**
     * Asynchronously accumulate the results only from those Futures which have completed successfully,
     *  reducing them using the supplied Monoid (a combining BiFunction/BinaryOperator and identity element that takes two
     * input values of the same type and returns the combined result) {@see com.aol.cyclops.Monoids }
     * 
     * <pre>
     * {@code 
     * CompletableFuture<Integer> just =CompletableFuture.completedFuture(10);
     * CompletableFuture<Integer> future =CompletableFutures.accumulate(Monoids.intSum, ListX.of(just,CompletableFuture.completedFuture(1)));
       //CompletableFuture[11]
     * }
     * </pre>
     * 
     * 
     * @param fts Collection of Futures to accumulate successes
     * @param reducer Monoid to combine values from each Future
     * @return CompletableFuture asynchronously populated with the accumulate operation
     */
    public static <T, R> CompletableFuture<T> accumulateSuccess(final Monoid<T> reducer,final CollectionX<CompletableFuture<T>> fts) {
        CompletableFuture<T> result = new CompletableFuture<>();
        ReactiveSeq<T> successes = fts.stream()
                                      .filter(ft->!ft.isCompletedExceptionally())
                                      .map(CompletableFuture::join);
        CompletableFuture.allOf(fts.toArray(new CompletableFuture[0]))
                        .thenRun(()-> result.complete(successes.reduce(reducer)))
                        .exceptionally(e->{ result.complete(successes.reduce(reducer)); return null;});
        
        return result;    
    }
    /**
     * Asynchronously accumulate the results of Futures, a single failure will cause a failed result, using the supplied Reducer {@see com.aol.cyclops.Reducers}
     * <pre>
     * {@code 
     * 
     * CompletableFuture<Integer> just =CompletableFuture.completedFuture(10);
       CompletableFuture<Integer> none = FutureW.ofError(new NoSuchElementException()).getFuture();
       
     * CompletableFuture<PSetX<Integer>> futures = CompletableFutures.accumulateSuccess(ListX.of(just,none,CompletableFuture.completedFuture(1)),Reducers.toPSetX());
       
       //CompletableFuture[PSetX[10,1]]
     *  }
     *  </pre>
     * 
     * @param fts Collection of Futures to accumulate successes
     * @param reducer Reducer to accumulate results
     * @return FutureW asynchronously populated with the accumulate success operation
     */
    public static <T, R> CompletableFuture<R> accumulate(final CollectionX<CompletableFuture<T>> fts, final Reducer<R> reducer) {
        return sequence(fts).thenApply(s -> s.mapReduce(reducer));
    }
    /**
     * Asynchronously accumulate the results of a batch of Futures which using the supplied mapping function to
     * convert the data from each FutureW before reducing them using the supplied supplied Monoid (a combining BiFunction/BinaryOperator and identity element that takes two
     * input values of the same type and returns the combined result) {@see com.aol.cyclops.Monoids }.
     * A single Failure results in a Failed  Future.
     * 
     * <pre>
     * {@code 
     * CompletableFuture<String> future = FutureW.accumulate(ListX.of(CompletableFuture.completedFuture(10),CompletableFuture.completedFuture(1)),i->""+i,Monoids.stringConcat);
        //CompletableFuture["101"]
     * }
     * </pre>
     * 
     * @param fts Collection of Futures to accumulate successes
     * @param mapper Mapping function to be applied to the result of each Future
     * @param reducer Monoid to combine values from each Future
     * @return CompletableFuture asynchronously populated with the accumulate operation
     */
    public static <T, R> CompletableFuture<R> accumulate(final CollectionX<CompletableFuture<T>> fts, final Function<? super T, R> mapper,
            final Monoid<R> reducer) {
        return sequence(fts).thenApply(s -> s.map(mapper)
                                             .reduce(reducer));
    }
    /**
     * Asynchronously accumulate the results only from the provided Futures,
     *  reducing them using the supplied Monoid (a combining BiFunction/BinaryOperator and identity element that takes two
     * input values of the same type and returns the combined result) {@see com.aol.cyclops.Monoids }.
     * 
     * A single Failure results in a Failed  Future.
     * 
     * <pre>
     * {@code 
     * CompletableFuture<Integer> just =CompletableFuture.completedFuture(10);
     * 
     * CompletableFuture<Integer> future =CompletableFutures.accumulate(Monoids.intSum,ListX.of(just,CompletableFuture.completableFuture(1)));
       //CompletableFuture[11]
     * }
     * </pre>
     * 
     * 
     * @param fts Collection of Futures to accumulate successes
     * @param reducer Monoid to combine values from each Future
     * @return CompletableFuture asynchronously populated with the accumulate operation
     */
    public static <T> CompletableFuture<T> accumulate( final Monoid<T> reducer,final CollectionX<CompletableFuture<T>> fts
           ) {
        return sequence(fts).thenApply(s -> s
                                             .reduce(reducer));
    }

    public static <T> CompletableFuture<T> schedule(final String cron, final ScheduledExecutorService ex, final Supplier<T> t) {
        return FutureW.schedule(cron, ex, t)
                      .getFuture();
    }

    public static <T> CompletableFuture<T> schedule(final long delay, final ScheduledExecutorService ex, final Supplier<T> t) {
        return FutureW.schedule(delay, ex, t)
                      .getFuture();
    }

    public static <T1, T2, R> CompletableFuture<R> combine(final CompletableFuture<? extends T1> f, final Value<? extends T2> v,
            final BiFunction<? super T1, ? super T2, ? extends R> fn) {
        return narrow(FutureW.of(f)
                             .combine(v, fn)
                             .getFuture());
    }

    public static <T1, T2, R> CompletableFuture<R> zip(final CompletableFuture<? extends T1> f, final Iterable<? extends T2> v,
            final BiFunction<? super T1, ? super T2, ? extends R> fn) {
        return narrow(FutureW.of(f)
                             .zip(v, fn)
                             .getFuture());
    }

    public static <T1, T2, R> CompletableFuture<R> zip(final Publisher<? extends T2> p, final CompletableFuture<? extends T1> f,
            final BiFunction<? super T1, ? super T2, ? extends R> fn) {
        return narrow(FutureW.of(f)
                             .zip(fn, p)
                             .getFuture());
    }

    public static <T> CompletableFuture<T> narrow(final CompletableFuture<? extends T> f) {
        return (CompletableFuture<T>) f;
    }
}
