package com.aol.cyclops.internal.stream;


import cyclops.Monoid;
import cyclops.Reducer;
import com.aol.cyclops.control.*;
import com.aol.cyclops.data.collections.extensions.CollectionX;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.internal.stream.publisher.PublisherIterable;
import com.aol.cyclops.internal.stream.spliterators.*;
import com.aol.cyclops.types.FoldableTraversable;
import com.aol.cyclops.types.Unwrapable;
import com.aol.cyclops.types.anyM.AnyMSeq;
import com.aol.cyclops.types.anyM.Witness;
import com.aol.cyclops.types.stream.CyclopsCollectable;
import com.aol.cyclops.types.stream.HeadAndTail;
import com.aol.cyclops.types.stream.HotStream;
import com.aol.cyclops.types.stream.PausableHotStream;
import org.jooq.lambda.Collectable;
import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;
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
import java.util.stream.*;

public class ReactiveSeqImpl<T> implements Unwrapable, ReactiveSeq<T>, Iterable<T> {

    private final Spliterator<T> stream;
    private final Optional<PushingSpliterator<?>> split; //should be an Xor3 type here
    private final Optional<ReversableSpliterator> reversible;

    public ReactiveSeqImpl(final Stream<T> stream) {

        this.stream = unwrapStream().spliterator();
        this.reversible = Optional.empty();
        this.split = Optional.empty();

    }

