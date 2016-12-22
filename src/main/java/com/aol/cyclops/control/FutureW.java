package com.aol.cyclops.control;

import cyclops.Monoid;
import cyclops.Reducer;
import com.aol.cyclops.control.monads.transformers.FutureT;
import com.aol.cyclops.data.collections.extensions.CollectionX;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.react.Status;
import com.aol.cyclops.react.collectors.lazy.Blocker;
import com.aol.cyclops.types.MonadicValue;
import com.aol.cyclops.types.To;
import com.aol.cyclops.types.Value;
import com.aol.cyclops.types.Zippable;
import com.aol.cyclops.types.anyM.Witness;
import com.aol.cyclops.types.anyM.WitnessType;
import com.aol.cyclops.types.stream.reactive.ValueSubscriber;
import com.aol.cyclops.util.CompletableFutures;
import com.aol.cyclops.util.ExceptionSoftener;
import cyclops.function.F3;
import cyclops.function.F4;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.jooq.lambda.tuple.Tuple2;
import org.reactivestreams.Publisher;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A Wrapper around CompletableFuture that implements cyclops-react interfaces and provides a more standard api
 * 
 * e.g.
 *   map instead of thenApply
 *   flatMap instead of thenCompose
 *   combine instead of thenCombine (applicative functor ap)
 *
 * @author johnmcclean
        *
        * @param <T> Type of wrapped future value
        */
@AllArgsConstructor
@EqualsAndHashCode
@Slf4j
public class FutureW<T> implements To<FutureW<T>>,MonadicValue<T> {

    public <W extends WitnessType<W>> FutureT<W, T> liftM(W witness) {
        return FutureT.of(witness.adapter().unit(this));
    }

    interface Futureable<T>{
        FutureW<T> execute(Executor ex);
    }
    @AllArgsConstructor
    static class RunnableFuture<T> implements Futureable<T>{

        private final Supplier<T> t;
        @Override
        public FutureW<T> execute(Executor ex) {
            return FutureW.ofSupplier(t,ex);
        }
    }
    /**
     * An empty FutureW
     * 
     * @return A FutureW that wraps a CompletableFuture with a null result
     */
    public static <T> FutureW<T> empty() {
        return new FutureW<>(
                           CompletableFuture.completedFuture(null));
    }
    /**
     * An empty FutureW
     * 
     * @return A FutureW that wraps a CompletableFuture with a null result
     */
    public static <T> FutureW<T> future() {
        return new FutureW<>(
                           new CompletableFuture<>());
    }
    
    /**
     * Select the first FutureW to complete
     * 
     * @see CompletableFuture#anyOf(CompletableFuture...)
     * @param fts FutureWs to race
     * @return First FutureW to complete
     */
    public static <T> FutureW<T> anyOf(FutureW<T>... fts) {
        
       return (FutureW<T>) FutureW.ofResult( (CompletableFuture<T>)CompletableFuture.anyOf(Stream.of(fts)
                                                                              .map(FutureW::getFuture)
                                                                              .collect(Collectors.toList())
                                                                              .toArray(new CompletableFuture[0])));
    }
    /**
     * Wait until all the provided FutureW's to complete
     * 
     * @see CompletableFuture#allOf(CompletableFuture...)
     * 
     * @param fts FutureWs to  wait on
     * @return FutureW that completes when all the provided Futures Complete. Empty Future result, or holds an Exception
     *         from a provided FutureW that failed.
     */
    public static <T> FutureW<T> allOf(FutureW<T>... fts) {
        
        return (FutureW<T>) FutureW.ofResult((CompletableFuture<T>)CompletableFuture.allOf(Stream.of(fts)
                                                                      .map(FutureW::getFuture)
                                                                      .collect(Collectors.toList())
                                                                      .toArray(new CompletableFuture[0])));
     }
    /**
     * Block until a Quorum of results have returned as determined by the provided Predicate
     * 
     * <pre>
     * {@code 
     * 
     * FutureW<ListX<Integer>> strings = FutureW.quorum(status -> status.getCompleted() >0, FutureW.ofSupplier(()->1),FutureW.future(),FutureW.future());
               

        strings.get().size()
        //1
     * 
     * }
     * </pre>
     * 
     * 
     * @param breakout Predicate that determines whether the block should be
     *            continued or removed
     * @param fts FutureWs to  wait on results from
     * @return FutureW which will be populated with a Quorum of results
     */
    @SafeVarargs
    public static <T> FutureW<ListX<T>> quorum(Predicate<Status<T>> breakout,FutureW<T>... fts) {
        
        List<CompletableFuture<?>> list = Stream.of(fts)
                                                .map(FutureW::getFuture)
                                                .collect(Collectors.toList());
        
        return FutureW.of(new Blocker<T>(list, Optional.of(e-> {
                    log.error(e.getMessage(), e);
                })).nonBlocking(breakout));
                
       
    }
    /**
     * Select the first Future to return with a successful result
     * 
     * <pre>
     * {@code 
     * FutureW<Integer> ft = FutureW.future();
       FutureW<Integer> result = FutureW.firstSuccess(FutureW.ofSupplier(()->1),ft);
               
       ft.complete(10);
       result.get() //1
     * }
     * </pre>
     * 
     * @param fts Futures to race
     * @return First Future to return with a result
     */
    @SafeVarargs
    public static <T> FutureW<T> firstSuccess(FutureW<T>... fts) {
        FutureW<T> future = FutureW.future();
        Stream.of(fts)
              .forEach(f->f.peek(r->future.complete(r)));
        FutureW<T> all = allOf(fts).recover(e->{ future.completeExceptionally(e); return null;});
        return future;
        
      }
    
