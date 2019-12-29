package com.oath.cyclops.internal.stream;

import com.oath.cyclops.types.Unwrappable;

import com.oath.cyclops.util.ExceptionSoftener;

import cyclops.companion.Streams;
import cyclops.data.Seq;
import cyclops.function.Monoid;
import cyclops.function.Reducer;

import cyclops.reactive.ReactiveSeq;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.function.*;
import java.util.stream.*;

/**
 * Created by johnmcclean on 13/01/2017.
 */
public abstract class BaseExtendedStream<T> implements Unwrappable, ReactiveSeq<T>, Iterable<T>  {

    public abstract Stream<T> unwrapStream();
    @Override
    public final ReactiveSeq<T> parallel() {
        return this;
    }

    @Override
    public boolean endsWith(final Iterable<T> iterable) {
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
    public final Optional<T> min(final Comparator<? super T> comparator) {
        return Streams.min(this, comparator);
    }


    @Override
    public final Optional<T> max(final Comparator<? super T> comparator) {
        return Streams.max(this, comparator);
    }


    @Override
    public final Optional<T> findAny() {
        return findFirst();
    }

    @Override
    public final <R> R foldMap(final Reducer<R,T> reducer) {
        return reducer.foldMap(unwrapStream());
    }

    @Override
    public final <R> R foldMap(final Function<? super T, ? extends R> mapper, final Monoid<R> reducer) {
        return reducer.foldLeft(map(mapper));
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

        return reduce(reducer.zero(),reducer);
    }

    @Override
    public <U> U reduce(final U identity, final BiFunction<U, ? super T, U> accumulator, final BinaryOperator<U> combiner) {
        return unwrapStream().reduce(identity, accumulator, combiner);
    }



    @Override
    public final Seq<T> reduce(final Iterable<? extends Monoid<T>> reducers) {
        return Streams.reduce(this, reducers);
    }

    public final T foldLeft(final Monoid<T> reducer) {
        return reduce(reducer);
    }

    public final T foldLeft(final T identity, final BinaryOperator<T> accumulator) {
        return unwrapStream().reduce(identity, accumulator);
    }

    public final <R> R foldLeftMapToType(final Reducer<R,T> reducer) {
        return reducer.foldMap(unwrapStream());
    }

    @Override
    public final T foldRight(final Monoid<T> reducer) {
        return reducer.foldLeft(reverse());
    }

    @Override
    public final <U> U foldRight(final U seed, final BiFunction<? super T, ? super U, ? extends U> function) {
        return reverse().foldLeft(seed, (u,t)->function.apply(t, u));

    }

    @Override
    public final <R> R foldMapRight(final Reducer<R,T> reducer) {
        return reducer.foldMap(reverse());
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
    public final ReactiveSeq<T> stream() {
        return this;

    }

    @Override
    public final boolean startsWith(final Iterable<T> iterable) {
        return Streams.startsWith(this, iterable);

    }


    @Override
    public final <R> ReactiveSeq<R> flatMapStream(final Function<? super T, BaseStream<? extends R, ?>> fn) {
        return createSeq(Streams.flatMapStream(this, fn));

    }

    protected abstract <R> ReactiveSeq<R> createSeq(Stream<R> rStream);


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

            long[] last = {System.nanoTime()};
            int[] count = {0};
            return a-> {
                if (count[0] < x) {
                    last[0] = System.nanoTime();
                    count[0]++;
                    return a;
                }
                count[0] = 1;

                long since = System.nanoTime() - last[0];
                final long sleepFor = next - since;

                if(sleepFor>0)
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

            long[] last = {System.nanoTime()};
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

                if(last[0]==-1) {

                    last[0] = System.nanoTime();
                    return true;
                }
                long elapsedNanos  =  (System.nanoTime() - last[0]);
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

    }

    @Override
    public T foldRight(final T identity, final BinaryOperator<T> accumulator) {
        return reverse().foldLeft(identity, accumulator);
    }

    @Override
    public T firstValue(T alt) {
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

