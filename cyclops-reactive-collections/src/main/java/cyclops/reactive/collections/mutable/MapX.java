package cyclops.reactive.collections.mutable;

import com.oath.cyclops.ReactiveConvertableSequence;
import com.oath.cyclops.data.collections.extensions.FluentMapX;
import com.oath.cyclops.types.Unwrappable;
import com.oath.cyclops.types.foldable.Folds;
import com.oath.cyclops.types.foldable.To;
import com.oath.cyclops.types.functor.BiTransformable;
import com.oath.cyclops.types.functor.Transformable;
import com.oath.cyclops.types.persistent.PersistentMap;
import com.oath.cyclops.types.reactive.ReactiveStreamsTerminalOperations;
import com.oath.cyclops.types.recoverable.OnEmpty;
import com.oath.cyclops.types.recoverable.OnEmptySwitch;
import com.oath.cyclops.types.traversable.IterableFilterable;
import cyclops.companion.Streams;
import cyclops.data.tuple.Tuple;
import cyclops.data.tuple.Tuple2;
import cyclops.reactive.ReactiveSeq;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.oath.cyclops.data.collections.extensions.standard.MapXImpl;

/**
 * An eXtended Map type, that offers additional eagerly executed functional style operators such as bimap, filter and more
 *
 * @author johnmcclean
 *
 * @param <K> Key type
 * @param <V> Value type
 */
