package com.oath.cyclops.rx2.adapter;

import com.oath.cyclops.types.persistent.PersistentCollection;
import cyclops.companion.rx2.Functions;
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
import cyclops.reactive.Spouts;
import io.reactivex.Flowable;
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
public class FlowableReactiveSeqImpl<T> implements ReactiveSeq<T> {
    @Wither
    @Getter
    Flowable<T> flowable;

    public <R> FlowableReactiveSeqImpl<R> flux(Flowable<R> flux){
        return new FlowableReactiveSeqImpl<>(flux);
    }
    public <R> FlowableReactiveSeqImpl<R> flux(ReactiveSeq<R> flux){
        if(flux instanceof FlowableReactiveSeqImpl){
            return  (FlowableReactiveSeqImpl)flux;
        }
        return new FlowableReactiveSeqImpl<>(Flowable.fromPublisher(flux));
    }

    @Override
    public <R> ReactiveSeq<R> coflatMap(Function<? super ReactiveSeq<T>, ? extends R> fn) {
        return flux(Flowable.just(fn.apply(this)));
    }

    @Override
    public <T1> ReactiveSeq<T1> unit(T1 unit) {
        return flux(Flowable.just(unit));
    }

    @Override
    public <U> U foldRight(U identity, BiFunction<? super T, ? super U, ? extends U> accumulator) {
        return flowable.reduce(identity,(a, b)->accumulator.apply(b,a)).blockingGet();
    }

    @Override
    public <U, R> ReactiveSeq<R> zipWithStream(Stream<? extends U> other, BiFunction<? super T, ? super U, ? extends R> zipper) {
        if(other instanceof Publisher){
            return zip(zipper,(Publisher<U>)other);
        }
        return flux(flowable.zipWith((Iterable<U>)ReactiveSeq.fromStream((Stream<U>)other), Functions.rxBifunction(zipper)));
    }

    @Override
    public <U, R> ReactiveSeq<R> zipLatest(Publisher<? extends U> other, BiFunction<? super T, ? super U, ? extends R> zipper) {
        return flux(Flowable.combineLatest(flowable,other,(a,b)->zipper.apply(a,b)));
    }

    @Override
    public <U, R> ReactiveSeq<R> zip(BiFunction<? super T, ? super U, ? extends R> zipper,Publisher<? extends U> other) {
        return flux(flowable.zipWith(other,Functions.rxBifunction(zipper)));
    }

    @Override
    public <U> ReactiveSeq<Tuple2<T, U>> zipWithPublisher(Publisher<? extends U> other) {
        return flux(flowable.zipWith(other,Tuple::tuple));
    }

    @Override
    public ReactiveSeq<T> cycle() {
        return flux(flowable.repeat());
    }

    @Override
    public Tuple2<ReactiveSeq<T>, ReactiveSeq<T>> duplicate() {
        return Spouts.from(flowable).duplicate().transform((s1, s2)->Tuple.tuple(flux(s1),flux(s2)));
    }

    @Override
    public Tuple2<ReactiveSeq<T>, ReactiveSeq<T>> duplicate(Supplier<Deque<T>> bufferFactory) {
        return Spouts.from(flowable).duplicate(bufferFactory).transform((s1, s2)->Tuple.tuple(flux(s1),flux(s2)));
    }

    @Override
    public Tuple3<ReactiveSeq<T>, ReactiveSeq<T>, ReactiveSeq<T>> triplicate() {
        return Spouts.from(flowable).triplicate().transform((s1, s2, s3)->Tuple.tuple(flux(s1),flux(s2),flux(s3)));
    }

    @Override
    public Tuple3<ReactiveSeq<T>, ReactiveSeq<T>, ReactiveSeq<T>> triplicate(Supplier<Deque<T>> bufferFactory) {
        return Spouts.from(flowable).triplicate(bufferFactory).transform((s1, s2, s3)->Tuple.tuple(flux(s1),flux(s2),flux(s3)));
    }

    @Override
    public Tuple4<ReactiveSeq<T>, ReactiveSeq<T>, ReactiveSeq<T>, ReactiveSeq<T>> quadruplicate() {
        return Spouts.from(flowable).quadruplicate().to(t4->Tuple.tuple(flux(t4._1()),flux(t4._2()),flux(t4._3()),flux(t4._4())));
    }

