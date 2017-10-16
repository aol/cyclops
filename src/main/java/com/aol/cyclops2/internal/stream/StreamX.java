package com.aol.cyclops2.internal.stream;

import com.aol.cyclops2.internal.stream.spliterators.CopyableSpliterator;
import com.aol.cyclops2.internal.stream.spliterators.IteratableSpliterator;
import com.aol.cyclops2.internal.stream.spliterators.ReversableSpliterator;
import cyclops.companion.Streams;
import cyclops.collectionx.mutable.ListX;
import cyclops.control.Option;
import cyclops.reactive.ReactiveSeq;
import cyclops.data.tuple.Tuple;
import cyclops.data.tuple.Tuple2;
import cyclops.data.tuple.Tuple3;
import cyclops.data.tuple.Tuple4;

import java.util.*;
import java.util.function.*;
import java.util.stream.Stream;


public class StreamX<T> extends SpliteratorBasedStream<T> {

    public StreamX(Stream<T> stream) {
        super(stream);
    }

    public StreamX(Spliterator<T> stream, Optional<ReversableSpliterator> rev) {
        super(stream, rev);
    }

    public StreamX(Stream<T> stream, Optional<ReversableSpliterator> rev) {
        super(stream, rev);
    }
    @Override
    public ReactiveSeq<T> reverse() {
        if(this.stream instanceof ReversableSpliterator){
            ReversableSpliterator rev = (ReversableSpliterator)stream;
            return createSeq(rev.invert(),reversible);
        }
        return createSeq(Streams.reverse(this), reversible);
    }

    @Override
    public ReactiveSeq<T> combine(BiPredicate<? super T, ? super T> predicate, BinaryOperator<T> op) {
        return createSeq(new IteratableSpliterator<>(Streams.combineI(this,predicate,op)))
                .flatMapI(i->i);
    }

    @Override
    <X> ReactiveSeq<X> createSeq(Stream<X> stream, Optional<ReversableSpliterator> reversible) {
        return new StreamX<X>(stream,reversible);
    }

    @Override
    <X> ReactiveSeq<X> createSeq(Spliterator<X> stream, Optional<ReversableSpliterator> reversible) {
        return new StreamX<X>(stream,reversible);
    }



    @Override
    public ReactiveSeq<T> cycle() {

        Spliterator<T> t = copy();
        return  ReactiveSeq.fill(1)
                .flatMap(i->createSeq(CopyableSpliterator.copy(t),reversible));
    }

    @Override
    public Tuple2<ReactiveSeq<T>, ReactiveSeq<T>> duplicate() {
        ListX<Iterable<T>> copy = Streams.toBufferingCopier(() -> Spliterators.iterator(copy()), 2);

        return Tuple.tuple(createSeq(new IteratableSpliterator<>(copy.get(0))),
                createSeq(new IteratableSpliterator<>(copy.get(1))));


    }
    @Override
    public Tuple2<ReactiveSeq<T>, ReactiveSeq<T>> duplicate(Supplier<Deque<T>> bufferFactory) {

        ListX<Iterable<T>> copy = Streams.toBufferingCopier(() -> Spliterators.iterator(copy()), 2,bufferFactory);

        return Tuple.tuple(createSeq(new IteratableSpliterator<>(copy.get(0))),
                createSeq(new IteratableSpliterator<>(copy.get(1))));

    }

    @Override
    @SuppressWarnings("unchecked")
    public Tuple3<ReactiveSeq<T>, ReactiveSeq<T>, ReactiveSeq<T>> triplicate() {
        ListX<Iterable<T>> copy = Streams.toBufferingCopier(() -> Spliterators.iterator(copy()), 3);

        return Tuple.tuple(createSeq(new IteratableSpliterator<>(copy.get(0))),
                createSeq(new IteratableSpliterator<>(copy.get(1))),
                createSeq(new IteratableSpliterator<>(copy.get(2))));


    }

