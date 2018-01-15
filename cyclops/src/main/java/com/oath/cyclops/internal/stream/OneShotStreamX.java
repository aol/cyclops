package com.oath.cyclops.internal.stream;

import com.oath.cyclops.internal.stream.spliterators.LimitWhileClosedSpliterator;
import com.oath.cyclops.internal.stream.spliterators.ReversableSpliterator;
import cyclops.control.Option;
import cyclops.data.tuple.Tuple;
import cyclops.companion.Streams;
import cyclops.data.Seq;
import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.Streamable;
import cyclops.data.tuple.Tuple2;
import cyclops.data.tuple.Tuple3;
import cyclops.data.tuple.Tuple4;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;


public class OneShotStreamX<T> extends SpliteratorBasedStream<T> {

    public OneShotStreamX(Stream<T> stream) {
        super(stream);
    }

    public OneShotStreamX(Spliterator<T> stream, Optional<ReversableSpliterator> rev) {
        super(stream, rev);
    }

    public OneShotStreamX(Stream<T> stream, Optional<ReversableSpliterator> rev) {
        super(stream, rev);
    }
    @Override
    public ReactiveSeq<T> reverse() {
        if (reversible.isPresent()) {
            reversible.ifPresent(r -> r.invert());
            return this;
        }
        return createSeq(Streams.reverse(this), reversible);
    }

    @Override
    public <U> ReactiveSeq<Tuple2<T, U>> crossJoin(ReactiveSeq<? extends U> other) {
        Streamable<? extends U> s = Streamable.fromStream(other);
        return forEach2(a->ReactiveSeq.fromIterable(s), Tuple::tuple);
    }

    @Override
    public final ReactiveSeq<T> cycle() {
        return createSeq(Streams.cycle(unwrapStream()), reversible);
    }
    @Override
    public final Tuple2<ReactiveSeq<T>, ReactiveSeq<T>> duplicate() {
        final Tuple2<Stream<T>, Stream<T>> tuple = Streams.duplicate(unwrapStream());
        return tuple.map1(s -> createSeq(s, reversible.map(r -> r.copy())))
                .map2(s -> createSeq(s, reversible.map(r -> r.copy())));
    }
    @Override
    public final Tuple2<ReactiveSeq<T>, ReactiveSeq<T>> duplicate(Supplier<Deque<T>> bufferFactory) {
        final Tuple2<Stream<T>, Stream<T>> tuple = Streams.duplicate(unwrapStream(),bufferFactory);
        return tuple.map1(s -> createSeq(s, reversible.map(r -> r.copy())))
                .map2(s -> createSeq(s, reversible.map(r -> r.copy())));
    }

    @Override
    @SuppressWarnings("unchecked")
    public final Tuple3<ReactiveSeq<T>, ReactiveSeq<T>, ReactiveSeq<T>> triplicate() {

        final Tuple3<Stream<T>, Stream<T>, Stream<T>> tuple = Streams.triplicate(unwrapStream());
        return tuple.map1(s -> createSeq(s, reversible.map(r -> r.copy())))
                .map2(s -> createSeq(s, reversible.map(r -> r.copy())))
                .map3(s -> createSeq(s, reversible.map(r -> r.copy())));

    }
    @Override
    @SuppressWarnings("unchecked")
    public final Tuple3<ReactiveSeq<T>, ReactiveSeq<T>, ReactiveSeq<T>> triplicate(Supplier<Deque<T>> bufferFactory) {

        final Tuple3<Stream<T>, Stream<T>, Stream<T>> tuple = Streams.triplicate(unwrapStream(),bufferFactory);
        return tuple.map1(s -> createSeq(s, reversible.map(r -> r.copy())))
                .map2(s -> createSeq(s, reversible.map(r -> r.copy())))
                .map3(s -> createSeq(s, reversible.map(r -> r.copy())));

    }

