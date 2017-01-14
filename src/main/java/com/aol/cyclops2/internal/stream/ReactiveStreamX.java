package com.aol.cyclops2.internal.stream;

import com.aol.cyclops2.internal.stream.publisher.PublisherIterable;
import com.aol.cyclops2.internal.stream.spliterators.*;
import com.aol.cyclops2.internal.stream.spliterators.push.*;
import com.aol.cyclops2.types.FoldableTraversable;
import com.aol.cyclops2.types.stream.CyclopsCollectable;
import com.aol.cyclops2.types.stream.HotStream;
import com.aol.cyclops2.types.stream.reactive.ReactiveSubscriber;
import com.aol.cyclops2.util.ExceptionSoftener;
import cyclops.Streams;
import cyclops.async.Future;
import cyclops.collections.ListX;
import cyclops.collections.immutable.PVectorX;
import cyclops.control.Eval;
import cyclops.function.Monoid;
import cyclops.monads.AnyM;
import cyclops.monads.Witness;
import cyclops.stream.ReactiveSeq;
import lombok.AllArgsConstructor;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.io.BufferedReader;
import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.*;
import java.util.stream.BaseStream;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.aol.cyclops2.internal.comprehensions.comprehenders.StreamAdapter.stream;


@AllArgsConstructor
public class ReactiveStreamX<T> extends BaseExtendedStream<T> {

    final Operator<T> source;

    @Override
    public ReactiveSeq<T> reverse() {
        return coflatMap(s->ReactiveSeq.reversedListOf(s.toList()))
                .flatMap(i->i);
    }




   
    <X> ReactiveSeq<X> createSeq(Operator<X> stream) {
        return new ReactiveStreamX<X>(stream);
    }


    public  <R> ReactiveSeq<R> coflatMap(Function<? super ReactiveSeq<T>, ? extends R> fn){
        return createSeq(new LazySingleValueOperator<ReactiveSeq<T>,R>(createSeq( source),fn));

    }



    @Override
    public final ReactiveSeq<PVectorX<T>> sliding(final int windowSize, final int increment) {
        return createSeq(new SlidingOperator<>( source,Function.identity(), windowSize,increment));
    }

    @Override
    public ReactiveSeq<ListX<T>> grouped(final int groupSize) {
        return createSeq(new GroupingOperator<T,List<T>,ListX<T>>( source,()->new ArrayList(groupSize), c->ListX.fromIterable(c),groupSize));

    }
    @Override
    public ReactiveSeq<ListX<T>> groupedStatefullyWhile(final BiPredicate<ListX<? super T>, ? super T> predicate) {
        return createSeq(new GroupedStatefullyOperator<>( source,()->ListX.of(),Function.identity(), predicate));
    }
    @Override
    public <C extends Collection<T>,R> ReactiveSeq<R> groupedStatefullyWhile(final BiPredicate<C, ? super T> predicate, final Supplier<C> factory,
                                                                             Function<? super C, ? extends R> finalizer) {
        return this.<R>createSeq(new GroupedStatefullyOperator<>( source,factory,finalizer, predicate));
    }
    @Override
    public ReactiveSeq<ListX<T>> groupedStatefullyUntil(final BiPredicate<ListX<? super T>, ? super T> predicate) {
        return createSeq(new GroupedStatefullySpliterator<>( source,()->ListX.of(),Function.identity(), predicate.negate()), this.reversible,split);
    }
    @Override
    public <C extends Collection<T>,R> ReactiveSeq<R> groupedStatefullyUntil(final BiPredicate<C, ? super T> predicate, final Supplier<C> factory,
                                                                             Function<? super C, ? extends R> finalizer) {
        return this.<R>createSeq(new GroupedStatefullySpliterator<T,C,R>( source,factory,finalizer, predicate.negate()), this.reversible,split);
    }

    @Override
    public final ReactiveSeq<T> distinct() {
        return createSeq(new DistinctSpliterator<T,T>( source),);
    }

