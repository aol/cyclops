package com.aol.cyclops2.internal.stream;

import com.aol.cyclops2.internal.stream.spliterators.ReversableSpliterator;
import com.aol.cyclops2.internal.stream.spliterators.push.CapturingOperator;
import cyclops.Streams;
import cyclops.stream.ReactiveSeq;
import cyclops.stream.Streamable;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;

import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.Spliterator;
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
    public final Tuple2<ReactiveSeq<T>, ReactiveSeq<T>> duplicate(Supplier<List<T>> bufferFactory) {
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
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public final Tuple2<Optional<T>, ReactiveSeq<T>> splitAtHead() {
        final Tuple2<ReactiveSeq<T>, ReactiveSeq<T>> Tuple2 = splitAt(1);
        return new Tuple2(
                Tuple2.v1.toOptional()
                        .flatMap(l -> l.size() > 0 ? Optional.of(l.get(0)) : Optional.empty()),
                Tuple2.v2);
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

    @Override @SafeVarargs
    public  final ReactiveSeq<T> insertAt(final int pos, final T... values) {
        return createSeq(Streams.insertAt(this, pos, values), Optional.empty());

    }

    @Override
    public ReactiveSeq<T> deleteBetween(final int start, final int end) {
        return createSeq(Streams.deleteBetween(this, start, end), Optional.empty());
    }

    @Override
    public ReactiveSeq<T> insertAtS(final int pos, final Stream<T> stream) {

        return createSeq(Streams.insertStreamAt(this, pos, stream), Optional.empty());

    }
    Spliterator<T> get() {
        return stream;
    }



}