    @Override
    @SuppressWarnings("unchecked")
    public final Tuple4<ReactiveSeq<T>, ReactiveSeq<T>, ReactiveSeq<T>, ReactiveSeq<T>> quadruplicate() {
        final Tuple4<Stream<T>, Stream<T>, Stream<T>, Stream<T>> tuple = Streams.quadruplicate(unwrapStream());
        return tuple.map1(s -> createSeq(s, reversible.map(r -> r.copy())))
                .map2(s -> createSeq(s, reversible.map(r -> r.copy())))
                .map3(s -> createSeq(s, reversible.map(r -> r.copy())))
                .map4(s -> createSeq(s, reversible.map(r -> r.copy())));
    }
    @Override
    @SuppressWarnings("unchecked")
    public final Tuple4<ReactiveSeq<T>, ReactiveSeq<T>, ReactiveSeq<T>, ReactiveSeq<T>> quadruplicate(Supplier<Deque<T>> bufferFactory) {
        final Tuple4<Stream<T>, Stream<T>, Stream<T>, Stream<T>> tuple = Streams.quadruplicate(unwrapStream(),bufferFactory);
        return tuple.map1(s -> createSeq(s, reversible.map(r -> r.copy())))
                .map2(s -> createSeq(s, reversible.map(r -> r.copy())))
                .map3(s -> createSeq(s, reversible.map(r -> r.copy())))
                .map4(s -> createSeq(s, reversible.map(r -> r.copy())));
    }
    @Override
    public Seq<ReactiveSeq<T>> multicast(int num){
        return Streams.toBufferingCopier(iterator(),num,()->new ArrayDeque<T>(100))
                .map(ReactiveSeq::fromIterator);
    }
    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public final Tuple2<Option<T>, ReactiveSeq<T>> splitAtHead() {
        final Tuple2<ReactiveSeq<T>, ReactiveSeq<T>> Tuple2 = splitAt(1);
        return Tuple2.of(
                Tuple2._1().to().option()
                        .flatMap(l -> l.size() > 0 ? Option.some(l.get(0)) : Option.none()),
                Tuple2._2());
    }

    @Override
    public final Tuple2<ReactiveSeq<T>, ReactiveSeq<T>> splitAt(final int where) {
        return Streams.splitAt(this, where)
                .map1(s -> createSeq(s, reversible.map(r -> r.copy())))
                .map2(s -> createSeq(s, reversible.map(r -> r.copy())));

    }

    @Override
    public final Tuple2<ReactiveSeq<T>, ReactiveSeq<T>> splitBy(final Predicate<T> splitter) {
        return Streams.splitBy(this, splitter)
                .map1(s -> createSeq(s, reversible.map(r -> r.copy())))
                .map2(s -> createSeq(s, reversible.map(r -> r.copy())));
    }

    @Override
    public final Tuple2<ReactiveSeq<T>, ReactiveSeq<T>> partition(final Predicate<? super T> splitter) {
        return Streams.partition(this, splitter)
                .map1(s -> createSeq(s, reversible.map(r -> r.copy())))
                .map2(s -> createSeq(s, reversible.map(r -> r.copy())));
    }


    @Override
    public final ReactiveSeq<T> cycleWhile(final Predicate<? super T> predicate) {

        return createSeq(Streams.cycle(unwrapStream()), reversible)
                .limitWhile(predicate);
    }

    @Override
    public final ReactiveSeq<T> cycleUntil(final Predicate<? super T> predicate) {
        return createSeq(Streams.cycle(unwrapStream()), reversible)
                .limitWhile(predicate.negate());
    }

    @Override
    public ReactiveSeq<T> limitWhileClosed(Predicate<? super T> predicate) {
        return createSeq(new LimitWhileClosedSpliterator<T>(get(),predicate),reversible);
    }
    @Override
    public ReactiveSeq<T> limitUntilClosed(Predicate<? super T> predicate) {
        return createSeq(new LimitWhileClosedSpliterator<T>(get(),predicate.negate()),reversible);
    }

    @Override
    public ReactiveSeq<T> cycle(long times) {
        return createSeq(Streams.cycle(times, Streamable.fromStream(unwrapStream())), reversible);
    }

    @Override
    <X> ReactiveSeq<X> createSeq(Stream<X> stream, Optional<ReversableSpliterator> reversible) {
        return new OneShotStreamX<X>(stream,reversible);
    }

    @Override
    <X> ReactiveSeq<X> createSeq(Spliterator<X> stream, Optional<ReversableSpliterator> reversible) {
        return new OneShotStreamX<X>(stream,reversible);
    }

    Spliterator<T> get() {
        return stream;
    }

    public <R> R visit(Function<? super ReactiveSeq<T>,? extends R> sync, Function<? super ReactiveSeq<T>,? extends R> reactiveStreams,
                       Function<? super ReactiveSeq<T>,? extends R> asyncNoBackPressure){
        return sync.apply(this);
    }


}
