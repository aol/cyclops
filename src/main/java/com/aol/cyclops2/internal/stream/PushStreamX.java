package com.aol.cyclops2.internal.stream;

import com.aol.cyclops2.internal.stream.spliterators.CopyableSpliterator;
import com.aol.cyclops2.internal.stream.spliterators.GroupingSpliterator;
import com.aol.cyclops2.internal.stream.spliterators.ReversableSpliterator;
import com.aol.cyclops2.internal.stream.spliterators.push.*;
import com.aol.cyclops2.types.stream.reactive.ReactiveSubscriber;
import cyclops.Streams;
import cyclops.collections.ListX;
import cyclops.function.Lambda;
import cyclops.stream.ReactiveSeq;
import lombok.AllArgsConstructor;
import org.agrona.concurrent.ManyToOneConcurrentArrayQueue;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.*;
import java.util.stream.Stream;


@AllArgsConstructor
public class PushStreamX<T> implements ReactiveSeq<T>  {

    final Operator<T> source;

    @Override
    public ReactiveSeq<T> reverse() {
        return coflatMap(s->ReactiveSeq.reversedListOf(s.toList()))
                .flatMap(i->i);
    }


    @Override
    <X> ReactiveSeq<X> createSeq(Stream<X> stream, Optional<ReversableSpliterator> reversible, Optional<PushingSpliterator<?>> split) {
        return new PushStreamX<X>(stream,reversible,split);
    }

    @Override
    <X> ReactiveSeq<X> createSeq(Spliterator<X> stream, Optional<ReversableSpliterator> reversible, Optional<PushingSpliterator<?>> split) {
        return new PushStreamX<X>(stream,reversible,split);
    }
    <R> ReactiveSeq<R> mapSubscribe(Supplier<Tuple2<Function<? super T, ? extends R>, Consumer<Consumer<? super R >>>> events){
        Operator<T> source;
        return new LazyMapOperator<>(source,events);
    }
    <R> ReactiveSeq<R> recover(Function<? super T, ? extends R> onNext, Function<? super Throwable,? extends R> onError,  Runnable onComplete){
        Operator<T> source;
        return new RecoverOperator<>(source,onNext,onError,onComplete);
    }
    @Override
    public ReactiveSeq<ListX<T>> grouped(final int groupSize) {
        //parallel grouping
        ListX<T> empty = ListX.empty();
        mapSubscribe(()->{
            final ArrayList[] next = {new ArrayList<T>(groupSize)};
            Tuple.<Function<? super T, ? extends ListX<T>>, Consumer<Consumer<? super ListX<T>>>>tuple( (T in)->{

                next[0].add(in);
                if(next[0].size()==groupSize){
                    List<T> toUse = next[0];
                    next[0] = new ArrayList<T>(groupSize);
                    return ListX.fromIterable(toUse);
                }
                return empty;
            },c-> c.accept(ListX.fromIterable(next[0])));
        }).filter(t->t!=empty);
    }
    @Override
    public ReactiveSeq<T> limit(long num){
        AtomicLong count = new AtomicLong();
        return filter(i->count.incrementAndGet()<num);
    }
    @Override
    public ReactiveSeq<T> skip(long num){
        AtomicLong count = new AtomicLong();
        return filter(i->count.incrementAndGet()>num);
    }
    @Override
    public ReactiveSeq<T> cycle() {
        return coflatMap(s->ReactiveSeq.reversedListOf(s.toList()).cycle())
                .flatMap(i->i);

    }

    @Override
    public Tuple2<ReactiveSeq<T>, ReactiveSeq<T>> duplicate() {
        ReactiveSubscriber<T> sub1 = ReactiveSeq.pushable();
        ReactiveSubscriber<T> sub2 = ReactiveSeq.pushable();
        this.peek(e->{sub1.onNext(e); sub2.onNext(e);})
                .spliterator()
                .tryAdvance(e->{}); //register spliterators

        return Tuple.tuple(sub1.stream(),sub2.stream());
    }

    @Override
    @SuppressWarnings("unchecked")
    public Tuple3<ReactiveSeq<T>, ReactiveSeq<T>, ReactiveSeq<T>> triplicate() {

        final Tuple3<Spliterator<T>, Spliterator<T>, Spliterator<T>> tuple = Tuple.tuple(copy(),copy(),copy());
        return tuple.map1(s -> createSeq(s, reversible.map(r -> r.copy()),split))
                .map2(s -> createSeq(s, reversible.map(r -> r.copy()),split))
                .map3(s -> createSeq(s, reversible.map(r -> r.copy()),split));

    }

    @Override
    @SuppressWarnings("unchecked")
    public Tuple4<ReactiveSeq<T>, ReactiveSeq<T>, ReactiveSeq<T>, ReactiveSeq<T>> quadruplicate() {
        final Tuple4<Spliterator<T>, Spliterator<T>, Spliterator<T>, Spliterator<T>> tuple = Tuple.tuple(copy(),copy(),copy(),copy());
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
                .flatMap(i -> createSeq(copy(), reversible, split));

    }

    @Override
    Spliterator<T> get() {
        return super.get();
    }

    @Override
    Spliterator<T> copy() {
        return super.get();
    }
}
