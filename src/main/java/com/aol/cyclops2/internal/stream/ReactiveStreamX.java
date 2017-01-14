package com.aol.cyclops2.internal.stream;

import com.aol.cyclops2.internal.stream.publisher.PublisherIterable;
import com.aol.cyclops2.internal.stream.spliterators.*;
import com.aol.cyclops2.internal.stream.spliterators.push.*;
import com.aol.cyclops2.types.FoldableTraversable;
import com.aol.cyclops2.types.stream.CyclopsCollectable;
import com.aol.cyclops2.types.stream.HotStream;
import com.aol.cyclops2.types.stream.reactive.ReactiveSubscriber;
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


@AllArgsConstructor
public class ReactiveStreamX<T> extends BaseExtendedStream<T> {

    final Operator<T> source;

    @Override
    public ReactiveSeq<T> reverse() {
        return coflatMap(s->ReactiveSeq.reversedListOf(s.toList()))
                .flatMap(i->i);
    }


    @Override
    <X> ReactiveSeq<X> createSeq(Stream<X> stream, Optional<ReversableSpliterator> reversible, Optional<PushingSpliterator<?>> split) {
        return new ReactiveStreamX<X>(stream,reversible,split);
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
        return createSeq(new ConcatonatingSpliterator<>(new SingleSpliterator<U>(seed),
                new ScanLeftSpliterator<T,U>( source,
                        seed,function)),reversible,this.split);


    }

    @Override
    public final ReactiveSeq<T> scanRight(final Monoid<T> monoid) {
        return reverse().scanLeft(monoid.zero(), (u, t) -> monoid.apply(t, u));
    }

    @Override
    public final <U> ReactiveSeq<U> scanRight(final U identity, final BiFunction<? super T, ? super U, ? extends U> combiner) {
        return reverse().scanLeft(identity,(u,t)->combiner.apply(t,u));

    }

    @Override
    public final ReactiveSeq<T> sorted() {
        return createSeq(unwrapStream().sorted(),);
    }

    @Override
    public final ReactiveSeq<T> sorted(final Comparator<? super T> c) {
        final Supplier<TreeSet<T>> supplier =  () -> new TreeSet<T>(c);
        return coflatMap(r-> r.collect(Collectors.toCollection(supplier))  )
                .flatMap(col->col.stream());

    }



    @Override
    public ReactiveSeq<T> skip(final long num) {
        
        
        return createSeq(new SkipSpliterator<>( source,num),);
    }

    @Override
    public final ReactiveSeq<T> skipWhile(final Predicate<? super T> p) {
        return createSeq(new SkipWhileSpliterator<T>( source,p),);
    }

    @Override
    public final ReactiveSeq<T> skipUntil(final Predicate<? super T> p) {
        return skipWhile(p.negate());
    }

    @Override
    public ReactiveSeq<T> limit(final long num) {
        
       
        return createSeq(new LimitOperator<T>( source,num),);
    }

    @Override
    public final ReactiveSeq<T> limitWhile(final Predicate<? super T> p) {
        return createSeq(new LimitWhileOperator<T>( source, p),);
    }

    @Override
    public final ReactiveSeq<T> limitUntil(final Predicate<? super T> p) {
        return limitWhile(p.negate());
    }

    @Override
    public final ReactiveSeq<T> parallel() {
        return this;
    }

    @Override
    public ReactiveSeq<T> skipWhileClosed(Predicate<? super T> predicate) {
        return createSeq(new SkipWhileSpliterator<T>( source,predicate),reversible,split );
    }

    @Override
    public ReactiveSeq<T> limitWhileClosed(Predicate<? super T> predicate) {
        return createSeq(new LimitWhileClosedSpliterator<T>( source,predicate),reversible,split);
    }

    @Override
    public <U> ReactiveSeq<T> sorted(Function<? super T, ? extends U> function, Comparator<? super U> comparator) {
        return sorted(Comparator.comparing(function, comparator));

    }
    @Override
    public final <R> ReactiveSeq<R> map(final Function<? super T, ? extends R> fn) {

        if(this.stream instanceof ComposableFunction){
            ComposableFunction f = (ComposableFunction)stream;
            return createSeq(f.compose(fn),reversible,split);
        }
        return createSeq(new MappingSpliterator<T,R>(this. source,fn),);
    }

    @Override
    public final ReactiveSeq<T> peek(final Consumer<? super T> c) {
        return map(i->{c.accept(i); return i;});
    }

    @Override
    public final <R> ReactiveSeq<R> flatMap(final Function<? super T, ? extends Stream<? extends R>> fn) {
        if(this.stream instanceof FunctionSpliterator){
            FunctionSpliterator f = (FunctionSpliterator)stream;
            return createSeq(StreamFlatMappingSpliterator.compose(f,fn),reversible,split);
        }
        return createSeq(new StreamFlatMappingSpliterator<>( source,fn), Optional.empty(),split);

    }

