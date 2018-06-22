package com.oath.cyclops.rx2.adapter;

import com.oath.cyclops.types.persistent.PersistentCollection;
import cyclops.companion.rx2.Observables;
import cyclops.control.LazyEither;
import cyclops.control.Maybe;
import cyclops.control.Option;
import cyclops.data.Seq;
import cyclops.data.Vector;
import cyclops.data.tuple.Tuple;
import cyclops.data.tuple.Tuple2;
import cyclops.data.tuple.Tuple3;
import cyclops.data.tuple.Tuple4;
import cyclops.function.Monoid;
import cyclops.function.Reducer;
import cyclops.reactive.ReactiveSeq;
import io.reactivex.Observable;
import io.reactivex.Single;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Wither;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.Deque;
import java.util.Iterator;
import java.util.Optional;
import java.util.Spliterator;
import java.util.concurrent.TimeUnit;
import java.util.function.*;
import java.util.stream.*;


@AllArgsConstructor
public class ObservableReactiveSeqImpl<T> implements ReactiveSeq<T> {
    @Wither
    @Getter
    Observable<T> observable;

    public <R> ObservableReactiveSeqImpl<R> observable(Observable<R> observable){
        return new ObservableReactiveSeqImpl<>(observable);
    }
    public <R> ObservableReactiveSeqImpl<R> observable(ReactiveSeq<R> observable){
        if(observable instanceof ObservableReactiveSeqImpl){
            return  (ObservableReactiveSeqImpl)observable;
        }
        return new ObservableReactiveSeqImpl<>(Observables.observableFrom(observable));
    }

    @Override
    public <R> ReactiveSeq<R> coflatMap(Function<? super ReactiveSeq<T>, ? extends R> fn) {
        return observable(Observable.just(fn.apply(this)));
    }

    @Override
    public <T1> ReactiveSeq<T1> unit(T1 unit) {
        return observable(Observable.just(unit));
    }

    @Override
    public <U> U foldRight(U identity, BiFunction<? super T, ? super U, ? extends U> accumulator) {
        return observable.reduce(identity,(a,b)->accumulator.apply(b,a))
                         .blockingGet();
    }

    @Override
    public <U, R> ReactiveSeq<R> zipWithStream(Stream<? extends U> other, BiFunction<? super T, ? super U, ? extends R> zipper) {
         if(other instanceof ReactiveSeq){
            ReactiveSeq<U> o = (ReactiveSeq<U>)other;
            return o.visit(sync->observable(observable.zipWith(ReactiveSeq.fromStream((Stream<U>)other),(a,b)->zipper.apply(a,b))),
                    rs->observable(observable.zipWith(ReactiveSeq.fromStream((Stream<U>)other),(a,b)->zipper.apply(a,b))),
                    async->observable(observable.zipWith(ReactiveSeq.fromStream((Stream<U>)other),(a,b)->zipper.apply(a,b))));

        }
        if(other instanceof Publisher){
            return zip(zipper,(Publisher<U>)other);
        }

        return observable(observable.zipWith(ReactiveSeq.fromStream((Stream<U>)other),(a,b)->zipper.apply(a,b)));
    }

    @Override
    public <U, R> ReactiveSeq<R> zipLatest(Publisher<? extends U> other, BiFunction<? super T, ? super U, ? extends R> zipper) {
        Observable<R> obs = Observable.combineLatest(observable, Observables.observable(other), (a, b) -> zipper.apply((T)a, (U)b));
        return observable(obs);
    }

    @Override
    public <U, R> ReactiveSeq<R> zip(BiFunction<? super T, ? super U, ? extends R> zipper,Publisher<? extends U> other) {
        return observable(observable.zipWith(Observables.observable(other),(a,b)->zipper.apply(a,b)));
    }

    @Override
    public <U> ReactiveSeq<Tuple2<T, U>> zipWithPublisher(Publisher<? extends U> other) {
        return observable(observable.zipWith(Observables.observable(other),Tuple::tuple));
    }

    @Override
    public ReactiveSeq<T> cycle() {
        return observable(observable.repeat());
    }

