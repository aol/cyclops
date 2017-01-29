package com.aol.cyclops2.internal.stream;

import com.aol.cyclops2.data.collections.extensions.CollectionX;
import com.aol.cyclops2.internal.stream.spliterators.LazyMappingSpliterator;
import com.aol.cyclops2.types.Unwrapable;
import com.aol.cyclops2.types.anyM.AnyMSeq;
import com.aol.cyclops2.types.stream.HeadAndTail;
import com.aol.cyclops2.util.ExceptionSoftener;
import cyclops.Streams;
import cyclops.collections.ListX;
import cyclops.function.Monoid;
import cyclops.function.Reducer;
import cyclops.monads.AnyM;
import cyclops.monads.Witness;
import cyclops.stream.ReactiveSeq;
import cyclops.stream.Streamable;

import java.io.BufferedReader;
import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.function.*;
import java.util.stream.*;

/**
 * Created by johnmcclean on 13/01/2017.
 */
public abstract class BaseExtendedStream<T> implements Unwrapable, ReactiveSeq<T>, Iterable<T>  {

    public abstract Stream<T> unwrapStream();
    @Override
    public final ReactiveSeq<T> parallel() {
        return this;
    }

    @Override
    public boolean endsWithIterable(final Iterable<T> iterable) {
        return Streams.endsWith(this, iterable);
    }
    @Override
    public boolean allMatch(final Predicate<? super T> c) {
        return unwrapStream().allMatch(c);
    }

    @Override
    public boolean anyMatch(final Predicate<? super T> c) {
        return unwrapStream().anyMatch(c);
    }

    @Override
    public boolean xMatch(final int num, final Predicate<? super T> c) {
        return Streams.xMatch(this, num, c);
    }

    @Override
    public final boolean noneMatch(final Predicate<? super T> c) {
        return allMatch(c.negate());
    }

    @Override
    public final String join() {
        return Streams.join(this, "");
    }

    @Override
    public final String join(final String sep) {
        return Streams.join(this, sep);
    }

    @Override
    public final String join(final String sep, final String start, final String end) {
        return Streams.join(this, sep, start, end);
    }

    @Override
    public final <U extends Comparable<? super U>> Optional<T> minBy(final Function<? super T, ? extends U> function) {

        return Streams.minBy(this, function);
    }

    @Override
    public final Optional<T> min(final Comparator<? super T> comparator) {
        return Streams.min(this, comparator);
    }

    @Override
    public final <C extends Comparable<? super C>> Optional<T> maxBy(final Function<? super T, ? extends C> f) {
        return Streams.maxBy(this, f);
    }

    @Override
    public final Optional<T> max(final Comparator<? super T> comparator) {
        return Streams.max(this, comparator);
    }

    @Override
    public final HeadAndTail<T> headAndTail() {
        return Streams.headAndTail(this);
    }
    @Override
    public final Optional<T> findAny() {
        return findFirst();
    }

    @Override
    public final <R> R mapReduce(final Reducer<R> reducer) {
        return reducer.mapReduce(unwrapStream());
    }

    @Override
    public final <R> R mapReduce(final Function<? super T, ? extends R> mapper, final Monoid<R> reducer) {
        return reducer.reduce(map(mapper));
    }

    @Override
    public <R, A> R collect(final Collector<? super T, A, R> collector) {

        return unwrapStream().collect(collector);


    }

    @Override
    public <R> R collect(final Supplier<R> supplier, final BiConsumer<R, ? super T> accumulator, final BiConsumer<R, R> combiner) {
        return unwrapStream().collect(supplier, accumulator, combiner);
    }

    @Override
    public final T reduce(final Monoid<T> reducer) {

        return reducer.reduce(this);
    }
    @Override
    public <U> U reduce(final U identity, final BiFunction<U, ? super T, U> accumulator, final BinaryOperator<U> combiner) {
        return unwrapStream().reduce(identity, accumulator, combiner);
    }

    @Override
    public final ListX<T> reduce(final Stream<? extends Monoid<T>> reducers) {
        return Streams.reduce(this, reducers);
    }

    @Override
    public final ListX<T> reduce(final Iterable<? extends Monoid<T>> reducers) {
        return Streams.reduce(this, reducers);
    }

    public final T foldLeft(final Monoid<T> reducer) {
        return reduce(reducer);
    }

    public final T foldLeft(final T identity, final BinaryOperator<T> accumulator) {
        return unwrapStream().reduce(identity, accumulator);
    }

    public final <T> T foldLeftMapToType(final Reducer<T> reducer) {
        return reducer.mapReduce(unwrapStream());
    }