    @Override
    public Tuple4<ReactiveSeq<T>, ReactiveSeq<T>, ReactiveSeq<T>, ReactiveSeq<T>> quadruplicate(Supplier<Deque<T>> bufferFactory) {
        return Spouts.from(flowable).quadruplicate(bufferFactory).to(t4->Tuple.tuple(flux(t4._1()),flux(t4._2()),flux(t4._3()),flux(t4._4())));
    }

    @Override
    public Tuple2<Option<T>, ReactiveSeq<T>> splitAtHead() {
        return Spouts.from(flowable).splitAtHead().transform((s1, s2)->Tuple.tuple(s1,flux(s2)));
    }

    @Override
    public Tuple2<ReactiveSeq<T>, ReactiveSeq<T>> splitAt(int where) {
        return Spouts.from(flowable).splitAt(where).transform((s1, s2)->Tuple.tuple(flux(s1),flux(s2)));
    }

    @Override
    public Tuple2<ReactiveSeq<T>, ReactiveSeq<T>> splitBy(Predicate<T> splitter) {
        return Spouts.from(flowable).splitBy(splitter).transform((s1, s2)->Tuple.tuple(flux(s1),flux(s2)));
    }

    @Override
    public Tuple2<ReactiveSeq<T>, ReactiveSeq<T>> partition(Predicate<? super T> splitter) {
        return Spouts.from(flowable).partition(splitter).transform((s1, s2)->Tuple.tuple(flux(s1),flux(s2)));
    }

    @Override
    public <U> ReactiveSeq<Tuple2<T, U>> zipWithStream(Stream<? extends U> other) {
        if(other instanceof Publisher){
            return zipWithPublisher((Publisher<U>)other);
        }
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
        return flux(Spouts.from(flowable).sliding(windowSize,increment));
    }

    @Override
    public ReactiveSeq<Vector<T>> grouped(int groupSize) {
        return flux(Spouts.from(flowable).grouped(groupSize));
    }

    @Override
    public ReactiveSeq<Vector<T>> groupedUntil(BiPredicate<Vector<? super T>, ? super T> predicate) {
        return flux(Spouts.from(flowable).groupedUntil(predicate));
    }

    @Override
    public <C extends PersistentCollection<T>, R> ReactiveSeq<R> groupedUntil(BiPredicate<C, ? super T> predicate, Supplier<C> factory, Function<? super C, ? extends R> finalizer) {
        return flux(Spouts.from(flowable).groupedUntil(predicate,factory,finalizer));
    }

    @Override
    public ReactiveSeq<Vector<T>> groupedWhile(BiPredicate<Vector<? super T>, ? super T> predicate) {
        return flux(Spouts.from(flowable).groupedWhile(predicate));
    }

    @Override
    public <C extends PersistentCollection<T>, R> ReactiveSeq<R> groupedWhile(BiPredicate<C, ? super T> predicate, Supplier<C> factory, Function<? super C, ? extends R> finalizer) {
        return flux(Spouts.from(flowable).groupedWhile(predicate,factory,finalizer));
    }

    @Override
    public ReactiveSeq<Vector<T>> groupedBySizeAndTime(int size, long time, TimeUnit t) {
        return flux(Spouts.from(flowable).groupedBySizeAndTime(size, time, t));
    }

    @Override
    public <C extends PersistentCollection<? super T>> ReactiveSeq<C> groupedBySizeAndTime(int size, long time, TimeUnit unit, Supplier<C> factory) {
        return flux(Spouts.from(flowable).groupedBySizeAndTime(size,time,unit,factory));
    }

    @Override
    public <C extends PersistentCollection<? super T>, R> ReactiveSeq<R> groupedBySizeAndTime(int size, long time, TimeUnit unit, Supplier<C> factory, Function<? super C, ? extends R> finalizer) {
        return flux(Spouts.from(flowable).groupedBySizeAndTime(size,time,unit,factory,finalizer));
    }

    @Override
    public <C extends PersistentCollection<? super T>, R> ReactiveSeq<R> groupedByTime(long time, TimeUnit unit, Supplier<C> factory, Function<? super C, ? extends R> finalizer) {
        return groupedBySizeAndTime(Integer.MAX_VALUE,time,unit,factory,finalizer);
    }