    @Override
    public final ReactiveSeq<T> scanLeft(final Monoid<T> monoid) {
        return scanLeft(monoid.zero(),monoid);

    }

    @Override
    public final <U> ReactiveSeq<U> scanLeft(final U seed, final BiFunction<? super U, ? super T, ? extends U> function) {
        Supplier<Function<? super T, ? extends Object>> scanLeft = () -> {
             Object[] current = {seed};
             return in-> current[0]=function.apply((U)current[0],in);
        };

        return createSeq(new ArrayConcatonatingOperator<>(new SingleValueOperator<U>(seed),
                        extract((ReactiveSeq<U>)mapLazyFn(scanLeft))));


    }

    private <U> Operator<U> extract(ReactiveSeq<U> seq){
        return ((ReactiveStreamX<U>)seq).source;
    }








    @Override
    public final ReactiveSeq<T> skipWhile(final Predicate<? super T> p) {
        return createSeq(new SkipWhileOperator<>( source,p));
    }





    @Override
    public final ReactiveSeq<T> limitWhile(final Predicate<? super T> p) {
        return createSeq(new LimitWhileOperator<>( source, p));
    }

    @Override
    public final ReactiveSeq<T> limitUntil(final Predicate<? super T> p) {
        return limitWhile(p.negate());
    }



    @Override
    public ReactiveSeq<T> skipWhileClosed(Predicate<? super T> predicate) {
        return createSeq(new SkipWhileOperator<>( source,predicate) );
    }

    @Override
    public ReactiveSeq<T> limitWhileClosed(Predicate<? super T> predicate) {
        return createSeq(new LimitWhileClosedOperator<>( source,predicate));
    }


    @Override
    public final <R> ReactiveSeq<R> map(final Function<? super T, ? extends R> fn) {


        return createSeq(new MapOperator<T,R>(this. source,fn));
    }


    @Override
    public final <R> ReactiveSeq<R> flatMap(final Function<? super T, ? extends Stream<? extends R>> fn) {

        return createSeq(new FlatMapOperator<>( source,fn));

    }

    @Override
    public final <R> ReactiveSeq<R> flatMapAnyM(final Function<? super T, AnyM<Witness.stream,? extends R>> fn) {
        return createSeq(Streams.flatMapAnyM(this, fn));
    }

    @Override
    public final <R> ReactiveSeq<R> flatMapI(final Function<? super T, ? extends Iterable<? extends R>> fn) {

        return createSeq(new IterableFlatMapOperator<>( source,fn));

    }
    @Override
    public final <R> ReactiveSeq<R> flatMapP(final Function<? super T, ? extends Publisher<? extends R>> fn) {

        return createSeq(new PublisherFlatMapOperator<>( source,fn));
    }


    @Override
    public final ReactiveSeq<T> filter(final Predicate<? super T> fn) {
        return createSeq(new FilterOperator<T>( source,fn));

    }

    public final ReactiveSeq<T> filterLazyPredicate(final Supplier<Predicate<? super T>> fn) {
        return createSeq(new LazyFilterOperator<T>( source,fn));

    }



    @Override
    public void forEach(final Consumer<? super T> action) {
        this. source.subscribeAll(action,e->{},()->{});

    }
    @Override
    public long count() {

        long[] result = {0};
        forEach(t -> result[0]++);
        return result[0];


    }

    @Override //TODO can be replaced by a dedicated Spliterator that keeps an index
    public  ReactiveSeq<T> insertAt(final int pos, final T... values) {
        Tuple2<ReactiveSeq<T>, ReactiveSeq<T>> s = this.splitAt(pos);
        return ReactiveSeq.concat(s.v1,ReactiveSeq.of(values),s.v2);

    }