    /**
     * Complete this FutureW with an Exception
     * @see CompletableFuture#completeExceptionally(Throwable)
     *
     * @param e Throwable to complete this FutureW with
     */
    public boolean completeExceptionally(Throwable e) {
        return this.future.completeExceptionally(e);
        
    }
    /**
     * Construct a FutureW asyncrhonously that contains a single value extracted from the supplied reactive-streams Publisher
     * 
     * 
     * <pre>
     * {@code 
     *   ReactiveSeq<Integer> stream =  ReactiveSeq.of(1,2,3);
        
        FutureW<Integer> future = FutureW.fromPublisher(stream,ex);
        
        //FutureW[1]
     * 
     * }
     * </pre>
     * 
     * 
     * @param pub Publisher to extract value from
     * @param ex Executor to extract value on
     * @return FutureW populated asyncrhonously from Publisher
     */
    public static <T> FutureW<T> fromPublisher(final Publisher<T> pub, final Executor ex) {
        final ValueSubscriber<T> sub = ValueSubscriber.subscriber();
        pub.subscribe(sub);
        return sub.toFutureWAsync(ex);
    }

    /**
     * Construct a FutureW asyncrhonously that contains a single value extracted from the supplied Iterable
     * <pre>
     * {@code 
     *  ReactiveSeq<Integer> stream =  ReactiveSeq.of(1,2,3);
        
        FutureW<Integer> future = FutureW.fromIterable(stream,ex);
        
        //FutureW[1]
     * 
     * }
     * </pre>
     * @param iterable Iterable to generate a FutureW from
     * @param ex  Executor to extract value on
     * @return FutureW populated asyncrhonously from Iterable
     */
    public static <T> FutureW<T> fromIterable(final Iterable<T> iterable, final Executor ex) {

        return FutureW.ofSupplier(() -> Eval.fromIterable(iterable))
                      .map(e -> e.get());
    }

    /**
     * Construct a FutureW syncrhonously that contains a single value extracted from the supplied reactive-streams Publisher
     * <pre>
     * {@code 
     *   ReactiveSeq<Integer> stream =  ReactiveSeq.of(1,2,3);
        
        FutureW<Integer> future = FutureW.fromPublisher(stream);
        
        //FutureW[1]
     * 
     * }
     * </pre>
     * @param pub Publisher to extract value from
     * @return FutureW populated syncrhonously from Publisher
     */
    public static <T> FutureW<T> fromPublisher(final Publisher<T> pub) {
        final ValueSubscriber<T> sub = ValueSubscriber.subscriber();
        pub.subscribe(sub);
        return sub.toFutureW();
    }

    /**
     * Construct a FutureW syncrhonously that contains a single value extracted from the supplied Iterable
     * 
     * <pre>
     * {@code 
     *  ReactiveSeq<Integer> stream =  ReactiveSeq.of(1,2,3);
        
        FutureW<Integer> future = FutureW.fromIterable(stream);
        
        //FutureW[1]
     * 
     * }
     * </pre>
     * 
     * 
     * @param iterable Iterable to extract value from
     * @return FutureW populated syncrhonously from Iterable
     */
    public static <T> FutureW<T> fromIterable(final Iterable<T> iterable) {
        if(iterable instanceof FutureW){
            return (FutureW)iterable;
        }
        return FutureW.ofResult(Eval.fromIterable(iterable))
                      .map(e -> e.get());
    }

    /**
     * Create a FutureW instance from the supplied CompletableFuture
     * 
     * @param f CompletableFuture to wrap as a FutureW
     * @return FutureW wrapping the supplied CompletableFuture
     */
    public static <T> FutureW<T> of(final CompletableFuture<T> f) {
        return new FutureW<>(
                             f);
    }

    /**
     * Construct a FutureW asyncrhonously from the Supplied Try
     * 
     * @param value Try to populate Future from
     * @param ex Executor to execute 
     * @return FutureW populated with either the value or error in provided Try
     */
    @Deprecated
    public static <T, X extends Throwable> FutureW<T> fromTry(final Try<T, X> value, final Executor ex) {
        return FutureW.ofSupplier(value, ex);
    }
    /**
     * Construct a FutureW syncrhonously from the Supplied Try
     * 
     * @param value Try to populate Future from
     * @return FutureW populated with either the value or error in provided Try
     */
    public static <T, X extends Throwable> FutureW<T> fromTry(final Try<T, X> value) {
        return FutureW.ofSupplier(value);
    }

