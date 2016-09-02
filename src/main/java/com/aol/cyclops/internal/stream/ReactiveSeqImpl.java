package com.aol.cyclops.internal.stream;

import java.io.BufferedReader;
import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.Spliterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.stream.BaseStream;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import org.jooq.lambda.Collectable;
import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import com.aol.cyclops.Monoid;
import com.aol.cyclops.Reducer;
import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.For;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.StreamUtils;
import com.aol.cyclops.control.Streamable;
import com.aol.cyclops.data.collections.extensions.CollectionX;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.data.collections.extensions.standard.MapX;
import com.aol.cyclops.internal.monads.ComprehenderSelector;
import com.aol.cyclops.internal.stream.spliterators.ReversableSpliterator;
import com.aol.cyclops.types.Unwrapable;
import com.aol.cyclops.types.anyM.AnyMSeq;
import com.aol.cyclops.types.stream.HeadAndTail;
import com.aol.cyclops.types.stream.HotStream;
import com.aol.cyclops.types.stream.PausableHotStream;
import com.aol.cyclops.types.stream.future.FutureOperations;

public class ReactiveSeqImpl<T> implements Unwrapable, ReactiveSeq<T>, Iterable<T> {
    private final Seq<T> stream;
    private final Optional<ReversableSpliterator> reversable;

    public ReactiveSeqImpl(Stream<T> stream) {

        this.stream = Seq.seq(stream);
        this.reversable = Optional.empty();

    }

    public ReactiveSeqImpl(Stream<T> stream, ReversableSpliterator rev) {
        this.stream = Seq.seq(stream);
        this.reversable = Optional.of(rev);

    }

