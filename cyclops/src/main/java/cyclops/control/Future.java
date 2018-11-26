package cyclops.control;


import com.oath.cyclops.hkt.Higher;


import com.oath.cyclops.types.OrElseValue;
import com.oath.cyclops.types.foldable.To;
import com.oath.cyclops.types.functor.ReactiveTransformable;
import com.oath.cyclops.types.reactive.Completable;
import com.oath.cyclops.types.recoverable.RecoverableFrom;
import cyclops.data.tuple.Tuple;
import cyclops.data.tuple.Tuple3;
import cyclops.data.tuple.Tuple4;
import cyclops.function.Monoid;
import cyclops.function.Reducer;
import com.oath.cyclops.hkt.DataWitness.future;


import com.oath.cyclops.types.MonadicValue;
import com.oath.cyclops.types.Zippable;

import com.oath.cyclops.util.box.Mutable;

import cyclops.companion.CompletableFutures;

import com.oath.cyclops.types.reactive.ValueSubscriber;
import cyclops.function.Function3;
import cyclops.function.Function4;
import cyclops.reactive.ReactiveSeq;

import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import cyclops.data.tuple.Tuple2;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;


import java.util.concurrent.*;


/**
 * A Wrapper around CompletableFuture that implements cyclops interfaces and provides a more standard api
 *
 * e.g.
 *   transform instead of thenApply
 *   flatMap instead of thenCompose
 *   combine instead of thenCombine (applicative functor ap)
 *
 * @author johnmcclean
 *
 * @param <T> Type of wrapped future value
 */