    @Override //TODO can be replaced by a dedicated Spliterator that keeps an index
    public ReactiveSeq<T> deleteBetween(final int start, final int end) {
        Tuple2<ReactiveSeq<T>, ReactiveSeq<T>> s = duplicate();
        return ReactiveSeq.concat(s.v1.limit(start),s.v2.skip(end));
    }

    @Override //TODO can be replaced by a dedicated Spliterator that keeps an index
    public ReactiveSeq<T> insertAtS(final int pos, final Stream<T> stream) {
        Tuple2<ReactiveSeq<T>, ReactiveSeq<T>> s = this.splitAt(pos);
        return ReactiveSeq.concat(s.v1,ReactiveSeq.fromStream(
                stream),s.v2);

    }

    @Override
    public void subscribe(final Subscriber<? super T> sub) {
        sub.onSubscribe(source.subscribe(sub::onNext,sub::onError,sub::onComplete));
    }



    @Override
    public ReactiveSeq<T> onEmpty(final T value) {
        return createSeq(new OnEmptyOperator<T>(source,()->value));

    }
    @Override
    public ReactiveSeq<T> onEmptySwitch(final Supplier<? extends Stream<T>> switchTo) {
        final Object value = new Object();
        ReactiveSeq res = createSeq(onEmptyGet((Supplier) () ->value).flatMap(s -> {
            if (s==value)
                return (Stream) switchTo.get();
            return Stream.of(s);
        }));
        return res;
    }

    @Override
    public ReactiveSeq<T> onEmptyGet(final Supplier<? extends T> supplier) {
        return createSeq(new OnEmptyOperator<T>(source,supplier));
    }

    @Override
    public <X extends Throwable> ReactiveSeq<T> onEmptyThrow(final Supplier<? extends X> supplier) {

        return createSeq(new OnEmptyOperator<T>(source,()->{throw supplier.get();}));
    }
    @Override
    public ReactiveSeq<T> appendS(final Stream<? extends T> other) {
        return ReactiveSeq.concat( source,avoidCopy(other));
    }
    public ReactiveSeq<T> append(final Iterable<? extends T> other) {
        return ReactiveSeq.concat( source,avoidCopy(other));
    }

    //TODO use spliterators and createSeq
    @Override
    public ReactiveSeq<T> append(final T other) {
        return ReactiveSeq.concat( source,new SingleSpliterator<T>(other));
    }

    @Override
    public ReactiveSeq<T> append(final T... other) {
        return ReactiveSeq.concat( source,Stream.of(other).spliterator());
    }
    @Override
    public ReactiveSeq<T> prependS(final Stream<? extends T> other) {
        return ReactiveSeq.concat(avoidCopy(other), source);
    }
    public ReactiveSeq<T> prepend(final Iterable<? extends T> other) {
        return ReactiveSeq.concat(avoidCopy(other), source);
    }

    @Override
    public ReactiveSeq<T> prepend(final T other) {
        return ReactiveSeq.concat(new SingleSpliterator<T>(other), source);
    }

    @Override
    public ReactiveSeq<T> prepend(final T... other) {
        return ReactiveSeq.concat(Stream.of(other).spliterator(), source);
    }


