package com.oath.cyclops.internal.stream;


import com.oath.cyclops.async.adapters.Queue;
import com.oath.cyclops.types.futurestream.Continuation;
import com.oath.cyclops.types.persistent.PersistentCollection;
import com.oath.cyclops.types.stream.Connectable;
import com.oath.cyclops.util.ExceptionSoftener;

import com.oath.cyclops.internal.stream.spliterators.push.*;
import cyclops.control.Future;
import com.oath.cyclops.async.QueueFactories;
import com.oath.cyclops.async.adapters.QueueFactory;
import com.oath.cyclops.async.adapters.Signal;
import com.oath.cyclops.async.adapters.Topic;
import cyclops.data.Seq;
import cyclops.companion.Streams;
import cyclops.control.*;
import cyclops.data.Vector;
import cyclops.function.Monoid;

import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.Spouts;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Wither;
import org.agrona.concurrent.OneToOneConcurrentArrayQueue;
import cyclops.data.tuple.Tuple;
import cyclops.data.tuple.Tuple2;
import cyclops.data.tuple.Tuple3;
import cyclops.data.tuple.Tuple4;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;


import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.*;

import static com.oath.cyclops.internal.stream.ReactiveStreamX.Type.BACKPRESSURE;
import static com.oath.cyclops.internal.stream.ReactiveStreamX.Type.SYNC;


@AllArgsConstructor
public class ReactiveStreamX<T> extends BaseExtendedStream<T> {

    @Getter
    final Operator<T> source;
    @Wither
    final Consumer<? super Throwable> defaultErrorHandler;

    @Wither
    final Type async; //SYNC streams should switch to lazy Backpressured or No backpressure when zip or flatMapP are called

    public Type getType() {
        return async;
    }
    //zip can check the provided Stream settings for async usage
    //flatMapP should assume async

    public static enum Type {SYNC, BACKPRESSURE, NO_BACKPRESSURE}


    public ReactiveStreamX(Operator<T> source) {
        this.source = source;
        this.defaultErrorHandler = e -> {
            if (!(e instanceof Queue.ClosedQueueException)) throw ExceptionSoftener.throwSoftenedException(e);
        };
        this.async = SYNC;
    }

    public ReactiveStreamX(Operator<T> source, Type async) {
        this.source = source;
        this.defaultErrorHandler = e -> {
            if (!(e instanceof Queue.ClosedQueueException)) throw ExceptionSoftener.throwSoftenedException(e);
        };
        this.async = async;
    }

    @Override
    public ReactiveSeq<T> reverse() {
        return coflatMap(s -> ReactiveSeq.reversedListOf(s.toList()))
                .flatMap(i -> i);
    }

    @Override
    public ReactiveSeq<T> combine(BiPredicate<? super T, ? super T> predicate, BinaryOperator<T> op) {
        return createSeq(new CombineOperator<>(source, predicate, op)).flatMap(i -> i);
    }

    @Override
    public <R> ReactiveSeq<R> reduceAll(R identity, BiFunction<R, ? super T, R>  accumulator){
        return createSeq(new ReduceAllOperator<>(source, identity, accumulator));
    }

    @Override
    public <R, A> ReactiveSeq<R> collectAll(Collector<? super T, A, R> collector) {
        return createSeq(new CollectAllOperator<T, A, R>(source, collector));
    }

    @Override
    public Iterator<T> iterator() {
        if (async == Type.NO_BACKPRESSURE) {

            Queue<T> queue = QueueFactories.<T>unboundedNonBlockingQueue()
                    .build();

            AtomicBoolean wip = new AtomicBoolean(false);
            Subscription[] sub = {null};

            Continuation cont = new Continuation(() -> {
                if (wip.compareAndSet(false, true)) {
                    this.source.subscribeAll(queue::offer,
                            i -> queue.close(),
                            () -> queue.close());
                }
                return Continuation.empty();
            });
            queue.addContinuation(cont);


            return queue.stream().iterator();

        }
        return new OperatorToIterable<>(source, this.defaultErrorHandler, async == BACKPRESSURE).iterator();
    }

    <X> ReactiveStreamX<X> createSeq(Operator<X> stream) {
        return new ReactiveStreamX<X>(stream, defaultErrorHandler, async);
    }

    <X> ReactiveStreamX<X> createSeq(Operator<X> stream, Type async) {
        return new ReactiveStreamX<X>(stream, defaultErrorHandler, async);
    }


    public <R> ReactiveSeq<R> coflatMap(Function<? super ReactiveSeq<T>, ? extends R> fn) {
        return createSeq(new LazySingleValueOperator<ReactiveSeq<T>, R>(createSeq(source), fn));

    }

    static final Object UNSET = new Object();

    @Override
    public Maybe<T> takeOne() {
        if (async == Type.NO_BACKPRESSURE) {
            Future<T> result = Future.future();
            source.subscribeAll(e -> {
                result.complete(e);
                throw new Queue.ClosedQueueException();
            }, t -> {
                result.completeExceptionally(t);
            }, () -> {
                if (!result.isDone()) {
                    result.complete(null);
                }
            });

            return result.toMaybe();
        }
        return Maybe.fromPublisher(this);
    }

    @Override
    public LazyEither<Throwable, T> findFirstOrError() {
        if (async == Type.NO_BACKPRESSURE) {
            Future<T> result = Future.future();
            source.subscribeAll(e -> {
                result.complete(e);
                throw new Queue.ClosedQueueException();
            }, t -> {
                result.completeExceptionally(t);
            }, () -> {
                if (!result.isDone()) {
                    result.complete(null);
                }
            });

            return LazyEither.fromFuture(result);
        }
        return LazyEither.fromPublisher(this);
    }