    @Override
    public Tuple2<ReactiveSeq<T>, ReactiveSeq<T>> duplicate() {
        return Observables.connectToReactiveSeq(observable).duplicate().transform((s1, s2)->Tuple.tuple(observable(s1),observable(s2)));
    }

    @Override
    public Tuple2<ReactiveSeq<T>, ReactiveSeq<T>> duplicate(Supplier<Deque<T>> bufferFactory) {
        return Observables.connectToReactiveSeq(observable).duplicate(bufferFactory).transform((s1, s2)->Tuple.tuple(observable(s1),observable(s2)));
    }

    @Override
    public Tuple3<ReactiveSeq<T>, ReactiveSeq<T>, ReactiveSeq<T>> triplicate() {
        return Observables.connectToReactiveSeq(observable).triplicate().transform((s1, s2, s3)->Tuple.tuple(observable(s1),observable(s2),observable(s3)));
    }

    @Override
    public Tuple3<ReactiveSeq<T>, ReactiveSeq<T>, ReactiveSeq<T>> triplicate(Supplier<Deque<T>> bufferFactory) {
        return Observables.connectToReactiveSeq(observable).triplicate(bufferFactory).transform((s1, s2, s3)->Tuple.tuple(observable(s1),observable(s2),observable(s3)));
    }

    @Override
    public Tuple4<ReactiveSeq<T>, ReactiveSeq<T>, ReactiveSeq<T>, ReactiveSeq<T>> quadruplicate() {
        return Observables.connectToReactiveSeq(observable).quadruplicate().to(t4->Tuple.tuple(observable(t4._1()),observable(t4._2()),observable(t4._3()),observable(t4._4())));
    }

    @Override
    public Tuple4<ReactiveSeq<T>, ReactiveSeq<T>, ReactiveSeq<T>, ReactiveSeq<T>> quadruplicate(Supplier<Deque<T>> bufferFactory) {
        return Observables.connectToReactiveSeq(observable).quadruplicate(bufferFactory).to(t4->Tuple.tuple(observable(t4._1()),observable(t4._2()),observable(t4._3()),observable(t4._4())));
    }

    @Override
    public Tuple2<Option<T>, ReactiveSeq<T>> splitAtHead() {
        return Observables.connectToReactiveSeq(observable).splitAtHead().transform((s1, s2)->Tuple.tuple(s1,observable(s2)));
    }

    @Override
    public Tuple2<ReactiveSeq<T>, ReactiveSeq<T>> splitAt(int where) {
        return Observables.connectToReactiveSeq(observable).splitAt(where).transform((s1, s2)->Tuple.tuple(observable(s1),observable(s2)));
    }

    @Override
    public Tuple2<ReactiveSeq<T>, ReactiveSeq<T>> splitBy(Predicate<T> splitter) {
        return Observables.connectToReactiveSeq(observable).splitBy(splitter).transform((s1, s2)->Tuple.tuple(observable(s1),observable(s2)));
    }

    @Override
    public Tuple2<ReactiveSeq<T>, ReactiveSeq<T>> partition(Predicate<? super T> splitter) {
        return Observables.connectToReactiveSeq(observable).partition(splitter).transform((s1, s2)->Tuple.tuple(observable(s1),observable(s2)));
    }

    @Override
    public <U> ReactiveSeq<Tuple2<T, U>> zipWithStream(Stream<? extends U> other) {

        return zipWithStream(other,Tuple::tuple);
    }

    @Override
    public <S, U> ReactiveSeq<Tuple3<T, S, U>> zip3(Iterable<? extends S> second, Iterable<? extends U> third) {
        return zip(second,Tuple::tuple).zip(third,(a,b)->Tuple.tuple(a._1(),a._2(),b));
    }

    @Override
    public <T2, T3, T4> ReactiveSeq<Tuple4<T, T2, T3, T4>> zip4(Iterable<? extends T2> second, Iterable<? extends T3> third, Iterable<? extends T4> fourth) {
        return zip(second,Tuple::tuple).zip(third,(a,b)->Tuple.tuple(a._1(),a._2(),b))
                .zip(fourth,(a,b)->(Tuple4<T,T2,T3,T4>)Tuple.tuple(a._1(),a._2(),a._3(),b));
    }