    /**
     * Schedule the population of a FutureW from the provided Supplier, the provided Cron (Quartz format) expression will be used to
     * trigger the population of the FutureW. The provided ScheduledExecutorService provided the thread on which the 
     * Supplier will be executed.
     * 
     * <pre>
     * {@code 
     *  
     *    FutureW<String> future = FutureW.schedule("* * * * * ?", Executors.newScheduledThreadPool(1), ()->"hello");
     *    
     *    //FutureW["hello"]
     * 
     * }</pre>
     * 
     * 
     * @param cron Cron expression in Quartz format
     * @param ex ScheduledExecutorService used to execute the provided Supplier
     * @param t The Supplier to execute to populate the FutureW
     * @return FutureW populated on a Cron based Schedule
     */
    public static <T> FutureW<T> schedule(final String cron, final ScheduledExecutorService ex, final Supplier<T> t) {
        final CompletableFuture<T> future = new CompletableFuture<>();
        final FutureW<T> wrapped = FutureW.of(future);
        ReactiveSeq.generate(() -> {
            try {
                future.complete(t.get());
            } catch (final Throwable t1) {
                future.completeExceptionally(t1);
            }
            return 1;

        })
                   .limit(1)
                   .schedule(cron, ex);

        return wrapped;
    }

    /**
     * Schedule the population of a FutureW from the provided Supplier after the specified delay. The provided ScheduledExecutorService provided the thread on which the 
     * Supplier will be executed.
     * <pre>
     * {@code 
     *  
     *    FutureW<String> future = FutureW.schedule(10l, Executors.newScheduledThreadPool(1), ()->"hello");
     *    
     *    //FutureW["hello"]
     * 
     * }</pre>
     * 
     * @param delay Delay after which the FutureW should be populated
     * @param ex ScheduledExecutorService used to execute the provided Supplier
     * @param t he Supplier to execute to populate the FutureW
     * @return FutureW populated after the specified delay
     */
    public static <T> FutureW<T> schedule(final long delay, final ScheduledExecutorService ex, final Supplier<T> t) {
        final CompletableFuture<T> future = new CompletableFuture<>();
        final FutureW<T> wrapped = FutureW.of(future);

        ReactiveSeq.generate(() -> {
            try {
                future.complete(t.get());
            } catch (final Throwable t1) {
                future.completeExceptionally(t1);
            }
            return 1;

        })
                   .limit(1)
                   .scheduleFixedDelay(delay, ex);

        return wrapped;
    }

    /**
     * Asynchronous sequence operation that convert a Collection of FutureWs to a FutureW with a List
     * 
     * <pre>
     * {@code 
     *   FutureW<ListX<Integer>> futures =FutureW.sequence(ListX.of(FutureW.ofResult(10),FutureW.ofResult(1)));
         //ListX.of(10,1)
     * 
     * }
     * </pre>
     * 
     * 
     * @param fts Collection of Futures to Sequence into a Future with a List
     * @return Future with a List
     */
    public static <T> FutureW<ListX<T>> sequence(final CollectionX<FutureW<T>> fts) {
        return sequence(fts.stream()).map(s -> s.toListX());

    }

    /**
     * Sequence operation that convert a Stream of FutureWs to a FutureW with a Stream
     *
     * <pre>
     * {@code 
     *   FutureW<Integer> just = FutureW.ofResult(10);
     *   FutureW<ReactiveSeq<Integer>> futures =FutureW.sequence(Stream.of(just,FutureW.ofResult(1)));
         //ListX.of(10,1)
     * 
     * }
     * </pre>
     *
     * @param fts Strean of Futures to Sequence into a Future with a Stream
     * @return Future with a Stream
     */
    public static <T> FutureW<ReactiveSeq<T>> sequence(final Stream<? extends FutureW<T>> fts) {
        return AnyM.sequence(fts.map(AnyM::fromFutureW), Witness.future.INSTANCE)
                   .map(ReactiveSeq::fromStream)
                   .to(Witness::future);
    }