    @Override
    public <U> ReactiveSeq<T> distinct(final Function<? super T, ? extends U> keyExtractor) {
        Supplier<Predicate<? super T>> predicate = ()->{
            Set<U> values = new HashSet<>();
            return in-> values.add(keyExtractor.apply(in);
        };
        return this.filterLazyPredicate(predicate);
    }

    @Override
    public ReactiveSeq<ListX<T>> groupedBySizeAndTime(final int size, final long time, final TimeUnit t) {
        return createSeq(new GroupedByTimeAndSizeOperator<>(this. source,()->ListX.fromIterable(new ArrayList<>(size)),
                        Function.identity(),time,t,size)
               );

    }

    @Override
    public ReactiveSeq<ListX<T>> groupedByTime(final long time, final TimeUnit t) {
        return createSeq(new GroupedByTimeOperator<>( source,
                ()->ListX.fromIterable(new ArrayList<>(100)),
                Function.identity(),time, t));
    }
    @Override
    public ReactiveSeq<T> skip(final long time, final TimeUnit unit) {
        return createSeq(new SkipWhileTimeOperator<>( source, time, unit));
    }

    @Override
    public ReactiveSeq<T> limit(final long time, final TimeUnit unit) {
        return createSeq(new LimitWhileTimeOperator<>( source,time,unit));

    }

    @Override
    public ReactiveSeq<ListX<T>> groupedWhile(final Predicate<? super T> predicate) {
        return createSeq(new GroupedWhileOperator<>( source,()->ListX.of(),Function.identity(), predicate));


    }

    @Override
    public <C extends Collection<? super T>> ReactiveSeq<C> groupedWhile(final Predicate<? super T> predicate, final Supplier<C> factory) {
        return createSeq(new GroupedWhileOperator<>( source,factory,Function.identity(), predicate));
    }


    @Override
    public <C extends Collection<? super T>> ReactiveSeq<C> groupedBySizeAndTime(final int size, final long time, final TimeUnit unit,
                                                                                 final Supplier<C> factory) {
        return createSeq(new GroupedByTimeAndSizeOperator(this. source,factory,
                        Function.identity(),time,unit,size));

    }

    @Override
    public <C extends Collection<? super T>,R> ReactiveSeq<R> groupedBySizeAndTime(final int size, final long time,
                                                                                   final TimeUnit unit,
                                                                                   final Supplier<C> factory,
                                                                                   Function<? super C, ? extends R> finalizer
    ) {
        return createSeq(new GroupedByTimeAndSizeOperator(this. source,factory,
                        finalizer,time,unit,size)
               );

    }
    @Override
    public <C extends Collection<? super T>,R> ReactiveSeq<R> groupedByTime(final long time, final TimeUnit unit, final Supplier<C> factory, Function<? super C, ? extends R> finalizer) {
        return createSeq(new GroupedByTimeOperator(this. source,factory,
                        finalizer,time,unit)
               );

    }
    @Override
    public <C extends Collection<? super T>> ReactiveSeq<C> groupedByTime(final long time, final TimeUnit unit, final Supplier<C> factory) {
        return createSeq(new GroupedByTimeOperator(this. source,factory,
                        Function.identity(),time,unit)
               );

    }

    @Override
    public <C extends Collection<? super T>> ReactiveSeq<C> grouped(final int size, final Supplier<C> factory) {
        return createSeq(new GroupingOperator<>( source,factory, Function.identity(),size));

    }

    @Override
    public ReactiveSeq<T> skipLast(final int num) {
        if(num==1)
            return createSeq(new SkipLastOneOperator<>( source));
        return createSeq(new SkipLastOperator<>( source, num));
    }

    @Override
    public ReactiveSeq<T> limitLast(final int num) {
        if(num==1)
            return createSeq(new LimitLastOneOperator<>( source));
        return createSeq(new LimitLastOperator<>( source, num));
    }

    @Override
    public ReactiveSeq<T> recover(final Function<? super Throwable, ? extends T> fn) {
        return createSeq(new RecoverOperator<>( source,fn));
    }

    @Override
    public <EX extends Throwable> ReactiveSeq<T> recover(final Class<EX> exceptionClass, final Function<? super EX, ? extends T> fn) {

        Function<? super Throwable, ? extends EX> accept =  e->{
            if (exceptionClass.isAssignableFrom(e.getClass())){
                return (EX)e;
            }
            throw ExceptionSoftener.throwSoftenedException(e);
        };
        return createSeq(new RecoverOperator<>( source,fn.compose(accept)));
    }






    @Override
    public <X extends Throwable> Subscription forEach(final long numberOfElements, final Consumer<? super T> consumer) {
        StreamSubscription sub = source.subscribe(consumer, e->{}, ()->{});
        sub.request(numberOfElements);
        return sub;
    }

    @Override
    public <X extends Throwable> Subscription forEach(final long numberOfElements, final Consumer<? super T> consumer,
                                                      final Consumer<? super Throwable> consumerError) {

        StreamSubscription sub = source.subscribe(consumer, consumerError, ()->{});
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
        source.subscribeAll(consumerElement,consumerError,()->{});


    }

    @Override
    public <X extends Throwable> void forEach(final Consumer<? super T> consumerElement, final Consumer<? super Throwable> consumerError,
                                              final Runnable onComplete) {
        source.subscribeAll(consumerElement,consumerError,onComplete);


    }



    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Pure#unit(java.lang.Object)
     */
    @Override
    public <T> ReactiveSeq<T> unit(final T unit) {
        return ReactiveSeq.of(unit);
    }

    @Override
    public HotStream<T> schedule(final String cron, final ScheduledExecutorService ex) {
        return Streams.schedule(this, cron, ex);

    }

    @Override
    public HotStream<T> scheduleFixedDelay(final long delay, final ScheduledExecutorService ex) {
        return Streams.scheduleFixedDelay(this, delay, ex);
    }

    @Override
    public HotStream<T> scheduleFixedRate(final long rate, final ScheduledExecutorService ex) {
        return Streams.scheduleFixedRate(this, rate, ex);

    }
    

    @Override
    public ReactiveSeq<T> limit(long num){
        return createSeq(new LimitOperator<>(source,num));
    }
    @Override
    public ReactiveSeq<T> skip(long num){
        return createSeq(new SkipOperator<>(source,num));
    }
    @Override
    public ReactiveSeq<T> cycle() {
        return grouped(Integer.MAX_VALUE)
                          .flatMapI(s -> s.cycle(Long.MAX_VALUE));

    }

    @Override
    public Tuple2<ReactiveSeq<T>, ReactiveSeq<T>> duplicate() {
        Iterable<T> sourceIt = new OperatorToIterable<T,T>(source);
        Tuple2<Iterable<T>, Iterable<T>> copy = Streams.toBufferingDuplicator(sourceIt);
        Tuple2<Operator<T>, Operator<T>> operators = copy.map((a,b)->
            Tuple.tuple(new SpliteratorToOperator<T>(a.spliterator()),new SpliteratorToOperator<T>(b.spliterator()))
        );
        return operators.map((a,b)->Tuple.tuple(createSeq(a),createSeq(b)));

    }

    @Override
    @SuppressWarnings("unchecked")
    public Tuple3<ReactiveSeq<T>, ReactiveSeq<T>, ReactiveSeq<T>> triplicate() {
        Iterable<T> sourceIt = new OperatorToIterable<T,T>(source);
        ListX<SpliteratorToOperator<T>> copy = Streams.toBufferingCopier(sourceIt, 3)
                                         .map(it->new SpliteratorToOperator<>(it.spliterator()));

        return Tuple.tuple(createSeq(copy.get(0)),
                createSeq(copy.get(1)),
                createSeq(copy.get(2)));

    }

    @Override
    @SuppressWarnings("unchecked")
    public Tuple4<ReactiveSeq<T>, ReactiveSeq<T>, ReactiveSeq<T>, ReactiveSeq<T>> quadruplicate() {
        Iterable<T> sourceIt = new OperatorToIterable<T,T>(source);
        ListX<SpliteratorToOperator<T>> copy = Streams.toBufferingCopier(sourceIt, 4)
                .map(it->new SpliteratorToOperator<>(it.spliterator()));

        return Tuple.tuple(createSeq(copy.get(0)),
                            createSeq(copy.get(1)),
                            createSeq(copy.get(2)),
                            createSeq(copy.get(3)));
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
        return grouped(Integer.MAX_VALUE)
                .flatMapI(s->s.cycle(times));

    }

    
    

    
}
