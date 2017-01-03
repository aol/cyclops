package com.aol.cyclops.internal.stream;


import com.aol.cyclops.internal.stream.spliterators.push.CollectingSinkSpliterator;
import com.aol.cyclops.internal.stream.spliterators.push.FoldingSinkSpliterator;
import com.aol.cyclops.internal.stream.spliterators.push.PushingSpliterator;
import com.aol.cyclops.internal.stream.spliterators.push.ValueEmittingSpliterator;
import com.aol.cyclops.util.ExceptionSoftener;
import cyclops.*;
import com.aol.cyclops.data.collections.extensions.CollectionX;
import cyclops.collections.ListX;
import com.aol.cyclops.internal.stream.publisher.PublisherIterable;
import com.aol.cyclops.internal.stream.spliterators.*;
import com.aol.cyclops.types.FoldableTraversable;
import com.aol.cyclops.types.Unwrapable;
import com.aol.cyclops.types.anyM.AnyMSeq;
import cyclops.collections.immutable.PVectorX;
import cyclops.control.Eval;
import cyclops.function.Fn3;
import cyclops.function.Fn4;
import cyclops.monads.Witness;
import com.aol.cyclops.types.stream.CyclopsCollectable;
import com.aol.cyclops.types.stream.HeadAndTail;
import com.aol.cyclops.types.stream.HotStream;
import com.aol.cyclops.types.stream.PausableHotStream;
import cyclops.async.Future;
import cyclops.function.Monoid;
import cyclops.function.Reducer;
import cyclops.monads.AnyM;
import cyclops.stream.ReactiveSeq;
import cyclops.stream.Streamable;
import lombok.AllArgsConstructor;
import org.jooq.lambda.Collectable;
import org.jooq.lambda.Seq;
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
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;
import java.util.function.*;
import java.util.stream.*;

import static java.util.Comparator.comparing;
/*
 * Note on Organization
 * Composite operators (those that work by composing calls to existing operators) should move to ReactiveSeq
 * Shared operators should be defined here
 * Specific operators in the specific base class
 *
 */
public abstract class BaseExtendedStream<T> implements Unwrapable, ReactiveSeq<T>, Iterable<T> {

    final Spliterator<T> stream;
    final Optional<PushingSpliterator<?>> split; //should be an Xor3 type here
    final Optional<ReversableSpliterator> reversible;

    public BaseExtendedStream(final Stream<T> stream) {

        this.stream = unwrapStream().spliterator();
        this.reversible = Optional.empty();
        this.split = Optional.empty();

    }
    public BaseExtendedStream(final Spliterator<T> stream, final Optional<ReversableSpliterator> rev, Optional<PushingSpliterator<?>> split) {
        this.stream = stream;
        this.reversible = rev;
        this.split = split;

    }
    public BaseExtendedStream(final Stream<T> stream, final Optional<ReversableSpliterator> rev, Optional<PushingSpliterator<?>> split) {
        this.stream = stream.spliterator();
        this.reversible = rev;
        this.split = split;

    }
    @Override
    public Iterator<T> iterator(){

        if(!this.split.isPresent())
            return Spliterators.iterator(copyOrGet());
        //Iterator for push streams
        Spliterator<T> split = copyOrGet();
        class QueueingIterator implements Iterator<T>,Consumer<T>{

            boolean available;
            ArrayDeque<T> qd = new ArrayDeque<>();
            @Override
            public void accept(T t) {
               

                qd.offer(t);

                available = true;
                    
                
            }

            @Override
            public boolean hasNext() {
                if(!available)
                    split.tryAdvance(this);
                return available;
            }

            @Override
            public T next() {
                if (!available && !hasNext())
                    throw new NoSuchElementException();
                else {
                    available = qd.size()-1>0;
                    return qd.pop();

                }
            }
            
        }
        return new QueueingIterator();
    }
    