    /**
     * 
     * Asynchronously accumulate the results only from those Futures which have completed successfully.
     * Also @see {@link FutureW#accumulate(CollectionX, Reducer)} if you would like a failure to result in a FutureW 
     * with an error
     * <pre>
     * {@code 
     * 
     * FutureW<Integer> just =FutureW.of(CompletableFuture.completedFuture(10));
       FutureW<Integer> none = FutureW.ofError(new NoSuchElementException());
       
     * FutureW<PSetX<Integer>> futures = FutureW.accumulateSuccess(ListX.of(just,none,FutureW.ofResult(1)),Reducers.toPSetX());
       
       //FutureW[PSetX[10,1]]
     *  }
     *  </pre>
     * 
     * @param fts Collection of Futures to accumulate successes
     * @param reducer Reducer to accumulate results
     * @return FutureW asynchronously populated with the accumulate success operation
     */
    public static <T, R> FutureW<R> accumulateSuccess(final CollectionX<FutureW<T>> fts, final Reducer<R> reducer) {
       return FutureW.of(CompletableFutures.accumulateSuccess(fts.map(FutureW::getFuture), reducer));  
    }
    /**
     * Asynchronously accumulate the results of Futures, a single failure will cause a failed result, using the supplied Reducer {@see cyclops.Reducers}
     * <pre>
     * {@code 
     * 
     * FutureW<Integer> just =FutureW.of(CompletableFuture.completedFuture(10));
       FutureW<Integer> none = FutureW.ofError(new NoSuchElementException());
       
     * FutureW<PSetX<Integer>> futures = FutureW.accumulateSuccess(ListX.of(just,none,FutureW.ofResult(1)),Reducers.toPSetX());
       
       //FutureW[PSetX[10,1]]
     *  }
     *  </pre>
     * 
     * @param fts Collection of Futures to accumulate successes
     * @param reducer Reducer to accumulate results
     * @return FutureW asynchronously populated with the accumulate success operation
     */
    public static <T, R> FutureW<R> accumulate(final CollectionX<FutureW<T>> fts, final Reducer<R> reducer) {
        return sequence(fts).map(s -> s.mapReduce(reducer));
    }
    /**
     * Asynchronously accumulate the results only from those Futures which have completed successfully, using the supplied mapping function to
     * convert the data from each FutureW before reducing them using the supplied Monoid (a combining BiFunction/BinaryOperator and identity element that takes two
     * input values of the same type and returns the combined result) {@see cyclops.Monoids }.
     * 
     * <pre>
     * {@code 
     * FutureW<String> future = FutureW.accumulate(ListX.of(FutureW.ofResult(10),FutureW.ofResult(1)),i->""+i,Monoids.stringConcat);
        //FutureW["101"]
     * }
     * </pre>
     * 
     * @param fts Collection of Futures to accumulate successes
     * @param mapper Mapping function to be applied to the result of each Future
     * @param reducer Monoid to combine values from each Future
     * @return FutureW asynchronously populated with the accumulate operation
     */
    public static <T, R> FutureW<R> accumulateSuccess(final CollectionX<FutureW<T>> fts, final Function<? super T, R> mapper, final Monoid<R> reducer) {
        return FutureW.of(CompletableFutures.accumulateSuccess(fts.map(FutureW::getFuture),mapper,reducer)); 
    }

    /**
     * Asynchronously accumulate the results only from those Futures which have completed successfully,
     *  reducing them using the supplied Monoid (a combining BiFunction/BinaryOperator and identity element that takes two
     * input values of the same type and returns the combined result) {@see cyclops.Monoids }.
     * 
     * <pre>
     * {@code 
     * FutureW<Integer> just =FutureW.of(CompletableFuture.completedFuture(10));
     * FutureW<Integer> future =FutureW.accumulate(Monoids.intSum, ListX.of(just,FutureW.ofResult(1)));
       //FutureW[11]
     * }
     * </pre>
     * 
     * 
     * @param fts Collection of Futures to accumulate successes
     * @param reducer Monoid to combine values from each Future
     * @return FutureW asynchronously populated with the accumulate operation
     */
    public static <T> FutureW<T> accumulateSuccess(final Monoid<T> reducer,final CollectionX<FutureW<T>> fts ) {
        return FutureW.of(CompletableFutures.accumulateSuccess(reducer,fts.map(FutureW::getFuture))); 
    }

    /**
     * Asynchronously accumulate the results of a batch of Futures which using the supplied mapping function to
     * convert the data from each FutureW before reducing them using the supplied supplied Monoid (a combining BiFunction/BinaryOperator and identity element that takes two
     * input values of the same type and returns the combined result) {@see cyclops.Monoids }.
     * A single Failure results in a Failed  Future.
     * 
     * <pre>
     * {@code 
     * FutureW<String> future = FutureW.accumulate(ListX.of(FutureW.ofResult(10),FutureW.ofResult(1)),i->""+i,Monoids.stringConcat);
        //FutureW["101"]
     * }
     * </pre>
     * 
     * @param fts Collection of Futures to accumulate successes
     * @param mapper Mapping function to be applied to the result of each Future
     * @param reducer Monoid to combine values from each Future
     * @return FutureW asynchronously populated with the accumulate operation
     */
    public static <T, R> FutureW<R> accumulate(final CollectionX<FutureW<T>> fts, final Function<? super T, R> mapper, final Monoid<R> reducer) {
        return sequence(fts).map(s -> s.map(mapper)
                                       .reduce(reducer)
                                       );
    }