    @Override
    public ReactiveSeq<Vector<T>> groupedByTime(long time, TimeUnit t) {
        return flux(Spouts.from(flowable).groupedByTime(time, t));
    }

    @Override
    public <C extends PersistentCollection<? super T>> ReactiveSeq<C> groupedByTime(long time, TimeUnit unit, Supplier<C> factory) {
        return flux(Spouts.from(flowable).groupedByTime(time, unit, factory));
    }

    @Override
    public <C extends PersistentCollection<? super T>> ReactiveSeq<C> grouped(int size, Supplier<C> supplier) {
        return flux(Spouts.from(flowable).grouped(size,()->supplier.get()));
    }

    @Override
    public ReactiveSeq<Vector<T>> groupedWhile(Predicate<? super T> predicate) {
        return flux(Spouts.from(flowable).groupedWhile(predicate));
    }

    @Override
    public <C extends PersistentCollection<? super T>> ReactiveSeq<C> groupedWhile(Predicate<? super T> predicate, Supplier<C> factory) {
        return flux(Spouts.from(flowable).groupedWhile(predicate,factory));
    }

    @Override
    public ReactiveSeq<T> distinct() {
        return flux(flowable.distinct());
    }

    @Override
    public <U> ReactiveSeq<U> scanLeft(U seed, BiFunction<? super U, ? super T, ? extends U> function) {
        return flux(flowable.scan(seed,(a, b)->function.apply(a,b)));
    }

    @Override
    public ReactiveSeq<T> sorted() {
        return flux(flowable.sorted());
    }

    @Override
    public ReactiveSeq<T> skip(long num) {
        return flux(flowable.skip(num));
    }


    @Override
    public void forEach(Consumer<? super T> action) {
        Spouts.from(flowable).forEach(action);
    }

    @Override
    public void forEachOrdered(Consumer<? super T> action) {
        Spouts.from(flowable).forEachOrdered(action);
    }

    @Override
    public Object[] toArray() {
        return Spouts.from(flowable).toArray();
    }

    @Override
    public <A> A[] toArray(IntFunction<A[]> generator) {
        return Spouts.from(flowable).toArray(generator);
    }

    @Override
    public ReactiveSeq<T> skipWhile(Predicate<? super T> p) {
        return flux(flowable.skipWhile(Functions.rxPredicate(p)));
    }

    @Override
    public ReactiveSeq<T> limit(long num) {
        return flux(flowable.take(num));
    }

    @Override
    public ReactiveSeq<T> limitWhile(Predicate<? super T> p) {
        return flux(Spouts.from(flowable).takeWhile(p));
    }
    @Override
    public ReactiveSeq<T> limitWhileClosed(Predicate<? super T> p) {
        return flux(flowable.takeWhile(Functions.rxPredicate(p)));
    }

    @Override
    public ReactiveSeq<T> limitUntil(Predicate<? super T> p) {
       return flux(Spouts.from(flowable).limitUntil(p));
    }

    @Override
    public ReactiveSeq<T> limitUntilClosed(Predicate<? super T> p) {
        return flux(flowable.takeUntil(Functions.rxPredicate(p)));
    }

    @Override
    public ReactiveSeq<T> parallel() {
        return this;
    }

    @Override
    public boolean allMatch(Predicate<? super T> c) {
        return Spouts.from(flowable).allMatch(c);
    }

    @Override
    public boolean anyMatch(Predicate<? super T> c) {
        return Spouts.from(flowable).anyMatch(c);
    }

    @Override
    public boolean xMatch(int num, Predicate<? super T> c) {
        return Spouts.from(flowable).xMatch(num,c);
    }

    @Override
    public boolean noneMatch(Predicate<? super T> c) {
        return Spouts.from(flowable).noneMatch(c);
    }

    @Override
    public String join() {
        return Spouts.from(flowable).join();
    }

    @Override
    public String join(String sep) {
        return Spouts.from(flowable).join(sep);
    }

    @Override
    public String join(String sep, String start, String end) {
        return Spouts.from(flowable).join(sep,start,end);
    }


    @Override
    public Optional<T> findFirst() {
        return Spouts.from(flowable).findFirst();
    }