    @Override
    public ReactiveSeq<Seq<T>> sliding(int windowSize, int increment) {
        return observable(Observables.connectToReactiveSeq(observable).sliding(windowSize,increment));
    }

    @Override
    public ReactiveSeq<Vector<T>> grouped(int groupSize) {

        return observable(Observables.connectToReactiveSeq(observable).grouped(groupSize));
    }

    @Override
    public ReactiveSeq<Vector<T>> groupedUntil(BiPredicate<Vector<? super T>, ? super T> predicate) {
        return observable(Observables.connectToReactiveSeq(observable).groupedUntil(predicate));
    }

    @Override
    public <C extends PersistentCollection<T>, R> ReactiveSeq<R> groupedUntil(BiPredicate<C, ? super T> predicate, Supplier<C> factory, Function<? super C, ? extends R> finalizer) {
        return observable(Observables.connectToReactiveSeq(observable).groupedUntil(predicate,factory,finalizer));
    }

    @Override
    public ReactiveSeq<Vector<T>> groupedWhile(BiPredicate<Vector<? super T>, ? super T> predicate) {
        return observable(Observables.connectToReactiveSeq(observable).groupedWhile(predicate));
    }

    @Override
    public <C extends PersistentCollection<T>, R> ReactiveSeq<R> groupedWhile(BiPredicate<C, ? super T> predicate, Supplier<C> factory, Function<? super C, ? extends R> finalizer) {
        return observable(Observables.connectToReactiveSeq(observable).groupedWhile(predicate,factory,finalizer));
    }

    @Override
    public ReactiveSeq<Vector<T>> groupedBySizeAndTime(int size, long time, TimeUnit t) {
        return observable(Observables.connectToReactiveSeq(observable).groupedBySizeAndTime(size, time, t));
    }

    @Override
    public <C extends PersistentCollection<? super T>> ReactiveSeq<C> groupedBySizeAndTime(int size, long time, TimeUnit unit, Supplier<C> factory) {
        return observable(Observables.connectToReactiveSeq(observable).groupedBySizeAndTime(size,time,unit,factory));
    }

    @Override
    public <C extends PersistentCollection<? super T>, R> ReactiveSeq<R> groupedBySizeAndTime(int size, long time, TimeUnit unit, Supplier<C> factory, Function<? super C, ? extends R> finalizer) {
        return observable(Observables.connectToReactiveSeq(observable).groupedBySizeAndTime(size,time,unit,factory,finalizer));
    }

    @Override
    public <C extends PersistentCollection<? super T>, R> ReactiveSeq<R> groupedByTime(long time, TimeUnit unit, Supplier<C> factory, Function<? super C, ? extends R> finalizer) {
        return groupedBySizeAndTime(Integer.MAX_VALUE,time,unit,factory,finalizer);
    }

    @Override
    public ReactiveSeq<Vector<T>> groupedByTime(long time, TimeUnit t) {
        return observable(Observables.connectToReactiveSeq(observable).groupedByTime(time, t));
    }

    @Override
    public <C extends PersistentCollection<? super T>> ReactiveSeq<C> groupedByTime(long time, TimeUnit unit, Supplier<C> factory) {
        return observable(Observables.connectToReactiveSeq(observable).groupedByTime(time, unit, factory));
    }

    @Override
    public <C extends PersistentCollection<? super T>> ReactiveSeq<C> grouped(int size, Supplier<C> supplier) {
        return observable(Observables.connectToReactiveSeq(observable).grouped(size,supplier));
    }

    @Override
    public ReactiveSeq<Vector<T>> groupedWhile(Predicate<? super T> predicate) {
        return observable(Observables.connectToReactiveSeq(observable).groupedWhile(predicate));
    }

    @Override
    public <C extends PersistentCollection<? super T>> ReactiveSeq<C> groupedWhile(Predicate<? super T> predicate, Supplier<C> factory) {
        return observable(Observables.connectToReactiveSeq(observable).groupedWhile(predicate,factory));
    }