@AllArgsConstructor
@EqualsAndHashCode
public class Future<T> implements To<Future<T>>,
                                  MonadicValue<T>,
                                  Completable<T>,
                                  Higher<future,T>,
                                  RecoverableFrom<Throwable,T>,
                                  Zippable<T>,
                                  ReactiveTransformable<T>,
                                  OrElseValue<T,Future<T>> {


    public static  <T,R> Future<R> tailRec(T initial, Function<? super T, ? extends Future<? extends Either<T, R>>> fn){
      Future<? extends Either<T, R>> ft = fn.apply(initial);
      return ft.flatMap(e -> e.fold(t->Future.of(() -> Future.<T,R>tailRec(t,fn)).flatMap(a->a),r->Future.ofResult(r)));
    }
    public static <T> Higher<future, T> widen(Future<T> narrow) {
    return narrow;
  }



    @Override
    public final void subscribe(final Subscriber<? super T> sub) {
        Mutable<Future<T>> future = Mutable.of(this);
        sub.onSubscribe(new Subscription() {

            AtomicBoolean running = new AtomicBoolean(
                    true);
            AtomicBoolean cancelled = new AtomicBoolean(false);

            @Override
            public void request(final long n) {

                if (n < 1) {
                    sub.onError(new IllegalArgumentException(
                            "3.9 While the Subscription is not cancelled, Subscription.request(long n) MUST throw a java.lang.IllegalArgumentException if the argument is <= 0."));
                }

                if (!running.compareAndSet(true, false)) {

                    return;

                }
                    future.mutate(f -> f.peek(sub::onNext)
                            .recover(t -> {
                                sub.onError(t);
                                return null;
                            })
                            .peek(i -> sub.onComplete()));


            }


            @Override
            public void cancel() {

                cancelled.set(true);
                future.get().cancel();

            }

        });

    }
    /**
     * Convert the raw Higher Kinded Type for  FutureType types into the FutureType type definition class
     *
     * @param future HKT encoded list into a FutureType
     * @return FutureType
     */
    public static <T> Future<T> narrowK(final Higher<future, T> future) {
        return (Future<T>)future;
    }

    /**
     * An zero Future
     *
     * @return A Future that wraps a CompletableFuture with a null result
     */
    public static <T> Future<T> empty() {
        return new Future<>(
                           CompletableFuture.completedFuture(null));
    }
    /**
     * An zero Future
     *
     * @return A Future that wraps a CompletableFuture with a null result
     */
    public static <T> Future<T> future() {
        return new Future<>(
                           new CompletableFuture<>());
    }

    /**
     * Select the first Future to complete
     *
     * @see CompletableFuture#anyOf(CompletableFuture...)
     * @param fts Futures to race
     * @return First Future to complete
     */
    public static <T> Future<T> anyOf(Future<T>... fts) {
      CompletableFuture<T>[] array = new CompletableFuture[fts.length];
      for(int i=0;i<fts.length;i++){
        array[i] = fts[i].getFuture();
      }

       return (Future<T>) Future.of(CompletableFuture.anyOf(array));
    }
    /**
     * Wait until all the provided Future's to complete
     *
     * @see CompletableFuture#allOf(CompletableFuture...)
     *
     * @param fts Futures to  wait on
     * @return Future that completes when all the provided Futures Complete. Empty Future result, or holds an Exception
     *         from a provided Future that failed.
     */
    public static <T> Future<T> allOf(Future<T>... fts) {
      CompletableFuture<T>[] array = new CompletableFuture[fts.length];
      for(int i=0;i<fts.length;i++){
        array[i] = fts[i].getFuture();
      }
        return (Future<T>) Future.of(CompletableFuture.allOf(array));
     }

    /**
     * Select the first Future to return with a successful result
     *
     * <pre>
     * {@code
     * Future<Integer> ft = Future.future();
       Future<Integer> result = Future.firstSuccess(Future.of(()->1),ft);

       ft.complete(10);
       result.getValue() //1
     * }
     * </pre>
     *
     * @param fts Futures to race
     * @return First Future to return with a result
     */
    @SafeVarargs
    public static <T> Future<T> firstSuccess(Future<T>... fts) {
        Future<T> future = Future.future();
        Stream.of(fts)
              .forEach(f->f.peek(r->future.complete(r)));
        Future<T> all = allOf(fts).recover(e->{ future.completeExceptionally(e); return null;});
        return future;

      }

    /**
     * Complete this Future with an Exception
     * @see CompletableFuture#completeExceptionally(Throwable)
     *
     * @param e Throwable to complete this Future with
     */
    public boolean completeExceptionally(Throwable e) {
        return this.future.completeExceptionally(e);

    }
    /**
     * Construct a Future asynchronously that contains a single value extracted from the supplied reactive-streams Publisher
     *
     *
     * <pre>
     * {@code
     *   ReactiveSeq<Integer> stream =  ReactiveSeq.of(1,2,3);

        Future<Integer> future = Future.fromPublisher(stream,ex);

        //Future[1]
     *
     * }
     * </pre>
     *
     *
     * @param pub Publisher to extract value from
     * @param ex Executor to extract value on
     * @return Future populated asyncrhonously from Publisher
     */
    public static <T> Future<T> fromPublisher(final Publisher<T> pub, final Executor ex) {
        final ValueSubscriber<T> sub = ValueSubscriber.subscriber();
        pub.subscribe(sub);
        return sub.toFutureAsync(ex);
    }

    /**
     * Construct a Future asyncrhonously that contains a single value extracted from the supplied Iterable
     * <pre>
     * {@code
     *  ReactiveSeq<Integer> stream =  ReactiveSeq.of(1,2,3);

        Future<Integer> future = Future.fromIterable(stream,ex);

        //Future[1]
     *
     * }
     * </pre>
     * @param iterable Iterable to generate a Future from
     * @param ex  Executor to extract value on
     * @return Future populated asyncrhonously from Iterable
     */
    public static <T> Future<T> fromIterable(final Iterable<T> iterable, final Executor ex) {

        return Future.of(() -> Eval.fromIterable(iterable))
                      .map(e -> e.get());
    }

    /**
     * Construct a Future syncrhonously that contains a single value extracted from the supplied reactive-streams Publisher
     * <pre>
     * {@code
     *   ReactiveSeq<Integer> stream =  ReactiveSeq.of(1,2,3);

        Future<Integer> future = Future.fromPublisher(stream);

        //Future[1]
     *
     * }
     * </pre>
     * @param pub Publisher to extract value from
     * @return Future populated syncrhonously from Publisher
     */
    public static <T> Future<T> fromPublisher(final Publisher<? extends T> pub) {
        if (pub instanceof Future){
            return (Future<T>) pub;
        }
        Future<T> result = future();

        pub.subscribe(new Subscriber<T>() {
            @Override
            public void onSubscribe(Subscription s) {
                s.request(1l);
            }

            @Override
            public void onNext(T t) {
                result.complete(t);
            }

            @Override
            public void onError(Throwable t) {
                result.completeExceptionally(t);
            }

            @Override
            public void onComplete() {
                if(!result.isDone())  {
                    result.completeExceptionally(new NoSuchElementException());
                }
            }
        });
        return result;
    }

    /**
     * Construct a Future syncrhonously that contains a single value extracted from the supplied Iterable
     *
     * <pre>
     * {@code
     *  ReactiveSeq<Integer> stream =  ReactiveSeq.of(1,2,3);

        Future<Integer> future = Future.fromIterable(stream);

        //Future[1]
     *
     * }
     * </pre>
     *
     *
     * @param iterable Iterable to extract value from
     * @return Future populated syncrhonously from Iterable
     */
    public static <T> Future<T> fromIterable(final Iterable<T> iterable) {
        if(iterable instanceof Future){
            return (Future)iterable;
        }
        return Future.ofResult(Eval.fromIterable(iterable))
                      .map(e -> e.get());
    }

    /**
     * Create a Future instance from the supplied CompletableFuture
     *
     * @param f CompletableFuture to wrap as a Future
     * @return Future wrapping the supplied CompletableFuture
     */
    public static <T> Future<T> of(final CompletableFuture<T> f) {
        return new Future<>(
                             f);
    }


    /**
     * Construct a Future syncrhonously from the Supplied Try
     *
     * @param value Try to populate Future from
     * @return Future populated with lazy the value or error in provided Try
     */
    public static <T, X extends Throwable> Future<T> fromTry(final Try<T, X> value) {

        return value.fold(s->Future.ofResult(s), e->Future.<T>of(CompletableFutures.error(e)));
    }

    /**
     * Schedule the population of a Future from the provided Supplier, the provided Cron (Quartz format) expression will be used to
     * trigger the population of the Future. The provided ScheduledExecutorService provided the thread on which the
     * Supplier will be executed.
     *
     * <pre>
     * {@code
     *
     *    Future<String> future = Future.schedule("* * * * * ?", Executors.newScheduledThreadPool(1), ()->"hello");
     *
     *    //Future["hello"]
     *
     * }</pre>
     *
     *
     * @param cron Cron expression in Quartz format
     * @param ex ScheduledExecutorService used to execute the provided Supplier
     * @param t The Supplier to execute to populate the Future
     * @return Future populated on a Cron based Schedule
     */
    public static <T> Future<T> schedule(final String cron, final ScheduledExecutorService ex, final Supplier<T> t) {

        final CompletableFuture<T> future = new CompletableFuture<>();
        final Future<T> wrapped = Future.of(future);
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
     * Schedule the population of a Future from the provided Supplier after the specified delay. The provided ScheduledExecutorService provided the thread on which the
     * Supplier will be executed.
     * <pre>
     * {@code
     *
     *    Future<String> future = Future.schedule(10l, Executors.newScheduledThreadPool(1), ()->"hello");
     *
     *    //Future["hello"]
     *
     * }</pre>
     *
     * @param delay Delay after which the Future should be populated
     * @param ex ScheduledExecutorService used to execute the provided Supplier
     * @param t he Supplier to execute to populate the Future
     * @return Future populated after the specified delay
     */
    public static <T> Future<T> schedule(final long delay, final ScheduledExecutorService ex, final Supplier<T> t) {
        final CompletableFuture<T> future = new CompletableFuture<>();
        final Future<T> wrapped = Future.of(future);

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
     * Asynchronous sequence operation that convert a Collection of Futures to a Future with a List
     *
     * <pre>
     * {@code
     *   Future<Seq<Integer>> futures =Future.sequence(Seq.of(Future.ofResult(10),Future.ofResult(1)));
         //Seq.of(10,1)
     *
     * }
     * </pre>
     *
     *
     * @param fts Collection of Futures to Sequence into a Future with a List
     * @return Future with a List
     */
    public static <T> Future<ReactiveSeq<T>> sequence(final Iterable<? extends Future<T>> fts) {
        return sequence(ReactiveSeq.fromIterable(fts));

    }

    /**
     * Sequence operation that convert a Stream of Futures to a Future with a Stream
     *
     * <pre>
     * {@code
     *   Future<Integer> just = Future.ofResult(10);
     *   Future<ReactiveSeq<Integer>> futures =Future.sequence(Stream.of(just,Future.ofResult(1)));
         //Seq.of(10,1)
     *
     * }
     * </pre>
     *
     * @param fts Strean of Futures to Sequence into a Future with a Stream
     * @return Future with a Stream
     */
    public static <T> Future<ReactiveSeq<T>> sequence(final Stream<? extends Future<T>> fts) {
        return sequence(ReactiveSeq.fromStream(fts));
    }
  public static  <T> Future<ReactiveSeq<T>> sequence(ReactiveSeq<? extends Future<T>> stream) {

    Future<ReactiveSeq<T>> identity = Future.ofResult(ReactiveSeq.empty());

    BiFunction<Future<ReactiveSeq<T>>,Future<T>,Future<ReactiveSeq<T>>> combineToStream = (acc,next) ->acc.zip(next,(a,b)->a.append(b));

    BinaryOperator<Future<ReactiveSeq<T>>> combineStreams = (a,b)-> a.zip(b,(z1,z2)->z1.appendStream(z2));

    return stream.reduce(identity,combineToStream,combineStreams);
  }
  public static <T,R> Future<ReactiveSeq<R>> traverse(Function<? super T,? extends R> fn,ReactiveSeq<Future<T>> stream) {
    ReactiveSeq<Future<R>> s = stream.map(h -> h.map(fn));
    return sequence(s);
  }

    /**
     *
     * Asynchronously accumulate the results only from those Futures which have completed successfully.
     * Also @see {@link Future#accumulate(Iterable, Reducer)} if you would like a failure to result in a Future
     * with an error
     * <pre>
     * {@code
     *
     * Future<Integer> just =Future.of(CompletableFuture.completedFuture(10));
       Future<Integer> none = Future.ofError(new NoSuchElementException());


     * Future<PersistentSetX<Integer>> futures = Future.accumulateSuccess(Seq.of(just,none,Future.ofResult(1)),Reducers.toPersistentSetX());
       //Future[PersistentSetX[10,1]]
     *  }
     *  </pre>
     *
     * @param fts Collection of Futures to accumulate successes
     * @param reducer Reducer to accumulate results
     * @return Future asynchronously populated with the accumulate success operation
     */
    public static <T, R> Future<R> accumulateSuccess(final Iterable<Future<T>> fts, final Reducer<R,T> reducer) {
       return Future.of(CompletableFutures.accumulateSuccess(ReactiveSeq.fromIterable(fts).map(Future::getFuture), reducer));
    }
    /**
     * Asynchronously accumulate the results of Futures, a single failure will cause a failed result, using the supplied Reducer {@see cyclops2.Reducers}
     * <pre>
     * {@code
     *
     * Future<Integer> just =Future.of(CompletableFuture.completedFuture(10));
       Future<Integer> none = Future.ofError(new NoSuchElementException());
     * Future<PersistentSetX<Integer>> futures = Future.accumulateSuccess(Seq.of(just,none,Future.ofResult(1)),Reducers.toPersistentSetX());

       //Future[PersistentSetX[10,1]]
     *  }
     *  </pre>
     *
     * @param fts Collection of Futures to accumulate successes
     * @param reducer Reducer to accumulate results
     * @return Future asynchronously populated with the accumulate success operation
     */
    public static <T, R> Future<R> accumulate(final Iterable<Future<T>> fts, final Reducer<R,T> reducer) {
        return sequence(fts).map(s -> s.foldMap(reducer));
    }
    /**
     * Asynchronously accumulate the results only from those Futures which have completed successfully, using the supplied mapping function to
     * convert the data from each Future before reducing them using the supplied Monoid (a combining BiFunction/BinaryOperator and identity element that takes two
     * input values of the same type and returns the combined result) {@see cyclops2.Monoids }.
     *
     * <pre>
     * {@code
     * Future<String> future = Future.accumulate(Seq.of(Future.ofResult(10),Future.ofResult(1)),i->""+i,Monoids.stringConcat);
        //Future["101"]
     * }
     * </pre>
     *
     * @param fts Collection of Futures to accumulate successes
     * @param mapper Mapping function to be applied to the result of each Future
     * @param reducer Monoid to combine values from each Future
     * @return Future asynchronously populated with the accumulate operation
     */
    public static <T, R> Future<R> accumulateSuccess(final Iterable<Future<T>> fts, final Function<? super T, R> mapper, final Monoid<R> reducer) {
        return Future.of(CompletableFutures.accumulateSuccess(ReactiveSeq.fromIterable(fts).map(Future::getFuture),mapper,reducer));
    }

    /**
     * Asynchronously accumulate the results only from those Futures which have completed successfully,
     *  reducing them using the supplied Monoid (a combining BiFunction/BinaryOperator and identity element that takes two
     * input values of the same type and returns the combined result) {@see cyclops2.Monoids }.
     *
     * <pre>
     * {@code
     * Future<Integer> just =Future.of(CompletableFuture.completedFuture(10));
     * Future<Integer> future =Future.accumulate(Monoids.intSum, Seq.of(just,Future.ofResult(1)));
       //Future[11]
     * }
     * </pre>
     *
     *
     * @param fts Collection of Futures to accumulate successes
     * @param reducer Monoid to combine values from each Future
     * @return Future asynchronously populated with the accumulate operation
     */
    public static <T> Future<T> accumulateSuccess(final Monoid<T> reducer, final Iterable<Future<T>> fts ) {
        return Future.of(CompletableFutures.accumulateSuccess(reducer,ReactiveSeq.fromIterable(fts).map(Future::getFuture)));
    }

    /**
     * Asynchronously accumulate the results of a batch of Futures which using the supplied mapping function to
     * convert the data from each Future before reducing them using the supplied supplied Monoid (a combining BiFunction/BinaryOperator and identity element that takes two
     * input values of the same type and returns the combined result) {@see cyclops2.Monoids }.
     * A single Failure results in a Failed  Future.
     *
     * <pre>
     * {@code
     * Future<String> future = Future.accumulate(Seq.of(Future.ofResult(10),Future.ofResult(1)),i->""+i,Monoids.stringConcat);
        //Future["101"]
     * }
     * </pre>
     *
     * @param fts Collection of Futures to accumulate successes
     * @param mapper Mapping function to be applied to the result of each Future
     * @param reducer Monoid to combine values from each Future
     * @return Future asynchronously populated with the accumulate operation
     */
    public static <T, R> Future<R> accumulate(final Iterable<Future<T>> fts, final Function<? super T, R> mapper, final Monoid<R> reducer) {
        return sequence(fts).map(s -> s.map(mapper)
                                       .reduce(reducer)
                                       );
    }

    /**
     * Asynchronously accumulate the results only from the provided Futures,
     *  reducing them using the supplied Monoid (a combining BiFunction/BinaryOperator and identity element that takes two
     * input values of the same type and returns the combined result) {@see cyclops2.Monoids }
     *
     * A single Failure results in a Failed  Future.
     *
     * <pre>
     * {@code
     * Future<Integer> future =Future.accumulate(Monoids.intSum,Seq.of(just,Future.ofResult(1)));
       //Future[11]
     * }
     * </pre>
     *
     *
     * @param fts Collection of Futures to accumulate successes
     * @param reducer Monoid to combine values from each Future
     * @return Future asynchronously populated with the accumulate operation
     */
    public static <T> Future<T> accumulate(final Monoid<T> reducer, final Iterable<Future<T>> fts) {
        return sequence(fts).map(s -> s.reduce(reducer)
                                      );
    }



    /* (non-Javadoc)
     * @see com.oath.cyclops.types.MonadicValue#forEach4(java.util.function.Function, java.util.function.BiFunction, com.oath.cyclops.util.function.TriFunction, com.oath.cyclops.util.function.QuadFunction)
     */
    @Override
    public <T2, R1, R2, R3, R> Future<R> forEach4(Function<? super T, ? extends MonadicValue<R1>> value1,
                                                  BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
                                                  Function3<? super T, ? super R1, ? super R2, ? extends MonadicValue<R3>> value3,
                                                  Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        return (Future<R>)MonadicValue.super.forEach4(value1, value2, value3, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.MonadicValue#forEach4(java.util.function.Function, java.util.function.BiFunction, com.oath.cyclops.util.function.TriFunction, com.oath.cyclops.util.function.QuadFunction, com.oath.cyclops.util.function.QuadFunction)
     */
    @Override
    public <T2, R1, R2, R3, R> Future<R> forEach4(Function<? super T, ? extends MonadicValue<R1>> value1,
                                                  BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
                                                  Function3<? super T, ? super R1, ? super R2, ? extends MonadicValue<R3>> value3,
                                                  Function4<? super T, ? super R1, ? super R2, ? super R3, Boolean> filterFunction,
                                                  Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {

        return (Future<R>)MonadicValue.super.forEach4(value1, value2, value3, filterFunction, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.MonadicValue#forEach3(java.util.function.Function, java.util.function.BiFunction, com.oath.cyclops.util.function.TriFunction)
     */
    @Override
    public <T2, R1, R2, R> Future<R> forEach3(Function<? super T, ? extends MonadicValue<R1>> value1,
                                              BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
                                              Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return (Future<R>)MonadicValue.super.forEach3(value1, value2, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.MonadicValue#forEach3(java.util.function.Function, java.util.function.BiFunction, com.oath.cyclops.util.function.TriFunction, com.oath.cyclops.util.function.TriFunction)
     */
    @Override
    public <T2, R1, R2, R> Future<R> forEach3(Function<? super T, ? extends MonadicValue<R1>> value1,
                                              BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
                                              Function3<? super T, ? super R1, ? super R2, Boolean> filterFunction,
                                              Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return (Future<R>)MonadicValue.super.forEach3(value1, value2, filterFunction, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.MonadicValue#forEach2(java.util.function.Function, java.util.function.BiFunction)
     */
    @Override
    public <R1, R> Future<R> forEach2(Function<? super T, ? extends MonadicValue<R1>> value1,
                                      BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {

        return (Future<R>)MonadicValue.super.forEach2(value1, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.MonadicValue#forEach2(java.util.function.Function, java.util.function.BiFunction, java.util.function.BiFunction)
     */
    @Override
    public <R1, R> Future<R> forEach2(Function<? super T, ? extends MonadicValue<R1>> value1,
                                      BiFunction<? super T, ? super R1, Boolean> filterFunction,
                                      BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {
        return (Future<R>)MonadicValue.super.forEach2(value1, filterFunction, yieldingFunction);
    }



    @Override
    public Maybe<T> toMaybe() {

        return Maybe.fromFuture(this);
    }

    private final CompletableFuture<T> future;

    /**
     * Non-blocking visit on the state of this Future
     *
     * <pre>
     * {@code
     * Future.ofResult(10)
              .visitAsync(i->i*2, e->-1);
       //Future[20]

       Future.<Integer>ofError(new RuntimeException())
              .visitAsync(i->i*2, e->-1)
       //Future[-1]
     *
     * }
     * </pre>
     *
     * @param success Function to execute if the previous stage completes successfully
     * @param failure Function to execute if this Future fails
     * @return Future with the eventual result of the executed Function
     */
    @Deprecated //use foldAsync
    public <R> Future<R> visitAsync(Function<? super T,? extends R> success, Function<? super Throwable,? extends R> failure){
       return foldAsync(success,failure);
    }
    public <R> Future<R> foldAsync(Function<? super T,? extends R> success, Function<? super Throwable,? extends R> failure){
        Future<R> f = map(success);
        return f.recover(failure);
    }
    /**
     * Blocking analogue to visitAsync. Visit the state of this Future, block until ready.
     *
     * <pre>
     * {@code
     *  Future.ofResult(10)
               .visit(i->i*2, e->-1);
        //20

        Future.<Integer>ofError(new RuntimeException())
               .visit(i->i*2, e->-1)
        //[-1]
     *
     * }
     * </pre>
     * @param success Function to execute if the previous stage completes successfully
     * @param failure  Function to execute if this Future fails
     * @return Result of the executed Function
     */
    public <R> R fold(Function<? super T,? extends R> success, Function<? super Throwable,? extends R> failure){
        try {
            return success.apply(future.join());
        }catch(Throwable t){
            return failure.apply(t);
        }

    }



  /*
   * (non-Javadoc)
   *
   * @see
   * com.oath.cyclops.types.ConvertableFunctor#transform(java.util.function.Function)
   */
    @Override
    public <R> Future<R> map(final Function<? super T, ? extends R> fn) {
        return new Future<R>(
                              future.thenApply(fn));
    }
    /**
     * Asyncrhonous transform operation
     *
     * @see CompletableFuture#thenApplyAsync(Function, Executor)
     *
     * @param fn Transformation function
     * @param ex Executor to execute the transformation asynchronously
     * @return Mapped Future
     */
    public <R> Future<R> map(final Function<? super T, ? extends R> fn, Executor ex) {
        return new Future<R>(
                              future.thenApplyAsync(fn,ex));
    }



    /*
     * (non-Javadoc)
     *
     * @see java.util.function.Supplier#getValue()
     */

    public T getOrElse(T alt) {
        try {
            return future.join();
        } catch (final Throwable t) {
            return alt;
        }
    }

    public Try<T,Throwable> get(){
        try {
            return Try.success(future.join());
        } catch (final CompletionException t) {
            return Try.failure(t.getCause());
        }
    }

    /**
     * @return true if this Future is both complete, and completed without an
     *         Exception
     */
    public boolean isSuccess() {
        return future.isDone() && !future.isCompletedExceptionally();
    }
    /**
     * @see java.util.concurrent.CompletableFuture#isDone
     * @return true if this Future has completed executing
     */
    public boolean isDone(){

        return future.isDone();
    }
    /**
     * @see java.util.concurrent.CompletableFuture#isCancelled
     * @return True if this Future has been cancelled
     */
    public boolean isCancelled(){
        return future.isCancelled();
    }
    /**
     *  If not already completed, completes this Future with a {@link java.util.concurrent.CancellationException}
     *  Passes true to @see java.util.concurrent.CompletableFuture#cancel as mayInterruptIfRunning parameter on that method
     *  has no effect for the default CompletableFuture implementation
     */
    public void cancel(){
        future.cancel(true);
    }
    /**If not already completed, sets the value of this Future to the provided value
     *
     * @param value Value to set this Future to
     */
    public boolean complete(T value){
        return future.complete(value);
    }

    /**
     * @return true if this Future is complete, but completed with an Exception
     */
    public boolean isFailed() {
        return future.isCompletedExceptionally();
    }



    /*
     * (non-Javadoc)
     *
     * @see com.oath.cyclops.lambda.monads.Pure#unit(java.lang.Object)
     */
    @Override
    public <T> Future<T> unit(final T unit) {
        return new Future<T>(
                              CompletableFuture.completedFuture(unit));
    }

    @Override
    public <T1> Future<T1> emptyUnit() {
        return future();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.oath.cyclops.types.Value#stream()
     */
    @Override
    public ReactiveSeq<T> stream() {
        return ReactiveSeq.generate(() -> get())
                          .limit(1)
                          .filter(t -> t.isSuccess())
                          .map(t->t.orElse(null));
    }

    /*
     * (non-Javadoc)
     *
     * @see com.oath.cyclops.types.FlatMap#flatten()
     */
    public static <R> Future<R> flatten(Future<? extends Future<R>> nested) {
        return nested.flatMap(Function.identity());
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.oath.cyclops.types.MonadicValue#flatMap(java.util.function.Function)
     */
    @Override
    public <R> Future<R> flatMap(final Function<? super T, ? extends MonadicValue<? extends R>> mapper) {
        return Future.<R>of(future.<R>thenCompose(t -> (CompletionStage<R>) Future.fromMonadicValue(mapper.apply(t)).getFuture()));
    }

    private static <R> Future<R> fromMonadicValue(MonadicValue<R> apply) {
        if(apply instanceof Future)
            return (Future<R>)apply;
        return Future.fromPublisher(apply);
    }

    /**
     * A flatMap operation that accepts a CompleteableFuture CompletionStage as
     * the return type
     *
     * @param mapper
     *            Mapping function
     * @return FlatMapped Future
     */
    public <R> Future<R> flatMapCf(final Function<? super T, ? extends CompletionStage<? extends R>> mapper) {
        return Future.<R> of(future.<R> thenCompose(t -> (CompletionStage<R>) mapper.apply(t)));
    }


    public Either<Throwable, T> toEither() {
        try {
            return Either.right(future.join());
        } catch (final Throwable t) {
            return Either.<Throwable, T>left(t.getCause());
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.oath.cyclops.types.Value#toIor()
     */
    public Ior<Throwable, T> toIor() {
        try {
            return Ior.right(future.join());
        } catch (final Throwable t) {
            return Ior.<Throwable, T>left(t.getCause());
        }
    }


    /*
     * (non-Javadoc)
     *
     * @see com.oath.cyclops.closures.Convertable#toCompletableFuture()
     */
    public CompletableFuture<T> toCompletableFuture() {
        return this.future;
    }



    /**
     * Returns a new Future that, when this Future completes exceptionally is
     * executed with this Future exception as the argument to the supplied
     * function. Otherwise, if this Future completes normally, applyHKT the
     * returned Future also completes normally with the same value.
     *
     * <pre>
     * {@code
     *     Future.ofError(new RuntimeException())
     *            .recover(__ -> true)
     *
     *    //Future[true]
     *
     * }
     * </pre>
     *
     * @param fn
     *            the function to use to compute the value of the returned
     *            Future if this Future completed exceptionally
     * @return the new Future
     */
    public Future<T> recover(final Function<? super Throwable, ? extends T> fn) {
        return Future.of(toCompletableFuture().exceptionally((Function)fn));
    }
    public Future<T> recover(final Supplier<? extends T> fn) {
        return Future.of(toCompletableFuture().exceptionally(a->fn.get()));
    }

    /**
     * Map this Future differently depending on whether the previous stage
     * completed successfully or failed
     *
     * <pre>
     * {@code
     *  Future.ofResult(1)
     *         .map(i->i*2,e->-1);
     * //Future[2]
     *
     * }</pre>
     *
     * @param success
     *            Mapping function for successful outcomes
     * @param failure
     *            Mapping function for failed outcomes
     * @return New Future mapped to a new state
     */
    public <R> Future<R> map(final Function<? super T, R> success, final Function<Throwable, R> failure) {
        return Future.of(future.thenApply(success)
                                .exceptionally(failure));
    }


    /*
     * (non-Javadoc)
     *
     * @see
     * com.oath.cyclops.lambda.monads.Transformable#peek(java.util.function.Consumer)
     */
    @Override
    public Future<T> peek(final Consumer<? super T> c) {

        return (Future<T>) MonadicValue.super.peek(c);
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
     * Construct a successfully completed Future from the given value
     *
     * @param result
     *            To wrap inside a Future
     * @return Future containing supplied result
     */
    public static <T> Future<T> ofResult(final T result) {
        return Future.of(CompletableFuture.completedFuture(result));
    }

    /**
     * Construct a completed-with-error Future from the given Exception
     *
     * @param error
     *            To wrap inside a Future
     * @return Future containing supplied error
     */
    public static <T> Future<T> ofError(final Throwable error) {
        final CompletableFuture<T> cf = new CompletableFuture<>();
        cf.completeExceptionally(error);

        return Future.<T> of(cf);
    }


    @Override
    public boolean isPresent() {
        return !this.future.isCompletedExceptionally();
    }


    @Override
    public String mkString() {

        if(future.isDone()){
            if(!future.isCompletedExceptionally())
                return "Future[" + future.join() + "]";
        }
        return "Future[" + future.toString() + "]";
    }


    @Override
    public Maybe<T> filter(final Predicate<? super T> fn) {
        return toMaybe().filter(fn);
    }


    @Override
    public <U> Maybe<U> ofType(final Class<? extends U> type) {

        return (Maybe<U>) MonadicValue.super.ofType(type);
    }


    @Override
    public Maybe<T> filterNot(final Predicate<? super T> fn) {

        return (Maybe<T>) MonadicValue.super.filterNot(fn);
    }


    @Override
    public Maybe<T> notNull() {

        return (Maybe<T>) MonadicValue.super.notNull();
    }


  @Override
  public <U> Future<Tuple2<T, U>> zipWithPublisher(Publisher<? extends U> other) {
    return zip(Tuple::tuple,other);
  }

  @Override
  public <S, U> Future<Tuple3<T, S, U>> zip3(Iterable<? extends S> second, Iterable<? extends U> third) {
    return (Future)Zippable.super.zip3(second,third);
  }

  @Override
  public <S, U, R> Future<R> zip3(Iterable<? extends S> second, Iterable<? extends U> third, Function3<? super T, ? super S, ? super U, ? extends R> fn3) {
    return (Future<R>)Zippable.super.zip3(second,third,fn3);
  }

  @Override
  public <T2, T3, T4> Future<Tuple4<T, T2, T3, T4>> zip4(Iterable<? extends T2> second, Iterable<? extends T3> third, Iterable<? extends T4> fourth) {
    return (Future)Zippable.super.zip4(second,third,fourth);
  }

  @Override
  public <T2, T3, T4, R> Future<R> zip4(Iterable<? extends T2> second, Iterable<? extends T3> third, Iterable<? extends T4> fourth, Function4<? super T, ? super T2, ? super T3, ? super T4, ? extends R> fn) {
    return (Future<R>)Zippable.super.zip4(second,third,fourth,fn);
  }

  /*
     * Equivalent to combine, but accepts an Iterable and takes the first value
     * only from that iterable. (non-Javadoc)
     *
     * @see com.oath.cyclops.types.Zippable#zip(java.lang.Iterable,
     * java.util.function.BiFunction)
     */
    @Override
    public <T2, R> Future<R> zip(final Iterable<? extends T2> app, final BiFunction<? super T, ? super T2, ? extends R> fn) {

      return Future.of(future.thenCombine(Future.fromIterable(app).getFuture(), fn));
    }

    /*
     * Equivalent to combine, but accepts a Publisher and takes the first value
     * only from that publisher.
     *
     * (non-Javadoc)
     *
     * @see com.oath.cyclops.types.Zippable#zip(java.util.function.BiFunction,
     * org.reactivestreams.Publisher)
     */
    @Override
    public <T2, R> Future<R> zip(final BiFunction<? super T, ? super T2, ? extends R> fn, final Publisher<? extends T2> app) {
        return Future.of(future.thenCombine(Future.fromPublisher(app).getFuture(), fn));
    }


  /**
     * Create a Future object that asyncrhonously populates using the Common
     * ForkJoinPool from the user provided Supplier
     *
     * @param s
     *            Supplier to asynchronously populate results from
     * @return Future asynchronously populated from the Supplier
     */
    public static <T> Future<T> of(final Supplier<T> s) {
        return Future.of(CompletableFuture.supplyAsync(s));
    }

    /**
     * Create a Future object that asyncrhonously populates using the provided
     * Executor and Supplier
     *
     * @param s
     *            Supplier to asynchronously populate results from
     * @param ex
     *            Executro to asynchronously populate results with
     * @return Future asynchronously populated from the Supplier
     */
    public static <T> Future<T> of(final Supplier<T> s, final Executor ex) {
        return Future.of(CompletableFuture.supplyAsync(s, ex));
    }
    public static <T> Future<T> async(final Executor ex,final Supplier<T> s) {
        return of(s,ex);
    }




    /*
     * (non-Javadoc)
     *
     * @see com.oath.cyclops.types.Zippable#zip(java.lang.Iterable)
     */
    @Override
    public <U> Future<Tuple2<T, U>> zip(final Iterable<? extends U> other) {
        return (Future) Zippable.super.zip(other);
    }


    /* (non-Javadoc)
     * @see com.oath.cyclops.types.MonadicValue#concatMap(java.util.function.Function)
     */
    @Override
    public <R> Future<R> concatMap(final Function<? super T, ? extends Iterable<? extends R>> mapper) {
        return (Future<R>) MonadicValue.super.concatMap(mapper);
    }


    /* (non-Javadoc)
     * @see com.oath.cyclops.types.MonadicValue#flatMapP(java.util.function.Function)
     */
    @Override
    public <R> Future<R> mergeMap(final Function<? super T, ? extends Publisher<? extends R>> mapper) {
        return (Future<R>) MonadicValue.super.mergeMap(mapper);
    }


    public CompletableFuture<T> getFuture() {
        return this.future;
    }

    @Override
    public <R> R fold(Function<? super T,? extends R> success, Supplier<? extends R> failure){
        try {
            return success.apply(future.join());
        }catch(Throwable t){
            return failure.get();
        }
    }


  public static <T> Future<T> narrow(Future<? extends T> of) {
      return (Future<T>)of;
  }
}
