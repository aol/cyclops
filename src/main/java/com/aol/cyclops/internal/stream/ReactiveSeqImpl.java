package com.aol.cyclops.internal.stream;

import com.aol.cyclops.internal.stream.spliterators.ReversableSpliterator;
import com.aol.cyclops.internal.stream.spliterators.push.PushingSpliterator;
import cyclops.stream.ReactiveSeq;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;

import java.util.Optional;
import java.util.Spliterator;
import java.util.function.Predicate;
import java.util.stream.Stream;


public class ReactiveSeqImpl<T> extends BaseExtendedStream<T> {

    public ReactiveSeqImpl(Stream<T> stream) {
        super(stream);
    }

    public ReactiveSeqImpl(Spliterator<T> stream, Optional<ReversableSpliterator> rev, Optional<PushingSpliterator<?>> split) {
        super(stream, rev, split);
    }

    public ReactiveSeqImpl(Stream<T> stream, Optional<ReversableSpliterator> rev, Optional<PushingSpliterator<?>> split) {
        super(stream, rev, split);
    }

    @Override
    <X> ReactiveSeq<X> createSeq(Stream<X> stream, Optional<ReversableSpliterator> reversible, Optional<PushingSpliterator<?>> split) {
        return new ReactiveSeqImpl<X>(stream,reversible,split);
    }

    @Override
    <X> ReactiveSeq<X> createSeq(Spliterator<X> stream, Optional<ReversableSpliterator> reversible, Optional<PushingSpliterator<?>> split) {
        return new ReactiveSeqImpl<X>(stream,reversible,split);
    }
    @Override
    public ReactiveSeq<T> cycle() {
        return  ReactiveSeq.fill(1)
                .flatMap(i->createSeq(copyOrGet(),reversible,split));
    }

    @Override
    public Tuple2<ReactiveSeq<T>, ReactiveSeq<T>> duplicate() {
        final Tuple2<Spliterator<T>, Spliterator<T>> tuple = Tuple.tuple(copyOrGet(),copyOrGet());
        return tuple.map1(s -> createSeq(s, reversible.map(r -> r.copy()),split))
                .map2(s -> createSeq(s, reversible.map(r -> r.copy()),split));
    }

    @Override
    @SuppressWarnings("unchecked")
    public Tuple3<ReactiveSeq<T>, ReactiveSeq<T>, ReactiveSeq<T>> triplicate() {

        final Tuple3<Spliterator<T>, Spliterator<T>, Spliterator<T>> tuple = Tuple.tuple(copyOrGet(),copyOrGet(),copyOrGet());
        return tuple.map1(s -> createSeq(s, reversible.map(r -> r.copy()),split))
                .map2(s -> createSeq(s, reversible.map(r -> r.copy()),split))
                .map3(s -> createSeq(s, reversible.map(r -> r.copy()),split));

    }

    @Override
    @SuppressWarnings("unchecked")
    public Tuple4<ReactiveSeq<T>, ReactiveSeq<T>, ReactiveSeq<T>, ReactiveSeq<T>> quadruplicate() {
        final Tuple4<Spliterator<T>, Spliterator<T>, Spliterator<T>, Spliterator<T>> tuple = Tuple.tuple(copyOrGet(),copyOrGet(),copyOrGet(),copyOrGet());
        return tuple.map1(s -> createSeq(s, reversible.map(r -> r.copy()),split))
                .map2(s -> createSeq(s, reversible.map(r -> r.copy()),split))
                .map3(s -> createSeq(s, reversible.map(r -> r.copy()),split))
                .map4(s -> createSeq(s, reversible.map(r -> r.copy()),split));
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Tuple2<Optional<T>, ReactiveSeq<T>> splitAtHead() {
        final Tuple2<ReactiveSeq<T>, ReactiveSeq<T>> Tuple2 = splitAt(1);
        return new Tuple2(
                Tuple2.v1.toOptional()
                        .flatMap(l -> l.size() > 0 ? Optional.of(l.get(0)) : Optional.empty()),
                Tuple2.v2);
    }

    @Override
    public Tuple2<ReactiveSeq<T>, ReactiveSeq<T>> splitAt(final int where) {
        final Tuple2<ReactiveSeq<T>, ReactiveSeq<T>> Tuple2 = duplicate();
        return new Tuple2(
                Tuple2.v1.limit(where), Tuple2.v2.skip(where));


    }

    @Override
    public Tuple2<ReactiveSeq<T>, ReactiveSeq<T>> splitBy(final Predicate<T> splitter) {
        final Tuple2<ReactiveSeq<T>, ReactiveSeq<T>> Tuple2 = duplicate();
        return new Tuple2(
                Tuple2.v1.limitWhile(splitter), Tuple2.v2.skipWhile(splitter));
    }

    @Override
    public Tuple2<ReactiveSeq<T>, ReactiveSeq<T>> partition(final Predicate<? super T> splitter) {
        final Tuple2<ReactiveSeq<T>, ReactiveSeq<T>> Tuple2 = duplicate();
        return new Tuple2(
                Tuple2.v1.filter(splitter), Tuple2.v2.filter(splitter.negate()));

    }
    @Override
    public ReactiveSeq<T> cycle(long times) {
        return ReactiveSeq.fill(1)
                .limit(times)
                .flatMap(i -> createSeq(copyOrGet(), reversible, split));

    }

}