    @Override
    public ReactiveSeq<T> distinct() {
        return observable(observable.distinct());
    }

    @Override
    public <U> ReactiveSeq<U> scanLeft(U seed, BiFunction<? super U, ? super T, ? extends U> function) {
        return observable(observable.scan(seed,(a,b)->function.apply(a,b)));
    }

    @Override
    public ReactiveSeq<T> sorted() {
        return observable(Observables.connectToReactiveSeq(observable).sorted());
    }

    @Override
    public ReactiveSeq<T> skip(long num) {
        return observable(observable.skip((int)num));
    }


    @Override
    public void forEach(Consumer<? super T> action) {
        Observables.connectToReactiveSeq(observable).forEach(action);
    }

    @Override
    public void forEachOrdered(Consumer<? super T> action) {
        Observables.connectToReactiveSeq(observable).forEachOrdered(action);
    }

    @Override
    public Object[] toArray() {
        return Observables.connectToReactiveSeq(observable).toArray();
    }

    @Override
    public <A> A[] toArray(IntFunction<A[]> generator) {
        return Observables.connectToReactiveSeq(observable).toArray(generator);
    }

    @Override
    public ReactiveSeq<T> skipWhile(Predicate<? super T> p) {
        return observable(observable.skipWhile(t->p.test(t)));
    }

    @Override
    public ReactiveSeq<T> limit(long num) {
        return observable(observable.take((int)num));
    }

    @Override
    public ReactiveSeq<T> limitWhile(Predicate<? super T> p) {
        return observable(Observables.connectToReactiveSeq(observable).takeWhile(p));
    }
    @Override
    public ReactiveSeq<T> limitWhileClosed(Predicate<? super T> p) {
        return observable(observable.takeWhile(t->p.test(t)));
    }

    @Override
    public ReactiveSeq<T> limitUntil(Predicate<? super T> p) {
       return observable(Observables.connectToReactiveSeq(observable).limitUntil(p));
    }

    @Override
    public ReactiveSeq<T> limitUntilClosed(Predicate<? super T> p) {
        return observable(observable.takeUntil((T t)->p.test(t)));
    }

    @Override
    public ReactiveSeq<T> parallel() {
        return this;
    }

    @Override
    public boolean allMatch(Predicate<? super T> c) {
        return Observables.connectToReactiveSeq(observable).allMatch(c);
    }

    @Override
    public boolean anyMatch(Predicate<? super T> c) {
        return Observables.connectToReactiveSeq(observable).anyMatch(c);
    }

    @Override
    public boolean xMatch(int num, Predicate<? super T> c) {
        return Observables.connectToReactiveSeq(observable).xMatch(num,c);
    }

    @Override
    public boolean noneMatch(Predicate<? super T> c) {
        return Observables.connectToReactiveSeq(observable).noneMatch(c);
    }

    @Override
    public String join() {
        return Observables.connectToReactiveSeq(observable).join();
    }

    @Override
    public String join(String sep) {
        return Observables.connectToReactiveSeq(observable).join(sep);
    }

    @Override
    public String join(String sep, String start, String end) {
        return Observables.connectToReactiveSeq(observable).join(sep,start,end);
    }


    @Override
    public Optional<T> findFirst() {
        return Observables.connectToReactiveSeq(observable).findFirst();
    }

    @Override
    public Maybe<T> takeOne() {
        return Observables.connectToReactiveSeq(observable).takeOne();
    }

    @Override
    public LazyEither<Throwable, T> findFirstOrError() {
        return Observables.connectToReactiveSeq(observable).findFirstOrError();
    }

    @Override
    public Optional<T> findAny() {
        return Observables.connectToReactiveSeq(observable).findAny();
    }

    @Override
    public <R> R foldMap(Reducer<R,T> reducer) {
        return Observables.connectToReactiveSeq(observable).foldMap(reducer);
    }

    @Override
    public <R> R foldMap(Function<? super T, ? extends R> mapper, Monoid<R> reducer) {
        return Observables.connectToReactiveSeq(observable).foldMap(mapper,reducer);
    }

    @Override
    public T reduce(Monoid<T> reducer) {
        return Observables.connectToReactiveSeq(observable).reduce(reducer);
    }