    public  <A,R> ReactiveSeq<R> collectSeq(Collector<? super T,A,R> c){
        Spliterator<T> s = this.spliterator();
        CollectingSinkSpliterator<T,A,R> fs = new CollectingSinkSpliterator<T,A,R>(s.estimateSize(), s.characteristics(), s,c);
        split.ifPresent(p->{p.setOnComplete(fs);p.setHold(false);});
        return createSeq(new ValueEmittingSpliterator<R>(1, s.characteristics(),createSeq(fs)));


    }


    
    public ReactiveSeq<T> fold(Monoid<T> monoid){
        Spliterator<T> s = this.spliterator();
        FoldingSinkSpliterator<T> fs = new FoldingSinkSpliterator<>(s.estimateSize(), s.characteristics(), s, monoid);
        split.ifPresent(p->{p.setOnComplete(fs);p.setHold(false);});
        
        return createSeq(new ValueEmittingSpliterator<T>(1, s.characteristics(),createSeq(fs)));
    }
    public <R> Future<R> foldFuture(Function<? super FoldableTraversable<T>,? extends R> fn, Executor ex){
        split.ifPresent(p->p.setHold(true));
        split.ifPresent(p->p.setOnComplete(()->p.setHold(false)));
        return Future.ofSupplier(()->{
            
            return fn.apply(this);
        },ex);
    }
    public <R> Eval<R> foldLazy(Function<? super CyclopsCollectable<T>,? extends R> fn,Executor ex){
        split.ifPresent(p->p.setHold(true));
        split.ifPresent(p->p.setOnComplete(()->p.setHold(false)));
        return Eval.later(()->fn.apply(this));
    }
    