    @Override
    public final T foldRight(final Monoid<T> reducer) {
        return reducer.reduce(reverse());
    }

    @Override
    public final <U> U foldRight(final U seed, final BiFunction<? super T, ? super U, ? extends U> function) {
        return reverse().foldLeft(seed, (u,t)->function.apply(t, u));

    }

    @Override
    public final <T> T foldRightMapToType(final Reducer<T> reducer) {
        return reducer.mapReduce(reverse());
    }

    @Override
    public final Streamable<T> toStreamable() {
        return Streamable.fromStream(stream());
    }

    @Override
    public final Set<T> toSet() {
        return collect(Collectors.toSet());
    }

    @Override
    public final List<T> toList() {

        return collect(Collectors.toList());
    }

    @Override
    public final <C extends Collection<T>> C toCollection(final Supplier<C> collectionFactory) {

        return collect(Collectors.toCollection(collectionFactory));
    }

    @Override
    public final <T> Stream<T> toStream() {
        return (Stream<T>) this.unwrapStream();
    }

    @Override
    public final ReactiveSeq<T> stream() {
        return this;

    }

    @Override
    public final boolean startsWithIterable(final Iterable<T> iterable) {
        return Streams.startsWith(this, iterable);

    }

    @Override
    public final boolean startsWith(final Stream<T> stream2) {
        return Streams.startsWith(this, stream2);

    }

    @Override
    public AnyMSeq<Witness.reactiveSeq,T> anyM() {
        return AnyM.fromStream(this);

    }
    @Override
    public final <R> ReactiveSeq<R> flatMapStream(final Function<? super T, BaseStream<? extends R, ?>> fn) {
        return createSeq(Streams.flatMapStream(this, fn));

    }

    public final <R> ReactiveSeq<R> flatMapOptional(final Function<? super T, Optional<? extends R>> fn) {
        return createSeq(Streams.flatMapOptional(this, fn));

    }

    protected abstract <R> ReactiveSeq<R> createSeq(Stream<R> rStream);

    public final <R> ReactiveSeq<R> flatMapCompletableFuture(final Function<? super T, CompletableFuture<? extends R>> fn) {
        return createSeq(Streams.flatMapCompletableFuture(this, fn));
    }

    public final ReactiveSeq<Character> flatMapCharSequence(final Function<? super T, CharSequence> fn) {
        return createSeq(Streams.flatMapCharSequence(this, fn));
    }

    public final ReactiveSeq<String> flatMapFile(final Function<? super T, File> fn) {
        return createSeq(Streams.flatMapFile(this, fn));
    }

    public final ReactiveSeq<String> flatMapURL(final Function<? super T, URL> fn) {
        return createSeq(Streams.flatMapURL(this, fn));
    }

    public final ReactiveSeq<String> flatMapBufferedReader(final Function<? super T, BufferedReader> fn) {
        return createSeq(Streams.flatMapBufferedReader(this, fn));
    }
    @Override
    public boolean isParallel() {
        return false;
    }

    @Override
    public ReactiveSeq<T> sequential() {
        return this;
    }

    @Override
    public ReactiveSeq<T> unordered() {
        return this;
    }

    @Override
    public IntStream mapToInt(final ToIntFunction<? super T> mapper) {
        return unwrapStream().mapToInt(mapper);
    }

    @Override
    public LongStream mapToLong(final ToLongFunction<? super T> mapper) {
        return unwrapStream().mapToLong(mapper);
    }

    @Override
    public DoubleStream mapToDouble(final ToDoubleFunction<? super T> mapper) {
        return unwrapStream().mapToDouble(mapper);
    }

    @Override
    public IntStream flatMapToInt(final Function<? super T, ? extends IntStream> mapper) {
        return unwrapStream().flatMapToInt(mapper);
    }

    @Override
    public LongStream flatMapToLong(final Function<? super T, ? extends LongStream> mapper) {
        return unwrapStream().flatMapToLong(mapper);
    }

    @Override
    public DoubleStream flatMapToDouble(final Function<? super T, ? extends DoubleStream> mapper) {
        return unwrapStream().flatMapToDouble(mapper);
    }

    @Override
    public void forEachOrdered(final Consumer<? super T> action) {
        unwrapStream().forEachOrdered(action);

    }

    @Override
    public Object[] toArray() {
        return unwrapStream().toArray();
    }

    @Override
    public <A> A[] toArray(final IntFunction<A[]> generator) {
        return unwrapStream().toArray(generator);
    }


    @Override
    public CollectionX<T> toLazyCollection() {
        return Streams.toLazyCollection(this);
    }

    @Override
    public CollectionX<T> toConcurrentLazyCollection() {
        return Streams.toConcurrentLazyCollection(this);
    }