    @Override
    public Optional<T> reduce(BinaryOperator<T> accumulator) {
        return Observables.connectToReactiveSeq(observable).reduce(accumulator);
    }

    @Override
    public T reduce(T identity, BinaryOperator<T> accumulator) {
        return Observables.connectToReactiveSeq(observable).reduce(identity,accumulator);
    }

    @Override
    public <U> U reduce(U identity, BiFunction<U, ? super T, U> accumulator, BinaryOperator<U> combiner) {
        return Observables.connectToReactiveSeq(observable).reduce(identity, accumulator, combiner);
    }



    @Override
    public Seq<T> reduce(Iterable<? extends Monoid<T>> reducers) {
        return Observables.connectToReactiveSeq(observable).reduce(reducers);
    }

    @Override
    public T foldRight(Monoid<T> reducer) {
        return Observables.connectToReactiveSeq(observable).foldRight(reducer);
    }

    @Override
    public T foldRight(T identity, BinaryOperator<T> accumulator) {
        return Observables.connectToReactiveSeq(observable).foldRight(identity,accumulator);
    }

    @Override
    public <T1> T1 foldMapRight(Reducer<T1,T> reducer) {
        return Observables.connectToReactiveSeq(observable).foldMapRight(reducer);
    }

    @Override
    public ReactiveSeq<T> stream() {
        return Observables.connectToReactiveSeq(observable);
    }

    @Override
    public <U> ObservableReactiveSeqImpl<U> unitIterable(Iterable<U> U) {
        return new ObservableReactiveSeqImpl<>(Observable.fromIterable(U));
    }

    @Override
    public boolean startsWith(Iterable<T> iterable) {
        return Observables.connectToReactiveSeq(observable).startsWith(iterable);
    }

    @Override
    public <R> ReactiveSeq<R> map(Function<? super T, ? extends R> fn) {
        return observable(observable.map(e->fn.apply(e)));
    }

    @Override
    public <R> ReactiveSeq<R> flatMap(Function<? super T, ? extends Stream<? extends R>> fn) {
        return observable(observable.flatMap(s->Observables.fromStream(fn.apply(s))));
    }

    @Override
    public IntStream flatMapToInt(Function<? super T, ? extends IntStream> mapper) {
        return Observables.connectToReactiveSeq(observable).flatMapToInt(mapper);
    }

    @Override
    public LongStream flatMapToLong(Function<? super T, ? extends LongStream> mapper) {
        return Observables.connectToReactiveSeq(observable).flatMapToLong(mapper);
    }

    @Override
    public DoubleStream flatMapToDouble(Function<? super T, ? extends DoubleStream> mapper) {
        return Observables.connectToReactiveSeq(observable).flatMapToDouble(mapper);
    }



    @Override
    public <R> ReactiveSeq<R> concatMap(Function<? super T, ? extends Iterable<? extends R>> fn) {
        return observable(observable.flatMapIterable(a->fn.apply(a)));
    }

    @Override
    public <R> ReactiveSeq<R> mergeMap(Function<? super T, ? extends Publisher<? extends R>> fn) {
        return observable(Observable.merge(observable.map(a->Observables.observable(fn.apply(a)))));
    }

    @Override
    public <R> ReactiveSeq<R> mergeMap(int maxConcurrency, Function<? super T, ? extends Publisher<? extends R>> fn) {
        return observable(Observable.merge(observable.map(a->Observables.observable(fn.apply(a))),maxConcurrency));
    }

    @Override
    public <R> ReactiveSeq<R> flatMapStream(Function<? super T, BaseStream<? extends R, ?>> fn) {

        return this.<R>observable((Observable)observable.flatMap(a->fn.andThen(s->{
            ReactiveSeq<R> res = s instanceof ReactiveSeq ? (ReactiveSeq) s : (ReactiveSeq) ReactiveSeq.fromSpliterator(s.spliterator());
           return Observables.fromStream(res);
                }

        ).apply(a)));
    }

    @Override
    public ReactiveSeq<T> filter(Predicate<? super T> fn) {
        return observable(observable.filter(t->fn.test(t)));
    }