    @Override
    public final Optional<T> findFirst() {


        Future<T> result = Future.future();

        if (async == Type.NO_BACKPRESSURE) {
            source.subscribeAll(e -> {
                result.complete(e);
                throw new Queue.ClosedQueueException();
            }, t -> {
                result.completeExceptionally(t);
            }, () -> {
                if (!result.isDone()) {
                    result.complete(null);
                }
            });
            return Optional.ofNullable(result.get().fold(s->s, e->{throw ExceptionSoftener.throwSoftenedException(e);}));

        }


        Subscription sub[] = {null};
        //may be quicker toNested use forEachAsync and throw an Exception with fillInStackTrace overriden
        sub[0] = source.subscribe(e -> {

            result.complete(e);
            if (sub[0] != null)
                sub[0].cancel();


        }, e -> {
            result.completeExceptionally(e);
            if (sub[0] != null)
                sub[0].cancel();

        }, () -> {
            if (!result.isDone()) {
                result.complete(null);
            }
        });
        sub[0].request(1l);

        return Optional.ofNullable(result.get().fold(s->s, e->{throw ExceptionSoftener.throwSoftenedException(e);}));
    }

    public final static <T> Option<T> findFirstCallAll(ReactiveStreamX<T> stream) {


        Future<T> result = Future.future();

        if (stream.async == Type.NO_BACKPRESSURE) {
            stream.source.subscribeAll(e -> {
                result.complete(e);
                throw new Queue.ClosedQueueException();
            }, t -> {
                result.completeExceptionally(t);
            }, () -> {
                if (!result.isDone()) {
                    result.complete(null);
                }
            });
            return Option.fromNullable(result.get().fold(s->s, e->{throw ExceptionSoftener.throwSoftenedException(e);}));

        }

        Subscription sub[] = {null};
        //may be quicker to use forEachAsync and throw an Exception with fillInStackTrace overriden
        sub[0] = stream.source.subscribe(e -> {

            result.complete(e);
            if (sub[0] != null)
                sub[0].cancel();


        }, e -> {
            result.completeExceptionally(e);
            if (sub[0] != null)
                sub[0].cancel();

        }, () -> {
            if (!result.isDone()) {
                result.complete(null);
            }
        });
        sub[0].request(Long.MAX_VALUE);

        return Option.fromNullable(result.get().fold(s->s, e->{throw ExceptionSoftener.throwSoftenedException(e);}));


    }


    @Override
    public final ReactiveSeq<Seq<T>> sliding(final int windowSize, final int increment) {
        return createSeq(new SlidingOperator<>(source, Function.identity(), windowSize, increment));
    }

    @Override
    public ReactiveSeq<Vector<T>> grouped(final int groupSize) {
        return createSeq(new GroupingOperator<T, Vector<T>, Vector<T>>(source, () -> Vector.empty(), c -> c, groupSize));

    }

    @Override
    public ReactiveSeq<Vector<T>> groupedWhile(final BiPredicate<Vector<? super T>, ? super T> predicate) {
        return createSeq(new GroupedStatefullyOperator<>(source, () -> Vector.empty(), Function.identity(), predicate));
    }

    @Override
    public <C extends PersistentCollection<T>, R> ReactiveSeq<R> groupedWhile(final BiPredicate<C, ? super T> predicate, final Supplier<C> factory,
                                                                              Function<? super C, ? extends R> finalizer) {
        return this.<R>createSeq(new GroupedStatefullyOperator<>(source, factory, finalizer, predicate));
    }

    @Override
    public ReactiveSeq<Vector<T>> groupedUntil(final BiPredicate<Vector<? super T>, ? super T> predicate) {
        return createSeq(new GroupedStatefullyOperator<>(source, () -> Vector.empty(), Function.identity(), predicate.negate()));
    }

    @Override
    public <C extends PersistentCollection<T>, R> ReactiveSeq<R> groupedUntil(final BiPredicate<C, ? super T> predicate, final Supplier<C> factory,
                                                                              Function<? super C, ? extends R> finalizer) {
        return this.<R>createSeq(new GroupedStatefullyOperator<>(source, factory, finalizer, predicate.negate()));
    }

    @Override
    public final ReactiveSeq<T> distinct() {

        Supplier<Predicate<? super T>> predicate = () -> {
            Set<T> values = new java.util.HashSet<>();
            return in -> values.add(in);
        };
        return this.filterLazyPredicate(predicate);
    }

    @Override
    public final ReactiveSeq<T> scanLeft(final Monoid<T> monoid) {
        return scanLeft(monoid.zero(), monoid);

    }

    @Override
    public final <U> ReactiveSeq<U> scanLeft(final U seed, final BiFunction<? super U, ? super T, ? extends U> function) {
        Supplier<Function<? super T, ? extends Object>> scanLeft = () -> {
            Object[] current = {seed};
            return in -> current[0] = function.apply((U) current[0], in);
        };

        return createSeq(new ArrayConcatonatingOperator<>(new SingleValueOperator<U>(seed),
                extract((ReactiveSeq<U>) mapLazyFn(scanLeft))));


    }

    private <U> Operator<U> extract(ReactiveSeq<U> seq) {
        return ((ReactiveStreamX<U>) seq).source;
    }


    @Override
    public final ReactiveSeq<T> dropWhile(final Predicate<? super T> p) {
        return createSeq(new SkipWhileOperator<>(source, p));
    }


    @Override
    public final ReactiveSeq<T> takeWhile(final Predicate<? super T> p) {
        return createSeq(new LimitWhileOperator<>(source, p));
    }

    @Override
    public final ReactiveSeq<T> takeUntil(final Predicate<? super T> p) {
        return takeWhile(p.negate());
    }






    @Override
    public final <R> ReactiveSeq<R> map(final Function<? super T, ? extends R> fn) {


        return createSeq(new MapOperator<T, R>(this.source, fn));
    }


    @Override
    public final <R> ReactiveSeq<R> flatMap(final Function<? super T, ? extends Stream<? extends R>> fn) {

        return createSeq(new FlatMapOperator<>(source, fn));

    }


    @Override
    public final <R> ReactiveSeq<R> concatMap(final Function<? super T, ? extends Iterable<? extends R>> fn) {

        return createSeq(new FlatMapOperator<>(source, i->ReactiveSeq.fromIterable(fn.apply(i))));

    }

    @Override
    public final <R> ReactiveSeq<R> mergeMap(final Function<? super T, ? extends Publisher<? extends R>> fn) {

        return mergeMap(1, fn);
    }