    @Override
    public final <R> ReactiveSeq<R> flatMapAnyM(final Function<? super T, AnyM<Witness.stream,? extends R>> fn) {
        return createSeq(Streams.flatMapAnyM(this, fn),);
    }

    @Override
    public final <R> ReactiveSeq<R> flatMapI(final Function<? super T, ? extends Iterable<? extends R>> fn) {
        if(this.stream instanceof FunctionSpliterator){
            FunctionSpliterator f = (FunctionSpliterator)stream;
            return createSeq(IterableFlatMappingSpliterator.compose(f,fn),reversible,split);
        }

        return createSeq(new IterableFlatMappingSpliterator<>( source,fn), Optional.empty(),split);

    }
    @Override
    public final <R> ReactiveSeq<R> flatMapP(final Function<? super T, ? extends Publisher<? extends R>> fn) {
        if(this.stream instanceof FunctionSpliterator){
            FunctionSpliterator f = (FunctionSpliterator)stream;
            return createSeq(PublisherFlatMappingSpliterator.compose(f,fn),reversible,split);
        }
        return createSeq(new PublisherFlatMappingSpliterator<>( source,fn), Optional.empty(),split);
    }

    @Override
    public final <R> ReactiveSeq<R> flatMapStream(final Function<? super T, BaseStream<? extends R, ?>> fn) {
        return createSeq(Streams.flatMapStream(this, fn),);

    }

    public final <R> ReactiveSeq<R> flatMapOptional(final Function<? super T, Optional<? extends R>> fn) {
        return createSeq(Streams.flatMapOptional(this, fn),);

    }

    public final <R> ReactiveSeq<R> flatMapCompletableFuture(final Function<? super T, CompletableFuture<? extends R>> fn) {
        return createSeq(Streams.flatMapCompletableFuture(this, fn),);
    }

    public final ReactiveSeq<Character> flatMapCharSequence(final Function<? super T, CharSequence> fn) {
        return createSeq(Streams.flatMapCharSequence(this, fn),);
    }

    public final ReactiveSeq<String> flatMapFile(final Function<? super T, File> fn) {
        return createSeq(Streams.flatMapFile(this, fn),);
    }

    public final ReactiveSeq<String> flatMapURL(final Function<? super T, URL> fn) {
        return createSeq(Streams.flatMapURL(this, fn),);
    }

    public final ReactiveSeq<String> flatMapBufferedReader(final Function<? super T, BufferedReader> fn) {
        return createSeq(Streams.flatMapBufferedReader(this, fn),);
    }
    @Override
    public final ReactiveSeq<T> filter(final Predicate<? super T> fn) {
        return createSeq(new FilteringSpliterator<T>( source,fn).compose(),);

    }

    public final ReactiveSeq<T> filterLazyPredicate(final Supplier<Predicate<? super T>> fn) {
        return createSeq(new LazyFilteringSpliterator<T>( source,fn),);

    }



    @Override
    public void forEach(final Consumer<? super T> action) {
        this. source.forEachRemaining(action);

    }
    @Override
    public long count() {

        long[] result = {0};
        stream.forEachRemaining(t -> result[0]++);
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
        new PublisherIterable<>(this).subscribe(sub);
    }



    @Override
    public ReactiveSeq<T> onEmpty(final T value) {
        return createSeq(new OnEmptySpliterator<>(stream,value));

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
        return createSeq(new OnEmptyGetSpliterator<>(stream,supplier));
    }

    @Override
    public <X extends Throwable> ReactiveSeq<T> onEmptyThrow(final Supplier<? extends X> supplier) {

        return createSeq(new OnEmptyThrowSpliterator<>(stream,supplier));
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
        return createSeq(new DistinctKeySpliterator<>(keyExtractor,stream),reversible,split);
    }

    @Override
    public ReactiveSeq<ListX<T>> groupedBySizeAndTime(final int size, final long time, final TimeUnit t) {
        return createSeq(new GroupedByTimeAndSizeSpliterator(this. source,()->ListX.fromIterable(new ArrayList<>(size)),
                        Function.identity(),size,time,t),
               );

    }

    @Override
    public ReactiveSeq<ListX<T>> groupedByTime(final long time, final TimeUnit t) {
        return createSeq(new GroupedByTimeSpliterator<>( source,
                ()->ListX.fromIterable(new ArrayList<>(100)),
                Function.identity(),time, t),);
    }
    @Override
    public ReactiveSeq<T> skip(final long time, final TimeUnit unit) {
        return createSeq(new SkipWhileTimeSpliterator<T>( source, time, unit), this.reversible,split);
    }

    @Override
    public ReactiveSeq<T> limit(final long time, final TimeUnit unit) {
        return createSeq(new LimitWhileTimeSpliterator<T>( source,time,unit),reversible,split);

    }
    @Override
    public ReactiveSeq<ListX<T>> groupedUntil(final Predicate<? super T> predicate) {
        return groupedWhile(predicate.negate());

    }