    @Override
    public Maybe<T> takeOne() {
        return Spouts.from(flowable).takeOne();
    }

    @Override
    public LazyEither<Throwable, T> findFirstOrError() {
        return Spouts.from(flowable).findFirstOrError();
    }

    @Override
    public Optional<T> findAny() {
        return Spouts.from(flowable).findAny();
    }

    @Override
    public <R> R foldMap(Reducer<R,T> reducer) {
        return Spouts.from(flowable).foldMap(reducer);
    }

    @Override
    public <R> R foldMap(Function<? super T, ? extends R> mapper, Monoid<R> reducer) {
        return Spouts.from(flowable).foldMap(mapper,reducer);
    }

    @Override
    public T reduce(Monoid<T> reducer) {
        return Spouts.from(flowable).reduce(reducer);
    }

    @Override
    public Optional<T> reduce(BinaryOperator<T> accumulator) {
        return Spouts.from(flowable).reduce(accumulator);
    }

    @Override
    public T reduce(T identity, BinaryOperator<T> accumulator) {
        return Spouts.from(flowable).reduce(identity,accumulator);
    }

    @Override
    public <U> U reduce(U identity, BiFunction<U, ? super T, U> accumulator, BinaryOperator<U> combiner) {
        return Spouts.from(flowable).reduce(identity, accumulator, combiner);
    }


    @Override
    public Seq<T> reduce(Iterable<? extends Monoid<T>> reducers) {
        return Spouts.from(flowable).reduce(reducers);
    }

    @Override
    public T foldRight(Monoid<T> reducer) {
        return Spouts.from(flowable).foldRight(reducer);
    }

    @Override
    public T foldRight(T identity, BinaryOperator<T> accumulator) {
        return Spouts.from(flowable).foldRight(identity,accumulator);
    }

    @Override
    public <T1> T1 foldMapRight(Reducer<T1,T> reducer) {
        return Spouts.from(flowable).foldMapRight(reducer);
    }

    @Override
    public ReactiveSeq<T> stream() {
        return Spouts.from(flowable);
    }

    @Override
    public <U> FlowableReactiveSeqImpl<U> unitIterable(Iterable<U> U) {
        return new FlowableReactiveSeqImpl<>(Flowable.fromIterable(U));
    }

    @Override
    public boolean startsWith(Iterable<T> iterable) {
        return Spouts.from(flowable).startsWith(iterable);
    }


    @Override
    public <R> ReactiveSeq<R> map(Function<? super T, ? extends R> fn) {
        return flux(flowable.map(Functions.rxFunction(fn)));
    }

    @Override
    public <R> ReactiveSeq<R> flatMap(Function<? super T, ? extends Stream<? extends R>> fn) {
        return flux(flowable.flatMap(s->ReactiveSeq.fromStream(fn.apply(s))));
    }

    @Override
    public IntStream flatMapToInt(Function<? super T, ? extends IntStream> mapper) {
        return Spouts.from(flowable).flatMapToInt(mapper);
    }

    @Override
    public LongStream flatMapToLong(Function<? super T, ? extends LongStream> mapper) {
        return Spouts.from(flowable).flatMapToLong(mapper);
    }

    @Override
    public DoubleStream flatMapToDouble(Function<? super T, ? extends DoubleStream> mapper) {
        return Spouts.from(flowable).flatMapToDouble(mapper);
    }



    @Override
    public <R> ReactiveSeq<R> concatMap(Function<? super T, ? extends Iterable<? extends R>> fn) {
        return flux(flowable.flatMapIterable(Functions.rxFunction(fn)));
    }



    @Override
    public <R> ReactiveSeq<R> mergeMap(Function<? super T, ? extends Publisher<? extends R>> fn) {
        return flux(flowable.flatMap(Functions.rxFunction(fn)));
    }

    @Override
    public <R> ReactiveSeq<R> mergeMap(int maxConcurrency, Function<? super T, ? extends Publisher<? extends R>> fn) {
        return flux(flowable.flatMap(Functions.rxFunction(fn),maxConcurrency));
    }

