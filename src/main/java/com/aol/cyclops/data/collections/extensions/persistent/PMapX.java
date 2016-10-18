package com.aol.cyclops.data.collections.extensions.persistent;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.jooq.lambda.Collectable;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;
import org.pcollections.HashTreePMap;
import org.pcollections.PMap;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import com.aol.cyclops.control.Matchable.CheckValue1;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.Trampoline;
import com.aol.cyclops.data.collections.extensions.FluentMapX;
import com.aol.cyclops.types.BiFunctor;
import com.aol.cyclops.types.Foldable;
import com.aol.cyclops.types.Functor;
import com.aol.cyclops.types.IterableFilterable;
import com.aol.cyclops.types.OnEmpty;
import com.aol.cyclops.types.OnEmptySwitch;
import com.aol.cyclops.types.stream.CyclopsCollectable;

public interface PMapX<K, V>
        extends PMap<K, V>, FluentMapX<K, V>, BiFunctor<K, V>, Functor<V>, IterableFilterable<Tuple2<K, V>>, OnEmpty<Tuple2<K, V>>,
        OnEmptySwitch<Tuple2<K, V>, PMap<K, V>>, Publisher<Tuple2<K, V>>, Foldable<Tuple2<K, V>>, CyclopsCollectable<Tuple2<K, V>> {

    public static <K, V> PMapX<K, V> empty() {
        return new PMapXImpl<K, V>(
                                   HashTreePMap.empty());
    }

    public static <K, V> PMapX<K, V> singleton(final K key, final V value) {
        return new PMapXImpl<K, V>(
                                   HashTreePMap.singleton(key, value));
    }

    public static <K, V> PMapX<K, V> fromMap(final Map<? extends K, ? extends V> map) {
        return new PMapXImpl<K, V>(
                                   HashTreePMap.from(map));
    }

    default PMapX<K, V> fromStream(final ReactiveSeq<Tuple2<K, V>> stream) {
        return stream.toPMapX(k -> k.v1, v -> v.v2);
    }

    /* (non-Javadoc)
     * @see java.lang.Iterable#iterator()
     */
    @Override
    default Iterator<Tuple2<K, V>> iterator() {
        return stream().iterator();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.sequence.traits.SequenceMCollectable#collectable()
     */
    @Override
    default Collectable<Tuple2<K, V>> collectable() {

        return stream();
    }

    /* (non-Javadoc)
     * @see org.pcollections.PMap#plus(java.lang.Object, java.lang.Object)
     */
    @Override
    PMapX<K, V> plus(K key, V value);

    /* (non-Javadoc)
     * @see org.pcollections.PMap#plusAll(java.util.Map)
     */
    @Override
    PMapX<K, V> plusAll(Map<? extends K, ? extends V> map);

    /* (non-Javadoc)
     * @see org.pcollections.PMap#minus(java.lang.Object)
     */
    @Override
    PMapX<K, V> minus(Object key);

    /* (non-Javadoc)
     * @see org.pcollections.PMap#minusAll(java.util.Collection)
     */
    @Override
    PMapX<K, V> minusAll(Collection<?> keys);

    @Override
    default ReactiveSeq<Tuple2<K, V>> stream() {

        return ReactiveSeq.fromIterable(entrySet())
                          .map(e -> Tuple.tuple(e.getKey(), e.getValue()));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Functor#map(java.util.function.Function)
     */
    @Override
    default <R> PMapX<K, R> map(final Function<? super V, ? extends R> fn) {
        return stream().map(t -> t.map2(v -> fn.apply(v)))
                       .toPMapX(t -> t.v1, t -> t.v2);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.BiFunctor#bimap(java.util.function.Function, java.util.function.Function)
     */
    @Override
    default <R1, R2> PMapX<R1, R2> bimap(final Function<? super K, ? extends R1> fn1, final Function<? super V, ? extends R2> fn2) {

        return stream().map(t -> t.map2(v -> fn2.apply(v))
                                  .map1(k -> fn1.apply(k)))
                       .toPMapX(t -> t.v1, t -> t.v2);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.BiFunctor#bipeek(java.util.function.Consumer, java.util.function.Consumer)
     */
    @Override
    default PMapX<K, V> bipeek(final Consumer<? super K> c1, final Consumer<? super V> c2) {

        return (PMapX<K, V>) BiFunctor.super.bipeek(c1, c2);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.BiFunctor#bicast(java.lang.Class, java.lang.Class)
     */
    @Override
    default <U1, U2> PMapX<U1, U2> bicast(final Class<U1> type1, final Class<U2> type2) {

        return (PMapX<U1, U2>) BiFunctor.super.bicast(type1, type2);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.BiFunctor#bitrampoline(java.util.function.Function, java.util.function.Function)
     */
    @Override
    default <R1, R2> PMapX<R1, R2> bitrampoline(final Function<? super K, ? extends Trampoline<? extends R1>> mapper1,
            final Function<? super V, ? extends Trampoline<? extends R2>> mapper2) {

        return (PMapX) BiFunctor.super.bitrampoline(mapper1, mapper2);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Functor#cast(java.lang.Class)
     */
    @Override
    default <U> PMapX<K, U> cast(final Class<? extends U> type) {

        return (PMapX<K, U>) Functor.super.cast(type);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Functor#peek(java.util.function.Consumer)
     */
    @Override
    default PMapX<K, V> peek(final Consumer<? super V> c) {

        return (PMapX<K, V>) Functor.super.peek(c);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Functor#trampoline(java.util.function.Function)
     */
    @Override
    default <R> PMapX<K, R> trampoline(final Function<? super V, ? extends Trampoline<? extends R>> mapper) {

        return (PMapX<K, R>) Functor.super.trampoline(mapper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Filterable#filter(java.util.function.Predicate)
     */
    @Override
    default PMapX<K, V> filter(final Predicate<? super Tuple2<K, V>> fn) {
        return stream().filter(fn)
                       .toPMapX(t -> t.v1, t -> t.v2);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Filterable#filterNot(java.util.function.Predicate)
     */
    @Override
    default PMapX<K, V> filterNot(final Predicate<? super Tuple2<K, V>> fn) {

        return (PMapX<K, V>) IterableFilterable.super.filterNot(fn);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Filterable#notNull()
     */
    @Override
    default PMapX<K, V> notNull() {

        return (PMapX<K, V>) IterableFilterable.super.notNull();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Filterable#removeAll(java.util.stream.Stream)
     */
    @Override
    default PMapX<K, V> removeAll(final Stream<? extends Tuple2<K, V>> stream) {

        return (PMapX<K, V>) IterableFilterable.super.removeAll(stream);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Filterable#removeAll(java.lang.Iterable)
     */
    @Override
    default PMapX<K, V> removeAll(final Iterable<? extends Tuple2<K, V>> it) {

        return (PMapX<K, V>) IterableFilterable.super.removeAll(it);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Filterable#removeAll(java.lang.Object[])
     */
    @Override
    default PMapX<K, V> removeAll(final Tuple2<K, V>... values) {

        return (PMapX<K, V>) IterableFilterable.super.removeAll(values);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Filterable#retainAll(java.lang.Iterable)
     */
    @Override
    default PMapX<K, V> retainAll(final Iterable<? extends Tuple2<K, V>> it) {

        return (PMapX<K, V>) IterableFilterable.super.retainAll(it);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Filterable#retainAll(java.util.stream.Stream)
     */
    @Override
    default PMapX<K, V> retainAll(final Stream<? extends Tuple2<K, V>> stream) {

        return (PMapX<K, V>) IterableFilterable.super.retainAll(stream);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Filterable#retainAll(java.lang.Object[])
     */
    @Override
    default PMapX<K, V> retainAll(final Tuple2<K, V>... values) {

        return (PMapX<K, V>) IterableFilterable.super.retainAll(values);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Functor#patternMatch(java.util.function.Function, java.util.function.Supplier)
     */
    @Override
    default <R> PMapX<K, R> patternMatch(final Function<CheckValue1<V, R>, CheckValue1<V, R>> case1, final Supplier<? extends R> otherwise) {

        return (PMapX<K, R>) Functor.super.patternMatch(case1, otherwise);
    }

    /* (non-Javadoc)
     * @see org.reactivestreams.Publisher#subscribe(org.reactivestreams.Subscriber)
     */
    @Override
    default void subscribe(final Subscriber<? super Tuple2<K, V>> s) {
        stream().subscribe(s);

    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.OnEmpty#onEmpty(java.lang.Object)
     */
    @Override
    default PMapX<K, V> onEmpty(final Tuple2<K, V> value) {
        return fromStream(stream().onEmpty(value));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.OnEmpty#onEmptyGet(java.util.function.Supplier)
     */
    @Override
    default PMapX<K, V> onEmptyGet(final Supplier<? extends Tuple2<K, V>> supplier) {

        return fromStream(stream().onEmptyGet(supplier));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.OnEmpty#onEmptyThrow(java.util.function.Supplier)
     */
    @Override
    default <X extends Throwable> PMapX<K, V> onEmptyThrow(final Supplier<? extends X> supplier) {

        return fromStream(stream().onEmptyThrow(supplier));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.OnEmptySwitch#onEmptySwitch(java.util.function.Supplier)
     */
    @Override
    default PMapX<K, V> onEmptySwitch(final Supplier<? extends PMap<K, V>> supplier) {
        if (isEmpty())
            return PMapX.fromMap(supplier.get());
        return this;
    }

}