    /**
     * Asynchronously accumulate the results only from the provided Futures,
     *  reducing them using the supplied Monoid (a combining BiFunction/BinaryOperator and identity element that takes two
     * input values of the same type and returns the combined result) {@see cyclops.Monoids }
     * 
     * A single Failure results in a Failed  Future.
     * 
     * <pre>
     * {@code 
     * FutureW<Integer> future =FutureW.accumulate(Monoids.intSum,ListX.of(just,FutureW.ofResult(1)));
       //FutureW[11]
     * }
     * </pre>
     * 
     * 
     * @param fts Collection of Futures to accumulate successes
     * @param reducer Monoid to combine values from each Future
     * @return FutureW asynchronously populated with the accumulate operation
     */
    public static <T> FutureW<T> accumulate(final Monoid<T> reducer,final CollectionX<FutureW<T>> fts) {
        return sequence(fts).map(s -> s.reduce(reducer)
                                      );
    }


    
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.MonadicValue#forEach4(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops.util.function.TriFunction, com.aol.cyclops.util.function.QuadFunction)
     */
    @Override
    public <T2, R1, R2, R3, R> FutureW<R> forEach4(Function<? super T, ? extends MonadicValue<R1>> value1,
            BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
            F3<? super T, ? super R1, ? super R2, ? extends MonadicValue<R3>> value3,
            F4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        return (FutureW<R>)MonadicValue.super.forEach4(value1, value2, value3, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.MonadicValue#forEach4(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops.util.function.TriFunction, com.aol.cyclops.util.function.QuadFunction, com.aol.cyclops.util.function.QuadFunction)
     */
    @Override
    public <T2, R1, R2, R3, R> FutureW<R> forEach4(Function<? super T, ? extends MonadicValue<R1>> value1,
            BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
            F3<? super T, ? super R1, ? super R2, ? extends MonadicValue<R3>> value3,
            F4<? super T, ? super R1, ? super R2, ? super R3, Boolean> filterFunction,
            F4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        
        return (FutureW<R>)MonadicValue.super.forEach4(value1, value2, value3, filterFunction, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.MonadicValue#forEach3(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops.util.function.TriFunction)
     */
    @Override
    public <T2, R1, R2, R> FutureW<R> forEach3(Function<? super T, ? extends MonadicValue<R1>> value1,
            BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
            F3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {
      
        return (FutureW<R>)MonadicValue.super.forEach3(value1, value2, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.MonadicValue#forEach3(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops.util.function.TriFunction, com.aol.cyclops.util.function.TriFunction)
     */
    @Override
    public <T2, R1, R2, R> FutureW<R> forEach3(Function<? super T, ? extends MonadicValue<R1>> value1,
            BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
            F3<? super T, ? super R1, ? super R2, Boolean> filterFunction,
            F3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return (FutureW<R>)MonadicValue.super.forEach3(value1, value2, filterFunction, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.MonadicValue#forEach2(java.util.function.Function, java.util.function.BiFunction)
     */
    @Override
    public <R1, R> FutureW<R> forEach2(Function<? super T, ? extends MonadicValue<R1>> value1,
            BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {

        return (FutureW<R>)MonadicValue.super.forEach2(value1, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.MonadicValue#forEach2(java.util.function.Function, java.util.function.BiFunction, java.util.function.BiFunction)
     */
    @Override
    public <R1, R> FutureW<R> forEach2(Function<? super T, ? extends MonadicValue<R1>> value1,
            BiFunction<? super T, ? super R1, Boolean> filterFunction,
            BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {
        return (FutureW<R>)MonadicValue.super.forEach2(value1, filterFunction, yieldingFunction);
    }


    private final CompletableFuture<T> future;

    /**
     * Non-blocking visit on the state of this Future
     * 
     * <pre>
     * {@code 
     * FutureW.ofResult(10)
              .visitAsync(i->i*2, e->-1);
       //FutureW[20]
        
       FutureW.<Integer>ofError(new RuntimeException())
              .visitAsync(i->i*2, e->-1)
       //FutureW[-1]       
     * 
     * }
     * </pre>
     * 
     * @param success Function to execute if the previous stage completes successfully
     * @param failure Function to execute if this Future fails
     * @return Future with the eventual result of the executed Function
     */
    public <R> FutureW<R> visitAsync(Function<T,R> success, Function<Throwable,R> failure){
        return map(success).recover(failure);
                
    }
    /**
     * Blocking analogue to visitAsync. Visit the state of this Future, block until ready.
     * 
     * <pre>
     * {@code 
     *  FutureW.ofResult(10)
               .visit(i->i*2, e->-1);
        //20
        
        FutureW.<Integer>ofError(new RuntimeException())
               .visit(i->i*2, e->-1)
        //[-1]       
     * 
     * }
     * </pre>
     * @param success Function to execute if the previous stage completes successfully
     * @param failure  Function to execute if this Future fails
     * @return Result of the executed Function
     */
    public <R> R visit(Function<T,R> success, Function<Throwable,R> failure){
        return visitAsync(success,failure).get();
                
    }
    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops.types.MonadicValue#coflatMap(java.util.function.Function)
     */
    @Override
    public <R> FutureW<R> coflatMap(final Function<? super MonadicValue<T>, R> mapper) {
        return (FutureW<R>) MonadicValue.super.coflatMap(mapper);
    }

    /*
     * cojoin (non-Javadoc)
     * 
     * @see com.aol.cyclops.types.MonadicValue#nest()
     */
    @Override
    public FutureW<MonadicValue<T>> nest() {
        return (FutureW<MonadicValue<T>>) MonadicValue.super.nest();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.aol.cyclops.types.MonadicValue2#combine(cyclops.Monoid,
     * com.aol.cyclops.types.MonadicValue2)
     */
    @Override
    public FutureW<T> combineEager(final Monoid<T> monoid, final MonadicValue<? extends T> v2) {
        return (FutureW<T>) MonadicValue.super.combineEager(monoid, v2);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops.types.ConvertableFunctor#map(java.util.function.Function)
     */
    @Override
    public <R> FutureW<R> map(final Function<? super T, ? extends R> fn) {
        return new FutureW<R>(
                              future.thenApply(fn));
    }
    /**
     * Asyncrhonous map operation
     * 
     * @see CompletableFuture#thenApplyAsync(Function, Executor)
     * 
     * @param fn Transformation function
     * @param ex Executor to execute the transformation asynchronously
     * @return Mapped FutureW
     */
    public <R> FutureW<R> map(final Function<? super T, ? extends R> fn,Executor ex) {
        return new FutureW<R>(
                              future.thenApplyAsync(fn,ex));
    }



    /*
     * (non-Javadoc)
     * 
     * @see java.util.function.Supplier#get()
     */
    @Override
    public T get() {
        try {
            return future.join();
        } catch (final Throwable t) {
            throw ExceptionSoftener.throwSoftenedException(t.getCause());
        }
    }

    /**
     * @return true if this FutureW is both complete, and completed without an
     *         Exception
     */
    public boolean isSuccess() {
        return future.isDone() && !future.isCompletedExceptionally();
    }
    /**
     * @see java.util.concurrent.CompletableFuture#isDone
     * @return true if this FutureW has completed executing
     */
    public boolean isDone(){
       
        return future.isDone();
    }
    /**
     * @see java.util.concurrent.CompletableFuture#isCancelled
     * @return True if this FutureW has been cancelled
     */
    public boolean isCancelled(){
        return future.isCancelled();
    }
    /**
     *  If not already completed, completes this FutureW with a {@link java.util.concurrent.CancellationException}
     *  Passes true to @see java.util.concurrent.CompletableFuture#cancel as mayInterruptIfRunning parameter on that method
     *  has no effect for the default CompletableFuture implementation
     */
    public void cancel(){
        future.cancel(true);
    }
    /**If not already completed, sets the value of this FutureW to the provided value
     * 
     * @param value Value to set this FutureW to
     */
    public void complete(T value){
        future.complete(value);
    }

    /**
     * @return true if this FutureW is complete, but completed with an Exception
     */
    public boolean isFailed() {
        return future.isCompletedExceptionally();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.aol.cyclops.types.Value#iterator()
     */
    @Override
    public Iterator<T> iterator() {
        return toStream().iterator();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.aol.cyclops.lambda.monads.Unit#unit(java.lang.Object)
     */
    @Override
    public <T> FutureW<T> unit(final T unit) {
        return new FutureW<T>(
                              CompletableFuture.completedFuture(unit));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.aol.cyclops.types.Value#stream()
     */
    @Override
    public ReactiveSeq<T> stream() {
        return ReactiveSeq.generate(() -> Try.withCatch(() -> get()))
                          .limit(1)
                          .filter(t -> t.isSuccess())
                          .map(Value::get);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.aol.cyclops.types.FlatMap#flatten()
     */
    public static <R> FutureW<R> flatten(FutureW<? extends FutureW<R>> nested) {
        return nested.flatMap(Function.identity());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops.types.MonadicValue#flatMap(java.util.function.Function)
     */
    @Override
    public <R> FutureW<R> flatMap(final Function<? super T, ? extends MonadicValue<? extends R>> mapper) {
        return FutureW.<R> of(future.<R> thenCompose(t -> (CompletionStage<R>) mapper.apply(t)
                                                                                     .toFutureW()
                                                                                     .getFuture()));
    }

    /**
     * A flatMap operation that accepts a CompleteableFuture CompletionStage as
     * the return type
     * 
     * @param mapper
     *            Mapping function
     * @return FlatMapped FutureW
     */
    public <R> FutureW<R> flatMapCf(final Function<? super T, ? extends CompletionStage<? extends R>> mapper) {
        return FutureW.<R> of(future.<R> thenCompose(t -> (CompletionStage<R>) mapper.apply(t)));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.aol.cyclops.types.Value#toXor()
     */
    @Override
    public Xor<Throwable, T> toXor() {
        try {
            return Xor.primary(future.join());
        } catch (final Throwable t) {
            return Xor.<Throwable, T> secondary(t.getCause());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.aol.cyclops.types.Value#toIor()
     */
    @Override
    public Ior<Throwable, T> toIor() {
        try {
            return Ior.primary(future.join());
        } catch (final Throwable t) {
            return Ior.<Throwable, T> secondary(t.getCause());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.aol.cyclops.closures.Convertable#toFutureW()
     */
    @Override
    public FutureW<T> toFutureW() {
        return this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.aol.cyclops.closures.Convertable#toCompletableFuture()
     */
    @Override
    public CompletableFuture<T> toCompletableFuture() {
        return this.future;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.aol.cyclops.closures.Convertable#toCompletableFutureAsync()
     */
    @Override
    public CompletableFuture<T> toCompletableFutureAsync() {
        return this.future;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops.closures.Convertable#toCompletableFutureAsync(java.util.
     * concurrent.Executor)
     */
    @Override
    public CompletableFuture<T> toCompletableFutureAsync(final Executor exec) {
        return this.future;
    }

    /**
     * Returns a new FutureW that, when this FutureW completes exceptionally is
     * executed with this FutureW exception as the argument to the supplied
     * function. Otherwise, if this FutureW completes normally, then the
     * returned FutureW also completes normally with the same value.
     * 
     * <pre>
     * {@code 
     *     FutureW.ofError(new RuntimeException())
     *            .recover(__ -> true)
     *            
     *    //FutureW[true]
     * 
     * }
     * </pre>
     * 
     * @param fn
     *            the function to use to compute the value of the returned
     *            FutureW if this FutureW completed exceptionally
     * @return the new FutureW
     */
    public FutureW<T> recover(final Function<Throwable, ? extends T> fn) {
        return FutureW.of(toCompletableFuture().exceptionally(fn));
    }

    /**
     * Map this FutureW differently depending on whether the previous stage
     * completed successfully or failed
     * 
     * <pre>
     * {@code 
     *  FutureW.ofResult(1)
     *         .map(i->i*2,e->-1);
     * //FutureW[2]
     * 
     * }</pre>
     * 
     * @param success
     *            Mapping function for successful outcomes
     * @param failure
     *            Mapping function for failed outcomes
     * @return New futureW mapped to a new state
     */
    public <R> FutureW<R> map(final Function<? super T, R> success, final Function<Throwable, R> failure) {
        return FutureW.of(future.thenApply(success)
                                .exceptionally(failure));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.aol.cyclops.lambda.monads.Functor#cast(java.lang.Class)
     */
    @Override
    public <U> FutureW<U> cast(final Class<? extends U> type) {

        return (FutureW<U>) MonadicValue.super.cast(type);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops.lambda.monads.Functor#peek(java.util.function.Consumer)
     */
    @Override
    public FutureW<T> peek(final Consumer<? super T> c) {

        return (FutureW<T>) MonadicValue.super.peek(c);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.aol.cyclops.lambda.monads.Functor#trampoline(java.util.function.
     * Function)
     */
    @Override
    public <R> FutureW<R> trampoline(final Function<? super T, ? extends Trampoline<? extends R>> mapper) {

        return (FutureW<R>) MonadicValue.super.trampoline(mapper);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return mkString();
    }

    /**
     * Construct a successfully completed FutureW from the given value
     * 
     * @param result
     *            To wrap inside a FutureW
     * @return FutureW containing supplied result
     */
    public static <T> FutureW<T> ofResult(final T result) {
        return FutureW.of(CompletableFuture.completedFuture(result));
    }

    /**
     * Construct a completed-with-error FutureW from the given Exception
     * 
     * @param error
     *            To wrap inside a FutureW
     * @return FutureW containing supplied error
     */
    public static <T> FutureW<T> ofError(final Throwable error) {
        final CompletableFuture<T> cf = new CompletableFuture<>();
        cf.completeExceptionally(error);

        return FutureW.<T> of(cf);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.aol.cyclops.types.Convertable#isPresent()
     */
    @Override
    public boolean isPresent() {
        return !this.future.isCompletedExceptionally();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.aol.cyclops.types.Value#mkString()
     */
    @Override
    public String mkString() {

        if(future.isDone()){
            if(!future.isCompletedExceptionally())
                return "FutureW[" + future.join() + "]";
        }
        return "FutureW[" + future.toString() + "]";
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops.types.Filterable#filter(java.util.function.Predicate)
     */
    @Override
    public Maybe<T> filter(final Predicate<? super T> fn) {
        return toMaybe().filter(fn);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.aol.cyclops.types.Filterable#ofType(java.lang.Class)
     */
    @Override
    public <U> Maybe<U> ofType(final Class<? extends U> type) {

        return (Maybe<U>) MonadicValue.super.ofType(type);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops.types.Filterable#filterNot(java.util.function.Predicate)
     */
    @Override
    public Maybe<T> filterNot(final Predicate<? super T> fn) {

        return (Maybe<T>) MonadicValue.super.filterNot(fn);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.aol.cyclops.types.Filterable#notNull()
     */
    @Override
    public Maybe<T> notNull() {

        return (Maybe<T>) MonadicValue.super.notNull();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.aol.cyclops.types.Convertable#toOptional()
     */
    @Override
    public Optional<T> toOptional() {
        if (future.isDone() && future.isCompletedExceptionally())
            return Optional.empty();

        try {
            return Optional.ofNullable(get());
        } catch (final Throwable t) {
            return Optional.empty();
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.aol.cyclops.types.Convertable#toFutureWAsync()
     */
    @Override
    public FutureW<T> toFutureWAsync() {
        return this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops.types.Convertable#toFutureWAsync(java.util.concurrent.
     * Executor)
     */
    @Override
    public FutureW<T> toFutureWAsync(final Executor ex) {
        return this;
    }

    /*
     * Apply a function across two values at once. (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops.types.applicative.ApplicativeFunctor#combine(com.aol.
     * cyclops.types.Value, java.util.function.BiFunction)
     */
    @Override
    public <T2, R> FutureW<R> combine(final Value<? extends T2> app, final BiFunction<? super T, ? super T2, ? extends R> fn) {
        if (app instanceof FutureW) {
            return FutureW.of(future.thenCombine(((FutureW<T2>) app).getFuture(), fn));
        }
        return (FutureW<R>) MonadicValue.super.zip(app, fn);
    }

    /*
     * Equivalent to combine, but accepts an Iterable and takes the first value
     * only from that iterable. (non-Javadoc)
     * 
     * @see com.aol.cyclops.types.Zippable#zip(java.lang.Iterable,
     * java.util.function.BiFunction)
     */
    @Override
    public <T2, R> FutureW<R> zip(final Iterable<? extends T2> app, final BiFunction<? super T, ? super T2, ? extends R> fn) {

        return (FutureW<R>) MonadicValue.super.zip(app, fn);
    }

    /*
     * Equivalent to combine, but accepts a Publisher and takes the first value
     * only from that publisher.
     * 
     * (non-Javadoc)
     * 
     * @see com.aol.cyclops.types.Zippable#zip(java.util.function.BiFunction,
     * org.reactivestreams.Publisher)
     */
    @Override
    public <T2, R> FutureW<R> zipP(final Publisher<? extends T2> app, final BiFunction<? super T, ? super T2, ? extends R> fn) {
        return (FutureW<R>) MonadicValue.super.zipP(app,fn);

    }

    /**
     * Create a FutureW object that asyncrhonously populates using the Common
     * ForkJoinPool from the user provided Supplier
     * 
     * @param s
     *            Supplier to asynchronously populate results from
     * @return FutureW asynchronously populated from the Supplier
     */
    public static <T> FutureW<T> ofSupplier(final Supplier<T> s) {
        return FutureW.of(CompletableFuture.supplyAsync(s));
    }

    /**
     * Create a FutureW object that asyncrhonously populates using the provided
     * Executor and Supplier
     * 
     * @param s
     *            Supplier to asynchronously populate results from
     * @param ex
     *            Executro to asynchronously populate results with
     * @return FutureW asynchronously populated from the Supplier
     */
    public static <T> FutureW<T> ofSupplier(final Supplier<T> s, final Executor ex) {
        return FutureW.of(CompletableFuture.supplyAsync(s, ex));
    }



    /*
     * (non-Javadoc)
     * 
     * @see com.aol.cyclops.types.Zippable#zip(java.util.stream.Stream,
     * java.util.function.BiFunction)
     */
    @Override
    public <U, R> FutureW<R> zipS(final Stream<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {
        return (FutureW<R>) MonadicValue.super.zipS(other, zipper);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.aol.cyclops.types.Zippable#zip(java.util.stream.Stream)
     */
    @Override
    public <U> FutureW<Tuple2<T, U>> zipS(final Stream<? extends U> other) {
        return (FutureW) MonadicValue.super.zipS(other);
    }



    /*
     * (non-Javadoc)
     * 
     * @see com.aol.cyclops.types.Zippable#zip(java.lang.Iterable)
     */
    @Override
    public <U> FutureW<Tuple2<T, U>> zip(final Iterable<? extends U> other) {
        return (FutureW) MonadicValue.super.zip(other);
    }

    
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.MonadicValue#flatMapIterable(java.util.function.Function)
     */
    @Override
    public <R> FutureW<R> flatMapIterable(final Function<? super T, ? extends Iterable<? extends R>> mapper) {
        return (FutureW<R>) MonadicValue.super.flatMapIterable(mapper);
    }

    
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.MonadicValue#flatMapPublisher(java.util.function.Function)
     */
    @Override
    public <R> FutureW<R> flatMapPublisher(final Function<? super T, ? extends Publisher<? extends R>> mapper) {
        return (FutureW<R>) MonadicValue.super.flatMapPublisher(mapper);
    }

    @Override
    public FutureW<T> zip(BinaryOperator<Zippable<T>> combiner, Zippable<T> app) {
        return (FutureW<T>)MonadicValue.super.zip(combiner, app);
    }


    public CompletableFuture<T> getFuture() {
        return this.future;
    }
}