    @Override
    public <R> ReactiveSeq<R> flatMapStream(Function<? super T, BaseStream<? extends R, ?>> fn) {

        return this.<R>flux((Flowable) flowable.flatMap(Functions.rxFunction(fn.andThen(s->{
            ReactiveSeq<R> res = s instanceof ReactiveSeq ? (ReactiveSeq) s : (ReactiveSeq) ReactiveSeq.fromSpliterator(s.spliterator());
           return res;
                }

        ))));
    }

    @Override
    public ReactiveSeq<T> filter(Predicate<? super T> fn) {
        return flux(flowable.filter(Functions.rxPredicate(fn)));
    }

    @Override
    public Iterator<T> iterator() {
        return flowable.blockingIterable().iterator();
    }

    @Override
    public Spliterator<T> spliterator() {
        return flowable.blockingIterable().spliterator();
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
        return flux(Spouts.from(flowable).reverse());
    }

    @Override
    public ReactiveSeq<T> onClose(Runnable closeHandler) {
        return flux(flowable.doOnComplete(()->closeHandler.run()));
    }

    @Override
    public void close() {

    }

    @Override
    public ReactiveSeq<T> prependStream(Stream<? extends T> stream) {
        return flux(Spouts.from(flowable).prependStream(stream));
    }

    @Override
    public ReactiveSeq<T> appendAll(T... values) {
        return flux(Spouts.from(flowable).appendAll(values));
    }

    @Override
    public ReactiveSeq<T> append(T value) {
        return flux(Spouts.from(flowable).append(value));
    }

    @Override
    public ReactiveSeq<T> prepend(T value) {
        return flux(Spouts.from(flowable).prepend(value));
    }

    @Override
    public ReactiveSeq<T> prependAll(T... values) {
        return flux(Spouts.from(flowable).prependAll(values));
    }

    @Override
    public boolean endsWith(Iterable<T> iterable) {
        return Spouts.from(flowable).endsWith(iterable);
    }


    @Override
    public ReactiveSeq<T> skip(long time, TimeUnit unit) {
        return flux(flowable.skip(time,unit));
    }

    @Override
    public ReactiveSeq<T> limit(long time, TimeUnit unit) {
        return flux(flowable.take(time,unit));
    }

    @Override
    public ReactiveSeq<T> skipLast(int num) {
        return flux(flowable.skipLast(num));
    }

    @Override
    public ReactiveSeq<T> limitLast(int num) {
        return flux(flowable.takeLast(num));
    }

    @Override
    public T firstValue(T alt) {
        return flowable.blockingFirst(alt);
    }

    @Override
    public ReactiveSeq<T> onEmptySwitch(Supplier<? extends Stream<T>> switchTo) {
        return flux(Spouts.from(flowable).onEmptySwitch(switchTo));
    }

    @Override
    public ReactiveSeq<T> onEmptyGet(Supplier<? extends T> supplier) {
        return flux(Spouts.from(flowable).onEmptyGet(supplier));
    }

    @Override
    public <X extends Throwable> ReactiveSeq<T> onEmptyError(Supplier<? extends X> supplier) {
        return flux(Spouts.from(flowable).onEmptyError(supplier));
    }

    @Override
    public <U> ReactiveSeq<T> distinct(Function<? super T, ? extends U> keyExtractor) {
        return flux(flowable.distinct(Functions.rxFunction(keyExtractor)));
    }

    @Override
    public ReactiveSeq<T> xPer(int x, long time, TimeUnit t) {
        return flux(Spouts.from(flowable).xPer(x,time,t));
    }

    @Override
    public ReactiveSeq<T> onePer(long time, TimeUnit t) {
        return flux(Spouts.from(flowable).onePer(time,t));
    }

    @Override
    public ReactiveSeq<T> debounce(long time, TimeUnit t) {
        return flux(Spouts.from(flowable).debounce(time,t));
    }

    @Override
    public ReactiveSeq<T> fixedDelay(long l, TimeUnit unit) {
        return flux(Spouts.from(flowable).fixedDelay(l,unit));
    }

    @Override
    public ReactiveSeq<T> jitter(long maxJitterPeriodInNanos) {
        return flux(Spouts.from(flowable).jitter(maxJitterPeriodInNanos));
    }

    @Override
    public ReactiveSeq<T> onComplete(Runnable fn) {
        return flux(flowable.doOnComplete(()->fn.run()));
    }

