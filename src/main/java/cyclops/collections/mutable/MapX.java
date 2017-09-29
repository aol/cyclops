package cyclops.collections.mutable;

import com.aol.cyclops2.data.collections.extensions.standard.MapXImpl;
import com.aol.cyclops2.types.foldable.Folds;
import com.aol.cyclops2.types.foldable.To;
import com.aol.cyclops2.types.functor.BiTransformable;
import com.aol.cyclops2.types.functor.Transformable;
import com.aol.cyclops2.types.recoverable.OnEmpty;
import com.aol.cyclops2.types.recoverable.OnEmptySwitch;
import com.aol.cyclops2.types.traversable.IterableFilterable;
import cyclops.stream.ReactiveSeq;
import cyclops.companion.Streams;
import cyclops.control.Trampoline;
import com.aol.cyclops2.data.collections.extensions.FluentMapX;
import com.aol.cyclops2.types.*;
import com.aol.cyclops2.types.foldable.CyclopsCollectable;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * An eXtended Map type, that offers additional eagerly executed functional style operators such as bimap, filter and more
 * 
 * @author johnmcclean
 *
 * @param <K> Key type
 * @param <V> Value type
 */
public interface MapX<K, V> extends To<MapX<K,V>>,Map<K, V>,Unwrapable, FluentMapX<K, V>, BiTransformable<K, V>, Transformable<V>, IterableFilterable<Tuple2<K, V>>, OnEmpty<Tuple2<K, V>>,
        OnEmptySwitch<Tuple2<K, V>, Map<K, V>>, Publisher<Tuple2<K, V>>, Folds<Tuple2<K, V>>, CyclopsCollectable<Tuple2<K, V>> {


    /**
     * 
     * @return A Collector that generates a mutable Map from a Collection of Tuple2
     */
    static <K, V> Collector<Tuple2<? extends K, ? extends V>, ?, Map<K, V>> defaultCollector() {
        return Collectors.toMap(t -> t.v1, t -> t.v2);
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
     * @see com.aol.cyclops2.types.foldable.Folds#reactiveStream()
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
     * Wrap a map in a MapX, also supplying a Collector for use in operations
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
     * Construct a new MapX with the same collector from the supplied LazyList
     * 
     * @param stream ot Tuples to convert into a MapX
     * @return MapX
     */
    default MapX<K, V> fromStream(final ReactiveSeq<Tuple2<K, V>> stream) {
        return new MapXImpl<K,V>(
                              stream.toMap(t -> t.v1, t -> t.v2), getCollector());
    }

    /* (non-Javadoc)
     * @see java.lang.Iterable#iterator()
     */
    @Override
    default Iterator<Tuple2<K, V>> iterator() {
        return stream().iterator();
    }



    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Transformable#map(java.util.function.Function)
     */
    default <KR, VR> MapX<KR, VR> flatMap(final BiFunction<? super K, ? super V, ? extends MapX<KR, VR>> fn) {

        final ReactiveSeq<Tuple2<KR, VR>> s = stream().flatMap(t -> fn.apply(t.v1, t.v2)
                                                                      .stream());
        return new MapXImpl<>(
                              s.<KR, VR> toMap(t -> t.v1, t -> t.v2), getCollector());
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Transformable#map(java.util.function.Function)
     */
    @Override
    default <R> MapX<K, R> map(final Function<? super V, ? extends R> fn) {

        final ReactiveSeq<Tuple2<K, R>> s = stream().map(t -> t.map2(v -> fn.apply(v)));
        return new MapXImpl<>(
                              s.<K, R> toMap(t -> t.v1, t -> t.v2), getCollector());
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.MapX#bimap(java.util.function.Function, java.util.function.Function)
     */
    @Override
    default <R1, R2> MapX<R1, R2> bimap(final Function<? super K, ? extends R1> fn1, final Function<? super V, ? extends R2> fn2) {
        final ReactiveSeq<Tuple2<R1, V>> s1 = stream().map(t -> t.map1(v -> fn1.apply(v)));
        final ReactiveSeq<Tuple2<R1, R2>> s2 = s1.map(t -> t.map2(v -> fn2.apply(v)));
        return new MapXImpl<>(
                              s2.<R1, R2> toMap(t -> t.v1, t -> t.v2), getCollector());
    }

    @Override
    int size();

    @Override
    boolean isEmpty();

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.reactiveStream.CyclopsCollectable#allMatch(java.util.function.Predicate)
     */
    @Override
    default boolean allMatch(final Predicate<? super Tuple2<K, V>> c) {
        return CyclopsCollectable.super.allMatch(c);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.reactiveStream.CyclopsCollectable#anyMatch(java.util.function.Predicate)
     */
    @Override
    default boolean anyMatch(final Predicate<? super Tuple2<K, V>> c) {
        return CyclopsCollectable.super.anyMatch(c);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.reactiveStream.CyclopsCollectable#noneMatch(java.util.function.Predicate)
     */
    @Override
    default boolean noneMatch(final Predicate<? super Tuple2<K, V>> c) {
        return CyclopsCollectable.super.noneMatch(c);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.reactiveStream.CyclopsCollectable#max(java.util.Comparator)
     */
    @Override
    default Optional<Tuple2<K, V>> max(final Comparator<? super Tuple2<K, V>> comparator) {
        return CyclopsCollectable.super.max(comparator);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.reactiveStream.CyclopsCollectable#min(java.util.Comparator)
     */
    @Override
    default Optional<Tuple2<K, V>> min(final Comparator<? super Tuple2<K, V>> comparator) {
        return CyclopsCollectable.super.min(comparator);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.FluentMapX#plus(java.lang.Object, java.lang.Object)
     */
    @Override
    default MapX<K, V> plus(final K key, final V value) {
        return (MapX<K, V>) FluentMapX.super.plus(key, value);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.FluentMapX#plusAll(java.util.Map)
     */
    @Override
    default MapX<K, V> plusAll(final Map<? extends K, ? extends V> map) {
        return (MapX<K, V>) FluentMapX.super.plusAll(map);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.FluentMapX#minus(java.lang.Object)
     */
    @Override
    default MapX<K, V> minus(final Object key) {
        return (MapX<K, V>) FluentMapX.super.minus(key);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.data.collections.extensions.FluentMapX#minusAll(java.util.Collection)
     */
    @Override
    default MapX<K, V> minusAll(final Collection<?> keys) {
        return (MapX<K, V>) FluentMapX.super.minusAll(keys);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Transformable#cast(java.lang.Class)
     */
    @Override
    default <U> MapX<K, U> cast(final Class<? extends U> type) {

        return (MapX<K, U>) Transformable.super.cast(type);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Transformable#peek(java.util.function.Consumer)
     */
    @Override
    default MapX<K, V> peek(final Consumer<? super V> c) {

        return (MapX<K, V>) Transformable.super.peek(c);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Transformable#trampoline(java.util.function.Function)
     */
    @Override
    default <R> MapX<K, R> trampoline(final Function<? super V, ? extends Trampoline<? extends R>> mapper) {

        return (MapX<K, R>) Transformable.super.trampoline(mapper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Filters#filter(java.util.function.Predicate)
     */
    @Override
    default MapX<K, V> filter(final Predicate<? super Tuple2<K, V>> fn) {
        return stream().filter(fn).to()
                       .mapX(t -> t.v1, t -> t.v2);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Filters#filterNot(java.util.function.Predicate)
     */
    @Override
    default MapX<K, V> filterNot(final Predicate<? super Tuple2<K, V>> fn) {

        return (MapX<K, V>) IterableFilterable.super.filterNot(fn);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Filters#notNull()
     */
    @Override
    default MapX<K, V> notNull() {

        return (MapX<K, V>) IterableFilterable.super.notNull();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Filters#removeAll(java.util.reactiveStream.LazyList)
     */
    @Override
    default MapX<K, V> removeAllS(final Stream<? extends Tuple2<K, V>> stream) {

        return (MapX<K, V>) IterableFilterable.super.removeAllS(stream);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Filters#removeAll(java.lang.Iterable)
     */
    @Override
    default MapX<K, V> removeAllI(final Iterable<? extends Tuple2<K, V>> it) {

        return (MapX<K, V>) IterableFilterable.super.removeAllI(it);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Filters#removeAll(java.lang.Object[])
     */
    @Override
    default MapX<K, V> removeAll(final Tuple2<K, V>... values) {

        return (MapX<K, V>) IterableFilterable.super.removeAll(values);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Filters#retainAllI(java.lang.Iterable)
     */
    @Override
    default MapX<K, V> retainAllI(final Iterable<? extends Tuple2<K, V>> it) {

        return (MapX<K, V>) IterableFilterable.super.retainAllI(it);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Filters#retainAllI(java.util.reactiveStream.LazyList)
     */
    @Override
    default MapX<K, V> retainAllS(final Stream<? extends Tuple2<K, V>> stream) {

        return (MapX<K, V>) IterableFilterable.super.retainAllS(stream);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Filters#retainAllI(java.lang.Object[])
     */
    @Override
    default MapX<K, V> retainAll(final Tuple2<K, V>... values) {

        return (MapX<K, V>) IterableFilterable.super.retainAll(values);
    }


    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.functor.BiTransformable#bipeek(java.util.function.Consumer, java.util.function.Consumer)
     */
    @Override
    default MapX<K, V> bipeek(final Consumer<? super K> c1, final Consumer<? super V> c2) {

        return (MapX<K, V>) BiTransformable.super.bipeek(c1, c2);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.functor.BiTransformable#bicast(java.lang.Class, java.lang.Class)
     */
    @Override
    default <U1, U2> MapX<U1, U2> bicast(final Class<U1> type1, final Class<U2> type2) {

        return (MapX<U1, U2>) BiTransformable.super.bicast(type1, type2);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.functor.BiTransformable#bitrampoline(java.util.function.Function, java.util.function.Function)
     */
    @Override
    default <R1, R2> MapX<R1, R2> bitrampoline(final Function<? super K, ? extends Trampoline<? extends R1>> mapper1,
            final Function<? super V, ? extends Trampoline<? extends R2>> mapper2) {

        return (MapX<R1, R2>) BiTransformable.super.bitrampoline(mapper1, mapper2);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.traversable.Traversable#forEachAsync(org.reactivestreams.Subscriber)
     */
    @Override
    default void subscribe(final Subscriber<? super Tuple2<K, V>> s) {

        stream().subscribe(s);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.recoverable.OnEmpty#onEmpty(java.lang.Object)
     */
    @Override
    default MapX<K, V> onEmpty(final Tuple2<K, V> value) {

        return fromStream(stream().onEmpty(value));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.recoverable.OnEmpty#onEmptyGet(java.util.function.Supplier)
     */
    @Override
    default MapX<K, V> onEmptyGet(final Supplier<? extends Tuple2<K, V>> supplier) {

        return fromStream(stream().onEmptyGet(supplier));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.recoverable.OnEmpty#onEmptyThrow(java.util.function.Supplier)
     */
    @Override
    default <X extends Throwable> MapX<K, V> onEmptyThrow(final Supplier<? extends X> supplier) {

        return fromStream(stream().onEmptyThrow(supplier));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.recoverable.OnEmptySwitch#onEmptySwitch(java.util.function.Supplier)
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
     * @param fn Mapping function to transform each Map entry into a singleUnsafe value
     * @return ListX of transformed values
     */
    default <T> ListX<T> toListX(final Function<? super Tuple2<? super K, ? super V>, ? extends T> fn) {
        return ListX.narrow(stream().map(fn).to()
                                    .listX());
    }

    /**
     * Convert this MapX to a SetX via the provided transformation function
     * 
     * @param fn Mapping function to transform each Map entry into a singleUnsafe value
     * @return SetX of transformed values
     */
    default <T> SetX<T> toSetX(final Function<? super Tuple2<? super K, ? super V>, ? extends T> fn) {
        return SetX.narrow(stream().map(fn).to()
                                   .setX());
    }

    /**
     * Convert this MapX to a SortedSetX via the provided transformation function
     * 
     * @param fn Mapping function to transform each Map entry into a singleUnsafe value
     * @return SortedSetX of transformed values
     */
    default <T> SortedSetX<T> toSortedSetX(final Function<? super Tuple2<? super K, ? super V>, ? extends T> fn) {
        return SortedSetX.narrow(stream().map(fn).to()
                                         .sortedSetX());
    }

    /**
     * Convert this MapX to a QueueX via the provided transformation function
     * 
     * @param fn Mapping function to transform each Map entry into a singleUnsafe value
     * @return QueueX of transformed values
     */
    default <T> QueueX<T> toQueueX(final Function<? super Tuple2<? super K, ? super V>, ? extends T> fn) {
        return QueueX.narrow(stream().map(fn).to()
                                     .queueX());
    }

    /**
     * Convert this MapX to a DequeX via the provided transformation function
     * 
     * @param fn Mapping function to transform each Map entry into a singleUnsafe value
     * @return DequeX of transformed values
     */
    default <T> DequeX<T> toDequeX(final Function<? super Tuple2<? super K, ? super V>, ? extends T> fn) {
        return DequeX.narrow(stream().map(fn).to()
                                     .dequeX());
    }

}