    @Override
    public Iterator<T> iterator() {
        return Observables.connectToReactiveSeq(observable).iterator();
    }

    @Override
    public Spliterator<T> spliterator() {
        return Observables.connectToReactiveSeq(observable).spliterator();
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
    public ReactiveSeq<T> reverse() {
        return observable(Observables.connectToReactiveSeq(observable).reverse());
    }

    @Override
    public ReactiveSeq<T> onClose(Runnable closeHandler) {
        return observable(observable.doOnComplete(()->closeHandler.run()));
    }

    @Override
    public void close() {

    }

    @Override
    public ReactiveSeq<T> prependStream(Stream<? extends T> stream) {
        return observable(Observables.connectToReactiveSeq(observable).prependStream(stream));
    }

    @Override
    public ReactiveSeq<T> appendAll(T... values) {
        return observable(Observables.connectToReactiveSeq(observable).appendAll(values));
    }

    @Override
    public ReactiveSeq<T> append(T value) {
        return observable(Observables.connectToReactiveSeq(observable).append(value));
    }

    @Override
    public ReactiveSeq<T> prepend(T value) {
        return observable(Observables.connectToReactiveSeq(observable).prepend(value));
    }

    @Override
    public ReactiveSeq<T> prependAll(T... values) {
        return observable(Observables.connectToReactiveSeq(observable).prependAll(values));
    }

    @Override
    public boolean endsWith(Iterable<T> iterable) {
        return Observables.connectToReactiveSeq(observable).endsWith(iterable);
    }



    @Override
    public ReactiveSeq<T> skip(long time, TimeUnit unit) {
        return observable(observable.skip(time,unit));
    }

    @Override
    public ReactiveSeq<T> limit(long time, TimeUnit unit) {
        return observable(observable.take(time,unit));
    }

    @Override
    public ReactiveSeq<T> skipLast(int num) {
        return observable(observable.skipLast(num));
    }

    @Override
    public ReactiveSeq<T> limitLast(int num) {
        return observable(observable.takeLast(num));
    }

    @Override
    public T firstValue(T alt) {
        return observable.blockingFirst(alt);
    }

    @Override
    public ReactiveSeq<T> onEmptySwitch(Supplier<? extends Stream<T>> switchTo) {
        return observable(Observables.connectToReactiveSeq(observable).onEmptySwitch(switchTo));
    }

    @Override
    public ReactiveSeq<T> onEmptyGet(Supplier<? extends T> supplier) {
        return observable(Observables.connectToReactiveSeq(observable).onEmptyGet(supplier));
    }

    @Override
    public <X extends Throwable> ReactiveSeq<T> onEmptyError(Supplier<? extends X> supplier) {
        return observable(Observables.connectToReactiveSeq(observable).onEmptyError(supplier));
    }

    @Override
    public <U> ReactiveSeq<T> distinct(Function<? super T, ? extends U> keyExtractor) {
        return observable(observable.distinct(a->keyExtractor.apply(a)));
    }

    @Override
    public ReactiveSeq<T> xPer(int x, long time, TimeUnit t) {
        return observable(Observables.connectToReactiveSeq(observable).xPer(x,time,t));
    }

    @Override
    public ReactiveSeq<T> onePer(long time, TimeUnit t) {
        return observable(Observables.connectToReactiveSeq(observable).onePer(time,t));
    }

    @Override
    public ReactiveSeq<T> debounce(long time, TimeUnit t) {
        return observable(Observables.connectToReactiveSeq(observable).debounce(time,t));
    }

    @Override
    public ReactiveSeq<T> fixedDelay(long l, TimeUnit unit) {
        return observable(Observables.connectToReactiveSeq(observable).fixedDelay(l,unit));
    }

    @Override
    public ReactiveSeq<T> jitter(long maxJitterPeriodInNanos) {
        return observable(Observables.connectToReactiveSeq(observable).jitter(maxJitterPeriodInNanos));
    }

    @Override
    public ReactiveSeq<T> onComplete(Runnable fn) {
        return observable(observable.doOnComplete(()->fn.run()));
    }

    @Override
    public ReactiveSeq<T> recover(Function<? super Throwable, ? extends T> fn) {
        return observable(Observables.connectToReactiveSeq(observable).recover(fn));
    }

    @Override
    public <EX extends Throwable> ReactiveSeq<T> recover(Class<EX> exceptionClass, Function<? super EX, ? extends T> fn) {
        return observable(Observables.connectToReactiveSeq(observable).recover(exceptionClass,fn));
    }

    @Override
    public long count() {
        return Observables.connectToReactiveSeq(observable).count();
    }

    @Override
    public ReactiveSeq<T> appendStream(Stream<? extends T> other) {
        return observable(Observables.connectToReactiveSeq(observable).appendStream(other));
    }

    @Override
    public ReactiveSeq<T> appendAll(Iterable<? extends T> other) {
        return  observable(Observables.connectToReactiveSeq(observable).appendAll(other));
    }

    @Override
    public ReactiveSeq<T> prependAll(Iterable<? extends T> other) {
        return observable(Observables.connectToReactiveSeq(observable).prependAll(other));
    }

    @Override
    public ReactiveSeq<T> cycle(long times) {
        return observable(observable.repeat(times));
    }

    @Override
    public ReactiveSeq<T> skipWhileClosed(Predicate<? super T> predicate) {
        return observable(Observables.connectToReactiveSeq(observable).skipWhileClosed(predicate));
    }

    @Override
    public ReactiveSeq<T> changes() {
        return observable(Observables.connectToReactiveSeq(observable).changes());
    }



    @Override
    public <X extends Throwable> Subscription forEachSubscribe(Consumer<? super T> consumer) {
        return Observables.connectToReactiveSeq(observable).forEachSubscribe(consumer);
    }

    @Override
    public <X extends Throwable> Subscription forEachSubscribe(Consumer<? super T> consumer, Consumer<? super Throwable> consumerError) {
        return Observables.connectToReactiveSeq(observable).forEachSubscribe(consumer, consumerError);
    }

    @Override
    public <X extends Throwable> Subscription forEachSubscribe(Consumer<? super T> consumer, Consumer<? super Throwable> consumerError, Runnable onComplete) {
        return Observables.connectToReactiveSeq(observable).forEachSubscribe(consumer, consumerError,onComplete);
    }

    @Override
    public <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super T> accumulator, BiConsumer<R, R> combiner) {

        return Observables.connectToReactiveSeq(observable).collect(supplier,accumulator,combiner);
    }