    @Override
    public ReactiveSeq<ListX<T>> groupedWhile(final Predicate<? super T> predicate) {
        return createSeq(new GroupedWhileSpliterator<>( source,()->ListX.of(),Function.identity(), predicate), this.reversible,split);


    }

    @Override
    public <C extends Collection<? super T>> ReactiveSeq<C> groupedWhile(final Predicate<? super T> predicate, final Supplier<C> factory) {
        return createSeq(new GroupedWhileSpliterator<>( source,factory,Function.identity(), predicate), this.reversible,split);
    }

    @Override
    public <C extends Collection<? super T>> ReactiveSeq<C> groupedUntil(final Predicate<? super T> predicate, final Supplier<C> factory) {
        return groupedWhile(predicate.negate(),factory);
    }

    @Override
    public <C extends Collection<? super T>> ReactiveSeq<C> groupedBySizeAndTime(final int size, final long time, final TimeUnit unit,
                                                                                 final Supplier<C> factory) {
        return createSeq(new GroupedByTimeAndSizeSpliterator(this. source,factory,
                        Function.identity(),size,time,unit),
               );

    }

    @Override
    public <C extends Collection<? super T>,R> ReactiveSeq<R> groupedBySizeAndTime(final int size, final long time,
                                                                                   final TimeUnit unit,
                                                                                   final Supplier<C> factory,
                                                                                   Function<? super C, ? extends R> finalizer
    ) {
        return createSeq(new GroupedByTimeAndSizeSpliterator(this. source,factory,
                        finalizer,size,time,unit),
               );

    }
    @Override
    public <C extends Collection<? super T>,R> ReactiveSeq<R> groupedByTime(final long time, final TimeUnit unit, final Supplier<C> factory, Function<? super C, ? extends R> finalizer) {
        return createSeq(new GroupedByTimeSpliterator(this. source,factory,
                        finalizer,time,unit),
               );

    }
    @Override
    public <C extends Collection<? super T>> ReactiveSeq<C> groupedByTime(final long time, final TimeUnit unit, final Supplier<C> factory) {
        return createSeq(new GroupedByTimeSpliterator(this. source,factory,
                        Function.identity(),time,unit),
               );

    }

    @Override
    public <C extends Collection<? super T>> ReactiveSeq<C> grouped(final int size, final Supplier<C> factory) {
        return createSeq(new GroupingSpliterator<>( source,factory, Function.identity(),size), this.reversible,split);

    }

    @Override
    public ReactiveSeq<T> skipLast(final int num) {
        return createSeq(SkipLastSpliterator.skipLast( source, num), this.reversible,split);
    }

    @Override
    public ReactiveSeq<T> limitLast(final int num) {
        return createSeq(LimitLastSpliterator.limitLast( source, num), this.reversible,split);
    }

    @Override
    public ReactiveSeq<T> recover(final Function<? super Throwable, ? extends T> fn) {
        return createSeq(new RecoverSpliterator<T,Throwable>( source,fn,Throwable.class), this.reversible,split);
    }

    @Override
    public <EX extends Throwable> ReactiveSeq<T> recover(final Class<EX> exceptionClass, final Function<? super EX, ? extends T> fn) {
        return createSeq(new RecoverSpliterator<T,EX>( source,fn,exceptionClass), this.reversible,split);
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
        return ReactiveSeq.fill(1)
                          .flatMap(i -> createSeq( source));;

    }

    @Override
    public Tuple2<ReactiveSeq<T>, ReactiveSeq<T>> duplicate() {
        Iterable<T> sourceIt = new OperatorToIterable<T,T>(source);
        Tuple2<Iterable<T>, Iterable<T>> copy = Streams.toBufferingDuplicator(() -> Spliterators.iterator( source));
        return copy.map((a,b)->Tuple.tuple(createSeq(new IteratableSpliterator<>(a)),createSeq(new IteratableSpliterator<>(b))));

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

        final Tuple3<Spliterator<T>, Spliterator<T>, Spliterator<T>> tuple = Tuple.tuple( source, source, source);
        return tuple.map1(s -> createSeq(s, reversible.map(r -> r. source),split))
                .map2(s -> createSeq(s, reversible.map(r -> r. source),split))
                .map3(s -> createSeq(s, reversible.map(r -> r. source),split));

    }

    @Override
    @SuppressWarnings("unchecked")
    public Tuple4<ReactiveSeq<T>, ReactiveSeq<T>, ReactiveSeq<T>, ReactiveSeq<T>> quadruplicate() {
        final Tuple4<Spliterator<T>, Spliterator<T>, Spliterator<T>, Spliterator<T>> tuple = Tuple.tuple( source, source, source, source);
        return tuple.map1(s -> createSeq(s, reversible.map(r -> r. source),split))
                .map2(s -> createSeq(s, reversible.map(r -> r. source),split))
                .map3(s -> createSeq(s, reversible.map(r -> r. source),split))
                .map4(s -> createSeq(s, reversible.map(r -> r. source),split));
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