public interface MapX<K, V> extends To<MapX<K,V>>,Map<K, V>, Unwrappable, FluentMapX<K, V>, BiTransformable<K, V>, Transformable<V>, IterableFilterable<Tuple2<K, V>>, OnEmpty<Tuple2<K, V>>,
    OnEmptySwitch<Tuple2<K, V>, Map<K, V>>, Publisher<Tuple2<K, V>>, Folds<Tuple2<K, V>>,ReactiveStreamsTerminalOperations<Tuple2<K,V>> {


    /**
     *
     * @return A Collector that generates a mutable Map from a Collection of Tuple2
     */
    static <K, V> Collector<Tuple2<? extends K, ? extends V>, ?, Map<K, V>> defaultCollector() {
        return Collectors.toMap(t -> t._1(), t -> t._2());
    }

    /**
     * @return A Collector that generates an Immutable Map from a Collection of Tuple2
     */
    static <K, V> Collector<Tuple2<? extends K, ? extends V>, ?, Map<K, V>> immutableCollector() {
        return Collectors.collectingAndThen(defaultCollector(), Collections::unmodifiableMap);

    }

    /**
     * @return A Collector that generates a mutable MapX from a Collection of Tuple2
     */
    static <K, V> Collector<Tuple2<? extends K, ? extends V>, ?, Map<K, V>> toMapX() {
        return Collectors.collectingAndThen(defaultCollector(), (final Map<K, V> d) -> new MapXImpl<K, V>(
            d, defaultCollector()));

    }

    /**
     * @return The currently configured Map Collector for this MapX
     */
    public <K, V> Collector<Tuple2<? extends K, ? extends V>, ?, Map<K, V>> getCollector();

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.foldable.Folds#stream()
     */
    @Override
    default ReactiveSeq<Tuple2<K, V>> stream() {
        return ReactiveSeq.fromIterable(entrySet())
            .map(e -> Tuple.tuple(e.getKey(), e.getValue()));
    }

    /**
     * @return An zero MapX
     */
    static <K, V> MapX<K, V> empty() {
        return fromMap(new HashMap<K, V>());
    }
    static <K, V> MapX<K, V> fromPersistentMap(PersistentMap<K,V> map) {
        return fromMap(map.mapView());
    }

    /**
     * Wrap a Map in a MapX
     *
     * @param map to wrap
     * @return MapX wrapping the supplied Map
     */
    public static <K, V> MapX<K, V> fromMap(final Map<? extends K, ? extends V> map) {
        return fromMap(defaultCollector(), map);
    }

    /**
     * Wrap a transform in a MapX, also supplying a Collector for use in operations
     *
     * @param collector To generate new MapX's from
     * @param map to wrap
     * @return MapX wrapping the supplied Map
     */
    public static <K, V> MapX<K, V> fromMap(final Collector<Tuple2<? extends K, ? extends V>, ?, Map<K, V>> collector,
                                            final Map<? extends K, ? extends V> map) {
        if (map instanceof MapX)
            return (MapX) map;
        if (map instanceof Map)
            return new MapXImpl<K, V>(
                (Map) map, collector);
        return new MapXImpl<K, V>(
            Streams.stream(map)
                .map(e -> Tuple.tuple(e.getKey(), e.getValue()))
                .collect(collector),
            collector);
    }

    /**
     * Construct a new MapX with the same collector from the supplied Stream
     *
     * @param stream ot Tuples to convert into a MapX
     * @return MapX
     */
    default MapX<K, V> fromStream(final ReactiveSeq<Tuple2<K, V>> stream) {
        return new MapXImpl<K,V>(
            stream.toMap(t -> t._1(), t -> t._2()), getCollector());
    }

    /* (non-Javadoc)
     * @see java.lang.Iterable#iterator()
     */
    @Override
    default Iterator<Tuple2<K, V>> iterator() {
        return stream().iterator();
    }



    /* (non-Javadoc)
     * @see com.oath.cyclops.lambda.monads.Transformable#transform(java.util.function.Function)
     */
    default <KR, VR> MapX<KR, VR> flatMap(final BiFunction<? super K, ? super V, ? extends MapX<KR, VR>> fn) {

        final ReactiveSeq<Tuple2<KR, VR>> s = stream().flatMap(t -> fn.apply(t._1(), t._2())
            .stream());
        return new MapXImpl<>(
            s.<KR, VR> toMap(t -> t._1(), t -> t._2()), getCollector());
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.lambda.monads.Transformable#transform(java.util.function.Function)
     */
    @Override
    default <R> MapX<K, R> map(final Function<? super V, ? extends R> fn) {

        final ReactiveSeq<Tuple2<K, R>> s = stream().map(t -> t.map2(v -> fn.apply(v)));
        return new MapXImpl<>(
            s.<K, R> toMap(t -> t._1(), t -> t._2()), getCollector());
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.lambda.monads.MapX#bimap(java.util.function.Function, java.util.function.Function)
     */
    @Override
    default <R1, R2> MapX<R1, R2> bimap(final Function<? super K, ? extends R1> fn1, final Function<? super V, ? extends R2> fn2) {
        final ReactiveSeq<Tuple2<R1, V>> s1 = stream().map(t -> t.map1(v -> fn1.apply(v)));
        final ReactiveSeq<Tuple2<R1, R2>> s2 = s1.map(t -> t.map2(v -> fn2.apply(v)));
        return new MapXImpl<>(
            s2.<R1, R2> toMap(t -> t._1(), t -> t._2()), getCollector());
    }

    @Override
    int size();

    @Override
    boolean isEmpty();

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.stream.CyclopsCollectable#allMatch(java.util.function.Predicate)
     */
    @Override
    default boolean allMatch(final Predicate<? super Tuple2<K, V>> c) {
        return Folds.super.allMatch(c);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.stream.CyclopsCollectable#anyMatch(java.util.function.Predicate)
     */
    @Override
    default boolean anyMatch(final Predicate<? super Tuple2<K, V>> c) {
        return Folds.super.anyMatch(c);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.stream.CyclopsCollectable#noneMatch(java.util.function.Predicate)
     */
    @Override
    default boolean noneMatch(final Predicate<? super Tuple2<K, V>> c) {
        return Folds.super.noneMatch(c);
    }





    /* (non-Javadoc)
     * @see FluentMapX#plus(java.lang.Object, java.lang.Object)
     */
    @Override
    default MapX<K, V> plus(final K key, final V value) {
        return (MapX<K, V>) FluentMapX.super.plus(key, value);
    }

    /* (non-Javadoc)
     * @see FluentMapX#plusAll(com.oath.cyclops.types.persistent.PersistentMap)
     */
    @Override
    default MapX<K, V> plusAll(final PersistentMap<? extends K, ? extends V> map) {
        return (MapX<K, V>) FluentMapX.super.plusAll(map);
    }

    /* (non-Javadoc)
     * @see FluentMapX#plusAll(java.util.Map)
     */
    @Override
    default MapX<K, V> plusAll(final Map<? extends K, ? extends V> map) {
        return (MapX<K, V>) FluentMapX.super.plusAll(map);
    }

    /* (non-Javadoc)
     * @see FluentMapX#removeValue(java.lang.Object)
     */
    @Override
    default MapX<K, V> minus(final K key) {
        return (MapX<K, V>) FluentMapX.super.minus(key);
    }

    /* (non-Javadoc)
     * @see FluentMapX#removeAll(java.util.Collection)
     */
    @Override
    default MapX<K, V> minusAll(final Collection<? extends K> keys) {
        return (MapX<K, V>) FluentMapX.super.minusAll(keys);
    }


    /* (non-Javadoc)
     * @see com.oath.cyclops.lambda.monads.Transformable#peek(java.util.function.Consumer)
     */
    @Override
    default MapX<K, V> peek(final Consumer<? super V> c) {

        return (MapX<K, V>) Transformable.super.peek(c);
    }



    /* (non-Javadoc)
     * @see com.oath.cyclops.lambda.monads.Filters#filter(java.util.function.Predicate)
     */
    @Override
    default MapX<K, V> filter(final Predicate<? super Tuple2<K, V>> fn) {
        return stream().filter(fn).to(ReactiveConvertableSequence::converter)
            .mapX(t -> t._1(), t -> t._2());
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.lambda.monads.Filters#filterNot(java.util.function.Predicate)
     */
    @Override
    default MapX<K, V> filterNot(final Predicate<? super Tuple2<K, V>> fn) {

        return (MapX<K, V>) IterableFilterable.super.filterNot(fn);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.lambda.monads.Filters#notNull()
     */
    @Override
    default MapX<K, V> notNull() {

        return (MapX<K, V>) IterableFilterable.super.notNull();
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.lambda.monads.Filters#removeAll(java.util.stream.Stream)
     */
    @Override
    default MapX<K, V> removeStream(final Stream<? extends Tuple2<K, V>> stream) {

        return (MapX<K, V>) IterableFilterable.super.removeStream(stream);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.lambda.monads.Filters#removeAll(java.lang.Iterable)
     */
    @Override
    default MapX<K, V> removeAll(final Iterable<? extends Tuple2<K, V>> it) {

        return (MapX<K, V>) IterableFilterable.super.removeAll(it);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.lambda.monads.Filters#removeAll(java.lang.Object[])
     */
    @Override
    default MapX<K, V> removeAll(final Tuple2<K, V>... values) {

        return (MapX<K, V>) IterableFilterable.super.removeAll(values);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.lambda.monads.Filters#retainAllI(java.lang.Iterable)
     */
    @Override
    default MapX<K, V> retainAll(final Iterable<? extends Tuple2<K, V>> it) {

        return (MapX<K, V>) IterableFilterable.super.retainAll(it);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.lambda.monads.Filters#retainAllI(java.util.stream.Stream)
     */
    @Override
    default MapX<K, V> retainStream(final Stream<? extends Tuple2<K, V>> stream) {

        return (MapX<K, V>) IterableFilterable.super.retainStream(stream);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.lambda.monads.Filters#retainAllI(java.lang.Object[])
     */
    @Override
    default MapX<K, V> retainAll(final Tuple2<K, V>... values) {

        return (MapX<K, V>) IterableFilterable.super.retainAll(values);
    }


    /* (non-Javadoc)
     * @see com.oath.cyclops.types.functor.BiTransformable#bipeek(java.util.function.Consumer, java.util.function.Consumer)
     */
    @Override
    default MapX<K, V> bipeek(final Consumer<? super K> c1, final Consumer<? super V> c2) {

        return (MapX<K, V>) BiTransformable.super.bipeek(c1, c2);
    }



    /* (non-Javadoc)
     * @see com.oath.cyclops.types.traversable.Traversable#forEachAsync(org.reactivestreams.Subscriber)
     */
    @Override
    default void subscribe(final Subscriber<? super Tuple2<K, V>> s) {

        stream().subscribe(s);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.recoverable.OnEmpty#onEmpty(java.lang.Object)
     */
    @Override
    default MapX<K, V> onEmpty(final Tuple2<K, V> value) {

        return fromStream(stream().onEmpty(value));
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.recoverable.OnEmpty#onEmptyGet(java.util.function.Supplier)
     */
    @Override
    default MapX<K, V> onEmptyGet(final Supplier<? extends Tuple2<K, V>> supplier) {

        return fromStream(stream().onEmptyGet(supplier));
    }



    /* (non-Javadoc)
     * @see com.oath.cyclops.types.recoverable.OnEmptySwitch#onEmptySwitch(java.util.function.Supplier)
     */
    @Override
    default MapX<K, V> onEmptySwitch(final Supplier<? extends Map<K, V>> supplier) {
        if (isEmpty())
            return MapX.fromMap(supplier.get());
        return this;
    }

    /**
     * Convert this MapX to a ListX via the provided transformation function
     *
     * @param fn Mapping function to transform each Map entry into a single value
     * @return ListX of transformed values
     */
    default <T> ListX<T> toListX(final Function<? super Tuple2<? super K, ? super V>, ? extends T> fn) {
        return ListX.narrow(stream().map(fn).to(ReactiveConvertableSequence::converter)
            .listX());
    }

    /**
     * Convert this MapX to a SetX via the provided transformation function
     *
     * @param fn Mapping function to transform each Map entry into a single value
     * @return SetX of transformed values
     */
    default <T> SetX<T> toSetX(final Function<? super Tuple2<? super K, ? super V>, ? extends T> fn) {
        return SetX.narrow(stream().map(fn).to(ReactiveConvertableSequence::converter)
            .setX());
    }

    /**
     * Convert this MapX to a SortedSetX via the provided transformation function
     *
     * @param fn Mapping function to transform each Map entry into a single value
     * @return SortedSetX of transformed values
     */
    default <T> SortedSetX<T> toSortedSetX(final Function<? super Tuple2<? super K, ? super V>, ? extends T> fn) {
        return SortedSetX.narrow(stream().map(fn).to(ReactiveConvertableSequence::converter)
            .sortedSetX());
    }

    /**
     * Convert this MapX to a QueueX via the provided transformation function
     *
     * @param fn Mapping function to transform each Map entry into a single value
     * @return QueueX of transformed values
     */
    default <T> QueueX<T> toQueueX(final Function<? super Tuple2<? super K, ? super V>, ? extends T> fn) {
        return QueueX.narrow(stream().map(fn).to(ReactiveConvertableSequence::converter)
            .queueX());
    }

    /**
     * Convert this MapX to a DequeX via the provided transformation function
     *
     * @param fn Mapping function to transform each Map entry into a single value
     * @return DequeX of transformed values
     */
    default <T> DequeX<T> toDequeX(final Function<? super Tuple2<? super K, ? super V>, ? extends T> fn) {
        return DequeX.narrow(stream().map(fn).to(ReactiveConvertableSequence::converter)
            .dequeX());
    }

}
