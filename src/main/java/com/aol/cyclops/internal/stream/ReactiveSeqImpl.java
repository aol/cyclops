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
import com.aol.cyclops.util.function.QuadFunction;
import com.aol.cyclops.util.function.TriFunction;

public class ReactiveSeqImpl<T> implements Unwrapable, ReactiveSeq<T>, Iterable<T> {
    private final Seq<T> stream;
    private final Optional<ReversableSpliterator> reversable;

    public ReactiveSeqImpl(final Stream<T> stream) {

        this.stream = Seq.seq(stream);
        this.reversable = Optional.empty();

    }

    public ReactiveSeqImpl(final Stream<T> stream, final ReversableSpliterator rev) {
        this.stream = Seq.seq(stream);
        this.reversable = Optional.of(rev);

    }

    @Override
    public <U> U reduce(final U identity, final BiFunction<U, ? super T, U> accumulator) {
        return stream.foldLeft(identity, accumulator);
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

    @Override
    public final <T1> ReactiveSeq<T1> flatten() {
        return StreamUtils.flatten(stream);

    }

    public final Stream<T> unwrapStream() {

        return stream;

    }

    @Override
    public final ReactiveSeq<T> cycle(final int times) {
        return StreamUtils.reactiveSeq(StreamUtils.cycle(times, Streamable.fromStream(stream)), reversable);
    }

    @Override
    public final ReactiveSeq<T> cycle() {
        return StreamUtils.reactiveSeq(StreamUtils.cycle(stream), reversable);
    }

    @Override
    public final Tuple2<ReactiveSeq<T>, ReactiveSeq<T>> duplicateSequence() {
        final Tuple2<Stream<T>, Stream<T>> tuple = StreamUtils.duplicate(stream);
        return tuple.map1(s -> StreamUtils.reactiveSeq(s, reversable.map(r -> r.copy())))
                    .map2(s -> StreamUtils.reactiveSeq(s, reversable.map(r -> r.copy())));
    }

    @Override
    @SuppressWarnings("unchecked")
    public final Tuple3<ReactiveSeq<T>, ReactiveSeq<T>, ReactiveSeq<T>> triplicate() {

        final Tuple3<Stream<T>, Stream<T>, Stream<T>> tuple = StreamUtils.triplicate(stream);
        return tuple.map1(s -> StreamUtils.reactiveSeq(s, reversable.map(r -> r.copy())))
                    .map2(s -> StreamUtils.reactiveSeq(s, reversable.map(r -> r.copy())))
                    .map3(s -> StreamUtils.reactiveSeq(s, reversable.map(r -> r.copy())));

    }

    @Override
    @SuppressWarnings("unchecked")
    public final Tuple4<ReactiveSeq<T>, ReactiveSeq<T>, ReactiveSeq<T>, ReactiveSeq<T>> quadruplicate() {
        final Tuple4<Stream<T>, Stream<T>, Stream<T>, Stream<T>> tuple = StreamUtils.quadruplicate(stream);
        return tuple.map1(s -> StreamUtils.reactiveSeq(s, reversable.map(r -> r.copy())))
                    .map2(s -> StreamUtils.reactiveSeq(s, reversable.map(r -> r.copy())))
                    .map3(s -> StreamUtils.reactiveSeq(s, reversable.map(r -> r.copy())))
                    .map4(s -> StreamUtils.reactiveSeq(s, reversable.map(r -> r.copy())));
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public final Tuple2<Optional<T>, ReactiveSeq<T>> splitSequenceAtHead() {
        final Tuple2<ReactiveSeq<T>, ReactiveSeq<T>> Tuple2 = splitAt(1);
        return new Tuple2(
                          Tuple2.v1.toOptional()
                                   .flatMap(l -> l.size() > 0 ? Optional.of(l.get(0)) : Optional.empty()),
                          Tuple2.v2);
    }

    @Override
    public final Tuple2<ReactiveSeq<T>, ReactiveSeq<T>> splitAt(final int where) {
        return StreamUtils.splitAt(stream, where)
                          .map1(s -> StreamUtils.reactiveSeq(s, reversable.map(r -> r.copy())))
                          .map2(s -> StreamUtils.reactiveSeq(s, reversable.map(r -> r.copy())));

    }

    @Override
    public final Tuple2<ReactiveSeq<T>, ReactiveSeq<T>> splitBy(final Predicate<T> splitter) {
        return StreamUtils.splitBy(stream, splitter)
                          .map1(s -> StreamUtils.reactiveSeq(s, reversable.map(r -> r.copy())))
                          .map2(s -> StreamUtils.reactiveSeq(s, reversable.map(r -> r.copy())));
    }

    @Override
    public final Tuple2<ReactiveSeq<T>, ReactiveSeq<T>> partitionSequence(final Predicate<T> splitter) {
        return StreamUtils.partition(stream, splitter)
                          .map1(s -> StreamUtils.reactiveSeq(s, reversable.map(r -> r.copy())))
                          .map2(s -> StreamUtils.reactiveSeq(s, reversable.map(r -> r.copy())));
    }

    @Override
    public final ReactiveSeq<T> cycle(final Monoid<T> m, final int times) {
        return StreamUtils.reactiveSeq(StreamUtils.cycle(times, Streamable.of(m.reduce(stream))), reversable);

    }

    
    @Override
    public final ReactiveSeq<T> cycleWhile(final Predicate<? super T> predicate) {

        return StreamUtils.reactiveSeq(StreamUtils.cycle(stream), reversable)
                          .limitWhile(predicate);
    }

    @Override
    public final ReactiveSeq<T> cycleUntil(final Predicate<? super T> predicate) {
        return StreamUtils.reactiveSeq(StreamUtils.cycle(stream), reversable)
                          .limitWhile(predicate.negate());
    }

    @Override
    public final <S> ReactiveSeq<Tuple2<T, S>> zip(final Stream<? extends S> second) {

        return zipStream(second, (a, b) -> new Tuple2<>(
                                                        a, b));
    }

    @Override
    public final <S, U> ReactiveSeq<Tuple3<T, S, U>> zip3(final Stream<? extends S> second, final Stream<? extends U> third) {
        return zip(second).zip(third)
                          .map(p -> new Tuple3(
                                               p.v1()
                                                .v1(),
                                               p.v1()
                                                .v2(),
                                               p.v2()));

    }

    @Override
    public final <T2, T3, T4> ReactiveSeq<Tuple4<T, T2, T3, T4>> zip4(final Stream<? extends T2> second, final Stream<? extends T3> third,
            final Stream<? extends T4> fourth) {
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
    public final <S, R> ReactiveSeq<R> zipStream(final BaseStream<? extends S, ? extends BaseStream<? extends S, ?>> second,
            final BiFunction<? super T, ? super S, ? extends R> zipper) {
        return StreamUtils.reactiveSeq(StreamUtils.zipStream(stream, second, zipper), Optional.empty());
    }

    @Override
    public final ReactiveSeq<ListX<T>> sliding(final int windowSize) {
        return StreamUtils.reactiveSeq(StreamUtils.sliding(stream, windowSize), reversable);
    }

    @Override
    public final ReactiveSeq<ListX<T>> sliding(final int windowSize, final int increment) {
        return StreamUtils.reactiveSeq(StreamUtils.sliding(stream, windowSize, increment), reversable);
    }

    @Override
    public final ReactiveSeq<ListX<T>> grouped(final int groupSize) {
        return StreamUtils.reactiveSeq(StreamUtils.batchBySize(stream, groupSize), reversable);
    }

    @Override
    public ReactiveSeq<ListX<T>> groupedStatefullyUntil(final BiPredicate<ListX<? super T>, ? super T> predicate) {
        return StreamUtils.reactiveSeq(StreamUtils.groupedStatefullyUntil(stream, predicate), this.reversable);
    }

    @Override
    public final <K> MapX<K, List<T>> groupBy(final Function<? super T, ? extends K> classifier) {
        return MapX.fromMap(collect(Collectors.groupingBy(classifier)));
    }

    @Override
    public final ReactiveSeq<T> distinct() {
        return StreamUtils.reactiveSeq(stream.distinct(), reversable);
    }

    @Override
    public final ReactiveSeq<T> scanLeft(final Monoid<T> monoid) {
        return StreamUtils.reactiveSeq(StreamUtils.scanLeft(stream, monoid), reversable);
    }

    @Override
    public final <U> ReactiveSeq<U> scanLeft(final U seed, final BiFunction<? super U, ? super T, ? extends U> function) {

        return StreamUtils.reactiveSeq(Seq.seq(stream)
                                          .scanLeft(seed, function),
                                       reversable);

    }

    @Override
    public final ReactiveSeq<T> scanRight(final Monoid<T> monoid) {
        return StreamUtils.reactiveSeq(reverse().scanLeft(monoid.zero(), (u, t) -> monoid
                                                                                         .apply(t, u)),
                                       reversable);
    }

    @Override
    public final <U> ReactiveSeq<U> scanRight(final U identity, final BiFunction<? super T, ? super U, ? extends U> combiner) {

        return StreamUtils.reactiveSeq(StreamUtils.scanRight(stream, identity, combiner), reversable);
    }

    @Override
    public final ReactiveSeq<T> sorted() {
        return StreamUtils.reactiveSeq(stream.sorted(), reversable);
    }

    @Override
    public final ReactiveSeq<T> sorted(final Comparator<? super T> c) {
        return StreamUtils.reactiveSeq(stream.sorted(c), reversable);
    }

    @Override
    public final ReactiveSeq<T> skip(final long num) {
        return StreamUtils.reactiveSeq(stream.skip(num), reversable);
    }

    @Override
    public final ReactiveSeq<T> skipWhile(final Predicate<? super T> p) {
        return StreamUtils.reactiveSeq(StreamUtils.skipWhile(stream, p), reversable);
    }

    @Override
    public final ReactiveSeq<T> skipUntil(final Predicate<? super T> p) {
        return StreamUtils.reactiveSeq(StreamUtils.skipUntil(stream, p), reversable);
    }

    @Override
    public final ReactiveSeq<T> limit(final long num) {
        return StreamUtils.reactiveSeq(stream.limit(num), reversable);
    }

    @Override
    public final ReactiveSeq<T> limitWhile(final Predicate<? super T> p) {
        return StreamUtils.reactiveSeq(StreamUtils.limitWhile(stream, p), reversable);
    }

    @Override
    public final ReactiveSeq<T> limitUntil(final Predicate<? super T> p) {
        return StreamUtils.reactiveSeq(StreamUtils.limitUntil(stream, p), reversable);
    }

    @Override
    public final ReactiveSeq<T> parallel() {
        return this;
    }

    @Override
    public final boolean allMatch(final Predicate<? super T> c) {
        return stream.allMatch(c);
    }

    @Override
    public final boolean anyMatch(final Predicate<? super T> c) {
        return stream.anyMatch(c);
    }

    @Override
    public boolean xMatch(final int num, final Predicate<? super T> c) {
        return StreamUtils.xMatch(stream, num, c);
    }

    @Override
    public final boolean noneMatch(final Predicate<? super T> c) {
        return stream.allMatch(c.negate());
    }

    @Override
    public final String join() {
        return StreamUtils.join(stream, "");
    }

    @Override
    public final String join(final String sep) {
        return StreamUtils.join(stream, sep);
    }

    @Override
    public final String join(final String sep, final String start, final String end) {
        return StreamUtils.join(stream, sep, start, end);
    }

    @Override
    public final <U extends Comparable<? super U>> Optional<T> minBy(final Function<? super T, ? extends U> function) {

        return StreamUtils.minBy(stream, function);
    }

    @Override
    public final Optional<T> min(final Comparator<? super T> comparator) {
        return StreamUtils.min(stream, comparator);
    }

    @Override
    public final <C extends Comparable<? super C>> Optional<T> maxBy(final Function<? super T, ? extends C> f) {
        return StreamUtils.maxBy(stream, f);
    }

    @Override
    public final Optional<T> max(final Comparator<? super T> comparator) {
        return StreamUtils.max(stream, comparator);
    }

    @Override
    public final HeadAndTail<T> headAndTail() {
        return StreamUtils.headAndTail(stream);
    }

    @Override
    public final Optional<T> findFirst() {
        return stream.findFirst();
    }

    @Override
    public final Optional<T> findAny() {
        return stream.findAny();
    }

    @Override
    public final <R> R mapReduce(final Reducer<R> reducer) {
        return reducer.mapReduce(stream);
    }

    @Override
    public final <R> R mapReduce(final Function<? super T, ? extends R> mapper, final Monoid<R> reducer) {
        return reducer.reduce(stream.map(mapper));
    }

    @Override
    public final <R, A> R collect(final Collector<? super T, A, R> collector) {
        return stream.collect(collector);
    }

    @Override
    public final <R> R collect(final Supplier<R> supplier, final BiConsumer<R, ? super T> accumulator, final BiConsumer<R, R> combiner) {
        return stream.collect(supplier, accumulator, combiner);
    }

    @Override
    public final T reduce(final Monoid<T> reducer) {

        return reducer.reduce(stream);
    }

    @Override
    public final Optional<T> reduce(final BinaryOperator<T> accumulator) {
        return stream.reduce(accumulator);
    }

    @Override
    public final T reduce(final T identity, final BinaryOperator<T> accumulator) {
        return stream.reduce(identity, accumulator);
    }

    @Override
    public final <U> U reduce(final U identity, final BiFunction<U, ? super T, U> accumulator, final BinaryOperator<U> combiner) {
        return stream.reduce(identity, accumulator, combiner);
    }

    @Override
    public final ListX<T> reduce(final Stream<? extends Monoid<T>> reducers) {
        return StreamUtils.reduce(stream, reducers);
    }

    @Override
    public final ListX<T> reduce(final Iterable<? extends Monoid<T>> reducers) {
        return StreamUtils.reduce(stream, reducers);
    }

    public final T foldLeft(final Monoid<T> reducer) {
        return reduce(reducer);
    }

    public final T foldLeft(final T identity, final BinaryOperator<T> accumulator) {
        return stream.reduce(identity, accumulator);
    }

    public final <T> T foldLeftMapToType(final Reducer<T> reducer) {
        return reducer.mapReduce(stream);
    }

    @Override
    public final T foldRight(final Monoid<T> reducer) {
        return reducer.reduce(reverse());
    }

    @Override
    public final <U> U foldRight(final U seed, final BiFunction<? super T, ? super U, ? extends U> function) {
        return Seq.seq(stream)
                  .foldRight(seed, function);
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
        return stream.collect(Collectors.toSet());
    }

    @Override
    public final List<T> toList() {

        return stream.collect(Collectors.toList());
    }

    @Override
    public final <C extends Collection<T>> C toCollection(final Supplier<C> collectionFactory) {

        return stream.collect(Collectors.toCollection(collectionFactory));
    }

    @Override
    public final <T> Stream<T> toStream() {
        return (Stream<T>) stream;
    }

    @Override
    public final ReactiveSeq<T> stream() {
        return this;

    }

    @Override
    public final boolean startsWithIterable(final Iterable<T> iterable) {
        return StreamUtils.startsWith(stream, iterable);

    }

    @Override
    public final boolean startsWith(final Stream<T> stream2) {
        return StreamUtils.startsWith(stream, stream2);

    }

    @Override
    public AnyMSeq<T> anyM() {
        return AnyM.fromStream(stream);

    }

    @Override
    public final <R> ReactiveSeq<R> map(final Function<? super T, ? extends R> fn) {
        return new ReactiveSeqImpl(
                                   stream.map(fn));
    }

    @Override
    public final ReactiveSeq<T> peek(final Consumer<? super T> c) {
        return new ReactiveSeqImpl(
                                   stream.peek(c));
    }

    @Override
    public final <R> ReactiveSeq<R> flatMap(final Function<? super T, ? extends Stream<? extends R>> fn) {
        return StreamUtils.reactiveSeq(stream.flatMap(fn), reversable);
    }

    @Override
    public final <R> ReactiveSeq<R> flatMapAnyM(final Function<? super T, AnyM<? extends R>> fn) {
        return StreamUtils.reactiveSeq(StreamUtils.flatMapAnyM(stream, fn), reversable);
    }

    @Override
    public final <R> ReactiveSeq<R> flatMapIterable(final Function<? super T, ? extends Iterable<? extends R>> fn) {
        return StreamUtils.reactiveSeq(StreamUtils.flatMapIterable(stream, fn), Optional.empty());

    }

    @Override
    public final <R> ReactiveSeq<R> flatMapStream(final Function<? super T, BaseStream<? extends R, ?>> fn) {
        return StreamUtils.reactiveSeq(StreamUtils.flatMapStream(stream, fn), reversable);

    }

    public final <R> ReactiveSeq<R> flatMapOptional(final Function<? super T, Optional<? extends R>> fn) {
        return StreamUtils.reactiveSeq(StreamUtils.flatMapOptional(stream, fn), reversable);

    }

    public final <R> ReactiveSeq<R> flatMapCompletableFuture(final Function<? super T, CompletableFuture<? extends R>> fn) {
        return StreamUtils.reactiveSeq(StreamUtils.flatMapCompletableFuture(stream, fn), reversable);
    }

    public final ReactiveSeq<Character> flatMapCharSequence(final Function<? super T, CharSequence> fn) {
        return StreamUtils.reactiveSeq(StreamUtils.flatMapCharSequence(stream, fn), reversable);
    }

    public final ReactiveSeq<String> flatMapFile(final Function<? super T, File> fn) {
        return StreamUtils.reactiveSeq(StreamUtils.flatMapFile(stream, fn), reversable);
    }

    public final ReactiveSeq<String> flatMapURL(final Function<? super T, URL> fn) {
        return StreamUtils.reactiveSeq(StreamUtils.flatMapURL(stream, fn), reversable);
    }

    public final ReactiveSeq<String> flatMapBufferedReader(final Function<? super T, BufferedReader> fn) {
        return StreamUtils.reactiveSeq(StreamUtils.flatMapBufferedReader(stream, fn), reversable);
    }

    @Override
    public final ReactiveSeq<T> filter(final Predicate<? super T> fn) {
        return StreamUtils.reactiveSeq(stream.filter(fn), reversable);
    }

    @Override
    public void forEach(final Consumer<? super T> action) {
        stream.forEach(action);

    }

    @Override
    public Iterator<T> iterator() {
        return stream.iterator();
    }

    @Override
    public Spliterator<T> spliterator() {
        return stream.spliterator();
    }

    @Override
    public boolean isParallel() {
        return stream.isParallel();
    }

    @Override
    public ReactiveSeq<T> sequential() {
        return StreamUtils.reactiveSeq(stream.sequential(), reversable);
    }

    @Override
    public ReactiveSeq<T> unordered() {
        return StreamUtils.reactiveSeq(stream.unordered(), reversable);
    }

    @Override
    public IntStream mapToInt(final ToIntFunction<? super T> mapper) {
        return stream.mapToInt(mapper);
    }

    @Override
    public LongStream mapToLong(final ToLongFunction<? super T> mapper) {
        return stream.mapToLong(mapper);
    }

    @Override
    public DoubleStream mapToDouble(final ToDoubleFunction<? super T> mapper) {
        return stream.mapToDouble(mapper);
    }

    @Override
    public IntStream flatMapToInt(final Function<? super T, ? extends IntStream> mapper) {
        return stream.flatMapToInt(mapper);
    }

    @Override
    public LongStream flatMapToLong(final Function<? super T, ? extends LongStream> mapper) {
        return stream.flatMapToLong(mapper);
    }

    @Override
    public DoubleStream flatMapToDouble(final Function<? super T, ? extends DoubleStream> mapper) {
        return stream.flatMapToDouble(mapper);
    }

    @Override
    public void forEachOrdered(final Consumer<? super T> action) {
        stream.forEachOrdered(action);

    }

    @Override
    public Object[] toArray() {
        return stream.toArray();
    }

    @Override
    public <A> A[] toArray(final IntFunction<A[]> generator) {
        return stream.toArray(generator);
    }

    @Override
    public long count() {
        return stream.count();
    }

    @Override
    public ReactiveSeq<T> intersperse(final T value) {

        return StreamUtils.reactiveSeq(stream.flatMap(t -> Stream.of(value, t))
                                             .skip(1l),
                                       reversable);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <U> ReactiveSeq<U> ofType(final Class<? extends U> type) {
        return StreamUtils.reactiveSeq(StreamUtils.ofType(stream, type), reversable);
    }

    @Override
    public <U> ReactiveSeq<U> cast(final Class<? extends U> type) {
        return StreamUtils.reactiveSeq(StreamUtils.cast(stream, type), reversable);
    }

    @Override
    public CollectionX<T> toLazyCollection() {
        return StreamUtils.toLazyCollection(stream);
    }

    @Override
    public CollectionX<T> toConcurrentLazyCollection() {
        return StreamUtils.toConcurrentLazyCollection(stream);
    }

    public Streamable<T> toLazyStreamable() {
        return StreamUtils.toLazyStreamable(stream);
    }

    @Override
    public Streamable<T> toConcurrentLazyStreamable() {
        return StreamUtils.toConcurrentLazyStreamable(stream);

    }

    @Override
    public ReactiveSeq<T> reverse() {
        if (reversable.isPresent()) {
            reversable.ifPresent(r -> r.invert());
            return this;
        }
        return StreamUtils.reactiveSeq(StreamUtils.reverse(stream), reversable);
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
        return StreamUtils.reactiveSeq(StreamUtils.shuffle(stream)
                                                  .stream(),
                                       reversable);

    }

    @Override
    public ReactiveSeq<T> appendStream(final Stream<T> stream) {
        return StreamUtils.reactiveSeq(StreamUtils.appendStream(this.stream, stream), Optional.empty());
    }

    @Override
    public ReactiveSeq<T> prependStream(final Stream<T> stream) {

        return StreamUtils.reactiveSeq(StreamUtils.prependStream(this.stream, stream), Optional.empty());
    }

    @Override
    public ReactiveSeq<T> append(final T... values) {
        return StreamUtils.reactiveSeq(StreamUtils.append(stream, values), Optional.empty());

    }

    @Override
    public ReactiveSeq<T> prepend(final T... values) {
        return StreamUtils.reactiveSeq(StreamUtils.prepend(stream, values), Optional.empty());
    }

    @Override
    public ReactiveSeq<T> insertAt(final int pos, final T... values) {
        return StreamUtils.reactiveSeq(StreamUtils.insertAt(stream, pos, values), Optional.empty());

    }

    @Override
    public ReactiveSeq<T> deleteBetween(final int start, final int end) {
        return StreamUtils.reactiveSeq(StreamUtils.deleteBetween(stream, start, end), Optional.empty());
    }

    @Override
    public ReactiveSeq<T> insertStreamAt(final int pos, final Stream<T> stream) {

        return StreamUtils.reactiveSeq(StreamUtils.insertStreamAt(this.stream, pos, stream), Optional.empty());

    }

    @Override
    public FutureOperations<T> futureOperations(final Executor exec) {
        return StreamUtils.futureOperations(stream, exec);
    }

    @Override
    public boolean endsWithIterable(final Iterable<T> iterable) {
        return StreamUtils.endsWith(stream, iterable);
    }

    @Override
    public HotStream<T> hotStream(final Executor e) {
        return StreamUtils.hotStream(stream, e);
    }

    @Override
    public T firstValue() {
        return StreamUtils.firstValue(stream);
    }

    @Override
    public void subscribe(final Subscriber<? super T> sub) {
        final Iterator<T> it = stream.iterator();
        sub.onSubscribe(new Subscription() {

            volatile boolean running = true;
            boolean active = false;
            final LinkedList<Long> requests = new LinkedList<Long>();

            @Override
            public void request(final long n) {
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

                    final long num = requests.pop();
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
                        } catch (final Throwable t) {
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
    public <U> ReactiveSeq<Tuple2<T, U>> zip(final Seq<? extends U> other) {
        return StreamUtils.reactiveSeq(Seq.seq(stream)
                                          .zip(other),
                                       reversable);
    }

    @Override
    public ReactiveSeq<T> onEmpty(final T value) {
        return StreamUtils.reactiveSeq(Seq.seq(stream)
                                          .onEmpty(value),
                                       Optional.empty());
    }

    @Override
    public ReactiveSeq<T> onEmptyGet(final Supplier<? extends T> supplier) {
        return StreamUtils.reactiveSeq(Seq.seq(stream)
                                          .onEmptyGet(supplier),
                                       Optional.empty());
    }

    @Override
    public <X extends Throwable> ReactiveSeq<T> onEmptyThrow(final Supplier<? extends X> supplier) {
        return StreamUtils.reactiveSeq(Seq.seq(stream)
                                          .onEmptyThrow(supplier),
                                       Optional.empty());
    }

    @Override
    public ReactiveSeq<T> concat(final Stream<? extends T> other) {
        return StreamUtils.reactiveSeq(Seq.seq(stream)
                                          .concat(other),
                                       Optional.empty());
    }

    @Override
    public ReactiveSeq<T> concat(final T other) {
        return StreamUtils.reactiveSeq(Seq.seq(stream)
                                          .concat(other),
                                       Optional.empty());
    }

    @Override
    public ReactiveSeq<T> concat(final T... other) {
        return StreamUtils.reactiveSeq(Seq.seq(stream)
                                          .concat(other),
                                       Optional.empty());
    }

    @Override
    public <U> ReactiveSeq<T> distinct(final Function<? super T, ? extends U> keyExtractor) {
        return StreamUtils.reactiveSeq(Seq.seq(stream)
                                          .distinct(keyExtractor),
                                       reversable);
    }

    @Override
    public <U, R> ReactiveSeq<R> zip(final Seq<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {
        return StreamUtils.reactiveSeq(Seq.seq(stream)
                                          .zip(other, zipper),
                                       Optional.empty());
    }

    @Override
    public ReactiveSeq<T> shuffle(final Random random) {
        return StreamUtils.reactiveSeq(Seq.seq(stream)
                                          .shuffle(random),
                                       reversable);
    }

    @Override
    public ReactiveSeq<T> slice(final long from, final long to) {
        return StreamUtils.reactiveSeq(Seq.seq(stream)
                                          .slice(from, to),
                                       reversable);
    }

    @Override
    public <U extends Comparable<? super U>> ReactiveSeq<T> sorted(final Function<? super T, ? extends U> function) {
        return StreamUtils.reactiveSeq(Seq.seq(stream)
                                          .sorted(function),
                                       reversable);
    }

    @Override
    public ReactiveSeq<T> xPer(final int x, final long time, final TimeUnit t) {
        return StreamUtils.reactiveSeq(StreamUtils.xPer(stream, x, time, t), reversable);
    }

    @Override
    public ReactiveSeq<T> onePer(final long time, final TimeUnit t) {
        return StreamUtils.reactiveSeq(StreamUtils.onePer(stream, time, t), reversable);
    }

    @Override
    public ReactiveSeq<T> debounce(final long time, final TimeUnit t) {
        return StreamUtils.reactiveSeq(StreamUtils.debounce(stream, time, t), reversable);
    }

    @Override
    public ReactiveSeq<ListX<T>> groupedBySizeAndTime(final int size, final long time, final TimeUnit t) {
        return StreamUtils.reactiveSeq(StreamUtils.batchBySizeAndTime(stream, size, time, t), reversable);
    }

    @Override
    public ReactiveSeq<ListX<T>> groupedByTime(final long time, final TimeUnit t) {
        return StreamUtils.reactiveSeq(StreamUtils.batchByTime(stream, time, t), reversable);
    }

    @Override
    public T foldRight(final T identity, final BinaryOperator<T> accumulator) {
        return reverse().foldLeft(identity, accumulator);
    }

    @Override
    public boolean endsWith(final Stream<T> iterable) {
        return StreamUtils.endsWith(stream, () -> iterable.iterator());
    }

    @Override
    public ReactiveSeq<T> skip(final long time, final TimeUnit unit) {
        return StreamUtils.reactiveSeq(StreamUtils.skip(stream, time, unit), this.reversable);
    }

    @Override
    public ReactiveSeq<T> limit(final long time, final TimeUnit unit) {
        return StreamUtils.reactiveSeq(StreamUtils.limit(stream, time, unit), this.reversable);
    }

    @Override
    public ReactiveSeq<T> fixedDelay(final long l, final TimeUnit unit) {
        return StreamUtils.reactiveSeq(StreamUtils.fixedDelay(stream, l, unit), this.reversable);
    }

    @Override
    public ReactiveSeq<T> jitter(final long l) {
        return StreamUtils.reactiveSeq(StreamUtils.jitter(stream, l), this.reversable);
    }

    @Override
    public ReactiveSeq<ListX<T>> groupedUntil(final Predicate<? super T> predicate) {
        return StreamUtils.reactiveSeq(StreamUtils.batchUntil(stream, predicate), this.reversable);
    }

    @Override
    public ReactiveSeq<ListX<T>> groupedWhile(final Predicate<? super T> predicate) {
        return StreamUtils.reactiveSeq(StreamUtils.batchWhile(stream, predicate), this.reversable);
    }

    @Override
    public <C extends Collection<? super T>> ReactiveSeq<C> groupedWhile(final Predicate<? super T> predicate, final Supplier<C> factory) {
        return StreamUtils.reactiveSeq(StreamUtils.batchWhile(stream, predicate, factory), this.reversable);
    }

    @Override
    public <C extends Collection<? super T>> ReactiveSeq<C> groupedUntil(final Predicate<? super T> predicate, final Supplier<C> factory) {
        return StreamUtils.reactiveSeq(StreamUtils.batchWhile(stream, predicate.negate(), factory), this.reversable);
    }

    @Override
    public <C extends Collection<? super T>> ReactiveSeq<C> groupedBySizeAndTime(final int size, final long time, final TimeUnit unit,
            final Supplier<C> factory) {
        return StreamUtils.reactiveSeq(StreamUtils.batchBySizeAndTime(stream, size, time, unit, factory), this.reversable);

    }

    @Override
    public <C extends Collection<? super T>> ReactiveSeq<C> groupedByTime(final long time, final TimeUnit unit, final Supplier<C> factory) {
        return StreamUtils.reactiveSeq(StreamUtils.batchByTime(stream, time, unit, factory), this.reversable);
    }

    @Override
    public <C extends Collection<? super T>> ReactiveSeq<C> grouped(final int size, final Supplier<C> factory) {
        return StreamUtils.reactiveSeq(StreamUtils.batchBySize(stream, size, factory), this.reversable);

    }

    @Override
    public ReactiveSeq<T> skipLast(final int num) {
        return StreamUtils.reactiveSeq(StreamUtils.skipLast(stream, num), this.reversable);
    }

    @Override
    public ReactiveSeq<T> limitLast(final int num) {
        return StreamUtils.reactiveSeq(StreamUtils.limitLast(stream, num), this.reversable);
    }

    @Override
    public ReactiveSeq<T> recover(final Function<Throwable, ? extends T> fn) {
        return StreamUtils.reactiveSeq(StreamUtils.recover(stream, fn), this.reversable);
    }

    @Override
    public <EX extends Throwable> ReactiveSeq<T> recover(final Class<EX> exceptionClass, final Function<EX, ? extends T> fn) {
        return StreamUtils.reactiveSeq(StreamUtils.recover(stream, exceptionClass, fn), this.reversable);
    }
    

  
 
    

    @Override
    public <X extends Throwable> Subscription forEachX(final long numberOfElements, final Consumer<? super T> consumer) {
        return StreamUtils.forEachX(this, numberOfElements, consumer);
    }

    @Override
    public <X extends Throwable> Subscription forEachXWithError(final long numberOfElements, final Consumer<? super T> consumer,
            final Consumer<? super Throwable> consumerError) {
        return StreamUtils.forEachXWithError(this, numberOfElements, consumer, consumerError);
    }

    @Override
    public <X extends Throwable> Subscription forEachXEvents(final long numberOfElements, final Consumer<? super T> consumer,
            final Consumer<? super Throwable> consumerError, final Runnable onComplete) {
        return StreamUtils.forEachXEvents(this, numberOfElements, consumer, consumerError, onComplete);
    }

    @Override
    public <X extends Throwable> void forEachWithError(final Consumer<? super T> consumerElement, final Consumer<? super Throwable> consumerError) {
        StreamUtils.forEachWithError(this, consumerElement, consumerError);
    }

    @Override
    public <X extends Throwable> void forEachEvent(final Consumer<? super T> consumerElement, final Consumer<? super Throwable> consumerError,
            final Runnable onComplete) {
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

    @Override
    public ReactiveSeq<T> append(final T value) {
        if (value instanceof Stream) {
            return appendStream((Stream<T>) value);
        }
        return append((T[]) new Object[] { value });
    }

    @Override
    public ReactiveSeq<T> prepend(final T value) {
        if (value instanceof Stream) {
            return prependStream((Stream<T>) value);
        }
        return prepend((T[]) new Object[] { value });
    }

}
