package cyclops.async;

import com.aol.cyclops2.hkt.Higher;
import com.aol.cyclops2.react.threads.SequentialElasticPools;
import cyclops.control.lazy.Trampoline;
import cyclops.typeclasses.*;
import com.aol.cyclops2.types.*;
import com.aol.cyclops2.types.foldable.To;
import com.aol.cyclops2.types.reactive.Completable;
import com.aol.cyclops2.types.recoverable.RecoverableFrom;
import cyclops.companion.Monoids;
import cyclops.collections.box.Mutable;
import cyclops.control.*;
import cyclops.control.lazy.Eval;
import cyclops.control.lazy.Maybe;
import cyclops.function.Monoid;
import cyclops.function.Reducer;
import cyclops.monads.Witness.future;
import cyclops.monads.transformers.FutureT;
import com.aol.cyclops2.data.collections.extensions.CollectionX;
import cyclops.collections.mutable.ListX;
import com.aol.cyclops2.react.Status;
import com.aol.cyclops2.react.collectors.lazy.Blocker;
import cyclops.monads.Witness;
import cyclops.monads.WitnessType;
import com.aol.cyclops2.types.reactive.ValueSubscriber;
import cyclops.companion.CompletableFutures;
import com.aol.cyclops2.util.ExceptionSoftener;
import cyclops.function.Function3;
import cyclops.function.Function4;
import cyclops.monads.AnyM;
import cyclops.stream.ReactiveSeq;
import cyclops.typeclasses.comonad.Comonad;
import cyclops.typeclasses.foldable.Foldable;
import cyclops.typeclasses.foldable.Unfoldable;
import cyclops.typeclasses.functor.Functor;
import cyclops.typeclasses.instances.General;
import cyclops.typeclasses.monad.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.experimental.UtilityClass;
import cyclops.collections.tuple.Tuple2;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A Wrapper around CompletableFuture that implements cyclops2-react interfaces and provides a more standard api
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
                                  OrElseValue<T,Future<T>>{

    public static  <T,R> Future<R> tailRec(T initial, Function<? super T, ? extends Future<? extends Either<T, R>>> fn){
        SimpleReact sr = SequentialElasticPools.simpleReact.nextReactor();
        return Future.of(()->{
            Future<? extends Either<T, R>> next[] = new Future[1];
            next[0]=Future.ofResult(Either.left(initial));
            boolean cont = true;
            do {
                cont = next[0].visit(p ->  p.visit(s -> {
                    next[0] = narrowK(fn.apply(s));
                    return true;
                }, pr -> false), () -> false);
            }while(cont);
            return next[0].map(x->x.orElse(null));
        }, sr.getExecutor()).flatMap(i->i)
                .peek(e->SequentialElasticPools.simpleReact.populate(sr)).recover(t->{
                    SequentialElasticPools.simpleReact.populate(sr);
                    throw ExceptionSoftener.throwSoftenedException(t);
                });
    }
    public static  <T> Kleisli<future,Future<T>,T> kindKleisli(){
        return Kleisli.of(Instances.monad(), Future::widen);
    }
    public static <T> Higher<future, T> widen(Future<T> narrow) {
        return narrow;
    }
    public static  <T> Cokleisli<future,T,Future<T>> kindCokleisli(){
        return Cokleisli.of(Future::narrowK);
    }
    public static <W1,T> Nested<future,W1,T> nested(Future<Higher<W1,T>> nested, InstanceDefinitions<W1> def2){
        return Nested.of(nested, Instances.definitions(),def2);
    }
    public <W1> Product<future,W1,T> product(Active<W1,T> active){
        return Product.of(allTypeclasses(),active);
    }
    public <W1> Coproduct<W1,future,T> coproduct(InstanceDefinitions<W1> def2){
        return Coproduct.right(this,def2, Instances.definitions());
    }
    public Active<future,T> allTypeclasses(){
        return Active.of(this, Instances.definitions());
    }
    public <W2,R> Nested<future,W2,R> mapM(Function<? super T,? extends Higher<W2,R>> fn, InstanceDefinitions<W2> defs){
        return Nested.of(map(fn), Instances.definitions(), defs);
    }
    public <W extends WitnessType<W>> FutureT<W, T> liftM(W witness) {
        return FutureT.of(witness.adapter().unit(this));
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

       return (Future<T>) Future.ofResult( (CompletableFuture<T>)CompletableFuture.anyOf(Stream.of(fts)
                                                                              .map(Future::getFuture)
                                                                              .collect(Collectors.toList())
                                                                              .toArray(new CompletableFuture[0])));
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

        return (Future<T>) Future.ofResult((CompletableFuture<T>)CompletableFuture.allOf(Stream.of(fts)
                                                                      .map(Future::getFuture)
                                                                      .collect(Collectors.toList())
                                                                      .toArray(new CompletableFuture[0])));
     }
    /**
     * Block until a Quorum of results have returned as determined by the provided Predicate
     *
     * <pre>
     * {@code
     *
     * Future<ListX<Integer>> strings = Future.quorum(status -> status.getCompleted() >0, Future.of(()->1),Future.future(),Future.future());


        strings.get().size()
        //1
     *
     * }
     * </pre>
     *
     *
     * @param breakout Predicate that determines whether the block should be
     *            continued or removed
     * @param fts Futures to  wait on results from
     * @param errorHandler Consumer to handle any exceptions thrown
     * @return Future which will be populated with a Quorum of results
     */
    @SafeVarargs
    public static <T> Future<ListX<T>> quorum(Predicate<Status<T>> breakout,Consumer<Throwable> errorHandler, Future<T>... fts) {

        List<CompletableFuture<?>> list = Stream.of(fts)
                                                .map(Future::getFuture)
                                                .collect(Collectors.toList());

        return Future.of(new Blocker<T>(list, Optional.of(errorHandler)).nonBlocking(breakout));


    }
    /**
     * Block until a Quorum of results have returned as determined by the provided Predicate
     *
     * <pre>
     * {@code
     *
     * Future<ListX<Integer>> strings = Future.quorum(status -> status.getCompleted() >0, Future.of(()->1),Future.future(),Future.future());


    strings.get().size()
    //1
     *
     * }
     * </pre>
     *
     *
     * @param breakout Predicate that determines whether the block should be
     *            continued or removed
     * @param fts Futures to  wait on results from
     * @return Future which will be populated with a Quorum of results
     */
    @SafeVarargs
    public static <T> Future<ListX<T>> quorum(Predicate<Status<T>> breakout,Future<T>... fts) {

        List<CompletableFuture<?>> list = Stream.of(fts)
                .map(Future::getFuture)
                .collect(Collectors.toList());

        return Future.of(new Blocker<T>(list, Optional.empty()).nonBlocking(breakout));


    }
    /**
     * Select the first Future to return with a successful result
     *
     * <pre>
     * {@code
     * Future<Integer> ft = Future.future();
       Future<Integer> result = Future.firstSuccess(Future.of(()->1),ft);

       ft.complete(10);
       result.get() //1
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
     * Construct a Future asyncrhonously that contains a singleUnsafe value extracted from the supplied reactiveBuffer-streams Publisher
     *
     *
     * <pre>
     * {@code
     *   ReactiveSeq<Integer> reactiveStream =  ReactiveSeq.of(1,2,3);

        Future<Integer> future = Future.fromPublisher(reactiveStream,ex);

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
     * Construct a Future asyncrhonously that contains a singleUnsafe value extracted from the supplied Iterable
     * <pre>
     * {@code
     *  ReactiveSeq<Integer> reactiveStream =  ReactiveSeq.of(1,2,3);

        Future<Integer> future = Future.fromIterable(reactiveStream,ex);

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
     * Construct a Future syncrhonously that contains a singleUnsafe value extracted from the supplied reactiveBuffer-streams Publisher
     * <pre>
     * {@code
     *   ReactiveSeq<Integer> reactiveStream =  ReactiveSeq.of(1,2,3);

        Future<Integer> future = Future.fromPublisher(reactiveStream);

        //Future[1]
     *
     * }
     * </pre>
     * @param pub Publisher to extract value from
     * @return Future populated syncrhonously from Publisher
     */
    public static <T> Future<T> fromPublisher(final Publisher<T> pub) {
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
                    result.complete(null);
                }
            }
        });
        return result;
    }

    /**
     * Construct a Future syncrhonously that contains a singleUnsafe value extracted from the supplied Iterable
     *
     * <pre>
     * {@code
     *  ReactiveSeq<Integer> reactiveStream =  ReactiveSeq.of(1,2,3);

        Future<Integer> future = Future.fromIterable(reactiveStream);

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

        return value.visit(s->Future.ofResult(s),e->Future.<T>of(CompletableFutures.error(e)));
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
     *   Future<ListX<Integer>> futures =Future.sequence(ListX.of(Future.ofResult(10),Future.ofResult(1)));
         //ListX.of(10,1)
     *
     * }
     * </pre>
     *
     *
     * @param fts Collection of Futures to Sequence into a Future with a List
     * @return Future with a List
     */
    public static <T> Future<ListX<T>> sequence(final CollectionX<Future<T>> fts) {
        return sequence(fts.stream()).map(s -> s.toListX());

    }

    /**
     * Sequence operation that convert a Stream of Futures to a Future with a Stream
     *
     * <pre>
     * {@code
     *   Future<Integer> just = Future.ofResult(10);
     *   Future<ReactiveSeq<Integer>> futures =Future.sequence(Stream.of(just,Future.ofResult(1)));
         //ListX.of(10,1)
     *
     * }
     * </pre>
     *
     * @param fts Strean of Futures to Sequence into a Future with a Stream
     * @return Future with a Stream
     */
    public static <T> Future<ReactiveSeq<T>> sequence(final Stream<? extends Future<T>> fts) {
        return AnyM.sequence(fts.map(AnyM::fromFuture), Witness.future.INSTANCE)
                   .map(ReactiveSeq::fromStream)
                   .to(Witness::future);
    }

    /**
     *
     * Asynchronously accumulate the results only from those Futures which have completed successfully.
     * Also @see {@link Future#accumulate(CollectionX, Reducer)} if you would like a failure to result in a Future
     * with an error
     * <pre>
     * {@code
     *
     * Future<Integer> just =Future.of(CompletableFuture.completedFuture(10));
       Future<Integer> none = Future.ofError(new NoSuchElementException());


     * Future<PersistentSetX<Integer>> futures = Future.accumulateSuccess(ListX.of(just,none,Future.ofResult(1)),Reducers.toPersistentSetX());
       //Future[PersistentSetX[10,1]]
     *  }
     *  </pre>
     *
     * @param fts Collection of Futures to accumulate successes
     * @param reducer Reducer to accumulate results
     * @return Future asynchronously populated with the accumulate success operation
     */
    public static <T, R> Future<R> accumulateSuccess(final CollectionX<Future<T>> fts, final Reducer<R> reducer) {
       return Future.of(CompletableFutures.accumulateSuccess(fts.map(Future::getFuture), reducer));
    }
    /**
     * Asynchronously accumulate the results of Futures, a singleUnsafe failure will cause a failed result, using the supplied Reducer {@see cyclops2.Reducers}
     * <pre>
     * {@code
     *
     * Future<Integer> just =Future.of(CompletableFuture.completedFuture(10));
       Future<Integer> none = Future.ofError(new NoSuchElementException());
     * Future<PersistentSetX<Integer>> futures = Future.accumulateSuccess(ListX.of(just,none,Future.ofResult(1)),Reducers.toPersistentSetX());

       //Future[PersistentSetX[10,1]]
     *  }
     *  </pre>
     *
     * @param fts Collection of Futures to accumulate successes
     * @param reducer Reducer to accumulate results
     * @return Future asynchronously populated with the accumulate success operation
     */
    public static <T, R> Future<R> accumulate(final CollectionX<Future<T>> fts, final Reducer<R> reducer) {
        return sequence(fts).map(s -> s.mapReduce(reducer));
    }
    /**
     * Asynchronously accumulate the results only from those Futures which have completed successfully, using the supplied mapping function to
     * convert the data from each Future before reducing them using the supplied Monoid (a combining BiFunction/BinaryOperator and identity element that takes two
     * input values of the same type and returns the combined result) {@see cyclops2.Monoids }.
     *
     * <pre>
     * {@code
     * Future<String> future = Future.accumulate(ListX.of(Future.ofResult(10),Future.ofResult(1)),i->""+i,Monoids.stringConcat);
        //Future["101"]
     * }
     * </pre>
     *
     * @param fts Collection of Futures to accumulate successes
     * @param mapper Mapping function to be applied to the result of each Future
     * @param reducer Monoid to combine values from each Future
     * @return Future asynchronously populated with the accumulate operation
     */
    public static <T, R> Future<R> accumulateSuccess(final CollectionX<Future<T>> fts, final Function<? super T, R> mapper, final Monoid<R> reducer) {
        return Future.of(CompletableFutures.accumulateSuccess(fts.map(Future::getFuture),mapper,reducer));
    }

    /**
     * Asynchronously accumulate the results only from those Futures which have completed successfully,
     *  reducing them using the supplied Monoid (a combining BiFunction/BinaryOperator and identity element that takes two
     * input values of the same type and returns the combined result) {@see cyclops2.Monoids }.
     *
     * <pre>
     * {@code
     * Future<Integer> just =Future.of(CompletableFuture.completedFuture(10));
     * Future<Integer> future =Future.accumulate(Monoids.intSum, ListX.of(just,Future.ofResult(1)));
       //Future[11]
     * }
     * </pre>
     *
     *
     * @param fts Collection of Futures to accumulate successes
     * @param reducer Monoid to combine values from each Future
     * @return Future asynchronously populated with the accumulate operation
     */
    public static <T> Future<T> accumulateSuccess(final Monoid<T> reducer, final CollectionX<Future<T>> fts ) {
        return Future.of(CompletableFutures.accumulateSuccess(reducer,fts.map(Future::getFuture)));
    }

    /**
     * Asynchronously accumulate the results of a batch of Futures which using the supplied mapping function to
     * convert the data from each Future before reducing them using the supplied supplied Monoid (a combining BiFunction/BinaryOperator and identity element that takes two
     * input values of the same type and returns the combined result) {@see cyclops2.Monoids }.
     * A singleUnsafe Failure results in a Failed  Future.
     *
     * <pre>
     * {@code
     * Future<String> future = Future.accumulate(ListX.of(Future.ofResult(10),Future.ofResult(1)),i->""+i,Monoids.stringConcat);
        //Future["101"]
     * }
     * </pre>
     *
     * @param fts Collection of Futures to accumulate successes
     * @param mapper Mapping function to be applied to the result of each Future
     * @param reducer Monoid to combine values from each Future
     * @return Future asynchronously populated with the accumulate operation
     */
    public static <T, R> Future<R> accumulate(final CollectionX<Future<T>> fts, final Function<? super T, R> mapper, final Monoid<R> reducer) {
        return sequence(fts).map(s -> s.map(mapper)
                                       .reduce(reducer)
                                       );
    }

    /**
     * Asynchronously accumulate the results only from the provided Futures,
     *  reducing them using the supplied Monoid (a combining BiFunction/BinaryOperator and identity element that takes two
     * input values of the same type and returns the combined result) {@see cyclops2.Monoids }
     *
     * A singleUnsafe Failure results in a Failed  Future.
     *
     * <pre>
     * {@code
     * Future<Integer> future =Future.accumulate(Monoids.intSum,ListX.of(just,Future.ofResult(1)));
       //Future[11]
     * }
     * </pre>
     *
     *
     * @param fts Collection of Futures to accumulate successes
     * @param reducer Monoid to combine values from each Future
     * @return Future asynchronously populated with the accumulate operation
     */
    public static <T> Future<T> accumulate(final Monoid<T> reducer, final CollectionX<Future<T>> fts) {
        return sequence(fts).map(s -> s.reduce(reducer)
                                      );
    }



    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#forEach4(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops2.util.function.TriFunction, com.aol.cyclops2.util.function.QuadFunction)
     */
    @Override
    public <T2, R1, R2, R3, R> Future<R> forEach4(Function<? super T, ? extends MonadicValue<R1>> value1,
                                                  BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
                                                  Function3<? super T, ? super R1, ? super R2, ? extends MonadicValue<R3>> value3,
                                                  Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        return (Future<R>)MonadicValue.super.forEach4(value1, value2, value3, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#forEach4(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops2.util.function.TriFunction, com.aol.cyclops2.util.function.QuadFunction, com.aol.cyclops2.util.function.QuadFunction)
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
     * @see com.aol.cyclops2.types.MonadicValue#forEach3(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops2.util.function.TriFunction)
     */
    @Override
    public <T2, R1, R2, R> Future<R> forEach3(Function<? super T, ? extends MonadicValue<R1>> value1,
                                              BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
                                              Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return (Future<R>)MonadicValue.super.forEach3(value1, value2, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#forEach3(java.util.function.Function, java.util.function.BiFunction, com.aol.cyclops2.util.function.TriFunction, com.aol.cyclops2.util.function.TriFunction)
     */
    @Override
    public <T2, R1, R2, R> Future<R> forEach3(Function<? super T, ? extends MonadicValue<R1>> value1,
                                              BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
                                              Function3<? super T, ? super R1, ? super R2, Boolean> filterFunction,
                                              Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return (Future<R>)MonadicValue.super.forEach3(value1, value2, filterFunction, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#forEach2(java.util.function.Function, java.util.function.BiFunction)
     */
    @Override
    public <R1, R> Future<R> forEach2(Function<? super T, ? extends MonadicValue<R1>> value1,
                                      BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {

        return (Future<R>)MonadicValue.super.forEach2(value1, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#forEach2(java.util.function.Function, java.util.function.BiFunction, java.util.function.BiFunction)
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
    public <R> Future<R> visitAsync(Function<? super T,? extends R> success, Function<? super Throwable,? extends R> failure){
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
    public <R> R visit(Function<? super T,? extends R> success, Function<? super Throwable,? extends R> failure){
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
     * com.aol.cyclops2.types.MonadicValue#coflatMap(java.util.function.Function)
     */
    @Override
    public <R> Future<R> coflatMap(final Function<? super MonadicValue<T>, R> mapper) {
        return (Future<R>) MonadicValue.super.coflatMap(mapper);
    }

    /*
     * cojoin (non-Javadoc)
     *
     * @see com.aol.cyclops2.types.MonadicValue#nest()
     */
    @Override
    public Future<MonadicValue<T>> nest() {
        return (Future<MonadicValue<T>>) MonadicValue.super.nest();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.aol.cyclops2.types.MonadicValue2#combine(cyclops2.function.Monoid,
     * com.aol.cyclops2.types.MonadicValue2)
     */
    @Override
    public Future<T> combineEager(final Monoid<T> monoid, final MonadicValue<? extends T> v2) {
        return (Future<T>) MonadicValue.super.combineEager(monoid, v2);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.aol.cyclops2.types.ConvertableFunctor#transform(java.util.function.Function)
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
     * @see java.util.function.Supplier#get()
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
     * @see com.aol.cyclops2.lambda.monads.Pure#unit(java.lang.Object)
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
     * @see com.aol.cyclops2.types.Value#reactiveStream()
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
     * @see com.aol.cyclops2.types.FlatMap#flatten()
     */
    public static <R> Future<R> flatten(Future<? extends Future<R>> nested) {
        return nested.flatMap(Function.identity());
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.aol.cyclops2.types.MonadicValue#flatMap(java.util.function.Function)
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

    /*
     * (non-Javadoc)
     *
     * @see com.aol.cyclops2.types.Value#toLazyEither()
     */
    public Either<Throwable, T> toXor() {
        try {
            return Either.right(future.join());
        } catch (final Throwable t) {
            return Either.<Throwable, T>left(t.getCause());
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.aol.cyclops2.types.Value#toIor()
     */
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
     * @see com.aol.cyclops2.closures.Convertable#toCompletableFuture()
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
     *         .transform(i->i*2,e->-1);
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
     * @see com.aol.cyclops2.lambda.monads.Transformable#cast(java.lang.Class)
     */
    @Override
    public <U> Future<U> cast(final Class<? extends U> type) {

        return (Future<U>) MonadicValue.super.cast(type);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.aol.cyclops2.lambda.monads.Transformable#peek(java.util.function.Consumer)
     */
    @Override
    public Future<T> peek(final Consumer<? super T> c) {

        return (Future<T>) MonadicValue.super.peek(c);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.aol.cyclops2.lambda.monads.Transformable#trampoline(java.util.function.
     * Function)
     */
    @Override
    public <R> Future<R> trampoline(final Function<? super T, ? extends Trampoline<? extends R>> mapper) {

        return (Future<R>) MonadicValue.super.trampoline(mapper);
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

    /*
     * (non-Javadoc)
     *
     * @see com.aol.cyclops2.types.foldable.Convertable#isPresent()
     */
    @Override
    public boolean isPresent() {
        return !this.future.isCompletedExceptionally();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.aol.cyclops2.types.Value#mkString()
     */
    @Override
    public String mkString() {

        if(future.isDone()){
            if(!future.isCompletedExceptionally())
                return "Future[" + future.join() + "]";
        }
        return "Future[" + future.toString() + "]";
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.aol.cyclops2.types.Filters#filter(java.util.function.Predicate)
     */
    @Override
    public Maybe<T> filter(final Predicate<? super T> fn) {
        return toMaybe().filter(fn);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.aol.cyclops2.types.Filters#ofType(java.lang.Class)
     */
    @Override
    public <U> Maybe<U> ofType(final Class<? extends U> type) {

        return (Maybe<U>) MonadicValue.super.ofType(type);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.aol.cyclops2.types.Filters#filterNot(java.util.function.Predicate)
     */
    @Override
    public Maybe<T> filterNot(final Predicate<? super T> fn) {

        return (Maybe<T>) MonadicValue.super.filterNot(fn);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.aol.cyclops2.types.Filters#notNull()
     */
    @Override
    public Maybe<T> notNull() {

        return (Maybe<T>) MonadicValue.super.notNull();
    }




    /*
     * Apply a function across two values at once. (non-Javadoc)
     *
     * @see
     * com.aol.cyclops2.types.applicative.ApplicativeFunctor#combine(com.aol.
     * cyclops2.types.Value, java.util.function.BiFunction)
     */
    @Override
    public <T2, R> Future<R> combine(final Value<? extends T2> app, final BiFunction<? super T, ? super T2, ? extends R> fn) {
        if (app instanceof Future) {
            return Future.of(future.thenCombine(((Future<T2>) app).getFuture(), fn));
        }
        return (Future<R>) MonadicValue.super.zip(app, fn);
    }

    /*
     * Equivalent to combine, but accepts an Iterable and takes the first value
     * only from that iterable. (non-Javadoc)
     *
     * @see com.aol.cyclops2.types.Zippable#zip(java.lang.Iterable,
     * java.util.function.BiFunction)
     */
    @Override
    public <T2, R> Future<R> zip(final Iterable<? extends T2> app, final BiFunction<? super T, ? super T2, ? extends R> fn) {

        return (Future<R>) MonadicValue.super.zip(app, fn);
    }

    /*
     * Equivalent to combine, but accepts a Publisher and takes the first value
     * only from that publisher.
     *
     * (non-Javadoc)
     *
     * @see com.aol.cyclops2.types.Zippable#zip(java.util.function.BiFunction,
     * org.reactivestreams.Publisher)
     */
    @Override
    public <T2, R> Future<R> zipP(final Publisher<? extends T2> app, final BiFunction<? super T, ? super T2, ? extends R> fn) {
        return (Future<R>) MonadicValue.super.zipP(app,fn);

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
     * @see com.aol.cyclops2.types.Zippable#zip(java.util.stream.Stream,
     * java.util.function.BiFunction)
     */
    @Override
    public <U, R> Future<R> zipS(final Stream<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {
        return (Future<R>) MonadicValue.super.zipS(other, zipper);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.aol.cyclops2.types.Zippable#zip(java.util.stream.Stream)
     */
    @Override
    public <U> Future<Tuple2<T, U>> zipS(final Stream<? extends U> other) {
        return (Future) MonadicValue.super.zipS(other);
    }



    /*
     * (non-Javadoc)
     *
     * @see com.aol.cyclops2.types.Zippable#zip(java.lang.Iterable)
     */
    @Override
    public <U> Future<Tuple2<T, U>> zip(final Iterable<? extends U> other) {
        return (Future) MonadicValue.super.zip(other);
    }


    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#flatMapI(java.util.function.Function)
     */
    @Override
    public <R> Future<R> flatMapI(final Function<? super T, ? extends Iterable<? extends R>> mapper) {
        return (Future<R>) MonadicValue.super.flatMapI(mapper);
    }


    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#flatMapP(java.util.function.Function)
     */
    @Override
    public <R> Future<R> flatMapP(final Function<? super T, ? extends Publisher<? extends R>> mapper) {
        return (Future<R>) MonadicValue.super.flatMapP(mapper);
    }

    @Override
    public Future<T> zip(BinaryOperator<Zippable<T>> combiner, Zippable<T> app) {
        return (Future<T>)MonadicValue.super.zip(combiner, app);
    }


    public CompletableFuture<T> getFuture() {
        return this.future;
    }

    @Override
    public <R> R visit(Function<? super T,? extends R> success, Supplier<? extends R> failure){
        try {
            return success.apply(future.join());
        }catch(Throwable t){
            return failure.get();
        }
    }


    /**
     * Companion class for creating Type Class instances for working with Futures
     * @author johnmcclean
     *
     */
    @UtilityClass
    public static class Instances {
        public static InstanceDefinitions<future> definitions(){
            return new InstanceDefinitions<future>() {
                @Override
                public <T, R> Functor<future> functor() {
                    return Instances.functor();
                }

                @Override
                public <T> Pure<future> unit() {
                    return Instances.unit();
                }

                @Override
                public <T, R> Applicative<future> applicative() {
                    return Instances.applicative();
                }

                @Override
                public <T, R> Monad<future> monad() {
                    return Instances.monad();
                }

                @Override
                public <T, R> Maybe<MonadZero<future>> monadZero() {
                    return Maybe.just(Instances.monadZero());
                }

                @Override
                public <T> Maybe<MonadPlus<future>> monadPlus() {
                    return Maybe.just(Instances.monadPlus());
                }

                @Override
                public <T> MonadRec<future> monadRec() {
                    return Instances.monadRec();
                }

                @Override
                public <T> Maybe<MonadPlus<future>> monadPlus(Monoid<Higher<future, T>> m) {
                    return Maybe.just(Instances.monadPlus((Monoid)m));
                }

                @Override
                public <C2, T> Traverse<future> traverse() {
                    return Instances.traverse();
                }

                @Override
                public <T> Foldable<future> foldable() {
                    return Instances.foldable();
                }

                @Override
                public <T> Maybe<Comonad<future>> comonad() {
                    return Maybe.nothing();
                }

                @Override
                public <T> Maybe<Unfoldable<future>> unfoldable() {
                    return Maybe.nothing();
                }
            };
        }

        /**
         *
         * Transform a future, mulitplying every element by 2
         *
         * <pre>
         * {@code
         *  Future<Integer> future = Futures.functor().transform(i->i*2, Future.widen(Future.ofResult(2));
         *
         *  //[4]
         *
         *
         * }
         * </pre>
         *
         * An example fluent api working with Futures
         * <pre>
         * {@code
         *   Future<Integer> future = Futures.unit()
        .unit("hello")
        .applyHKT(h->Futures.functor().transform((String v) ->v.length(), h))
        .convert(Future::narrowK3);
         *
         * }
         * </pre>
         *
         *
         * @return A functor for Futures
         */
        public static <T,R> Functor<future> functor(){
            BiFunction<Future<T>,Function<? super T, ? extends R>,Future<R>> map = Instances::map;
            return General.functor(map);
        }
        /**
         * <pre>
         * {@code
         * Future<String> future = Futures.unit()
        .unit("hello")
        .convert(Future::narrowK3);

        //Future("hello")
         *
         * }
         * </pre>
         *
         *
         * @return A factory for Futures
         */
        public static <T> Pure<future> unit(){
            return General.<future,T>unit(Instances::of);
        }
        /**
         *
         * <pre>
         * {@code
         * import static com.aol.cyclops.hkt.jdk.Future.widen;
         * import static com.aol.cyclops.util.function.Lambda.l1;
         * import static java.util.Arrays.asFuture;
         *
        Futures.zippingApplicative()
        .ap(widen(asFuture(l1(this::multiplyByTwo))),widen(asFuture(1,2,3)));
         *
         * //[2,4,6]
         * }
         * </pre>
         *
         *
         * Example fluent API
         * <pre>
         * {@code
         * Future<Function<Integer,Integer>> futureFn =Futures.unit()
         *                                                  .unit(Lambda.l1((Integer i) ->i*2))
         *                                                  .convert(Future::narrowK3);

        Future<Integer> future = Futures.unit()
        .unit("hello")
        .applyHKT(h->Futures.functor().transform((String v) ->v.length(), h))
        .applyHKT(h->Futures.applicative().ap(futureFn, h))
        .convert(Future::narrowK3);

        //Future("hello".length()*2))
         *
         * }
         * </pre>
         *
         *
         * @return A zipper for Futures
         */
        public static <T,R> Applicative<future> applicative(){
            BiFunction<Future< Function<T, R>>,Future<T>,Future<R>> ap = Instances::ap;
            return General.applicative(functor(), unit(), ap);
        }
        /**
         *
         * <pre>
         * {@code
         * import static com.aol.cyclops.hkt.jdk.Future.widen;
         * Future<Integer> future  = Futures.monad()
        .flatMap(i->widen(Future.ofResult(0)), widen(Future.ofResult(2)))
        .convert(Future::narrowK3);
         * }
         * </pre>
         *
         * Example fluent API
         * <pre>
         * {@code
         *    Future<Integer> future = Futures.unit()
        .unit("hello")
        .applyHKT(h->Futures.monad().flatMap((String v) ->Futures.unit().unit(v.length()), h))
        .convert(Future::narrowK3);

        //Future("hello".length())
         *
         * }
         * </pre>
         *
         * @return Type class with monad functions for Futures
         */
        public static <T,R> Monad<future> monad(){

            BiFunction<Higher<future,T>,Function<? super T, ? extends Higher<future,R>>,Higher<future,R>> flatMap = Instances::flatMap;
            return General.monad(applicative(), flatMap);
        }
        /**
         *
         * <pre>
         * {@code
         *  Future<String> future = Futures.unit()
        .unit("hello")
        .applyHKT(h->Futures.monadZero().filter((String t)->t.startsWith("he"), h))
        .convert(Future::narrowK3);

        //Future["hello"]
         *
         * }
         * </pre>
         *
         *
         * @return A filterable monad (with default value)
         */
        public static <T,R> MonadZero<future> monadZero(){

            return General.monadZero(monad(), Future.future());
        }

        public static <T,R> MonadRec<future> monadRec(){


            return new MonadRec<future>(){

                @Override
                public <T, R> Higher<future, R> tailRec(T initial, Function<? super T, ? extends Higher<future, ? extends Either<T, R>>> fn) {
                    return Future.tailRec(initial,fn.andThen(Future::narrowK));
                }
            };
        }

        /**
         * <pre>
         * {@code
         *  Future<Integer> future = Futures.<Integer>monadPlus()
        .plus(Future.widen(Future.future()), Future.widen(Future.ofResult(10)))
        .convert(Future::narrowK3);
        //Future[10]
         *
         * }
         * </pre>
         * @return Type class for combining Futures by concatenation
         */
        public static <T> MonadPlus<future> monadPlus(){
            Monoid<Future<T>> mn = Monoids.firstSuccessfulFuture();
            Monoid<Future<T>> m = Monoid.of(mn.zero(), (f,g)->
                    mn.apply(Future.narrowK(f), Future.narrowK(g)));

            Monoid<Higher<future,T>> m2= (Monoid)m;
            return General.monadPlus(monadZero(),m2);
        }
        /**
         *
         * <pre>
         * {@code
         *  Monoid<Future<Integer>> m = Monoid.of(Future.widen(Future.future()()), (a,b)->a.isDone() ? b : a);
        Future<Integer> future = Futures.<Integer>monadPlus(m)
        .plus(Future.widen(Future.ofResult(5)), Future.widen(Future.ofResult(10)))
        .convert(Future::narrowK3);
        //Future(5)
         *
         * }
         * </pre>
         *
         * @param m Monoid to use for combining Futures
         * @return Type class for combining Futures
         */
        public static <T> MonadPlus<future> monadPlus(Monoid<Future<T>> m){
            Monoid<Higher<future,T>> m2= (Monoid)m;
            return General.monadPlus(monadZero(),m2);
        }


        public static <L> Traverse<future> traverse() {
            return new Traverse<future>() {

                @Override
                public <T> Higher<Witness.future, T> unit(T value) {
                    return Instances.<T>unit().unit(value);
                }

                @Override
                public <C2, T, R> Higher<C2, Higher<future, R>> traverseA(Applicative<C2> applicative, Function<? super T, ? extends Higher<C2, R>> fn, Higher<future, T> ds) {
                    Future<T> maybe = Future.narrowK(ds);
                    return maybe.visit(right->applicative.map(m->Future.ofResult(m), fn.apply(right)),left->  applicative.unit(Future.ofError(left)));
                }

                @Override
                public <C2, T> Higher<C2, Higher<future, T>> sequenceA(Applicative<C2> applicative, Higher<future, Higher<C2, T>> ds) {
                    return traverseA(applicative,Function.identity(),ds);
                }



                @Override
                public <T, R> Higher<future, R> ap(Higher<future, ? extends Function<T, R>> fn, Higher<future, T> apply) {
                    return applicative().ap(fn,apply);

                }

                @Override
                public <T, R> Higher<future, R> map(Function<? super T, ? extends R> fn, Higher<future, T> ds) {
                    return functor().map(fn,ds);
                }

            };
        }
        public static <L> Foldable<future> foldable() {
            return new Foldable<future>() {


                @Override
                public <T> T foldRight(Monoid<T> monoid, Higher<future, T> ds) {
                    Future<T> ft = Future.narrowK(ds);
                    return ft.fold(monoid);
                }

                @Override
                public <T> T foldLeft(Monoid<T> monoid, Higher<future, T> ds) {
                    Future<T> ft = Future.narrowK(ds);
                    return ft.fold(monoid);
                }

                @Override
                public <T, R> R foldMap(Monoid<R> mb, Function<? super T, ? extends R> fn, Higher<future, T> nestedA) {
                    return foldLeft(mb,narrowK(nestedA).<R>map(fn));
                }
            };
        }



        private <T> Future<T> of(T value){
            return Future.ofResult(value);
        }
        private static <T,R> Future<R> ap(Future<Function< T, R>> lt,  Future<T> future){
            return lt.combine(future, (a,b)->a.apply(b));

        }
        private static <T,R> Higher<future,R> flatMap( Higher<future,T> lt, Function<? super T, ? extends  Higher<future,R>> fn){
            return Future.narrowK(lt).flatMap(fn.andThen(Future::narrowK));
        }
        private static <T,R> Future<R> map(Future<T> lt, Function<? super T, ? extends R> fn){
            return lt.map(fn);
        }



    }
}