    public <U> U reduce(U identity, BiFunction<U, ? super T, U> accumulator) {
        return stream.foldLeft(identity, accumulator);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Unit#unit(java.lang.Object)
     */
    @Override
    public <T> ReactiveSeq<T> unit(T unit) {
        return ReactiveSeq.of(unit);
    }

    public HotStream<T> schedule(String cron, ScheduledExecutorService ex) {
        return StreamUtils.schedule(this, cron, ex);

    }

    public HotStream<T> scheduleFixedDelay(long delay, ScheduledExecutorService ex) {
        return StreamUtils.scheduleFixedDelay(this, delay, ex);
    }

    public HotStream<T> scheduleFixedRate(long rate, ScheduledExecutorService ex) {
        return StreamUtils.scheduleFixedRate(this, rate, ex);

    }

    @Deprecated
    public final <R> R unwrap() {
        return (R) this;
    }

    
    public final <T1> ReactiveSeq<T1> flatten() {
        return StreamUtils.flatten(stream);

    }

    
    public final Stream<T> unwrapStream() {

        return stream;

    }

    
    public final ReactiveSeq<T> cycle(int times) {
        return StreamUtils.reactiveSeq(StreamUtils.cycle(times, Streamable.fromStream(stream)), reversable);
    }

    
    public final ReactiveSeq<T> cycle() {
        return StreamUtils.reactiveSeq(StreamUtils.cycle(stream), reversable);
    }

   
    public final Tuple2<ReactiveSeq<T>, ReactiveSeq<T>> duplicateSequence() {
        Tuple2<Stream<T>, Stream<T>> tuple = StreamUtils.duplicate(stream);
        return tuple.map1(s -> StreamUtils.reactiveSeq(s, reversable.map(r -> r.copy())))
                    .map2(s -> StreamUtils.reactiveSeq(s, reversable.map(r -> r.copy())));
    }

    
    @SuppressWarnings("unchecked")
    public final Tuple3<ReactiveSeq<T>, ReactiveSeq<T>, ReactiveSeq<T>> triplicate() {

        Tuple3<Stream<T>, Stream<T>, Stream<T>> tuple = StreamUtils.triplicate(stream);
        return tuple.map1(s -> StreamUtils.reactiveSeq(s, reversable.map(r -> r.copy())))
                    .map2(s -> StreamUtils.reactiveSeq(s, reversable.map(r -> r.copy())))
                    .map3(s -> StreamUtils.reactiveSeq(s, reversable.map(r -> r.copy())));

    }

    
    @SuppressWarnings("unchecked")
    public final Tuple4<ReactiveSeq<T>, ReactiveSeq<T>, ReactiveSeq<T>, ReactiveSeq<T>> quadruplicate() {
        Tuple4<Stream<T>, Stream<T>, Stream<T>, Stream<T>> tuple = StreamUtils.quadruplicate(stream);
        return tuple.map1(s -> StreamUtils.reactiveSeq(s, reversable.map(r -> r.copy())))
                    .map2(s -> StreamUtils.reactiveSeq(s, reversable.map(r -> r.copy())))
                    .map3(s -> StreamUtils.reactiveSeq(s, reversable.map(r -> r.copy())))
                    .map4(s -> StreamUtils.reactiveSeq(s, reversable.map(r -> r.copy())));
    }

   
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public final Tuple2<Optional<T>, ReactiveSeq<T>> splitSequenceAtHead() {
        Tuple2<ReactiveSeq<T>, ReactiveSeq<T>> Tuple2 = splitAt(1);
        return new Tuple2(
                          Tuple2.v1.toOptional()
                                   .flatMap(l -> l.size() > 0 ? Optional.of(l.get(0)) : Optional.empty()),
                          Tuple2.v2);
    }

    
    public final Tuple2<ReactiveSeq<T>, ReactiveSeq<T>> splitAt(int where) {
        return StreamUtils.splitAt(stream, where)
                          .map1(s -> StreamUtils.reactiveSeq(s, reversable.map(r -> r.copy())))
                          .map2(s -> StreamUtils.reactiveSeq(s, reversable.map(r -> r.copy())));

    }

    
    public final Tuple2<ReactiveSeq<T>, ReactiveSeq<T>> splitBy(Predicate<T> splitter) {
        return StreamUtils.splitBy(stream, splitter)
                          .map1(s -> StreamUtils.reactiveSeq(s, reversable.map(r -> r.copy())))
                          .map2(s -> StreamUtils.reactiveSeq(s, reversable.map(r -> r.copy())));
    }

    
    public final Tuple2<ReactiveSeq<T>, ReactiveSeq<T>> partitionSequence(Predicate<T> splitter) {
        return StreamUtils.partition(stream, splitter)
                          .map1(s -> StreamUtils.reactiveSeq(s, reversable.map(r -> r.copy())))
                          .map2(s -> StreamUtils.reactiveSeq(s, reversable.map(r -> r.copy())));
    }

    
    public final ReactiveSeq<T> cycle(Monoid<T> m, int times) {
        return StreamUtils.reactiveSeq(StreamUtils.cycle(times, Streamable.of(m.reduce(stream))), reversable);

    }

    
    public final <R> ReactiveSeq<R> cycle(Class<R> monadC, int times) {
        return (ReactiveSeqImpl) cycle(times).map(r -> new ComprehenderSelector().selectComprehender(monadC)
                                                                                 .of(r));
    }

    public final ReactiveSeq<T> cycleWhile(Predicate<? super T> predicate) {

        return StreamUtils.reactiveSeq(StreamUtils.cycle(stream), reversable)
                          .limitWhile(predicate);
    }

    
    public final ReactiveSeq<T> cycleUntil(Predicate<? super T> predicate) {
        return StreamUtils.reactiveSeq(StreamUtils.cycle(stream), reversable)
                          .limitWhile(predicate.negate());
    }

    
    public final <S> ReactiveSeq<Tuple2<T, S>> zip(Stream<? extends S> second) {

        return zipStream(second, (a, b) -> new Tuple2<>(
                                                        a, b));
    }

   
    public final <S, U> ReactiveSeq<Tuple3<T, S, U>> zip3(Stream<? extends S> second, Stream<? extends U> third) {
        return zip(second).zip(third)
                          .map(p -> new Tuple3(
                                               p.v1()
                                                .v1(),
                                               p.v1()
                                                .v2(),
                                               p.v2()));

    }

   
    public final <T2, T3, T4> ReactiveSeq<Tuple4<T, T2, T3, T4>> zip4(Stream<? extends T2> second, Stream<? extends T3> third,
            Stream<? extends T4> fourth) {
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

   
    public final <S, R> ReactiveSeq<R> zipStream(BaseStream<? extends S, ? extends BaseStream<? extends S, ?>> second,
            BiFunction<? super T, ? super S, ? extends R> zipper) {
        return StreamUtils.reactiveSeq(StreamUtils.zipStream(stream, second, zipper), Optional.empty());
    }

    
    public final ReactiveSeq<ListX<T>> sliding(int windowSize) {
        return StreamUtils.reactiveSeq(StreamUtils.sliding(stream, windowSize), reversable);
    }

    
    public final ReactiveSeq<ListX<T>> sliding(int windowSize, int increment) {
        return StreamUtils.reactiveSeq(StreamUtils.sliding(stream, windowSize, increment), reversable);
    }

    
    public final ReactiveSeq<ListX<T>> grouped(int groupSize) {
        return StreamUtils.reactiveSeq(StreamUtils.batchBySize(stream, groupSize), reversable);
    }

    @Override
    public ReactiveSeq<ListX<T>> groupedStatefullyWhile(BiPredicate<ListX<? super T>, ? super T> predicate) {
        return StreamUtils.reactiveSeq(StreamUtils.groupedStatefullyWhile(stream, predicate), this.reversable);
    }

    
    public final <K> MapX<K, List<T>> groupBy(Function<? super T, ? extends K> classifier) {
        return MapX.fromMap(collect(Collectors.groupingBy(classifier)));
    }

    public final ReactiveSeq<T> distinct() {
        return StreamUtils.reactiveSeq(stream.distinct(), reversable);
    }

    
    public final ReactiveSeq<T> scanLeft(Monoid<T> monoid) {
        return StreamUtils.reactiveSeq(StreamUtils.scanLeft(stream, monoid), reversable);
    }

   
    public final <U> ReactiveSeq<U> scanLeft(U seed, BiFunction<? super U, ? super T, ? extends U> function) {

        return StreamUtils.reactiveSeq(Seq.seq(stream)
                                          .scanLeft(seed, function),
                                       reversable);

    }

    public final ReactiveSeq<T> scanRight(Monoid<T> monoid) {
        return StreamUtils.reactiveSeq(reverse().scanLeft(monoid.zero(), (u, t) -> monoid.combiner()
                                                                                         .apply(t, u)),
                                       reversable);
    }

    
    public final <U> ReactiveSeq<U> scanRight(U identity, BiFunction<? super T, ? super U, ? extends U> combiner) {

        return StreamUtils.reactiveSeq(StreamUtils.scanRight(stream, identity, combiner), reversable);
    }

    
    public final ReactiveSeq<T> sorted() {
        return StreamUtils.reactiveSeq(stream.sorted(), reversable);
    }

    
    public final ReactiveSeq<T> sorted(Comparator<? super T> c) {
        return StreamUtils.reactiveSeq(stream.sorted(c), reversable);
    }

    public final ReactiveSeq<T> skip(long num) {
        return StreamUtils.reactiveSeq(stream.skip(num), reversable);
    }

   
    public final ReactiveSeq<T> skipWhile(Predicate<? super T> p) {
        return StreamUtils.reactiveSeq(StreamUtils.skipWhile(stream, p), reversable);
    }

    public final ReactiveSeq<T> skipUntil(Predicate<? super T> p) {
        return StreamUtils.reactiveSeq(StreamUtils.skipUntil(stream, p), reversable);
    }

    
    public final ReactiveSeq<T> limit(long num) {
        return StreamUtils.reactiveSeq(stream.limit(num), reversable);
    }

    
    public final ReactiveSeq<T> limitWhile(Predicate<? super T> p) {
        return StreamUtils.reactiveSeq(StreamUtils.limitWhile(stream, p), reversable);
    }

    
    public final ReactiveSeq<T> limitUntil(Predicate<? super T> p) {
        return StreamUtils.reactiveSeq(StreamUtils.limitUntil(stream, p), reversable);
    }

    
    public final ReactiveSeq<T> parallel() {
        return this;
    }

    
    public final boolean allMatch(Predicate<? super T> c) {
        return stream.allMatch(c);
    }

   
    public final boolean anyMatch(Predicate<? super T> c) {
        return stream.anyMatch(c);
    }

    
    public boolean xMatch(int num, Predicate<? super T> c) {
        return StreamUtils.xMatch(stream, num, c);
    }

   
    public final boolean noneMatch(Predicate<? super T> c) {
        return stream.allMatch(c.negate());
    }

    
    public final String join() {
        return StreamUtils.join(stream, "");
    }

   
    public final String join(String sep) {
        return StreamUtils.join(stream, sep);
    }

    public final String join(String sep, String start, String end) {
        return StreamUtils.join(stream, sep, start, end);
    }

   
    public final <U extends Comparable<? super U>> Optional<T> minBy(Function<? super T, ? extends U> function) {

        return StreamUtils.minBy(stream, function);
    }

    
    public final Optional<T> min(Comparator<? super T> comparator) {
        return StreamUtils.min(stream, comparator);
    }

    
    public final <C extends Comparable<? super C>> Optional<T> maxBy(Function<? super T, ? extends C> f) {
        return StreamUtils.maxBy(stream, f);
    }

    
    public final Optional<T> max(Comparator<? super T> comparator) {
        return StreamUtils.max(stream, comparator);
    }

  
    public final HeadAndTail<T> headAndTail() {
        return StreamUtils.headAndTail(stream);
    }

    
    public final Optional<T> findFirst() {
        return stream.findFirst();
    }

    
    public final Optional<T> findAny() {
        return stream.findAny();
    }

    
    public final <R> R mapReduce(Reducer<R> reducer) {
        return reducer.mapReduce(stream);
    }

   
    public final <R> R mapReduce(Function<? super T, ? extends R> mapper, Monoid<R> reducer) {
        return reducer.reduce(stream.map(mapper));
    }

    
    public final <R, A> R collect(Collector<? super T, A, R> collector) {
        return stream.collect(collector);
    }

    
    public final <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super T> accumulator, BiConsumer<R, R> combiner) {
        return stream.collect(supplier, accumulator, combiner);
    }

   
    public final T reduce(Monoid<T> reducer) {

        return reducer.reduce(stream);
    }

    
    public final Optional<T> reduce(BinaryOperator<T> accumulator) {
        return stream.reduce(accumulator);
    }

  
    public final T reduce(T identity, BinaryOperator<T> accumulator) {
        return stream.reduce(identity, accumulator);
    }

 
    public final <U> U reduce(U identity, BiFunction<U, ? super T, U> accumulator, BinaryOperator<U> combiner) {
        return stream.reduce(identity, accumulator, combiner);
    }

   
    public final ListX<T> reduce(Stream<? extends Monoid<T>> reducers) {
        return StreamUtils.reduce(stream, reducers);
    }

    
    public final ListX<T> reduce(Iterable<? extends Monoid<T>> reducers) {
        return StreamUtils.reduce(stream, reducers);
    }

    
    public final T foldLeft(Monoid<T> reducer) {
        return reduce(reducer);
    }

    
    public final T foldLeft(T identity, BinaryOperator<T> accumulator) {
        return stream.reduce(identity, accumulator);
    }

   
    public final <T> T foldLeftMapToType(Reducer<T> reducer) {
        return reducer.mapReduce(stream);
    }

   
    public final T foldRight(Monoid<T> reducer) {
        return reducer.reduce(reverse());
    }

    
    public final <U> U foldRight(U seed, BiFunction<? super T, ? super U, ? extends U> function) {
        return Seq.seq(stream)
                  .foldRight(seed, function);
    }

    
    public final <T> T foldRightMapToType(Reducer<T> reducer) {
        return reducer.mapReduce(reverse());
    }

    
    public final Streamable<T> toStreamable() {
        return Streamable.fromStream(stream());
    }

    
    public final Set<T> toSet() {
        return stream.collect(Collectors.toSet());
    }

    
    public final List<T> toList() {

        return stream.collect(Collectors.toList());
    }

    public final <C extends Collection<T>> C toCollection(Supplier<C> collectionFactory) {

        return stream.collect(Collectors.toCollection(collectionFactory));
    }

    
    public final <T> Stream<T> toStream() {
        return (Stream<T>) stream;
    }

    
    public final ReactiveSeq<T> stream() {
        return this;

    }

    
    public final boolean startsWithIterable(Iterable<T> iterable) {
        return StreamUtils.startsWith(stream, iterable);

    }

   
    public final boolean startsWith(Stream<T> stream2) {
        return StreamUtils.startsWith(stream, stream2);

    }

   
    @Override
    public AnyMSeq<T> anyM() {
        return AnyM.fromStream(stream);

    }

    
    public final <R> ReactiveSeq<R> map(Function<? super T, ? extends R> fn) {
        return new ReactiveSeqImpl(
                                   stream.map(fn));
    }

   
    public final ReactiveSeq<T> peek(Consumer<? super T> c) {
        return new ReactiveSeqImpl(
                                   stream.peek(c));
    }

    
    public final <R> ReactiveSeq<R> flatMap(Function<? super T, ? extends Stream<? extends R>> fn) {
        return StreamUtils.reactiveSeq(stream.flatMap(fn), reversable);
    }

    
    @Override
    public final <R> ReactiveSeq<R> flatMapAnyM(Function<? super T, AnyM<? extends R>> fn) {
        return StreamUtils.reactiveSeq(StreamUtils.flatMapAnyM(stream, fn), reversable);
    }

   
    public final <R> ReactiveSeq<R> flatMapIterable(Function<? super T, ? extends Iterable<? extends R>> fn) {
        return StreamUtils.reactiveSeq(StreamUtils.flatMapIterable(stream, fn), Optional.empty());

    }

    
    public final <R> ReactiveSeq<R> flatMapStream(Function<? super T, BaseStream<? extends R, ?>> fn) {
        return StreamUtils.reactiveSeq(StreamUtils.flatMapStream(stream, fn), reversable);

    }

   

    public final <R> ReactiveSeq<R> flatMapOptional(Function<? super T, Optional<? extends R>> fn) {
        return StreamUtils.reactiveSeq(StreamUtils.flatMapOptional(stream, fn), reversable);

    }

   
    public final <R> ReactiveSeq<R> flatMapCompletableFuture(Function<? super T, CompletableFuture<? extends R>> fn) {
        return StreamUtils.reactiveSeq(StreamUtils.flatMapCompletableFuture(stream, fn), reversable);
    }

    
    public final ReactiveSeq<Character> flatMapCharSequence(Function<? super T, CharSequence> fn) {
        return StreamUtils.reactiveSeq(StreamUtils.flatMapCharSequence(stream, fn), reversable);
    }

    
    public final ReactiveSeq<String> flatMapFile(Function<? super T, File> fn) {
        return StreamUtils.reactiveSeq(StreamUtils.flatMapFile(stream, fn), reversable);
    }

    
    public final ReactiveSeq<String> flatMapURL(Function<? super T, URL> fn) {
        return StreamUtils.reactiveSeq(StreamUtils.flatMapURL(stream, fn), reversable);
    }

    
    public final ReactiveSeq<String> flatMapBufferedReader(Function<? super T, BufferedReader> fn) {
        return StreamUtils.reactiveSeq(StreamUtils.flatMapBufferedReader(stream, fn), reversable);
    }

    public final ReactiveSeq<T> filter(Predicate<? super T> fn) {
        return StreamUtils.reactiveSeq(stream.filter(fn), reversable);
    }

    public void forEach(Consumer<? super T> action) {
        stream.forEach(action);

    }

    public Iterator<T> iterator() {
        return stream.iterator();
    }

    public Spliterator<T> spliterator() {
        return stream.spliterator();
    }

    public boolean isParallel() {
        return stream.isParallel();
    }

    public ReactiveSeq<T> sequential() {
        return StreamUtils.reactiveSeq(stream.sequential(), reversable);
    }

    public ReactiveSeq<T> unordered() {
        return StreamUtils.reactiveSeq(stream.unordered(), reversable);
    }

    public IntStream mapToInt(ToIntFunction<? super T> mapper) {
        return stream.mapToInt(mapper);
    }

    public LongStream mapToLong(ToLongFunction<? super T> mapper) {
        return stream.mapToLong(mapper);
    }

    public DoubleStream mapToDouble(ToDoubleFunction<? super T> mapper) {
        return stream.mapToDouble(mapper);
    }

    public IntStream flatMapToInt(Function<? super T, ? extends IntStream> mapper) {
        return stream.flatMapToInt(mapper);
    }

    public LongStream flatMapToLong(Function<? super T, ? extends LongStream> mapper) {
        return stream.flatMapToLong(mapper);
    }

    public DoubleStream flatMapToDouble(Function<? super T, ? extends DoubleStream> mapper) {
        return stream.flatMapToDouble(mapper);
    }

    public void forEachOrdered(Consumer<? super T> action) {
        stream.forEachOrdered(action);

    }

    public Object[] toArray() {
        return stream.toArray();
    }

    public <A> A[] toArray(IntFunction<A[]> generator) {
        return stream.toArray(generator);
    }

    public long count() {
        return stream.count();
    }

    
    public ReactiveSeq<T> intersperse(T value) {

        return StreamUtils.reactiveSeq(stream.flatMap(t -> Stream.of(value, t))
                                             .skip(1l),
                                       reversable);
    }

    
    @SuppressWarnings("unchecked")
    public <U> ReactiveSeq<U> ofType(Class<? extends U> type) {
        return StreamUtils.reactiveSeq(StreamUtils.ofType(stream, type), reversable);
    }

   
    public <U> ReactiveSeq<U> cast(Class<? extends U> type) {
        return StreamUtils.reactiveSeq(StreamUtils.cast(stream, type), reversable);
    }

    
    public CollectionX<T> toLazyCollection() {
        return StreamUtils.toLazyCollection(stream);
    }

   
    public CollectionX<T> toConcurrentLazyCollection() {
        return StreamUtils.toConcurrentLazyCollection(stream);
    }

    
    public Streamable<T> toLazyStreamable() {
        return StreamUtils.toLazyStreamable(stream);
    }

    
    public Streamable<T> toConcurrentLazyStreamable() {
        return StreamUtils.toConcurrentLazyStreamable(stream);

    }

    public ReactiveSeq<T> reverse() {
        if (reversable.isPresent()) {
            reversable.ifPresent(r -> r.invert());
            return this;
        }
        return StreamUtils.reactiveSeq(StreamUtils.reverse(stream), reversable);
    }

    @Override
    public ReactiveSeq<T> onClose(Runnable closeHandler) {

        return this;
    }

    @Override
    public void close() {

    }

    public ReactiveSeq<T> shuffle() {
        return StreamUtils.reactiveSeq(StreamUtils.shuffle(stream)
                                                  .stream(),
                                       reversable);

    }

    
    public ReactiveSeq<T> appendStream(Stream<T> stream) {
        return StreamUtils.reactiveSeq(StreamUtils.appendStream(this.stream, stream), Optional.empty());
    }

    
    public ReactiveSeq<T> prependStream(Stream<T> stream) {

        return StreamUtils.reactiveSeq(StreamUtils.prependStream(this.stream, stream), Optional.empty());
    }

    
    public ReactiveSeq<T> append(T... values) {
        return StreamUtils.reactiveSeq(StreamUtils.append(stream, values), Optional.empty());

    }

    
    public ReactiveSeq<T> prepend(T... values) {
        return StreamUtils.reactiveSeq(StreamUtils.prepend(stream, values), Optional.empty());
    }

    
    public ReactiveSeq<T> insertAt(int pos, T... values) {
        return StreamUtils.reactiveSeq(StreamUtils.insertAt(stream, pos, values), Optional.empty());

    }

    
    public ReactiveSeq<T> deleteBetween(int start, int end) {
        return StreamUtils.reactiveSeq(StreamUtils.deleteBetween(stream, start, end), Optional.empty());
    }

    
    public ReactiveSeq<T> insertStreamAt(int pos, Stream<T> stream) {

        return StreamUtils.reactiveSeq(StreamUtils.insertStreamAt(this.stream, pos, stream), Optional.empty());

    }

    @Override
    public FutureOperations<T> futureOperations(Executor exec) {
        return StreamUtils.futureOperations(stream, exec);
    }

    @Override
    public boolean endsWithIterable(Iterable<T> iterable) {
        return StreamUtils.endsWith(stream, iterable);
    }

    @Override
    public HotStream<T> hotStream(Executor e) {
        return StreamUtils.hotStream(stream, e);
    }

    @Override
    public T firstValue() {
        return StreamUtils.firstValue(stream);
    }

    @Override
    public void subscribe(Subscriber<? super T> sub) {
        Iterator<T> it = stream.iterator();
        sub.onSubscribe(new Subscription() {

            volatile boolean running = true;
            boolean active = false;
            final LinkedList<Long> requests = new LinkedList<Long>();

            @Override
            public void request(long n) {
                if (!running)
                    return;
                if (n < 1) {
                    sub.onError(new IllegalArgumentException(
                                                             "3.9 While the Subscription is not cancelled, Subscription.request(long n) MUST throw a java.lang.IllegalArgumentException if the argument is <= 0."));
                }
                requests.push(n);
                if (active)
                    return;
                active = true;//assume single thread calls to request
                while (requests.size() > 0) {

                    long num = requests.pop();
                    for (int i = 0; i < num && running; i++) {
                        boolean progressing = false;
                        boolean progressed = false;
                        try {

                            if (it.hasNext()) {
                                progressing = true;
                                sub.onNext(it.next());
                                progressed = true;
                            } else {
                                try {
                                    sub.onComplete();

                                } finally {
                                    running = false;
                                    break;
                                }
                            }
                        } catch (Throwable t) {
                            sub.onError(t);
                            if (progressing && !progressed)
                                break;

                        }

                    }
                }
                active = false;
            }

            @Override
            public void cancel() {
                running = false;

            }

        });

    }

    @Override
    public <U> ReactiveSeq<Tuple2<T, U>> zip(Seq<? extends U> other) {
        return StreamUtils.reactiveSeq(Seq.seq(stream)
                                          .zip(other),
                                       reversable);
    }

    @Override
    public ReactiveSeq<T> onEmpty(T value) {
        return StreamUtils.reactiveSeq(Seq.seq(stream)
                                          .onEmpty(value),
                                       Optional.empty());
    }

    @Override
    public ReactiveSeq<T> onEmptyGet(Supplier<? extends T> supplier) {
        return StreamUtils.reactiveSeq(Seq.seq(stream)
                                          .onEmptyGet(supplier),
                                       Optional.empty());
    }

    @Override
    public <X extends Throwable> ReactiveSeq<T> onEmptyThrow(Supplier<? extends X> supplier) {
        return StreamUtils.reactiveSeq(Seq.seq(stream)
                                          .onEmptyThrow(supplier),
                                       Optional.empty());
    }

    @Override
    public ReactiveSeq<T> concat(Stream<? extends T> other) {
        return StreamUtils.reactiveSeq(Seq.seq(stream)
                                          .concat(other),
                                       Optional.empty());
    }

    @Override
    public ReactiveSeq<T> concat(T other) {
        return StreamUtils.reactiveSeq(Seq.seq(stream)
                                          .concat(other),
                                       Optional.empty());
    }

    @Override
    public ReactiveSeq<T> concat(T... other) {
        return StreamUtils.reactiveSeq(Seq.seq(stream)
                                          .concat(other),
                                       Optional.empty());
    }

    @Override
    public <U> ReactiveSeq<T> distinct(Function<? super T, ? extends U> keyExtractor) {
        return StreamUtils.reactiveSeq(Seq.seq(stream)
                                          .distinct(keyExtractor),
                                       reversable);
    }

    @Override
    public <U, R> ReactiveSeq<R> zip(Seq<? extends U> other, BiFunction<? super T, ? super U, ? extends R> zipper) {
        return StreamUtils.reactiveSeq(Seq.seq(stream)
                                          .zip(other, zipper),
                                       Optional.empty());
    }

    @Override
    public ReactiveSeq<T> shuffle(Random random) {
        return StreamUtils.reactiveSeq(Seq.seq(stream)
                                          .shuffle(random),
                                       reversable);
    }

    @Override
    public ReactiveSeq<T> slice(long from, long to) {
        return StreamUtils.reactiveSeq(Seq.seq(stream)
                                          .slice(from, to),
                                       reversable);
    }

    @Override
    public <U extends Comparable<? super U>> ReactiveSeq<T> sorted(Function<? super T, ? extends U> function) {
        return StreamUtils.reactiveSeq(Seq.seq(stream)
                                          .sorted(function),
                                       reversable);
    }

    @Override
    public ReactiveSeq<T> xPer(int x, long time, TimeUnit t) {
        return StreamUtils.reactiveSeq(StreamUtils.xPer(stream, x, time, t), reversable);
    }

    @Override
    public ReactiveSeq<T> onePer(long time, TimeUnit t) {
        return StreamUtils.reactiveSeq(StreamUtils.onePer(stream, time, t), reversable);
    }

    @Override
    public ReactiveSeq<T> debounce(long time, TimeUnit t) {
        return StreamUtils.reactiveSeq(StreamUtils.debounce(stream, time, t), reversable);
    }

    @Override
    public ReactiveSeq<ListX<T>> groupedBySizeAndTime(int size, long time, TimeUnit t) {
        return StreamUtils.reactiveSeq(StreamUtils.batchBySizeAndTime(stream, size, time, t), reversable);
    }

    @Override
    public ReactiveSeq<ListX<T>> groupedByTime(long time, TimeUnit t) {
        return StreamUtils.reactiveSeq(StreamUtils.batchByTime(stream, time, t), reversable);
    }

    @Override
    public T foldRight(T identity, BinaryOperator<T> accumulator) {
        return reverse().foldLeft(identity, accumulator);
    }

    @Override
    public boolean endsWith(Stream<T> iterable) {
        return StreamUtils.endsWith(stream, () -> iterable.iterator());
    }

    @Override
    public ReactiveSeq<T> skip(long time, TimeUnit unit) {
        return StreamUtils.reactiveSeq(StreamUtils.skip(stream, time, unit), this.reversable);
    }

    @Override
    public ReactiveSeq<T> limit(long time, TimeUnit unit) {
        return StreamUtils.reactiveSeq(StreamUtils.limit(stream, time, unit), this.reversable);
    }

    @Override
    public ReactiveSeq<T> fixedDelay(long l, TimeUnit unit) {
        return StreamUtils.reactiveSeq(StreamUtils.fixedDelay(stream, l, unit), this.reversable);
    }

    @Override
    public ReactiveSeq<T> jitter(long l) {
        return StreamUtils.reactiveSeq(StreamUtils.jitter(stream, l), this.reversable);
    }

    @Override
    public ReactiveSeq<ListX<T>> groupedUntil(Predicate<? super T> predicate) {
        return StreamUtils.reactiveSeq(StreamUtils.batchUntil(stream, predicate), this.reversable);
    }

    @Override
    public ReactiveSeq<ListX<T>> groupedWhile(Predicate<? super T> predicate) {
        return StreamUtils.reactiveSeq(StreamUtils.batchWhile(stream, predicate), this.reversable);
    }

    @Override
    public <C extends Collection<? super T>> ReactiveSeq<C> groupedWhile(Predicate<? super T> predicate, Supplier<C> factory) {
        return StreamUtils.reactiveSeq(StreamUtils.batchWhile(stream, predicate, factory), this.reversable);
    }

    @Override
    public <C extends Collection<? super T>> ReactiveSeq<C> groupedUntil(Predicate<? super T> predicate, Supplier<C> factory) {
        return StreamUtils.reactiveSeq(StreamUtils.batchWhile(stream, predicate.negate(), factory), this.reversable);
    }

    @Override
    public <C extends Collection<? super T>> ReactiveSeq<C> groupedBySizeAndTime(int size, long time, TimeUnit unit, Supplier<C> factory) {
        return StreamUtils.reactiveSeq(StreamUtils.batchBySizeAndTime(stream, size, time, unit, factory), this.reversable);

    }

    @Override
    public <C extends Collection<? super T>> ReactiveSeq<C> groupedByTime(long time, TimeUnit unit, Supplier<C> factory) {
        return StreamUtils.reactiveSeq(StreamUtils.batchByTime(stream, time, unit, factory), this.reversable);
    }

    @Override
    public <C extends Collection<? super T>> ReactiveSeq<C> grouped(int size, Supplier<C> factory) {
        return StreamUtils.reactiveSeq(StreamUtils.batchBySize(stream, size, factory), this.reversable);

    }

    @Override
    public ReactiveSeq<T> skipLast(int num) {
        return StreamUtils.reactiveSeq(StreamUtils.skipLast(stream, num), this.reversable);
    }

    @Override
    public ReactiveSeq<T> limitLast(int num) {
        return StreamUtils.reactiveSeq(StreamUtils.limitLast(stream, num), this.reversable);
    }

    @Override
    public ReactiveSeq<T> recover(Function<Throwable, ? extends T> fn) {
        return StreamUtils.reactiveSeq(StreamUtils.recover(stream, fn), this.reversable);
    }

    @Override
    public <EX extends Throwable> ReactiveSeq<T> recover(Class<EX> exceptionClass, Function<EX, ? extends T> fn) {
        return StreamUtils.reactiveSeq(StreamUtils.recover(stream, exceptionClass, fn), this.reversable);
    }

    /** 
     * Perform a three level nested internal iteration over this Stream and the supplied streams
      *<pre>
     * {@code 
     * ReactiveSeq.of(1,2)
    					.forEach3(a->IntStream.range(10,13),
    					.a->b->Stream.of(""+(a+b),"hello world"),
    								a->b->c->c+":"a+":"+b);
    								
     * 
     *  //SequenceM[11:1:2,hello world:1:2,14:1:4,hello world:1:4,12:1:2,hello world:1:2,15:1:5,hello world:1:5]
     * }
     * </pre> 
     * @param stream1 Nested Stream to iterate over
     * @param stream2 Nested Stream to iterate over
     * @param yieldingFunction Function with pointers to the current element from both Streams that generates the new elements
     * @return SequenceM with elements generated via nested iteration
     */
    public <R1, R2, R> ReactiveSeq<R> forEach3(Function<? super T, ? extends BaseStream<R1, ?>> stream1,
            Function<? super T, Function<? super R1, ? extends BaseStream<R2, ?>>> stream2,
            Function<? super T, Function<? super R1, Function<? super R2, ? extends R>>> yieldingFunction) {
        return For.stream(this)
                  .stream(u -> stream1.apply(u))
                  .stream(u -> r1 -> stream2.apply(u)
                                            .apply(r1))
                  .yield(yieldingFunction)
                  .unwrap();

    }

    @Override
    public <R1, R2, R> ReactiveSeq<R> forEach3(Function<? super T, ? extends BaseStream<R1, ?>> stream1,
            Function<? super T, Function<? super R1, ? extends BaseStream<R2, ?>>> stream2,
            Function<? super T, Function<? super R1, Function<? super R2, Boolean>>> filterFunction,
            Function<? super T, Function<? super R1, Function<? super R2, ? extends R>>> yieldingFunction) {

        return For.stream(this)
                  .stream(u -> stream1.apply(u))
                  .stream(u -> r1 -> stream2.apply(u)
                                            .apply(r1))
                  .filter(filterFunction)
                  .yield(yieldingFunction)
                  .unwrap();

    }

    @Override
    public <R1, R> ReactiveSeq<R> forEach2(Function<? super T, ? extends BaseStream<R1, ?>> stream1,
            Function<? super T, Function<? super R1, ? extends R>> yieldingFunction) {
        return For.stream(this)
                  .stream(u -> stream1.apply(u))
                  .yield(yieldingFunction)
                  .unwrap();

    }

    @Override
    public <R1, R> ReactiveSeq<R> forEach2(Function<? super T, ? extends BaseStream<R1, ?>> stream1,
            Function<? super T, Function<? super R1, Boolean>> filterFunction,
            Function<? super T, Function<? super R1, ? extends R>> yieldingFunction) {
        return For.stream(this)
                  .stream(u -> stream1.apply(u))
                  .filter(filterFunction)
                  .yield(yieldingFunction)
                  .unwrap();

    }

    @Override
    public <X extends Throwable> Subscription forEachX(long numberOfElements, Consumer<? super T> consumer) {
        return StreamUtils.forEachX(this, numberOfElements, consumer);
    }
    @Override
    public <X extends Throwable> Subscription forEachXWithError(long numberOfElements, Consumer<? super T> consumer,
            Consumer<? super Throwable> consumerError) {
        return StreamUtils.forEachXWithError(this, numberOfElements, consumer, consumerError);
    }
    @Override
    public <X extends Throwable> Subscription forEachXEvents(long numberOfElements, Consumer<? super T> consumer,
            Consumer<? super Throwable> consumerError, Runnable onComplete) {
        return StreamUtils.forEachXEvents(this, numberOfElements, consumer, consumerError, onComplete);
    }
    @Override
    public <X extends Throwable> void forEachWithError(Consumer<? super T> consumerElement, Consumer<? super Throwable> consumerError) {
        StreamUtils.forEachWithError(this, consumerElement, consumerError);
    }
    @Override
    public <X extends Throwable> void forEachEvent(Consumer<? super T> consumerElement, Consumer<? super Throwable> consumerError,
            Runnable onComplete) {
        StreamUtils.forEachEvent(this, consumerElement, consumerError, onComplete);
    }

    
    @Override
    public HotStream<T> primedHotStream(Executor e) {
        return StreamUtils.primedHotStream(this, e);
    }

    
    @Override
    public PausableHotStream<T> pausableHotStream(Executor e) {
        return StreamUtils.pausableHotStream(this, e);
    }

   
    @Override
    public PausableHotStream<T> primedPausableHotStream(Executor e) {
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
    public <T> ReactiveSeq<T> unitIterator(Iterator<T> it) {
        return ReactiveSeq.fromIterator(it);
    }

    @Override
    public ReactiveSeq<T> append(T value) {
        if (value instanceof Stream) {
            return appendStream((Stream<T>) value);
        }
        return append((T[]) new Object[] { value });
    }

    @Override
    public ReactiveSeq<T> prepend(T value) {
        if (value instanceof Stream) {
            return prependStream((Stream<T>) value);
        }
        return prepend((T[]) new Object[] { value });
    }

}