    public ReactiveSeqImpl(final Stream<T> stream, final Optional<ReversableSpliterator> rev, Optional<PushingSpliterator<?>> split) {
        this.stream = stream.spliterator();
        this.reversible = rev;
        this.split = split;

    }
    @Override
    public Iterator<T> iterator(){
        if(!this.split.isPresent())
            return StreamSupport.stream(stream,false).iterator();
        //Iterator for push streams
        Spliterator<T> split = stream;
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
        return ReactiveSeq.fromSpliterator(new ValueEmittingSpliterator<R>(1, s.characteristics(),ReactiveSeq.fromSpliterator(fs)));


    }


    
    public ReactiveSeq<T> fold(Monoid<T> monoid){
        Spliterator<T> s = this.spliterator();
        FoldingSinkSpliterator<T> fs = new FoldingSinkSpliterator<>(s.estimateSize(), s.characteristics(), s, monoid);
        split.ifPresent(p->{p.setOnComplete(fs);p.setHold(false);});
        
        return ReactiveSeq.fromSpliterator(new ValueEmittingSpliterator<T>(1, s.characteristics(),ReactiveSeq.fromSpliterator(fs)));
    }
    public <R> FutureW<R> foldFuture(Function<? super FoldableTraversable<T>,? extends R> fn, Executor ex){
        split.ifPresent(p->p.setHold(true));
        split.ifPresent(p->p.setOnComplete(()->p.setHold(false)));
        return FutureW.ofSupplier(()->{
            
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
        return StreamUtils.schedule(this, cron, ex);

    }

    @Override
    public HotStream<T> scheduleFixedDelay(final long delay, final ScheduledExecutorService ex) {
        return StreamUtils.scheduleFixedDelay(this, delay, ex);
    }

    @Override
    public HotStream<T> scheduleFixedRate(final long rate, final ScheduledExecutorService ex) {
        return StreamUtils.scheduleFixedRate(this, rate, ex);

    }

    @Override
    @Deprecated
    public final <R> R unwrap() {
        return (R) this;
    }

   
    public final <T> ReactiveSeq<T> flatten(ReactiveSeq<ReactiveSeq<T>> s) {
        return s.flatMap(Function.identity());

    }

    public final Stream<T> unwrapStream() {

        return StreamSupport.stream(stream,false);

    }

    @Override
    public final ReactiveSeq<T> cycle(final int times) {
        return StreamUtils.reactiveSeq(StreamUtils.cycle(times, Streamable.fromStream(unwrapStream())), reversible,split);
    }

    @Override
    public final ReactiveSeq<T> cycle() {
        return StreamUtils.reactiveSeq(StreamUtils.cycle(unwrapStream()), reversible,split);
    }

    @Override
    public final Tuple2<ReactiveSeq<T>, ReactiveSeq<T>> duplicate() {
        final Tuple2<Stream<T>, Stream<T>> tuple = StreamUtils.duplicate(unwrapStream());
        return tuple.map1(s -> StreamUtils.reactiveSeq(s, reversible.map(r -> r.copy()),split))
                    .map2(s -> StreamUtils.reactiveSeq(s, reversible.map(r -> r.copy()),split));
    }

    @Override
    @SuppressWarnings("unchecked")
    public final Tuple3<ReactiveSeq<T>, ReactiveSeq<T>, ReactiveSeq<T>> triplicate() {

        final Tuple3<Stream<T>, Stream<T>, Stream<T>> tuple = StreamUtils.triplicate(unwrapStream());
        return tuple.map1(s -> StreamUtils.reactiveSeq(s, reversible.map(r -> r.copy()),split))
                    .map2(s -> StreamUtils.reactiveSeq(s, reversible.map(r -> r.copy()),split))
                    .map3(s -> StreamUtils.reactiveSeq(s, reversible.map(r -> r.copy()),split));

    }

    @Override
    @SuppressWarnings("unchecked")
    public final Tuple4<ReactiveSeq<T>, ReactiveSeq<T>, ReactiveSeq<T>, ReactiveSeq<T>> quadruplicate() {
        final Tuple4<Stream<T>, Stream<T>, Stream<T>, Stream<T>> tuple = StreamUtils.quadruplicate(unwrapStream());
        return tuple.map1(s -> StreamUtils.reactiveSeq(s, reversible.map(r -> r.copy()),split))
                    .map2(s -> StreamUtils.reactiveSeq(s, reversible.map(r -> r.copy()),split))
                    .map3(s -> StreamUtils.reactiveSeq(s, reversible.map(r -> r.copy()),split))
                    .map4(s -> StreamUtils.reactiveSeq(s, reversible.map(r -> r.copy()),split));
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
        return StreamUtils.splitAt(this, where)
                          .map1(s -> StreamUtils.reactiveSeq(s, reversible.map(r -> r.copy()),split))
                          .map2(s -> StreamUtils.reactiveSeq(s, reversible.map(r -> r.copy()),split));

    }

    @Override
    public final Tuple2<ReactiveSeq<T>, ReactiveSeq<T>> splitBy(final Predicate<T> splitter) {
        return StreamUtils.splitBy(this, splitter)
                          .map1(s -> StreamUtils.reactiveSeq(s, reversible.map(r -> r.copy()),split))
                          .map2(s -> StreamUtils.reactiveSeq(s, reversible.map(r -> r.copy()),split));
    }

    @Override
    public final Tuple2<ReactiveSeq<T>, ReactiveSeq<T>> partition(final Predicate<? super T> splitter) {
        return StreamUtils.partition(this, splitter)
                          .map1(s -> StreamUtils.reactiveSeq(s, reversible.map(r -> r.copy()),split))
                          .map2(s -> StreamUtils.reactiveSeq(s, reversible.map(r -> r.copy()),split));
    }

    @Override
    public final ReactiveSeq<T> cycle(final Monoid<T> m, final int times) {
        return StreamUtils.reactiveSeq(StreamUtils.cycle(times, Streamable.of(m.reduce(unwrapStream()))), reversible,split);

    }

    @Override
    public final ReactiveSeq<T> cycleWhile(final Predicate<? super T> predicate) {

        return StreamUtils.reactiveSeq(StreamUtils.cycle(unwrapStream()), reversible,split)
                          .limitWhile(predicate);
    }

    @Override
    public final ReactiveSeq<T> cycleUntil(final Predicate<? super T> predicate) {
        return StreamUtils.reactiveSeq(StreamUtils.cycle(unwrapStream()), reversible,split)
                          .limitWhile(predicate.negate());
    }

    @Override
    public final <S> ReactiveSeq<Tuple2<T, S>> zipS(final Stream<? extends S> second) {
        return ReactiveSeq.fromSpliterator( new ZippingSpliterator<>(stream,second.spliterator(),(a, b) -> new Tuple2<>(
                                                        a, b)));
    }

    @Override
    public final <S, U> ReactiveSeq<Tuple3<T, S, U>> zip3(final Iterable<? extends S> second, final Iterable<? extends U> third) {
        return zip(second).zip(third)
                          .map(p -> new Tuple3(
                                               p.v1()
                                                .v1(),
                                               p.v1()
                                                .v2(),
                                               p.v2()));

    }

    @Override
    public final <T2, T3, T4> ReactiveSeq<Tuple4<T, T2, T3, T4>> zip4(final Iterable<? extends T2> second, final Iterable<? extends T3> third,
            final Iterable<? extends T4> fourth) {
        return zip3(second, third).zip(fourth)
                                  .map(t -> new Tuple4(
                                                       t.v1()
                                                        .v1(),
                                                       t.v1()
                                                        .v2(),
                                                       t.v1()
                                                        .v3(),
                                                       t.v2()));

    }


    @Override
    public final ReactiveSeq<ListX<T>> sliding(final int windowSize) {
        return StreamUtils.reactiveSeq(StreamUtils.sliding(this, windowSize), reversible,split);
    }

    @Override
    public final ReactiveSeq<ListX<T>> sliding(final int windowSize, final int increment) {
        return StreamUtils.reactiveSeq(StreamUtils.sliding(this, windowSize, increment), reversible,split);
    }

    @Override
    public final ReactiveSeq<ListX<T>> grouped(final int groupSize) {
        return StreamUtils.reactiveSeq(StreamUtils.batchBySize(this, groupSize), reversible,split);
    }

    @Override
    public ReactiveSeq<ListX<T>> groupedStatefullyUntil(final BiPredicate<ListX<? super T>, ? super T> predicate) {
        return StreamUtils.reactiveSeq(StreamUtils.groupedStatefullyUntil(this, predicate), this.reversible,split);
    }
/**
    @Override
    public final <K> MapX<K, ListX<T>> groupBy(final Function<? super T, ? extends K> classifier) {
        return MapX.fromMap(collect(Collectors.groupingBy(classifier)));
    }
**/
    @Override
    public final ReactiveSeq<T> distinct() {
        return StreamUtils.reactiveSeq(unwrapStream().distinct(), reversible,split);
    }

    @Override
    public final ReactiveSeq<T> scanLeft(final Monoid<T> monoid) {
         return scanLeft(monoid.zero(),monoid);

    }

    @Override
    public final <U> ReactiveSeq<U> scanLeft(final U seed, final BiFunction<? super U, ? super T, ? extends U> function) {

        return StreamUtils.reactiveSeq(Stream.concat(Stream.of(seed), StreamSupport.stream(new ScanLeftSpliterator<T,U>(stream,
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
        return StreamUtils.reactiveSeq(unwrapStream().sorted(), reversible,split);
    }

    @Override
    public final ReactiveSeq<T> sorted(final Comparator<? super T> c) {
        return StreamUtils.reactiveSeq(unwrapStream().sorted(c), reversible,split);
    }

    @Override
    public final ReactiveSeq<T> skip(final long num) {
        return StreamUtils.reactiveSeq(unwrapStream().skip(num), reversible,split);
    }

    @Override
    public final ReactiveSeq<T> skipWhile(final Predicate<? super T> p) {
        return StreamUtils.reactiveSeq(StreamUtils.skipWhile(this, p), reversible,split);
    }

    @Override
    public final ReactiveSeq<T> skipUntil(final Predicate<? super T> p) {
        return StreamUtils.reactiveSeq(StreamUtils.skipUntil(this, p), reversible,split);
    }

    @Override
    public final ReactiveSeq<T> limit(final long num) {
        return StreamUtils.reactiveSeq(unwrapStream().limit(num), reversible,split);
    }

    @Override
    public final ReactiveSeq<T> limitWhile(final Predicate<? super T> p) {
        return StreamUtils.reactiveSeq(StreamUtils.limitWhile(this, p), reversible,split);
    }

    @Override
    public final ReactiveSeq<T> limitUntil(final Predicate<? super T> p) {
        return StreamUtils.reactiveSeq(StreamUtils.limitUntil(this, p), reversible,split);
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
        return StreamUtils.xMatch(this, num, c);
    }

    @Override
    public final boolean noneMatch(final Predicate<? super T> c) {
        return unwrapStream().allMatch(c.negate());
    }

    @Override
    public final String join() {
        return StreamUtils.join(this, "");
    }

    @Override
    public final String join(final String sep) {
        return StreamUtils.join(this, sep);
    }

    @Override
    public final String join(final String sep, final String start, final String end) {
        return StreamUtils.join(this, sep, start, end);
    }

    @Override
    public final <U extends Comparable<? super U>> Optional<T> minBy(final Function<? super T, ? extends U> function) {

        return StreamUtils.minBy(this, function);
    }

    @Override
    public final Optional<T> min(final Comparator<? super T> comparator) {
        return StreamUtils.min(this, comparator);
    }

    @Override
    public ReactiveSeq<T> cycle(long times) {

        return StreamUtils.reactiveSeq(Seq.seq((Stream<T>) this).cycle(times),reversible,split );
    }

    @Override
    public ReactiveSeq<T> skipWhileClosed(Predicate<? super T> predicate) {
        return StreamUtils.reactiveSeq(Seq.seq((Stream<T>) this).skipWhileClosed(predicate),reversible,split );
    }

    @Override
    public ReactiveSeq<T> limitWhileClosed(Predicate<? super T> predicate) {
        return StreamUtils.reactiveSeq(Seq.seq((Stream<T>) this).limitWhileClosed(predicate),reversible,split );
    }

    @Override
    public <U> ReactiveSeq<T> sorted(Function<? super T, ? extends U> function, Comparator<? super U> comparator) {
        return StreamUtils.reactiveSeq(Seq.seq((Stream<T>) this).sorted(function,comparator),reversible,split );
    }

    @Override
    public final <C extends Comparable<? super C>> Optional<T> maxBy(final Function<? super T, ? extends C> f) {
        return StreamUtils.maxBy(this, f);
    }

    @Override
    public final Optional<T> max(final Comparator<? super T> comparator) {
        return StreamUtils.max(this, comparator);
    }

    @Override
    public final HeadAndTail<T> headAndTail() {
        return StreamUtils.headAndTail(unwrapStream());
    }

    @Override
    public final Optional<T> findFirst() {
        return unwrapStream().findFirst();
    }

    @Override
    public final Optional<T> findAny() {
        return unwrapStream().findAny();
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

        return reducer.reduce(unwrapStream());
    }

    @Override
    public final Optional<T> reduce(final BinaryOperator<T> accumulator) {
        return unwrapStream().reduce(accumulator);
    }

    @Override
    public final T reduce(final T identity, final BinaryOperator<T> accumulator) {
        return unwrapStream().reduce(identity, accumulator);
    }

    @Override
    public final <U> U reduce(final U identity, final BiFunction<U, ? super T, U> accumulator, final BinaryOperator<U> combiner) {
        return unwrapStream().reduce(identity, accumulator, combiner);
    }

    @Override
    public final ListX<T> reduce(final Stream<? extends Monoid<T>> reducers) {
        return StreamUtils.reduce(this, reducers);
    }

    @Override
    public final ListX<T> reduce(final Iterable<? extends Monoid<T>> reducers) {
        return StreamUtils.reduce(this, reducers);
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
        return StreamUtils.startsWith(this, iterable);

    }

    @Override
    public final boolean startsWith(final Stream<T> stream2) {
        return StreamUtils.startsWith(this, stream2);

    }

    @Override
    public AnyMSeq<Witness.stream,T> anyM() {
        return AnyM.fromStream(unwrapStream());

    }

    @Override
    public final <R> ReactiveSeq<R> map(final Function<? super T, ? extends R> fn) {
        return new ReactiveSeqImpl(
                                   unwrapStream().map(fn), reversible,split);
    }

    @Override
    public final ReactiveSeq<T> peek(final Consumer<? super T> c) {
        return new ReactiveSeqImpl(
                                   unwrapStream().peek(c), reversible,split);
    }

    @Override
    public final <R> ReactiveSeq<R> flatMap(final Function<? super T, ? extends Stream<? extends R>> fn) {
        return StreamUtils.reactiveSeq(unwrapStream().flatMap(fn), reversible,split);
    }

    @Override

    public final <R> ReactiveSeq<R> flatMapAnyM(final Function<? super T, AnyM<Witness.stream,? extends R>> fn) {
        return StreamUtils.reactiveSeq(StreamUtils.flatMapAnyM(this, fn), reversible,split);
    }

    @Override
    public final <R> ReactiveSeq<R> flatMapIterable(final Function<? super T, ? extends Iterable<? extends R>> fn) {
        return StreamUtils.reactiveSeq(StreamUtils.flatMapIterable(this, fn), Optional.empty(),split);

    }

    @Override
    public final <R> ReactiveSeq<R> flatMapStream(final Function<? super T, BaseStream<? extends R, ?>> fn) {
        return StreamUtils.reactiveSeq(StreamUtils.flatMapStream(this, fn), reversible,split);

    }

    public final <R> ReactiveSeq<R> flatMapOptional(final Function<? super T, Optional<? extends R>> fn) {
        return StreamUtils.reactiveSeq(StreamUtils.flatMapOptional(this, fn), reversible,split);

    }

    public final <R> ReactiveSeq<R> flatMapCompletableFuture(final Function<? super T, CompletableFuture<? extends R>> fn) {
        return StreamUtils.reactiveSeq(StreamUtils.flatMapCompletableFuture(this, fn), reversible,split);
    }

    public final ReactiveSeq<Character> flatMapCharSequence(final Function<? super T, CharSequence> fn) {
        return StreamUtils.reactiveSeq(StreamUtils.flatMapCharSequence(this, fn), reversible,split);
    }

    public final ReactiveSeq<String> flatMapFile(final Function<? super T, File> fn) {
        return StreamUtils.reactiveSeq(StreamUtils.flatMapFile(this, fn), reversible,split);
    }

    public final ReactiveSeq<String> flatMapURL(final Function<? super T, URL> fn) {
        return StreamUtils.reactiveSeq(StreamUtils.flatMapURL(this, fn), reversible,split);
    }

    public final ReactiveSeq<String> flatMapBufferedReader(final Function<? super T, BufferedReader> fn) {
        return StreamUtils.reactiveSeq(StreamUtils.flatMapBufferedReader(this, fn), reversible,split);
    }

    @Override
    public final ReactiveSeq<T> filter(final Predicate<? super T> fn) {
        return StreamUtils.reactiveSeq(unwrapStream().filter(fn), reversible,split);
    }

    @Override
    public void forEach(final Consumer<? super T> action) {
        unwrapStream().forEach(action);

    }

   
    
    @Override
    public Spliterator<T> spliterator() {
        return unwrapStream().spliterator();
    }

    @Override
    public boolean isParallel() {
        return unwrapStream().isParallel();
    }

    @Override
    public ReactiveSeq<T> sequential() {
        return StreamUtils.reactiveSeq(unwrapStream().sequential(), reversible,split);
    }

    @Override
    public ReactiveSeq<T> unordered() {
        return StreamUtils.reactiveSeq(unwrapStream().unordered(), reversible,split);
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
        return unwrapStream().count();
    }

    @Override
    public ReactiveSeq<T> intersperse(final T value) {

        return StreamUtils.reactiveSeq(unwrapStream().flatMap(t -> Stream.of(value, t))
                                             .skip(1l),
                reversible,split);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <U> ReactiveSeq<U> ofType(final Class<? extends U> type) {
        return StreamUtils.reactiveSeq(StreamUtils.ofType(this, type), reversible,split);
    }

    @Override
    public <U> ReactiveSeq<U> cast(final Class<? extends U> type) {
        return StreamUtils.reactiveSeq(StreamUtils.cast(this, type), reversible,split);
    }

    @Override
    public CollectionX<T> toLazyCollection() {
        return StreamUtils.toLazyCollection(unwrapStream());
    }

    @Override
    public CollectionX<T> toConcurrentLazyCollection() {
        return StreamUtils.toConcurrentLazyCollection(unwrapStream());
    }

    public Streamable<T> toLazyStreamable() {
        return StreamUtils.toLazyStreamable(unwrapStream());
    }

    @Override
    public Streamable<T> toConcurrentLazyStreamable() {
        return StreamUtils.toConcurrentLazyStreamable(unwrapStream());

    }

    @Override
    public ReactiveSeq<T> reverse() {
        if (reversible.isPresent()) {
            reversible.ifPresent(r -> r.invert());
            return this;
        }
        return StreamUtils.reactiveSeq(StreamUtils.reverse(unwrapStream()), reversible,split);
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
        return StreamUtils.reactiveSeq(StreamUtils.shuffle(unwrapStream())
                                                  .stream(),
                reversible,split);

    }





    @Override @SafeVarargs
    public  final ReactiveSeq<T> insertAt(final int pos, final T... values) {
        return StreamUtils.reactiveSeq(StreamUtils.insertAt(this, pos, values), Optional.empty(),split);

    }

    @Override
    public ReactiveSeq<T> deleteBetween(final int start, final int end) {
        return StreamUtils.reactiveSeq(StreamUtils.deleteBetween(this, start, end), Optional.empty(),split);
    }

    @Override
    public ReactiveSeq<T> insertStreamAt(final int pos, final Stream<T> stream) {

        return StreamUtils.reactiveSeq(StreamUtils.insertStreamAt(this, pos, stream), Optional.empty(),split);

    }



    @Override
    public boolean endsWithIterable(final Iterable<T> iterable) {
        return StreamUtils.endsWith(this, iterable);
    }

    @Override
    public HotStream<T> hotStream(final Executor e) {
        return StreamUtils.hotStream(this, e);
    }

    @Override
    public T firstValue() {
        return StreamUtils.firstValue(unwrapStream());
    }

    @Override
    public void subscribe(final Subscriber<? super T> sub) {
       new PublisherIterable<>(this).subscribe(sub);
    }



    @Override
    public ReactiveSeq<T> onEmpty(final T value) {
        return ReactiveSeq.fromSpliterator(new OnEmptySpliterator<>(unwrapStream().spliterator(),value));

    }

    @Override
    public ReactiveSeq<T> onEmptyGet(final Supplier<? extends T> supplier) {
        return ReactiveSeq.fromSpliterator(new OnEmptyGetSpliterator<>(unwrapStream().spliterator(),supplier));
    }

    @Override
    public <X extends Throwable> ReactiveSeq<T> onEmptyThrow(final Supplier<? extends X> supplier) {

        return ReactiveSeq.fromSpliterator(new OnEmptyThrowSpliterator<>(unwrapStream().spliterator(),supplier));
    }


    @Override
    public ReactiveSeq<T> appendS(final Stream<? extends T> other) {
        return StreamUtils.reactiveSeq(Stream.concat(unwrapStream(),other),
                Optional.empty(),split);
    }
    public ReactiveSeq<T> append(final Iterable<? extends T> other) {
        return StreamUtils.reactiveSeq(Stream.concat(unwrapStream(),StreamSupport.stream(other.spliterator(),false)),
                Optional.empty(),split);
    }

    @Override
    public ReactiveSeq<T> append(final T other) {
        return StreamUtils.reactiveSeq(Stream.concat(unwrapStream(),Stream.of(other)),
                                       Optional.empty(),split);
    }

    @Override
    public ReactiveSeq<T> append(final T... other) {
        return StreamUtils.reactiveSeq(Stream.concat(unwrapStream(),Stream.of(other)),
                                       Optional.empty(),split);
    }
    @Override
    public ReactiveSeq<T> prependS(final Stream<? extends T> other) {
        return StreamUtils.reactiveSeq(Stream.concat(other,unwrapStream()),
                Optional.empty(),split);
    }
    public ReactiveSeq<T> prepend(final Iterable<? extends T> other) {
        return StreamUtils.reactiveSeq(Stream.concat(StreamSupport.stream(other.spliterator(),false),unwrapStream()),
                Optional.empty(),split);
    }

    @Override
    public ReactiveSeq<T> prepend(final T other) {
        return StreamUtils.reactiveSeq(Stream.concat(Stream.of(other),unwrapStream()),
                Optional.empty(),split);
    }

    @Override
    public ReactiveSeq<T> prepend(final T... other) {
        return StreamUtils.reactiveSeq(Stream.concat(Stream.of(other),unwrapStream()),
                Optional.empty(),split);
    }


    @Override
    public <U> ReactiveSeq<T> distinct(final Function<? super T, ? extends U> keyExtractor) {
        return StreamUtils.reactiveSeq(Seq.seq(stream)
                                          .distinct(keyExtractor),
                reversible,split);
    }



    @Override
    public ReactiveSeq<T> shuffle(final Random random) {
        return StreamUtils.reactiveSeq(Seq.shuffle((Stream<T>)this,random),
                reversible,split);
    }

    @Override
    public ReactiveSeq<T> slice(final long from, final long to) {
        return StreamUtils.reactiveSeq(Seq.slice(this, from, to),
                reversible,split);
    }

    @Override
    public <U extends Comparable<? super U>> ReactiveSeq<T> sorted(final Function<? super T, ? extends U> function) {
        return StreamUtils.reactiveSeq(sorted(Comparator.comparing(function)),
                reversible,split);
    }

    @Override
    public ReactiveSeq<T> xPer(final int x, final long time, final TimeUnit t) {
        return StreamUtils.reactiveSeq(StreamUtils.xPer(this, x, time, t), reversible,split);
    }

    @Override
    public ReactiveSeq<T> onePer(final long time, final TimeUnit t) {
        return StreamUtils.reactiveSeq(StreamUtils.onePer(this, time, t), reversible,split);
    }

    @Override
    public ReactiveSeq<T> debounce(final long time, final TimeUnit t) {
        return StreamUtils.reactiveSeq(StreamUtils.debounce(this, time, t), reversible,split);
    }

    @Override
    public ReactiveSeq<ListX<T>> groupedBySizeAndTime(final int size, final long time, final TimeUnit t) {
        return StreamUtils.reactiveSeq(StreamUtils.batchBySizeAndTime(this, size, time, t), reversible,split);
    }

    @Override
    public ReactiveSeq<ListX<T>> groupedByTime(final long time, final TimeUnit t) {
        return StreamUtils.reactiveSeq(StreamUtils.batchByTime(this, time, t), reversible,split);
    }

    @Override
    public T foldRight(final T identity, final BinaryOperator<T> accumulator) {
        return reverse().foldLeft(identity, accumulator);
    }

    @Override
    public boolean endsWith(final Stream<T> iterable) {
        return StreamUtils.endsWith(this, () -> iterable.iterator());
    }

    @Override
    public ReactiveSeq<T> skip(final long time, final TimeUnit unit) {
        return StreamUtils.reactiveSeq(StreamUtils.skip(this, time, unit), this.reversible,split);
    }

    @Override
    public ReactiveSeq<T> limit(final long time, final TimeUnit unit) {
        return StreamUtils.reactiveSeq(StreamUtils.limit(this, time, unit), this.reversible,split);
    }

    @Override
    public ReactiveSeq<T> fixedDelay(final long l, final TimeUnit unit) {
        return StreamUtils.reactiveSeq(StreamUtils.fixedDelay(this, l, unit), this.reversible,split);
    }

    @Override
    public ReactiveSeq<T> jitter(final long l) {
        return StreamUtils.reactiveSeq(StreamUtils.jitter(this, l), this.reversible,split);
    }

    @Override
    public ReactiveSeq<ListX<T>> groupedUntil(final Predicate<? super T> predicate) {
        return StreamUtils.reactiveSeq(StreamUtils.batchUntil(this, predicate), this.reversible,split);
    }

    @Override
    public ReactiveSeq<ListX<T>> groupedWhile(final Predicate<? super T> predicate) {
        return StreamUtils.reactiveSeq(StreamUtils.batchWhile(this, predicate), this.reversible,split);
    }

    @Override
    public <C extends Collection<? super T>> ReactiveSeq<C> groupedWhile(final Predicate<? super T> predicate, final Supplier<C> factory) {
        return StreamUtils.reactiveSeq(StreamUtils.batchWhile(this, predicate, factory), this.reversible,split);
    }

    @Override
    public <C extends Collection<? super T>> ReactiveSeq<C> groupedUntil(final Predicate<? super T> predicate, final Supplier<C> factory) {
        return StreamUtils.reactiveSeq(StreamUtils.batchWhile(this, predicate.negate(), factory), this.reversible,split);
    }

    @Override
    public <C extends Collection<? super T>> ReactiveSeq<C> groupedBySizeAndTime(final int size, final long time, final TimeUnit unit,
            final Supplier<C> factory) {
        return StreamUtils.reactiveSeq(StreamUtils.batchBySizeAndTime(this, size, time, unit, factory), this.reversible,split);

    }

    @Override
    public <C extends Collection<? super T>> ReactiveSeq<C> groupedByTime(final long time, final TimeUnit unit, final Supplier<C> factory) {
        return StreamUtils.reactiveSeq(StreamUtils.batchByTime(this, time, unit, factory), this.reversible,split);
    }

    @Override
    public <C extends Collection<? super T>> ReactiveSeq<C> grouped(final int size, final Supplier<C> factory) {
        return StreamUtils.reactiveSeq(StreamUtils.batchBySize(this, size, factory), this.reversible,split);

    }

    @Override
    public ReactiveSeq<T> skipLast(final int num) {
        return StreamUtils.reactiveSeq(StreamUtils.skipLast(this, num), this.reversible,split);
    }

    @Override
    public ReactiveSeq<T> limitLast(final int num) {
        return StreamUtils.reactiveSeq(StreamUtils.limitLast(this, num), this.reversible,split);
    }

    @Override
    public ReactiveSeq<T> recover(final Function<Throwable, ? extends T> fn) {
        return StreamUtils.reactiveSeq(StreamUtils.recover(this, fn), this.reversible,split);
    }

    @Override
    public <EX extends Throwable> ReactiveSeq<T> recover(final Class<EX> exceptionClass, final Function<EX, ? extends T> fn) {
        return StreamUtils.reactiveSeq(StreamUtils.recover(this, exceptionClass, fn), this.reversible,split);
    }
    

  
 
    

    @Override
    public <X extends Throwable> Subscription forEachX(final long numberOfElements, final Consumer<? super T> consumer) {
        return StreamUtils.forEachX(this, numberOfElements, consumer);
    }

    @Override
    public <X extends Throwable> Subscription forEachXWithError(final long numberOfElements, final Consumer<? super T> consumer,
            final Consumer<? super Throwable> consumerError) {
        this.split.ifPresent(s->{
            s.setError(consumerError);
        });
        return StreamUtils.forEachXWithError(this, numberOfElements, consumer, consumerError);
    }

    @Override
    public <X extends Throwable> Subscription forEachXEvents(final long numberOfElements, final Consumer<? super T> consumer,
            final Consumer<? super Throwable> consumerError, final Runnable onComplete) {
        this.split.ifPresent(s->{
            s.setError(consumerError);
            s.setOnComplete(onComplete);
        });
        return StreamUtils.forEachXEvents(this, numberOfElements, consumer, consumerError, onComplete);
    }

    @Override
    public <X extends Throwable> void forEachWithError(final Consumer<? super T> consumerElement, final Consumer<? super Throwable> consumerError) {
        this.split.ifPresent(s->{
            s.setError(consumerError);
        });
        StreamUtils.forEachWithError(this, consumerElement, consumerError);
    }

    @Override
    public <X extends Throwable> void forEachEvent(final Consumer<? super T> consumerElement, final Consumer<? super Throwable> consumerError,
            final Runnable onComplete) {
        this.split.ifPresent(s->{
            s.setError(consumerError);
            s.setOnComplete(onComplete);
        });
        StreamUtils.forEachEvent(this, consumerElement, consumerError, onComplete);
    }

    @Override
    public HotStream<T> primedHotStream(final Executor e) {
        return StreamUtils.primedHotStream(this, e);
    }

    @Override
    public PausableHotStream<T> pausableHotStream(final Executor e) {
        return StreamUtils.pausableHotStream(this, e);
    }

    @Override
    public PausableHotStream<T> primedPausableHotStream(final Executor e) {
        return StreamUtils.primedPausableHotStream(this, e);
    }

    @Override
    public String format() {
        return Seq.seq(this.stream)
                  .format();
    }

    @Override
    public Collectable<T> collectable() {
        return Seq.seq(stream);
    }

    @Override
    public <T> ReactiveSeq<T> unitIterator(final Iterator<T> it) {
        return ReactiveSeq.fromIterator(it);
    }




}