    @Override
    public <R, A> ReactiveSeq<R> collectAll(Collector<? super T, A, R> collector) {
        return observable(Observables.connectToReactiveSeq(observable).collectAll(collector));
    }
    @Override
    public <R> ReactiveSeq<R> reduceAll(R identity, BiFunction<R, ? super T, R>  accumulator) {
        Single<R> inter = observable.reduce(identity,(a, b)->accumulator.apply(a,b));
        return observable(inter.toObservable());
    }

    @Override
    public <R, A> R collect(Collector<? super T, A, R> collector) {
        return Observables.connectToReactiveSeq(observable).collect((Collector<T,A,R>)collector);
    }

    @Override
    public Maybe<T> single(Predicate<? super T> predicate) {
        return filter(predicate).single();
    }

    @Override
    public Maybe<T> single() {
        return Observables.connectToReactiveSeq(observable).single();
    }

    @Override
    public Seq<ReactiveSeq<T>> multicast(int num) {
        return Observables.connectToReactiveSeq(observable).multicast(num).map(s->observable(s));
    }

    @Override
    public void subscribe(Subscriber<? super T> s) {
        Observables.publisher(observable).subscribe(s);
    }
    @Override
    public <R> R visit(Function<? super ReactiveSeq<T>,? extends R> sync,Function<? super ReactiveSeq<T>,? extends R> reactiveStreams,
                       Function<? super ReactiveSeq<T>,? extends R> asyncNoBackPressure){
        return asyncNoBackPressure.apply(this);
    }

    @Override
    public void forEachAsync(Consumer<? super T> action) {
        observable.subscribe(a->action.accept(a));
    }
}