    @Override
    public final <R> ReactiveSeq<R> mergeMap(int maxConcurency, final Function<? super T, ? extends Publisher<? extends R>> fn) {
        FlatMapPublisher<T, R> pub = new FlatMapPublisher<>(source, fn,
                maxConcurency);

        return createSeq(pub);
    }


    @Override
    public final ReactiveSeq<T> filter(final Predicate<? super T> fn) {
        return createSeq(new FilterOperator<T>(source, fn));

    }

    @Override
    public Spliterator<T> spliterator() {
        return unwrapStream().spliterator();
    }

    @Override
    public Stream<T> unwrapStream() {

        if (async == Type.NO_BACKPRESSURE) {

            Queue<T> queue = QueueFactories.<T>unboundedNonBlockingQueue()
                                           .build();

            AtomicBoolean wip = new AtomicBoolean(false);
            Continuation cont = new Continuation(() -> {

                if (wip.compareAndSet(false, true)) {

                    this.source.subscribeAll(queue::offer, i -> {
                        queue.close();


                    }, () -> queue.close());
                }
                return Continuation.empty();
            });

            queue.addContinuation(cont);

            return queue.stream();

        }
        return StreamSupport.stream(new OperatorToIterable<>(source, this.defaultErrorHandler, async == BACKPRESSURE).spliterator(), false);
    }

    @Override
    protected <R> ReactiveSeq<R> createSeq(Stream<R> stream) {
        if (stream instanceof ReactiveSeq)
            return (ReactiveSeq) stream;
        if (stream instanceof Iterable)
            return new ReactiveStreamX<>(new IterableSourceOperator<>((Iterable<R>) stream));
        return new ReactiveStreamX<>(new SpliteratorToOperator<>(stream.spliterator()));
    }

    @Override
    public <R> ReactiveSeq<R> mapLazyFn(Supplier<Function<? super T, ? extends R>> fn) {
        return createSeq(new LazyMapOperator<>(source, fn));
    }

    public final ReactiveSeq<T> filterLazyPredicate(final Supplier<Predicate<? super T>> fn) {
        return createSeq(new LazyFilterOperator<T>(source, fn));

    }

    @Override
    public ReactiveSeq<T> changes() {
        if (async == Type.NO_BACKPRESSURE) {
            Queue<T> discrete = QueueFactories.<T>unboundedNonBlockingQueue()
                    .build()
                    .withTimeout(1);


            Signal<T> signal = new Signal<T>(null, discrete);
            publishTo(signal).forEach(e -> {
            }, e -> {
            }, () -> signal.close());

            return signal.getDiscrete().stream();
        } else {
            Queue<T> queue = QueueFactories.<T>unboundedNonBlockingQueue()
                    .build();
            Signal<T> signal = new Signal<T>(null, queue);
            Subscription sub = source.subscribe(signal::set, i -> {
                signal.close();


            }, () -> {
                signal.close();
            });

            Continuation[] contRef = {null};

            AtomicBoolean wip = new AtomicBoolean(false);
            Continuation cont = new Continuation(() -> {

                if (wip.compareAndSet(false, true)) {
                    sub.request(1l);
                    wip.set(false);
                }
                return contRef[0];
            });

            contRef[0] = cont;

            queue.addContinuation(cont);

            return signal.getDiscrete().stream();
        }

    }