    @Override
    public ReactiveSeq<T> recover(Function<? super Throwable, ? extends T> fn) {
        return flux(Spouts.from(flowable).recover(fn));
    }

    @Override
    public <EX extends Throwable> ReactiveSeq<T> recover(Class<EX> exceptionClass, Function<? super EX, ? extends T> fn) {
        return flux(Spouts.from(flowable).recover(exceptionClass,fn));
    }

    @Override
    public long count() {
        return Spouts.from(flowable).count();
    }

    @Override
    public ReactiveSeq<T> appendStream(Stream<? extends T> other) {
        return flux(Spouts.from(flowable).appendStream(other));
    }

    @Override
    public ReactiveSeq<T> appendAll(Iterable<? extends T> other) {
        return  flux(Spouts.from(flowable).appendAll(other));
    }

    @Override
    public ReactiveSeq<T> prependAll(Iterable<? extends T> other) {
        return flux(Spouts.from(flowable).prependAll(other));
    }

    @Override
    public ReactiveSeq<T> cycle(long times) {
        return flux(flowable.repeat(times));
    }

    @Override
    public ReactiveSeq<T> skipWhileClosed(Predicate<? super T> predicate) {
        return flux(Spouts.from(flowable).skipWhileClosed(predicate));
    }

    @Override
    public ReactiveSeq<T> changes() {
        return flux(Spouts.from(flowable).changes());
    }

    @Override
    public <X extends Throwable> Subscription forEachSubscribe(Consumer<? super T> consumer) {
        return Spouts.from(flowable).forEachSubscribe(consumer);
    }

    @Override
    public <X extends Throwable> Subscription forEachSubscribe(Consumer<? super T> consumer, Consumer<? super Throwable> consumerError) {
        return Spouts.from(flowable).forEachSubscribe(consumer, consumerError);
    }

    @Override
    public <X extends Throwable> Subscription forEachSubscribe(Consumer<? super T> consumer, Consumer<? super Throwable> consumerError, Runnable onComplete) {
        return Spouts.from(flowable).forEachSubscribe(consumer, consumerError,onComplete);
    }

    @Override
    public <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super T> accumulator, BiConsumer<R, R> combiner) {

        return flowable.collect(()->supplier.get(),(a,b)->accumulator.accept(a,b)).blockingGet();
    }

    @Override
    public <R> ReactiveSeq<R> reduceAll(R identity, BiFunction<R, ? super T, R>  accumulator) {
        Single<R> inter = flowable.reduce(identity,(a,b)->accumulator.apply(a,b));
        return flux(inter.toFlowable());
    }

    @Override
    public <R, A> ReactiveSeq<R> collectAll(Collector<? super T, A, R> collector) {

          Single<A> inter = flowable.collect(()->collector.supplier().get(), (a,b)->collector.accumulator().accept(a,b));
          Single<R> res = inter.map(Functions.rxFunction(collector.finisher()));
          return flux(res.toFlowable());

    }

    @Override
    public <R, A> R collect(Collector<? super T, A, R> collector) {

        A inter = collect(collector.supplier(), collector.accumulator(), null);
        return collector.finisher().apply(inter);
    }


    @Override
    public Maybe<T> single(Predicate<? super T> predicate) {
        return filter(predicate).single();
    }

    @Override
    public Maybe<T> single() {
        Maybe.CompletableMaybe<T,T> maybe = Maybe.<T>maybe();
        flowable.subscribe(new Subscriber<T>() {
            T value = null;
            Subscription sub;
            @Override
            public void onSubscribe(Subscription s) {
                this.sub=s;
                s.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(T t) {
                if(value==null)
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

    @Override
    public void subscribe(Subscriber<? super T> s) {
        flowable.subscribe(s);
    }

    @Override
    public <R> R visit(Function<? super ReactiveSeq<T>,? extends R> sync,Function<? super ReactiveSeq<T>,? extends R> reactiveStreams,
                         Function<? super ReactiveSeq<T>,? extends R> asyncNoBackPressure){
        return reactiveStreams.apply(this);
    }

    @Override
    public void forEachAsync(Consumer<? super T> action) {
        this.flowable.subscribe(a->action.accept(a));
    }

}