    @Override
    public <U> U reduce(final U identity, final BiFunction<U, ? super T, U> accumulator) {
        return seq().foldLeft(identity, accumulator);

    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Unit#unit(java.lang.Object)
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
    @Deprecated
    public final <R> R unwrap() {
        return (R) this;
    }

   


    public final Stream<T> unwrapStream() {

        return StreamSupport.stream(copyOrGet(),false);

    }




    @Override
    public ReactiveSeq<T> cycle(final Monoid<T> m, final long times) {
        return ReactiveSeq.of(m.reduce(this)).cycle(times);

    }

    @Override
    public ReactiveSeq<T> cycleWhile(final Predicate<? super T> predicate) {

        return cycle().limitWhile(predicate);
    }

    @Override
    public ReactiveSeq<T> cycleUntil(final Predicate<? super T> predicate) {
        return cycleWhile(predicate.negate());
    }

    @Override
    public final <S> ReactiveSeq<Tuple2<T, S>> zipS(final Stream<? extends S> second) {
        return createSeq( new ZippingSpliterator<>(copyOrGet(),second.spliterator(),(a, b) -> new Tuple2<>(
                                                        a, b)));
    }
    @Override
    public final <U, R> ReactiveSeq<R> zipS(final Stream<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper){
        return createSeq( new ZippingSpliterator<>(copyOrGet(),other.spliterator(),zipper));
    }
    @Override
    public final <S, U,R> ReactiveSeq<R> zip3(final Iterable<? extends S> second, final Iterable<? extends U> third,
                                                          final Fn3<? super T, ? super S, ? super U,? extends R> fn3) {
        return createSeq( new Zipping3Spliterator<>(copyOrGet(),second.spliterator(),third.spliterator(),fn3));
    }

    @Override
    public <S, U> ReactiveSeq<Tuple3<T, S, U>> zip3(Iterable<? extends S> second, Iterable<? extends U> third) {
        return createSeq( new Zipping3Spliterator<>(copyOrGet(),second.spliterator(),third.spliterator(),(a,b,c)->Tuple.tuple(a,b,c)));
    }


    @Override
    public <T2, T3, T4, R> ReactiveSeq<R> zip4(Iterable<? extends T2> second, Iterable<? extends T3> third, Iterable<? extends T4> fourth, Fn4<? super T, ? super T2, ? super T3, ? super T4, ? extends R> fn) {
        return createSeq( new Zipping4Spliterator<>(copyOrGet(),second.spliterator(),third.spliterator(),fourth.spliterator(),fn));

    }

    @Override
    public final <T2, T3, T4> ReactiveSeq<Tuple4<T, T2, T3, T4>> zip4(final Iterable<? extends T2> second, final Iterable<? extends T3> third,
            final Iterable<? extends T4> fourth) {
        return zip4(second,third,fourth,Tuple::tuple);

    }


    @Override
    public final ReactiveSeq<PVectorX<T>> sliding(final int windowSize) {
        return sliding(windowSize,1);
     }

    @Override
    public final ReactiveSeq<PVectorX<T>> sliding(final int windowSize, final int increment) {
        return createSeq(new SlidingSpliterator<>(copyOrGet(),Function.identity(), windowSize,increment), reversible,split);
    }

    @Override
    public final ReactiveSeq<ListX<T>> grouped(final int groupSize) {
        return createSeq(new GroupingSpliterator<T,List<T>,ListX<T>>(copyOrGet(),()->new ArrayList(groupSize),c->ListX.fromIterable(c),groupSize), this.reversible,split);

    }
    @Override
    public ReactiveSeq<ListX<T>> groupedStatefullyWhile(final BiPredicate<ListX<? super T>, ? super T> predicate) {
        return createSeq(new GroupedStatefullySpliterator<>(copyOrGet(),()->ListX.of(),Function.identity(), predicate), this.reversible,split);
    }
    @Override
    public <C extends Collection<T>,R> ReactiveSeq<R> groupedStatefullyWhile(final BiPredicate<C, ? super T> predicate, final Supplier<C> factory,
                                                                             Function<? super C, ? extends R> finalizer) {
        return this.<R>createSeq(new GroupedStatefullySpliterator<T,C,R>(copyOrGet(),factory,finalizer, predicate), this.reversible,split);
    }
    @Override
    public ReactiveSeq<ListX<T>> groupedStatefullyUntil(final BiPredicate<ListX<? super T>, ? super T> predicate) {
        return createSeq(new GroupedStatefullySpliterator<>(copyOrGet(),()->ListX.of(),Function.identity(), predicate.negate()), this.reversible,split);
    }
    @Override
    public <C extends Collection<T>,R> ReactiveSeq<R> groupedStatefullyUntil(final BiPredicate<C, ? super T> predicate, final Supplier<C> factory,
                                                        Function<? super C, ? extends R> finalizer) {
        return this.<R>createSeq(new GroupedStatefullySpliterator<T,C,R>(copyOrGet(),factory,finalizer, predicate.negate()), this.reversible,split);
    }

    @Override
    public final ReactiveSeq<T> distinct() {
        return createSeq(new DistinctSpliterator<T,T>(copyOrGet()), reversible,split);
    }

    @Override
    public final ReactiveSeq<T> scanLeft(final Monoid<T> monoid) {
         return scanLeft(monoid.zero(),monoid);

    }

    @Override
    public final <U> ReactiveSeq<U> scanLeft(final U seed, final BiFunction<? super U, ? super T, ? extends U> function) {

        return createSeq(ReactiveSeq.concat(ReactiveSeq.of(seed), StreamSupport.stream(new ScanLeftSpliterator<T,U>(copyOrGet(),
                                        seed,function),false)),reversible,this.split);


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
        return createSeq(unwrapStream().sorted(), reversible,split);
    }

    @Override
    public final ReactiveSeq<T> sorted(final Comparator<? super T> c) {
        final Supplier<TreeSet<T>> supplier =  () -> new TreeSet<T>(c);
        return coflatMap(r-> r.collect(Collectors.toCollection(supplier))  )
                .flatMap(col->col.stream());

    }

    @Override
   public <R> ReactiveSeq<R> coflatMap(Function<? super ReactiveSeq<T>, ? extends R> fn){
        //coflatMap lazily reconstructs the stream
        return ReactiveSeq.fromSpliterator(new LazySingleSpliterator<T,Supplier<ReactiveSeq<T>>,R>(()->createSeq(copyOrGet(),reversible,split),in->fn.apply(in.get())));

    }

    @Override
    public final ReactiveSeq<T> skip(final long num) {
        return createSeq(new SkipSpliterator<>(copyOrGet(),num), reversible,split);
    }

    @Override
    public final ReactiveSeq<T> skipWhile(final Predicate<? super T> p) {
        return createSeq(new SkipWhileSpliterator<T>(copyOrGet(),p), reversible,split);
    }

    @Override
    public final ReactiveSeq<T> skipUntil(final Predicate<? super T> p) {
        return skipWhile(p.negate());
    }

    @Override
    public final ReactiveSeq<T> limit(final long num) {
        return createSeq(new LimitSpliterator<T>(copyOrGet(),num), reversible,split);
    }

    @Override
    public final ReactiveSeq<T> limitWhile(final Predicate<? super T> p) {
        return createSeq(new LimitWhileSpliterator<T>(copyOrGet(), p), reversible,split);
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
    public final boolean allMatch(final Predicate<? super T> c) {
        return unwrapStream().allMatch(c);
    }

    @Override
    public final boolean anyMatch(final Predicate<? super T> c) {
        return unwrapStream().anyMatch(c);
    }

    @Override
    public boolean xMatch(final int num, final Predicate<? super T> c) {
        return Streams.xMatch(this, num, c);
    }

    @Override
    public final boolean noneMatch(final Predicate<? super T> c) {
        return unwrapStream().allMatch(c.negate());
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
    public final <U extends Comparable<? super U>> Optional<T> minBy(final Function<? super T, ? extends U> function) {

        return Streams.minBy(this, function);
    }

    @Override
    public final Optional<T> min(final Comparator<? super T> comparator) {
        return Streams.min(this, comparator);
    }




    @Override
    public ReactiveSeq<T> skipWhileClosed(Predicate<? super T> predicate) {
        return createSeq(new SkipWhileSpliterator<T>(copyOrGet(),predicate),reversible,split );
    }

    @Override
    public ReactiveSeq<T> limitWhileClosed(Predicate<? super T> predicate) {
        return createSeq(new LimitWhileClosedSpliterator<T>(copyOrGet(),predicate),reversible,split);
    }

    @Override
    public <U> ReactiveSeq<T> sorted(Function<? super T, ? extends U> function, Comparator<? super U> comparator) {
        return sorted(Comparator.comparing(function, comparator));

    }

    @Override
    public final <C extends Comparable<? super C>> Optional<T> maxBy(final Function<? super T, ? extends C> f) {
        return Streams.maxBy(this, f);
    }

    @Override
    public final Optional<T> max(final Comparator<? super T> comparator) {
        return Streams.max(this, comparator);
    }

    @Override
    public final HeadAndTail<T> headAndTail() {
        return Streams.headAndTail(this);
    }

    @Override
    public final Optional<T> findFirst() {

       try {
           //use forEachRemaining as it is the fast path for many operators
           stream.forEachRemaining(e -> {

               throw new FoundValueException(e);
           });
       }catch(FoundValueException t){
           return Optional.of((T)t.value);
       }

       return Optional.empty();
    }
    @AllArgsConstructor
    private static class FoundValueException extends RuntimeException{
       Object value;

        @Override
        public Throwable fillInStackTrace() {
            return this;
        }
    }

    @Override
    public final Optional<T> findAny() {
        return findFirst();
    }

    @Override
    public final <R> R mapReduce(final Reducer<R> reducer) {
        return reducer.mapReduce(unwrapStream());
    }

    @Override
    public final <R> R mapReduce(final Function<? super T, ? extends R> mapper, final Monoid<R> reducer) {
        return reducer.reduce(unwrapStream().map(mapper));
    }

    @Override
    public final <R, A> R collect(final Collector<? super T, A, R> collector) {
       return unwrapStream().collect(collector);


    }

    @Override
    public final <R> R collect(final Supplier<R> supplier, final BiConsumer<R, ? super T> accumulator, final BiConsumer<R, R> combiner) {
        return unwrapStream().collect(supplier, accumulator, combiner);
    }

    @Override
    public final T reduce(final Monoid<T> reducer) {

        return reducer.reduce(this);
    }

    @Override
    public final Optional<T> reduce(final BinaryOperator<T> accumulator) {
        Object[] result = {null};
        stream.forEachRemaining(e->{
            if(result[0]==null)
                result[0]=e;
            else{
                result[0] = accumulator.apply((T)result[0],e);
            }
        });
       return result[0]==null? Optional.empty() : Optional.of((T)result[0]);
    }

    @Override
    public final T reduce(final T identity, final BinaryOperator<T> accumulator) {
       Object[] result = {identity};
       stream.forEachRemaining(e->{
           result[0] = accumulator.apply((T)result[0],e);
       });
        return (T)result[0];
    }

    @Override
    public final <U> U reduce(final U identity, final BiFunction<U, ? super T, U> accumulator, final BinaryOperator<U> combiner) {
        return unwrapStream().reduce(identity, accumulator, combiner);
    }

    @Override
    public final ListX<T> reduce(final Stream<? extends Monoid<T>> reducers) {
        return Streams.reduce(this, reducers);
    }

    @Override
    public final ListX<T> reduce(final Iterable<? extends Monoid<T>> reducers) {
        return Streams.reduce(this, reducers);
    }

    public final T foldLeft(final Monoid<T> reducer) {
        return reduce(reducer);
    }

    public final T foldLeft(final T identity, final BinaryOperator<T> accumulator) {
        return unwrapStream().reduce(identity, accumulator);
    }

    public final <T> T foldLeftMapToType(final Reducer<T> reducer) {
        return reducer.mapReduce(unwrapStream());
    }

    @Override
    public final T foldRight(final Monoid<T> reducer) {
        return reducer.reduce(reverse());
    }

    @Override
    public final <U> U foldRight(final U seed, final BiFunction<? super T, ? super U, ? extends U> function) {
        return reverse().foldLeft(seed, (u,t)->function.apply(t, u));
                
    }

    @Override
    public final <T> T foldRightMapToType(final Reducer<T> reducer) {
        return reducer.mapReduce(reverse());
    }

    @Override
    public final Streamable<T> toStreamable() {
        return Streamable.fromStream(stream());
    }

    @Override
    public final Set<T> toSet() {
        return unwrapStream().collect(Collectors.toSet());
    }

    @Override
    public final List<T> toList() {

        return unwrapStream().collect(Collectors.toList());
    }

    @Override
    public final <C extends Collection<T>> C toCollection(final Supplier<C> collectionFactory) {

        return unwrapStream().collect(Collectors.toCollection(collectionFactory));
    }

    @Override
    public final <T> Stream<T> toStream() {
        return (Stream<T>) this.unwrapStream();
    }

    @Override
    public final ReactiveSeq<T> stream() {
        return this;

    }

    @Override
    public final boolean startsWithIterable(final Iterable<T> iterable) {
        return Streams.startsWith(this, iterable);

    }

    @Override
    public final boolean startsWith(final Stream<T> stream2) {
        return Streams.startsWith(this, stream2);

    }

    @Override
    public AnyMSeq<Witness.reactiveSeq,T> anyM() {
        return AnyM.fromStream(this);

    }

    @Override
    public final <R> ReactiveSeq<R> map(final Function<? super T, ? extends R> fn) {

        if(this.stream instanceof ComposableFunction){
            ComposableFunction f = (ComposableFunction)stream;
            return createSeq(f.compose(fn),reversible,split);
        }
        return createSeq(new MappingSpliterator<T,R>(this.copyOrGet(),fn), reversible,split);
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
        return createSeq(new StreamFlatMappingSpliterator<>(copyOrGet(),fn), Optional.empty(),split);

    }

    @Override
    public final <R> ReactiveSeq<R> flatMapAnyM(final Function<? super T, AnyM<Witness.stream,? extends R>> fn) {
        return createSeq(Streams.flatMapAnyM(this, fn), reversible,split);
    }

    @Override
    public final <R> ReactiveSeq<R> flatMapI(final Function<? super T, ? extends Iterable<? extends R>> fn) {
        if(this.stream instanceof FunctionSpliterator){
            FunctionSpliterator f = (FunctionSpliterator)stream;
            return createSeq(IterableFlatMappingSpliterator.compose(f,fn),reversible,split);
        }

        return createSeq(new IterableFlatMappingSpliterator<>(copyOrGet(),fn), Optional.empty(),split);

    }
    @Override
    public final <R> ReactiveSeq<R> flatMapP(final Function<? super T, ? extends Publisher<? extends R>> fn) {
        if(this.stream instanceof FunctionSpliterator){
            FunctionSpliterator f = (FunctionSpliterator)stream;
            return createSeq(PublisherFlatMappingSpliterator.compose(f,fn),reversible,split);
        }
        return createSeq(new PublisherFlatMappingSpliterator<>(copyOrGet(),fn), Optional.empty(),split);
   }

    @Override
    public final <R> ReactiveSeq<R> flatMapStream(final Function<? super T, BaseStream<? extends R, ?>> fn) {
        return createSeq(Streams.flatMapStream(this, fn), reversible,split);

    }

    public final <R> ReactiveSeq<R> flatMapOptional(final Function<? super T, Optional<? extends R>> fn) {
        return createSeq(Streams.flatMapOptional(this, fn), reversible,split);

    }

    public final <R> ReactiveSeq<R> flatMapCompletableFuture(final Function<? super T, CompletableFuture<? extends R>> fn) {
        return createSeq(Streams.flatMapCompletableFuture(this, fn), reversible,split);
    }

    public final ReactiveSeq<Character> flatMapCharSequence(final Function<? super T, CharSequence> fn) {
        return createSeq(Streams.flatMapCharSequence(this, fn), reversible,split);
    }

    public final ReactiveSeq<String> flatMapFile(final Function<? super T, File> fn) {
        return createSeq(Streams.flatMapFile(this, fn), reversible,split);
    }

    public final ReactiveSeq<String> flatMapURL(final Function<? super T, URL> fn) {
        return createSeq(Streams.flatMapURL(this, fn), reversible,split);
    }

    public final ReactiveSeq<String> flatMapBufferedReader(final Function<? super T, BufferedReader> fn) {
        return createSeq(Streams.flatMapBufferedReader(this, fn), reversible,split);
    }

    abstract <X> ReactiveSeq<X> createSeq(Stream<X> stream,Optional<ReversableSpliterator> reversible,
                                          Optional<PushingSpliterator<?>> split);
    abstract <X> ReactiveSeq<X> createSeq(Spliterator<X> stream,Optional<ReversableSpliterator> reversible,
                                          Optional<PushingSpliterator<?>> split);
     <X> ReactiveSeq<X> createSeq(Spliterator<X> stream){

         return createSeq(stream, Optional.empty(), Optional.empty());

     }
    <X> ReactiveSeq<X> createSeq(Stream<X> stream){

        return createSeq(stream, Optional.empty(), Optional.empty());

    }
    @Override
    public final ReactiveSeq<T> filter(final Predicate<? super T> fn) {
        return createSeq(new FilteringSpliterator<T>(copyOrGet(),fn).compose(), reversible,split);

    }

    public final ReactiveSeq<T> filterLazyPredicate(final Supplier<Predicate<? super T>> fn) {
        return createSeq(new LazyFilteringSpliterator<T>(copyOrGet(),fn), reversible,split);

    }



    @Override
    public void forEach(final Consumer<? super T> action) {
        this.stream.forEachRemaining(action);

    }

   
    
    @Override
    public Spliterator<T> spliterator() {
        return stream;
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
    public long count() {

        long[] result = {0};
        stream.forEachRemaining(t -> result[0]++);
        return result[0];


    }

    @Override
    public ReactiveSeq<T> intersperse(final T value) {

        return flatMap(t -> Stream.of(value, t)).skip(1l);

    }



    @Override
    public CollectionX<T> toLazyCollection() {
        return Streams.toLazyCollection(this);
    }

    @Override
    public CollectionX<T> toConcurrentLazyCollection() {
        return Streams.toConcurrentLazyCollection(this);
    }

    public Streamable<T> toLazyStreamable() {
        return Streams.toLazyStreamable(this);
    }

    @Override
    public Streamable<T> toConcurrentLazyStreamable() {
        return Streams.toConcurrentLazyStreamable(this);

    }


    @Override
    public ReactiveSeq<T> onClose(final Runnable closeHandler) {

        return this;
    }

    @Override
    public void close() {

    }

    @Override
    public ReactiveSeq<T> shuffle() {
        return coflatMap(r->{ List<T> list = r.toList(); Collections.shuffle(list); return list;})
                .flatMap(c->c.stream());

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
        return ReactiveSeq.concat(s.v1,ReactiveSeq.fromStream(stream),s.v2);

    }



    @Override
    public boolean endsWithIterable(final Iterable<T> iterable) {
        return Streams.endsWith(this, iterable);
    }

    @Override
    public HotStream<T> hotStream(final Executor e) {
        return Streams.hotStream(this, e);
    }

    @Override
    public T firstValue() {
        return Streams.firstValue(unwrapStream());
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
        })).peek(System.out::println);
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
        return ReactiveSeq.concat(copyOrGet(),other.spliterator());
    }
    public ReactiveSeq<T> append(final Iterable<? extends T> other) {
        return ReactiveSeq.concat(copyOrGet(),other.spliterator());
    }

    //TODO use spliterators and createSeq
    @Override
    public ReactiveSeq<T> append(final T other) {
        return ReactiveSeq.concat(unwrapStream(),Stream.of(other));
    }

    @Override
    public ReactiveSeq<T> append(final T... other) {
        return ReactiveSeq.concat(unwrapStream(),Stream.of(other));
    }
    @Override
    public ReactiveSeq<T> prependS(final Stream<? extends T> other) {
        return ReactiveSeq.concat(other,unwrapStream());
    }
    public ReactiveSeq<T> prepend(final Iterable<? extends T> other) {
        return ReactiveSeq.concat(StreamSupport.stream(other.spliterator(),false),unwrapStream());
    }

    @Override
    public ReactiveSeq<T> prepend(final T other) {
        return ReactiveSeq.concat(Stream.of(other),unwrapStream());
    }

    @Override
    public ReactiveSeq<T> prepend(final T... other) {
        return ReactiveSeq.concat(Stream.of(other),unwrapStream());
    }


    @Override
    public <U> ReactiveSeq<T> distinct(final Function<? super T, ? extends U> keyExtractor) {
        return createSeq(new DistinctKeySpliterator<>(keyExtractor,stream),reversible,split);
    }



    @Override
    public ReactiveSeq<T> shuffle(final Random random) {
        return coflatMap(r->{ List<T> list = r.toList(); Collections.shuffle(list,random); return list;})
                .flatMap(c->c.stream());

    }

    @Override
    public ReactiveSeq<T> slice(final long from, final long to) {

        return skip(Math.max(from, 0)).limit(Math.max(to - Math.max(from, 0), 0));

    }

    @Override
    public <U extends Comparable<? super U>> ReactiveSeq<T> sorted(final Function<? super T, ? extends U> function) {
        return createSeq(sorted(Comparator.comparing(function)),
                reversible,split);
    }

    @Override
    public ReactiveSeq<T> xPer(final int x, final long time, final TimeUnit t) {
        final long next = t.toNanos(time);
        Supplier<Function<? super T, ? extends T>> lazy = ()-> {

            long[] last = {-1};
            int[] count = {0};
            return a-> {
                if (++count[0] < x)
                    return a;
                count[0] = 0;
                final long sleepFor = next - (System.nanoTime() - last[0]);

                LockSupport.parkNanos(sleepFor);

                last[0] = System.nanoTime();
                return a;
            };
        };
        return mapLazyFn(lazy);

    }


    public  <R> ReactiveSeq<R> mapLazyFn(Supplier<Function<? super T, ? extends R>> fn){
        //not composable to the 'left' (as statefulness is lost)
        return createSeq(new LazyMappingSpliterator<T,R>(this.copyOrGet(),fn), reversible,split);

    }

    @Override
    public ReactiveSeq<T> onePer(final long time, final TimeUnit t) {
        final long next = t.toNanos(time);
        Supplier<Function<? super T, ? extends T>> lazy = ()-> {

            long[] last = {-1};
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
        final long next = t.toNanos(time);
        Supplier<Predicate<? super T>> lazy = ()-> {
            final long[] last = {0};
            long[] elapsedNanos = {1};
            return a-> {

                T nextValue = null;
                if (elapsedNanos[0] > 0) {


                    if (last[0] == 0) {
                        last[0] = System.nanoTime();
                        return true;
                    }
                    elapsedNanos[0] = timeNanos - (System.nanoTime() - last[0]);
                }

                return false;
            };
        };
        return filterLazyPredicate(lazy);

    }

    @Override
    public ReactiveSeq<ListX<T>> groupedBySizeAndTime(final int size, final long time, final TimeUnit t) {
        return createSeq(new GroupedByTimeAndSizeSpliterator(this.copyOrGet(),()->ListX.fromIterable(new ArrayList<>(size)),
                        Function.identity(),size,time,t),
                reversible,split);

    }

    @Override
    public ReactiveSeq<ListX<T>> groupedByTime(final long time, final TimeUnit t) {
        return createSeq(new GroupedByTimeSpliterator<>(copyOrGet(),
                ()->ListX.fromIterable(new ArrayList<>(100)),
                Function.identity(),time, t), reversible,split);
    }

    @Override
    public T foldRight(final T identity, final BinaryOperator<T> accumulator) {
        return reverse().foldLeft(identity, accumulator);
    }

    @Override
    public boolean endsWith(final Stream<T> iterable) {
        return Streams.endsWith(this, () -> iterable.iterator());
    }

    @Override
    public ReactiveSeq<T> skip(final long time, final TimeUnit unit) {
        return createSeq(new SkipWhileTimeSpliterator<T>(copyOrGet(), time, unit), this.reversible,split);
    }

    @Override
    public ReactiveSeq<T> limit(final long time, final TimeUnit unit) {
        return createSeq(new LimitWhileTimeSpliterator<T>(copyOrGet(),time,unit),reversible,split);

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
        //return createSeq(Streams.fixedDelay(this, l, unit), this.reversible,split);
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

       // return createSeq(Streams.jitter(this, l), this.reversible,split);
    }

    @Override
    public ReactiveSeq<ListX<T>> groupedUntil(final Predicate<? super T> predicate) {
        return groupedWhile(predicate.negate());

    }

    @Override
    public ReactiveSeq<ListX<T>> groupedWhile(final Predicate<? super T> predicate) {
        return createSeq(new GroupedWhileSpliterator<>(copyOrGet(),()->ListX.of(),Function.identity(), predicate), this.reversible,split);


    }

    @Override
    public <C extends Collection<? super T>> ReactiveSeq<C> groupedWhile(final Predicate<? super T> predicate, final Supplier<C> factory) {
        return createSeq(new GroupedWhileSpliterator<>(copyOrGet(),factory,Function.identity(), predicate), this.reversible,split);
    }

    @Override
    public <C extends Collection<? super T>> ReactiveSeq<C> groupedUntil(final Predicate<? super T> predicate, final Supplier<C> factory) {
      return groupedWhile(predicate.negate(),factory);
    }

    @Override
    public <C extends Collection<? super T>> ReactiveSeq<C> groupedBySizeAndTime(final int size, final long time, final TimeUnit unit,
            final Supplier<C> factory) {
        return createSeq(new GroupedByTimeAndSizeSpliterator(this.copyOrGet(),factory,
                        Function.identity(),size,time,unit),
                reversible,split);

    }

    @Override
    public <C extends Collection<? super T>,R> ReactiveSeq<R> groupedBySizeAndTime(final int size, final long time,
                                                                                 final TimeUnit unit,
                                                                                 final Supplier<C> factory,
                                                                                 Function<? super C, ? extends R> finalizer
    ) {
        return createSeq(new GroupedByTimeAndSizeSpliterator(this.copyOrGet(),factory,
                        finalizer,size,time,unit),
                reversible,split);

    }
    @Override
    public <C extends Collection<? super T>,R> ReactiveSeq<R> groupedByTime(final long time, final TimeUnit unit, final Supplier<C> factory, Function<? super C, ? extends R> finalizer) {
        return createSeq(new GroupedByTimeSpliterator(this.copyOrGet(),factory,
                        finalizer,time,unit),
                reversible,split);

    }
    @Override
    public <C extends Collection<? super T>> ReactiveSeq<C> groupedByTime(final long time, final TimeUnit unit, final Supplier<C> factory) {
        return createSeq(new GroupedByTimeSpliterator(this.copyOrGet(),factory,
                        Function.identity(),time,unit),
                reversible,split);

    }

    @Override
    public <C extends Collection<? super T>> ReactiveSeq<C> grouped(final int size, final Supplier<C> factory) {
        return createSeq(new GroupingSpliterator<>(copyOrGet(),factory, Function.identity(),size), this.reversible,split);

    }

    @Override
    public ReactiveSeq<T> skipLast(final int num) {
        return createSeq(SkipLastSpliterator.skipLast(copyOrGet(), num), this.reversible,split);
    }

    @Override
    public ReactiveSeq<T> limitLast(final int num) {
        return createSeq(LimitLastSpliterator.limitLast(copyOrGet(), num), this.reversible,split);
    }

    @Override
    public ReactiveSeq<T> recover(final Function<? super Throwable, ? extends T> fn) {
        return createSeq(new RecoverSpliterator<T,Throwable>(copyOrGet(),fn,Throwable.class), this.reversible,split);
    }

    @Override
    public <EX extends Throwable> ReactiveSeq<T> recover(final Class<EX> exceptionClass, final Function<? super EX, ? extends T> fn) {
        return createSeq(new RecoverSpliterator<T,EX>(copyOrGet(),fn,exceptionClass), this.reversible,split);
    }
    

  
 
    

    @Override
    public <X extends Throwable> Subscription forEach(final long numberOfElements, final Consumer<? super T> consumer) {
        return Streams.forEachX(this, numberOfElements, consumer);
    }

    @Override
    public <X extends Throwable> Subscription forEach(final long numberOfElements, final Consumer<? super T> consumer,
                                                      final Consumer<? super Throwable> consumerError) {
        this.split.ifPresent(s->{
            s.setError(consumerError);
        });
        return Streams.forEachXWithError(this, numberOfElements, consumer, consumerError);
    }

    @Override
    public <X extends Throwable> Subscription forEach(final long numberOfElements, final Consumer<? super T> consumer,
                                                      final Consumer<? super Throwable> consumerError, final Runnable onComplete) {
        this.split.ifPresent(s->{
            s.setError(consumerError);
            s.setOnComplete(onComplete);
        });
        return Streams.forEachXEvents(this, numberOfElements, consumer, consumerError, onComplete);
    }

    @Override
    public <X extends Throwable> void forEach(final Consumer<? super T> consumerElement, final Consumer<? super Throwable> consumerError) {
        this.split.ifPresent(s->{
            s.setHold(false);
            s.setError(consumerError);
        });

        new ForEachWithError<T>(this.copyOrGet(),consumerError).forEachRemaining(consumerElement);


    }

    @Override
    public <X extends Throwable> void forEach(final Consumer<? super T> consumerElement, final Consumer<? super Throwable> consumerError,
                                              final Runnable onComplete) {
        this.split.ifPresent(s->{
            s.setHold(false);
            s.setError(consumerError);
        });

        new ForEachWithError<T>(this.copyOrGet(),consumerError,onComplete).forEachRemaining(consumerElement);
    }

    @Override
    public HotStream<T> primedHotStream(final Executor e) {
        return Streams.primedHotStream(this, e);
    }

    @Override
    public PausableHotStream<T> pausableHotStream(final Executor e) {
        return Streams.pausableHotStream(this, e);
    }

    @Override
    public PausableHotStream<T> primedPausableHotStream(final Executor e) {
        return Streams.primedPausableHotStream(this, e);
    }

    @Override
    public String format() {
        return Seq.seq(this.copyOrGet())
                  .format();
    }

    @Override
    public Collectable<T> collectable() {
        return Seq.seq(copyOrGet());
    }

    @Override
    public <T> ReactiveSeq<T> unitIterator(final Iterator<T> it) {
        return ReactiveSeq.fromIterator(it);
    }


    Spliterator<T> copyOrGet() {
        return CopyableSpliterator.copy(stream);
    }
}