    public Streamable<T> toLazyStreamable() {
        return Streams.toLazyStreamable(this);
    }

    @Override
    public Streamable<T> toConcurrentLazyStreamable() {
        return Streams.toConcurrentLazyStreamable(this);

    }


    @Override
    public ReactiveSeq<T> onClose(final Runnable closeHandler) {

        return this;
    }

    @Override
    public void close() {

    }
    @Override
    public ReactiveSeq<T> xPer(final int x, final long time, final TimeUnit t) {
        final long next = t.toNanos(time);
        Supplier<Function<? super T, ? extends T>> lazy = ()-> {

            long[] last = {-1};
            int[] count = {0};
            return a-> {
                if (++count[0] < x)
                    return a;
                count[0] = 0;
                final long sleepFor = next - (System.nanoTime() - last[0]);

                LockSupport.parkNanos(sleepFor);

                last[0] = System.nanoTime();
                return a;
            };
        };
        return mapLazyFn(lazy);

    }
    @Override
    public final ReactiveSeq<T> sorted() {
        return createSeq(unwrapStream().sorted());
    }

    public  abstract <R> ReactiveSeq<R> mapLazyFn(Supplier<Function<? super T, ? extends R>> fn);
    public abstract ReactiveSeq<T> filterLazyPredicate(final Supplier<Predicate<? super T>> fn);
    @Override
    public ReactiveSeq<T> onePer(final long time, final TimeUnit t) {
        final long next = t.toNanos(time);
        Supplier<Function<? super T, ? extends T>> lazy = ()-> {

            long[] last = {-1};
            return a-> {
                final long sleepFor = next - (System.nanoTime() - last[0]);

                LockSupport.parkNanos(sleepFor);

                last[0] = System.nanoTime();
                return a;
            };
        };
        return mapLazyFn(lazy);

    }

    @Override
    public ReactiveSeq<T> debounce(final long time, final TimeUnit t) {
        final long timeNanos = t.toNanos(time);

        Supplier<Predicate<? super T>> lazy = ()-> {
            final long[] last = {-1};

            return a-> {
                System.out.println("A is  " +a);
                if(last[0]==-1) {
                    System.out.println("Allowing "+ a);
                    last[0] = System.nanoTime();
                    return true;
                }
                long elapsedNanos  =  (System.nanoTime() - last[0]);
                System.out.println(System.nanoTime() + " " + elapsedNanos + " " + last[0] + " " + timeNanos + " " + (elapsedNanos >= timeNanos));
                T nextValue = null;
                if (elapsedNanos >= timeNanos) {

                    last[0] = System.nanoTime();
                    return true;


                }
                return false;
            };
        };
        return filterLazyPredicate(lazy);

    }
    @Override
    public ReactiveSeq<T> fixedDelay(final long l, final TimeUnit unit) {
        final long elapsedNanos = unit.toNanos(l);
        final long millis = elapsedNanos / 1000000;
        final int nanos = (int) (elapsedNanos - millis * 1000000);
        return map(a->{
            try {

                Thread.sleep(Math.max(0, millis), Math.max(0, nanos));
                return a;
            } catch (final InterruptedException e) {
                throw ExceptionSoftener.throwSoftenedException(e);

            }
        });
        //return createSeq(Streams.fixedDelay(this, l, unit), this.reversible,split);
    }

    @Override
    public ReactiveSeq<T> jitter(final long l) {

        final Random r = new Random();
        return map(a->{
            try {
                final long elapsedNanos = (long) (l * r.nextDouble());
                final long millis = elapsedNanos / 1000000;
                final int nanos = (int) (elapsedNanos - millis * 1000000);
                Thread.sleep(Math.max(0, millis), Math.max(0, nanos));
                return a;
            } catch (final InterruptedException e) {
                throw ExceptionSoftener.throwSoftenedException(e);

            }
        });

        // return createSeq(Streams.jitter(this, l), this.reversible,split);
    }

    @Override
    public T foldRight(final T identity, final BinaryOperator<T> accumulator) {
        return reverse().foldLeft(identity, accumulator);
    }

    @Override
    public boolean endsWith(final Stream<T> iterable) {
        return Streams.endsWith(this, () -> iterable.iterator());
    }
    @Override
    public T firstValue() {
        return findFirst().get();
    }
    @Override
    public Optional<T> reduce(BinaryOperator<T> accumulator) {
        return unwrapStream().reduce(accumulator);
    }

    @Override
    public T reduce(T identity, BinaryOperator<T> accumulator) {
        return unwrapStream().reduce(identity,accumulator);
    }
}