    @Override
    public <U, R> ReactiveSeq<R> zipLatest(final Publisher<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {

        Operator<U> right;
        if (other instanceof ReactiveStreamX) {
            right = ((ReactiveStreamX<U>) other).source;
        } else {
            right = new PublisherToOperator<U>((Publisher<U>) other);
        }
        return createSeq(new ZippingLatestOperator<>(source, right, zipper), BACKPRESSURE);
    }

    @Override
    public void forEachAsync(final Consumer<? super T> action) {
        if (async == Type.NO_BACKPRESSURE)
            source.subscribeAll(action, this.defaultErrorHandler, () -> {
            });
        else
            source.subscribe(action, this.defaultErrorHandler, () -> {
            }).request(Long.MAX_VALUE);

    }

    @Override
    public void forEach(final Consumer<? super T> action) {
        Future<Boolean> complete = Future.future();
        if (async == Type.NO_BACKPRESSURE) {
            source.subscribeAll(action, this.defaultErrorHandler, () -> complete.complete(true));
        } else {
            source.subscribe(action, this.defaultErrorHandler, () -> complete.complete(true)).request(Long.MAX_VALUE);
        }
        complete.get();


    }

    @Override
    public long count() {

        AtomicBoolean complete = new AtomicBoolean(false);
        long[] result = {0};
        forEach(t -> result[0]++, e -> {
        }, () -> complete.set(true));
        while (!complete.get()) {
            LockSupport.parkNanos(0l);
        }
        return result[0];


    }


    @Override
    public <U> ReactiveSeq<U> unitIterable(Iterable<U> iterable) {
        return createSeq(new IterableSourceOperator<U>(iterable));
    }

    @Override
    public ReactiveSeq<T> backpressureAware() {
        if (async == Type.NO_BACKPRESSURE) {
            return this.withAsync(SYNC);
        }
        return this;
    }

    @Override
    public void subscribe(final Subscriber<? super T> sub) {

        if (async == Type.NO_BACKPRESSURE) {
            //if this Stream is not backpressure-aware demand requests / cancel requests are ignored.
            sub.onSubscribe(new StreamSubscription() {
                boolean requested = false;
                volatile boolean active = false;
                volatile boolean completed = false;
                OneToOneConcurrentArrayQueue<T> data = new OneToOneConcurrentArrayQueue<T>(1024 * 10);

                @Override
                public void request(long n) {

                    if (!requested) {
                        source.subscribeAll(e -> {
                            if (!data.offer((T) nilsafeIn(e))) {
                                sub.onError(new FullQueueException());
                            }
                        }, sub::onError, () -> {
                            completed = true;

                            if (data.size() == 0) {
                                while (active) {
                                    Thread.yield();
                                }
                                sub.onComplete();
                            }
                        });
                        requested = true;
                    }


                    this.singleActiveRequest(n, x -> {
                        while (super.requested.get() > 0) {
                            long sent = 0;

                            boolean completeSent = false;
                            active = true;
                            Object res = data.poll();
                            if (res != null) {
                                sub.onNext((nilsafeOut(res)));
                                super.requested.decrementAndGet();
                                sent++;
                            } else if (completed) {
                                completeSent = true;
                                sub.onComplete();
                                break;
                            } else {
                                Thread.yield();
                            }
                            active = false;

                        }
                    });

                }

                private Object nilsafeIn(Object o) {
                    if (o == null)
                        return com.oath.cyclops.async.adapters.Queue.NILL;
                    return o;
                }

                private <T> T nilsafeOut(Object o) {

                    if (Queue.NILL == o) {
                        return null;
                    }
                    return (T) o;
                }


            });
            return;

        }

        //should we force all Stream types on reactive-streams path?
        sub.onSubscribe(source.subscribe(sub::onNext, sub::onError, sub::onComplete));
    }


    @Override
    public ReactiveSeq<T> onEmpty(final T value) {
        return createSeq(new OnEmptyOperator<T>(source, () -> value));

    }

    @Override
    public ReactiveSeq<T> onEmptySwitch(final Supplier<? extends Stream<T>> switchTo) {
        final Object value = new Object();
        ReactiveSeq res = createSeq(onEmptyGet((Supplier) () -> value).flatMap(s -> {
            if (s == value)
                return (Stream) switchTo.get();
            return Stream.of(s);
        }));
        return res;
    }

    @Override
    public ReactiveSeq<T> onEmptyGet(final Supplier<? extends T> supplier) {


         return createSeq(new OnEmptyOperator<T>(source, supplier)).flatMap(a->ReactiveSeq.of(a));
    }

    @Override
    public <X extends Throwable> ReactiveSeq<T> onEmptyError(final Supplier<? extends X> supplier) {
        return createSeq(new OnEmptyErrorOperator<T,X>(source, supplier));
    }
    @Override
    public ReactiveSeq<T> appendStream(final Stream<? extends T> other) {
        return Spouts.concat(this, (Stream<T>) other);
    }

    public ReactiveSeq<T> appendAll(final Iterable<? extends T> other) {
        return Spouts.concat(this, (Stream<T>) Spouts.fromIterable(other));
    }

    //TODO use spliterators and createSeq
    @Override
    public ReactiveSeq<T> append(final T other) {
        return Spouts.concat(this, Spouts.of(other));
    }

    @Override
    public ReactiveSeq<T> appendAll(final T... other) {
        return Spouts.concat(this, Spouts.of(other));
    }

    @Override
    public ReactiveSeq<T> prependStream(final Stream<? extends T> other) {
        return Spouts.concat((Stream<T>) (other), this);
    }

    public ReactiveSeq<T> prependAll(final Iterable<? extends T> other) {
        return Spouts.concat((Stream<T>) (other), this);
    }

    @Override
    public ReactiveSeq<T> prepend(final T other) {
        return Spouts.concat(Spouts.of(other), this);
    }

    @Override
    public ReactiveSeq<T> prependAll(final T... other) {
        return Spouts.concat(Spouts.of(other), this);
    }


    @Override
    public <U> ReactiveSeq<T> distinct(final Function<? super T, ? extends U> keyExtractor) {
        Supplier<Predicate<? super T>> predicate = () -> {
            Set<U> values = new java.util.HashSet<>();
            return in -> values.add(keyExtractor.apply(in));
        };
        return this.filterLazyPredicate(predicate);
    }

    @Override
    public ReactiveSeq<Vector<T>> groupedBySizeAndTime(final int size, final long time, final TimeUnit t) {
        return createSeq(new GroupedByTimeAndSizeOperator<>(this.source, () -> Vector.empty(),
                Function.identity(), time, t, size)
        );

    }

    @Override
    public ReactiveSeq<Vector<T>> groupedByTime(final long time, final TimeUnit t) {
        return createSeq(new GroupedByTimeOperator<>(source,
                () -> Vector.empty(),
                Function.identity(), time, t));
    }

    @Override
    public ReactiveSeq<T> drop(final long time, final TimeUnit unit) {
        return createSeq(new SkipWhileTimeOperator<>(source, time, unit));
    }

    @Override
    public ReactiveSeq<T> take(final long time, final TimeUnit unit) {
        return createSeq(new LimitWhileTimeOperator<>(source, time, unit));

    }

    @Override
    public ReactiveSeq<Vector<T>> groupedWhile(final Predicate<? super T> predicate) {
        return createSeq(new GroupedWhileOperator<>(source, () -> Vector.empty(), Function.identity(), predicate));


    }

    @Override
    public <C extends PersistentCollection<? super T>> ReactiveSeq<C> groupedWhile(final Predicate<? super T> predicate, final Supplier<C> factory) {
        return createSeq(new GroupedWhileOperator<>(source, factory, Function.identity(), predicate));
    }


    @Override
    public <C extends PersistentCollection<? super T>> ReactiveSeq<C> groupedBySizeAndTime(final int size, final long time, final TimeUnit unit,
                                                                                 final Supplier<C> factory) {
        return createSeq(new GroupedByTimeAndSizeOperator(this.source, factory,
                Function.identity(), time, unit, size));

    }

    @Override
    public <C extends PersistentCollection<? super T>, R> ReactiveSeq<R> groupedBySizeAndTime(final int size, final long time,
                                                                                    final TimeUnit unit,
                                                                                    final Supplier<C> factory,
                                                                                    Function<? super C, ? extends R> finalizer
    ) {
        return createSeq(new GroupedByTimeAndSizeOperator(this.source, factory,
                finalizer, time, unit, size)
        );

    }

    @Override
    public <C extends PersistentCollection<? super T>, R> ReactiveSeq<R> groupedByTime(final long time, final TimeUnit unit, final Supplier<C> factory, Function<? super C, ? extends R> finalizer) {
        return createSeq(new GroupedByTimeOperator(this.source, factory,
                finalizer, time, unit)
        );

    }

    @Override
    public <C extends PersistentCollection<? super T>> ReactiveSeq<C> groupedByTime(final long time, final TimeUnit unit, final Supplier<C> factory) {
        return createSeq(new GroupedByTimeOperator(this.source, factory,
                Function.identity(), time, unit)
        );

    }

    @Override
    public <C extends PersistentCollection<? super T>> ReactiveSeq<C> grouped(final int size, final Supplier<C> factory) {
        return createSeq(new GroupingOperator<>(source, factory, Function.identity(), size));

    }

    @Override
    public ReactiveSeq<T> dropRight(final int num) {

        if(num==1)
            return createSeq(new SkipLastOneOperator<>(source));
        return createSeq(new SkipLastOperator<>(source, num < 0 ? 0 : num));
    }

    @Override
    public ReactiveSeq<T> takeRight(final int num) {
        if (num == 1)
            return createSeq(new LimitLastOneOperator<>(source));
        return createSeq(new LimitLastOperator<>(source, num < 0 ? 0 : num));
    }

    @Override
    public ReactiveSeq<T> onComplete(final Runnable fn) {
        return createSeq(new CompleteOperator<>(source, fn));
    }

    @Override
    public ReactiveSeq<T> recover(final Function<? super Throwable, ? extends T> fn) {
        return createSeq(new RecoverOperator<>(source, fn));
    }

    @Override
    public <EX extends Throwable> ReactiveSeq<T> recover(final Class<EX> exceptionClass, final Function<? super EX, ? extends T> fn) {

        Function<? super Throwable, ? extends EX> accept = e -> {
            if (exceptionClass.isAssignableFrom(e.getClass())) {
                return (EX) e;
            }
            throw ExceptionSoftener.throwSoftenedException(e);
        };
        return createSeq(new RecoverOperator<>(source, fn.compose(accept)));
    }


    @Override
    public void forEachOrdered(final Consumer<? super T> consumer) {
        Future<Boolean> complete = Future.future();
        if (async == Type.NO_BACKPRESSURE) {
            source.subscribeAll(consumer, this.defaultErrorHandler, () -> complete.complete(true));
        } else {
            source.subscribe(consumer, this.defaultErrorHandler, () -> complete.complete(true)).request(Long.MAX_VALUE);
        }
        complete.get();

    }

    @Override
    public <X extends Throwable> Subscription forEachSubscribe(final Consumer<? super T> consumer) {
        StreamSubscription sub = source.subscribe(consumer, this.defaultErrorHandler, () -> {
        });
        return sub;
    }

    @Override
    public <X extends Throwable> Subscription forEachSubscribe(final Consumer<? super T> consumer,
                                                               final Consumer<? super Throwable> consumerError) {

        StreamSubscription sub = source.subscribe(consumer, consumerError, () -> {
        });
        return sub;
    }

    @Override
    public <X extends Throwable> Subscription forEachSubscribe(final Consumer<? super T> consumer,
                                                               final Consumer<? super Throwable> consumerError, final Runnable onComplete) {
        StreamSubscription sub = source.subscribe(consumer, consumerError, onComplete);
        return sub;
    }

    @Override
    public <X extends Throwable> Subscription forEach(final long numberOfElements, final Consumer<? super T> consumer) {
        StreamSubscription sub = source.subscribe(consumer, this.defaultErrorHandler, () -> {
        });
        sub.request(numberOfElements);
        return sub;
    }

    @Override
    public <X extends Throwable> Subscription forEach(final long numberOfElements, final Consumer<? super T> consumer,
                                                      final Consumer<? super Throwable> consumerError) {

        StreamSubscription sub = source.subscribe(consumer, consumerError, () -> {
        });
        sub.request(numberOfElements);
        return sub;
    }

    @Override
    public <X extends Throwable> Subscription forEach(final long numberOfElements, final Consumer<? super T> consumer,
                                                      final Consumer<? super Throwable> consumerError, final Runnable onComplete) {
        StreamSubscription sub = source.subscribe(consumer, consumerError, onComplete);
        sub.request(numberOfElements);
        return sub;
    }

    @Override
    public <X extends Throwable> void forEach(final Consumer<? super T> consumerElement, final Consumer<? super Throwable> consumerError) {
        if (async != Type.NO_BACKPRESSURE) {
            this.source.subscribe(consumerElement, consumerError, () -> {
            }).request(Long.MAX_VALUE);
        } else {
            source.subscribeAll(consumerElement, consumerError, () -> {
            });
        }


    }

    @Override
    public <X extends Throwable> void forEach(final Consumer<? super T> consumerElement, final Consumer<? super Throwable> consumerError,
                                              final Runnable onComplete) {
        if (async != Type.NO_BACKPRESSURE) {
            this.source.subscribe(consumerElement, consumerError, onComplete).request(Long.MAX_VALUE);
        } else {
            source.subscribeAll(consumerElement, consumerError, onComplete);
        }


    }

    @Override
    public ReactiveSeq<T> mergeP(final QueueFactory<T> factory, final Publisher<T>... publishers) {
        Publisher<T>[] pubs = new Publisher[publishers.length + 1];
        pubs[0] = this;
        System.arraycopy(publishers, 0, pubs, 1, publishers.length);
        ReactiveStreamX<T> merged = (ReactiveStreamX<T>) Spouts.mergeLatest(pubs);
        if (async == SYNC || async == BACKPRESSURE)
            return merged.withAsync(BACKPRESSURE);
        else
            return merged.withAsync(Type.NO_BACKPRESSURE);
    }

    @Override
    public ReactiveSeq<T> mergeP(final Publisher<T>... publishers) {
        Publisher<T>[] pubs = new Publisher[publishers.length + 1];
        pubs[0] = this;
        System.arraycopy(publishers, 0, pubs, 1, publishers.length);

        ReactiveStreamX<T> merged = (ReactiveStreamX<T>) Spouts.mergeLatest(pubs);
        if (async == Type.SYNC || async == Type.BACKPRESSURE)
            return merged.withAsync(Type.BACKPRESSURE);
        else
            return merged.withAsync(Type.NO_BACKPRESSURE);
    }

    @Override
    public Topic<T> broadcast() {
        if (async == Type.NO_BACKPRESSURE) {
            Queue<T> queue = QueueFactories.<T>boundedNonBlockingQueue(1000)
                                           .build()
                                           .withTimeout(1);

            Topic<T> topic = new Topic<>(queue, QueueFactories.<T>boundedNonBlockingQueue(1000));
            AtomicBoolean wip = new AtomicBoolean(false);

            Continuation contRef[] = {null};
            Continuation cont =
                    new Continuation(() -> {

                        if (wip.compareAndSet(false, true)) {
                            try {
                                source.subscribeAll(topic::offer, e -> topic.close(), () -> topic.close());
                            } finally {
                                wip.set(false);
                            }

                        }


                        return Continuation.empty();
                    });

            contRef[0] = cont;
            queue.addContinuation(cont);
            return topic;
        }
        Queue<T> queue = QueueFactories.<T>boundedNonBlockingQueue(1000)
                                       .build()
                                       .withTimeout(1);

        Topic<T> topic = new Topic<>(queue, QueueFactories.<T>boundedNonBlockingQueue(1000));
        AtomicBoolean wip = new AtomicBoolean(false);
        Subscription s = source.subscribe(topic::offer, e -> topic.close(), () -> topic.close());
        Continuation contRef[] = {null};
        Continuation cont =
                new Continuation(() -> {

                    if (wip.compareAndSet(false, true)) {
                        try {

                            //use the first consuming thread to tell this Stream onto the Queue
                            s.request(1000-queue.size());
                        }finally {
                            wip.set(false);
                        }

                    }


                    return contRef[0];
                });

        contRef[0] = cont;
        queue.addContinuation(cont);
        return topic;
    }


    /* (non-Javadoc)
     * @see com.oath.cyclops.lambda.monads.Pure#unit(java.lang.Object)
     */
    @Override
    public <T> ReactiveSeq<T> unit(final T unit) {
        return Spouts.of(unit);
    }

    @Override
    public <U, R> ReactiveSeq<R> zipWithStream(Stream<? extends U> other, BiFunction<? super T, ? super U, ? extends R> zipper) {
        Operator<U> right;
        if (other instanceof ReactiveStreamX) {
            right = ((ReactiveStreamX<U>) other).source;
        } else {
            right = new SpliteratorToOperator<U>(((Stream<U>) other).spliterator());
        }
        return createSeq(new ZippingOperator<>(source, right, zipper), async);
    }


    @Override
    public Connectable<T> schedule(final String cron, final ScheduledExecutorService ex) {
        return Streams.schedule(this, cron, ex);

    }

    @Override
    public Connectable<T> scheduleFixedDelay(final long delay, final ScheduledExecutorService ex) {
        return Streams.scheduleFixedDelay(this, delay, ex);
    }

    @Override
    public Connectable<T> scheduleFixedRate(final long rate, final ScheduledExecutorService ex) {
        return Streams.scheduleFixedRate(this, rate, ex);

    }


    @Override
    public ReactiveSeq<T> limit(long num) {
        return createSeq(new LimitOperator<>(source, num));
    }

    @Override
    public ReactiveSeq<T> skip(long num) {
        return createSeq(new SkipOperator<>(source, num));
    }

    @Override
    public ReactiveSeq<T> cycle() {

        ReactiveSeq<T> cycling = collectAll(Collectors.toList())
                .map(s -> Spouts.fromIterable(s).cycle(Long.MAX_VALUE))
                .flatMap(i -> i);
        return createSeq(new IterableSourceOperator<T>(cycling), SYNC);


    }


    @Override
    public Tuple2<ReactiveSeq<T>, ReactiveSeq<T>> duplicate() {


        Seq<Iterable<T>> copy = Streams.toBufferingCopier(() -> iterator(), 2);
        return Tuple.tuple(createSeq(new IterableSourceOperator<>(copy.getOrElseGet(0,()->Arrays.asList())), SYNC),
                createSeq(new IterableSourceOperator<>(copy.getOrElseGet(1,()->Arrays.asList())), SYNC));


    }

    @Override
    public Tuple2<ReactiveSeq<T>, ReactiveSeq<T>> duplicate(Supplier<Deque<T>> bufferFactory) {


        Seq<Iterable<T>> copy = Streams.toBufferingCopier(() -> iterator(), 2, bufferFactory);
        return Tuple.tuple(createSeq(new IterableSourceOperator<>(copy.getOrElseGet(0,()->Arrays.asList())), SYNC),
                createSeq(new IterableSourceOperator<>(copy.getOrElseGet(1,()->Arrays.asList())), SYNC));


    }

    @Override
    @SuppressWarnings("unchecked")
    public Tuple3<ReactiveSeq<T>, ReactiveSeq<T>, ReactiveSeq<T>> triplicate() {


        Seq<Iterable<T>> copy = Streams.toBufferingCopier(() -> iterator(), 3);
        return Tuple.tuple(createSeq(new IterableSourceOperator<>(copy.getOrElseGet(0,()->Arrays.asList())), SYNC),
                createSeq(new IterableSourceOperator<>(copy.getOrElseGet(1,()->Arrays.asList())), SYNC),
            createSeq(new IterableSourceOperator<>(copy.getOrElseGet(2,()->Arrays.asList())), SYNC));


    }

    @Override
    @SuppressWarnings("unchecked")
    public Tuple3<ReactiveSeq<T>, ReactiveSeq<T>, ReactiveSeq<T>> triplicate(Supplier<Deque<T>> bufferFactory) {


        Seq<Iterable<T>> copy = Streams.toBufferingCopier(() -> iterator(), 3, bufferFactory);
        return Tuple.tuple(createSeq(new IterableSourceOperator<>(copy.getOrElseGet(0,()->Arrays.asList())), SYNC),
                createSeq(new IterableSourceOperator<>(copy.getOrElseGet(1,()->Arrays.asList())), SYNC),
            createSeq(new IterableSourceOperator<>(copy.getOrElseGet(2,()->Arrays.asList())), SYNC));

    }

    public Seq<ReactiveSeq<T>> multicast(int num) {
        if (this.async == Type.NO_BACKPRESSURE) {
            ConcurrentLinkedQueue<Subscriber> subs = new ConcurrentLinkedQueue<>();
            Seq<ReactiveSeq<T>> result = Seq.empty();
            for (int i = 0; i < num; i++) {
                ReactiveSeq<T> seq = Spouts.<T>async(s1 -> {
                    subs.add(s1.asSubscriber());
                    if (subs.size() == num) {
                        this.forEach(e -> subs.forEach(s -> s.onNext(e)), ex -> subs.forEach(s -> s.onError(ex)), () -> subs.forEach(s -> s.onComplete()));
                    }
                });
                result = result.plus(seq);
            }
            return result;

        }
        if (this.async == BACKPRESSURE) {
            ConcurrentLinkedQueue<Subscriber> subs = new ConcurrentLinkedQueue<>();
            Seq<ReactiveSeq<T>> result = Seq.empty();
            Subscription sub = forEachSubscribe(e -> subs.forEach(s -> s.onNext(e)), ex -> subs.forEach(s -> s.onError(ex)), () -> subs.forEach(s -> s.onComplete()));
            for (int i = 0; i < num; i++) {
                ReactiveSeq<T> seq = new ReactiveStreamX<T>(new PublisherToOperator<T>(new Publisher<T>() {
                    @Override
                    public void subscribe(Subscriber<? super T> s) {

                        subs.add(s);
                        s.onSubscribe(sub);


                    }
                }));

                result = result.plus(seq);
            }
            return result;

        }
        return Streams.toBufferingCopier(() -> iterator(), num, () -> new ArrayDeque<T>(100))
                .map(ReactiveSeq::fromIterable);
    }


    @Override
    @SuppressWarnings("unchecked")
    public Tuple4<ReactiveSeq<T>, ReactiveSeq<T>, ReactiveSeq<T>, ReactiveSeq<T>> quadruplicate() {
        Seq<Iterable<T>> copy = Streams.toBufferingCopier(() -> iterator(), 4);
        return Tuple.tuple(createSeq(new IterableSourceOperator<>(copy.getOrElseGet(0,()->Arrays.asList())), SYNC),
                createSeq(new IterableSourceOperator<>(copy.getOrElseGet(1,()->Arrays.asList())), SYNC),
                createSeq(new IterableSourceOperator<>(copy.getOrElseGet(2,()->Arrays.asList())), SYNC),
                createSeq(new IterableSourceOperator<>(copy.getOrElseGet(3,()->Arrays.asList())), SYNC));


    }

    @Override
    @SuppressWarnings("unchecked")
    public Tuple4<ReactiveSeq<T>, ReactiveSeq<T>, ReactiveSeq<T>, ReactiveSeq<T>> quadruplicate(Supplier<Deque<T>> bufferFactory) {
        Seq<Iterable<T>> copy = Streams.toBufferingCopier(() -> iterator(), 4, bufferFactory);
        return Tuple.tuple(createSeq(new IterableSourceOperator<>(copy.getOrElseGet(0,()->Arrays.asList())), SYNC),
                createSeq(new IterableSourceOperator<>(copy.getOrElseGet(1,()->Arrays.asList())), SYNC),
                createSeq(new IterableSourceOperator<>(copy.getOrElseGet(2,()->Arrays.asList())), SYNC),
                createSeq(new IterableSourceOperator<>(copy.getOrElseGet(3,()->Arrays.asList())), SYNC));
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Tuple2<Option<T>, ReactiveSeq<T>> splitAtHead() {
        final Tuple2<ReactiveSeq<T>, ReactiveSeq<T>> Tuple2 = splitAt(1);
        return Tuple.tuple(
                Tuple2._1().to().option()
                        .flatMap(l -> {
                            return l.size() > 0 ? l.get(0) : Option.none();
                        }),
                Tuple2._2());
    }

    @Override
    public Tuple2<ReactiveSeq<T>, ReactiveSeq<T>> splitAt(final int where) {
        final Tuple2<ReactiveSeq<T>, ReactiveSeq<T>> Tuple2 = duplicate();
        return Tuple.tuple(
                Tuple2._1().limit(where), Tuple2._2().skip(where));


    }

    @Override
    public Tuple2<ReactiveSeq<T>, ReactiveSeq<T>> splitBy(final Predicate<T> splitter) {
        final Tuple2<ReactiveSeq<T>, ReactiveSeq<T>> Tuple2 = duplicate();
        return Tuple.tuple(
                Tuple2._1().takeWhile(splitter), Tuple2._2().dropWhile(splitter));
    }

    @Override
    public Tuple2<ReactiveSeq<T>, ReactiveSeq<T>> partition(final Predicate<? super T> splitter) {
        final Tuple2<ReactiveSeq<T>, ReactiveSeq<T>> Tuple2 = duplicate();
        return Tuple.tuple(
                Tuple2._1().filter(splitter), Tuple2._2().filter(splitter.negate()));

    }

    @Override
    public <U> ReactiveSeq<Tuple2<T, U>> zipWithStream(Stream<? extends U> other) {
        Operator<U> right;
        if (other instanceof ReactiveStreamX) {
            right = ((ReactiveStreamX<U>) other).source;
        } else if (other instanceof Iterable) {
            right = new IterableSourceOperator<U>(((Iterable<U>) other));
        } else {
            //not replayable
            right = new SpliteratorToOperator<U>(((Stream<U>) other).spliterator());
        }
        ReactiveStreamX<Tuple2<T, U>> res = createSeq(new ZippingOperator<>(source, right, Tuple::tuple));
        if (this.async == SYNC) {
            //zip could recieve an asyncrhonous Stream so we force onto the async path
            return res.withAsync(BACKPRESSURE);
        }
        return res;

    }

    @Override
    public <S, U> ReactiveSeq<Tuple3<T, S, U>> zip3(Iterable<? extends S> second, Iterable<? extends U> third) {
        return zip(second, Tuple::tuple).zip(third, (a, b) -> Tuple.tuple(a._1(), a._2(), b));
    }

    @Override
    public <T2, T3, T4> ReactiveSeq<Tuple4<T, T2, T3, T4>> zip4(Iterable<? extends T2> second, Iterable<? extends T3> third, Iterable<? extends T4> fourth) {
        return zip(second, Tuple::tuple).zip(third, (a, b) -> Tuple.tuple(a._1(), a._2(), b))
                .zip(fourth, (a, b) -> (Tuple4<T, T2, T3, T4>) Tuple.tuple(a._1(), a._2(), a._3(), b));
    }

    @Override
    public ReactiveSeq<T> cycle(long times) {
        return grouped(Integer.MAX_VALUE, ()-> Vector.empty())
                .map(ReactiveSeq::fromIterable)
                .concatMap(s -> s.cycle(times));

    }

    @Override
    public <U> U reduce(final U identity, final BiFunction<U, ? super T, U> accumulator, final BinaryOperator<U> combiner) {
        return reduce(identity,accumulator);
    }
    @Override
    public <U> U reduce(final U identity, final BiFunction<U, ? super T, U> accumulator) {
        Future<U> future = Future.future();
        Object[] current = {identity};
        forEach(e -> current[0] = (U) accumulator.apply((U) current[0], e), this.defaultErrorHandler, () -> future.complete((U) current[0]));
        return future.getFuture().join();
    }

    @Override
    public T reduce(T identity, BinaryOperator<T> accumulator) {
        return findFirstCallAll((ReactiveStreamX<T>) reduceAll(identity, accumulator)).orElse(identity);
    }

    @Override
    public final <R, A> R collect(final Collector<? super T, A, R> collector) {

        return findFirstCallAll((ReactiveStreamX<R>) collectAll(collector)).orElseGet(()->collector.finisher().apply(collector.supplier().get()));


    }
    @Override
    public final ReactiveSeq<Tuple2<T,Integer>> occurances(){

         return Spouts.defer(() -> {
            ReactiveSeq<Map<T, Integer>> map = collectAll(Collectors.toMap(k -> k, v -> 1, (a, b) -> a + b));
            return map.flatMap(m->m.entrySet().stream());
        }).map(e->Tuple.tuple(e.getKey(),e.getValue()));
    }

    @Override
    public <R> R collect(final Supplier<R> supplier, final BiConsumer<R, ? super T> accumulator, final BiConsumer<R, R> combiner) {

        Future<R> future = Future.future();
        R container = supplier.get();
        forEach(e -> accumulator.accept(container, e), this.defaultErrorHandler, () -> future.complete(container));
        //@TODO - switch to visit and use the error handler?
        return future.getFuture().join();
    }

    @Override
    public Optional<T> reduce(BinaryOperator<T> accumulator) {
        Future<T> future = Future.future();
        Object[] current = {null};
        forEach(e -> current[0] = current[0] != null ? accumulator.apply((T) current[0], e) : e, this.defaultErrorHandler, () -> future.complete((T) current[0]));
        return Optional.ofNullable(future.get().fold(s->s, e->{throw ExceptionSoftener.throwSoftenedException(e);}));

    }

    @Override
    public final boolean allMatch(final Predicate<? super T> c) {
        Future<Boolean> result = Future.future();
        if (async == Type.NO_BACKPRESSURE) {
            ReactiveStreamX<T> filtered = (ReactiveStreamX<T>) filter(c.negate());
            filtered.source.subscribeAll(e -> {
                result.complete(false);
                throw new Queue.ClosedQueueException();
            }, t -> {
                result.completeExceptionally(t);
            }, () -> {
                if (!result.isDone()) {
                    result.complete(true);
                }
            });
        } else {
            ReactiveStreamX<T> filtered = (ReactiveStreamX<T>) filter(c.negate());
            filtered.source.subscribe(e -> {
                result.complete(false);

            }, t -> {
                result.completeExceptionally(t);
            }, () -> {
                if (!result.isDone()) {
                    result.complete(true);
                }
            }).request(1l);
        }

        return result.getFuture().join();

    }

    @Override
    public final boolean anyMatch(final Predicate<? super T> c) {
        Future<Boolean> result = Future.future();
        ReactiveStreamX<T> filtered = (ReactiveStreamX<T>) filter(c);
        if (async == Type.NO_BACKPRESSURE) {
            filtered.source.subscribeAll(e -> {
                result.complete(true);
                throw new Queue.ClosedQueueException();
            }, t -> {

                result.completeExceptionally(t);
            }, () -> {
                if (!result.isDone()) {
                    result.complete(false);
                }
            });


            return result.getFuture().join();
        }

        Subscription sub[] = {null};
        //may be quicker to use forEachAsync and throw an Exception with fillInStackTrace overriden
        sub[0] = filtered.source.subscribe(e -> {
            result.complete(true);
            sub[0].cancel();


        }, e -> {
            result.completeExceptionally(e);
            sub[0].cancel();

        }, () -> {
            if (!result.isDone()) {
                result.complete(false);
            }
        });
        sub[0].request(1l);

        return result.getFuture().join();
    }

    @Override
    public <R> R fold(Function<? super ReactiveSeq<T>, ? extends R> sync, Function<? super ReactiveSeq<T>, ? extends R> reactiveStreams,
                      Function<? super ReactiveSeq<T>, ? extends R> asyncNoBackPressure) {
        switch (this.async) {
            case SYNC:
                return sync.apply(this);
            case BACKPRESSURE:
                return reactiveStreams.apply(this);
            case NO_BACKPRESSURE:
                return asyncNoBackPressure.apply(this);
        }
        return null;
    }

    @Override
    public T singleOrElse(T alt) {
        return single().fold(s -> s, () -> alt);
    }

    @Override
    public Maybe<T> single(Predicate<? super T> predicate) {
        return filter(predicate).single();
    }

    @Override
    public Maybe<T> single() {
        Maybe.CompletableMaybe<T, T> maybe = Maybe.<T>maybe();
        subscribe(new Subscriber<T>() {
            T value = null;
            Subscription sub;

            @Override
            public void onSubscribe(Subscription s) {
                this.sub = s;
                s.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(T t) {
                if (value == null)
                    value = t;
                else {
                    maybe.complete(null);
                    sub.cancel();
                }
            }

            @Override
            public void onError(Throwable t) {
                maybe.completeExceptionally(t);
            }

            @Override
            public void onComplete() {
                maybe.complete(value);
            }
        });
        return maybe;
    }


}