    @Override
    @SuppressWarnings("unchecked")
    public Tuple4<ReactiveSeq<T>, ReactiveSeq<T>, ReactiveSeq<T>, ReactiveSeq<T>> quadruplicate() {
        ListX<Iterable<T>> copy = Streams.toBufferingCopier(() -> Spliterators.iterator(copy()), 4);

        return Tuple.tuple(createSeq(new IteratableSpliterator<>(copy.get(0))),
                createSeq(new IteratableSpliterator<>(copy.get(1))),
                createSeq(new IteratableSpliterator<>(copy.get(2))),
                createSeq(new IteratableSpliterator<>(copy.get(3))));
    }
    @Override
    @SuppressWarnings("unchecked")
    public Tuple3<ReactiveSeq<T>, ReactiveSeq<T>, ReactiveSeq<T>> triplicate(Supplier<Deque<T>> bufferFactory) {
        ListX<Iterable<T>> copy = Streams.toBufferingCopier(() -> Spliterators.iterator(copy()), 3,bufferFactory);

        return Tuple.tuple(createSeq(new IteratableSpliterator<>(copy.get(0))),
                createSeq(new IteratableSpliterator<>(copy.get(1))),
                createSeq(new IteratableSpliterator<>(copy.get(2))));


    }

    @Override
    @SuppressWarnings("unchecked")
    public Tuple4<ReactiveSeq<T>, ReactiveSeq<T>, ReactiveSeq<T>, ReactiveSeq<T>> quadruplicate(Supplier<Deque<T>> bufferFactory) {
        ListX<Iterable<T>> copy = Streams.toBufferingCopier(() -> Spliterators.iterator(copy()), 4,bufferFactory);

        return Tuple.tuple(createSeq(new IteratableSpliterator<>(copy.get(0))),
                createSeq(new IteratableSpliterator<>(copy.get(1))),
                createSeq(new IteratableSpliterator<>(copy.get(2))),
                createSeq(new IteratableSpliterator<>(copy.get(3))));
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Tuple2<Option<T>, ReactiveSeq<T>> splitAtHead() {
        final Tuple2<ReactiveSeq<T>, ReactiveSeq<T>> Tuple2 = splitAt(1);
        return new Tuple2(
                Tuple2._1().to().option()
                        .flatMap(l -> l.size() > 0 ? Option.of(l.get(0)) : Option.none()),
                Tuple2._2());
    }

    @Override
    public Tuple2<ReactiveSeq<T>, ReactiveSeq<T>> splitAt(final int where) {
        final Tuple2<ReactiveSeq<T>, ReactiveSeq<T>> Tuple2 = duplicate();
        return new Tuple2(
                Tuple2._1().limit(where), Tuple2._2().skip(where));


    }

    @Override
    public Tuple2<ReactiveSeq<T>, ReactiveSeq<T>> splitBy(final Predicate<T> splitter) {
        final Tuple2<ReactiveSeq<T>, ReactiveSeq<T>> Tuple2 = duplicate();
        return new Tuple2(
                Tuple2._1().limitWhile(splitter), Tuple2._2().skipWhile(splitter));
    }

    @Override
    public Tuple2<ReactiveSeq<T>, ReactiveSeq<T>> partition(final Predicate<? super T> splitter) {
        final Tuple2<ReactiveSeq<T>, ReactiveSeq<T>> Tuple2 = duplicate();
        return new Tuple2(
                Tuple2._1().filter(splitter), Tuple2._2().filter(splitter.negate()));

    }
    @Override
    public ReactiveSeq<T> cycle(long times) {
        return ReactiveSeq.fill(1)
                .limit(times)
                .flatMap(i -> createSeq(copy(), reversible));

    }
    public <R> R visit(Function<? super ReactiveSeq<T>,? extends R> sync, Function<? super ReactiveSeq<T>,? extends R> reactiveStreams,
                       Function<? super ReactiveSeq<T>,? extends R> asyncNoBackPressure){
        return sync.apply(this);
    }

}
