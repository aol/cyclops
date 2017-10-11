package cyclops.collections.immutable;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.aol.cyclops2.data.collections.extensions.persistent.PMapXImpl;
import com.aol.cyclops2.types.*;
import com.aol.cyclops2.types.foldable.Folds;
import com.aol.cyclops2.types.foldable.To;
import com.aol.cyclops2.types.functor.BiTransformable;
import com.aol.cyclops2.types.reactive.ReactiveStreamsTerminalOperations;
import com.aol.cyclops2.types.recoverable.OnEmpty;
import com.aol.cyclops2.types.recoverable.OnEmptySwitch;
import com.aol.cyclops2.types.traversable.IterableFilterable;
import cyclops.collections.tuple.Tuple;
import cyclops.collections.tuple.Tuple2;
import org.pcollections.HashTreePMap;
import org.pcollections.PMap;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import cyclops.stream.ReactiveSeq;
import cyclops.control.lazy.Trampoline;
import com.aol.cyclops2.data.collections.extensions.FluentMapX;
import cyclops.collections.mutable.ListX;
import com.aol.cyclops2.types.functor.Transformable;


public interface PersistentMapX<K, V>
        extends To<PersistentMapX<K,V>>,
                PMap<K, V>, Unwrapable,
                FluentMapX<K, V>, BiTransformable<K, V>, Transformable<V>, IterableFilterable<Tuple2<K, V>>, OnEmpty<Tuple2<K, V>>,
        OnEmptySwitch<Tuple2<K, V>, PMap<K, V>>, Publisher<Tuple2<K, V>>, Folds<Tuple2<K, V>>,ReactiveStreamsTerminalOperations<Tuple2<K,V>> {


    public static <K, V> PersistentMapX<K, V> empty() {
        return new PMapXImpl<K, V>(
                                   HashTreePMap.empty());
    }

    public static <K, V> PersistentMapX<K, V> singleton(final K key, final V value) {
        return new PMapXImpl<K, V>(
                                   HashTreePMap.singleton(key, value));
    }

    public static <K, V> PersistentMapX<K, V> fromMap(final Map<? extends K, ? extends V> map) {
        return new PMapXImpl<K, V>(
                                   HashTreePMap.from(map));
    }

    default PersistentMapX<K, V> fromStream(final ReactiveSeq<Tuple2<K, V>> stream) {
        return stream.to().persistentMapX(k -> k._1(), v -> v._2());
    }

    @Override
    boolean isEmpty();

    /* (non-Javadoc)
         * @see java.lang.Iterable#iterator()
         */
    @Override
    default Iterator<Tuple2<K, V>> iterator() {
        return stream().iterator();
    }



    /* (non-Javadoc)
     * @see org.pcollections.PMap#plus(java.lang.Object, java.lang.Object)
     */
    @Override
    PersistentMapX<K, V> plus(K key, V value);

    /* (non-Javadoc)
     * @see org.pcollections.PMap#plusAll(java.util.Map)
     */
    @Override
    PersistentMapX<K, V> plusAll(Map<? extends K, ? extends V> map);

    /* (non-Javadoc)
     * @see org.pcollections.PMap#minus(java.lang.Object)
     */
    @Override
    PersistentMapX<K, V> minus(Object key);

    /* (non-Javadoc)
     * @see org.pcollections.PMap#minusAll(java.util.Collection)
     */
    @Override
    PersistentMapX<K, V> minusAll(Collection<?> keys);

    @Override
    default ReactiveSeq<Tuple2<K, V>> stream() {

        return ReactiveSeq.fromIterable(entrySet())
                          .map(e -> Tuple.tuple(e.getKey(), e.getValue()));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Transformable#transform(java.util.function.Function)
     */
    @Override
    default <R> PersistentMapX<K, R> map(final Function<? super V, ? extends R> fn) {
        return stream().map(t -> t.map2(v -> fn.apply(v))).to()
                       .persistentMapX(t -> t._1(), t -> t._2());
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.BiTransformable#bimap(java.util.function.Function, java.util.function.Function)
     */
    @Override
    default <R1, R2> PersistentMapX<R1, R2> bimap(final Function<? super K, ? extends R1> fn1, final Function<? super V, ? extends R2> fn2) {

        return stream().map(t -> t.map2(v -> fn2.apply(v))
                                  .map1(k -> fn1.apply(k))).to()
                .persistentMapX(t -> t._1(), t -> t._2());
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.BiTransformable#bipeek(java.util.function.Consumer, java.util.function.Consumer)
     */
    @Override
    default PersistentMapX<K, V> bipeek(final Consumer<? super K> c1, final Consumer<? super V> c2) {

        return (PersistentMapX<K, V>) BiTransformable.super.bipeek(c1, c2);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.BiTransformable#bicast(java.lang.Class, java.lang.Class)
     */
    @Override
    default <U1, U2> PersistentMapX<U1, U2> bicast(final Class<U1> type1, final Class<U2> type2) {

        return (PersistentMapX<U1, U2>) BiTransformable.super.bicast(type1, type2);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.BiTransformable#bitrampoline(java.util.function.Function, java.util.function.Function)
     */
    @Override
    default <R1, R2> PersistentMapX<R1, R2> bitrampoline(final Function<? super K, ? extends Trampoline<? extends R1>> mapper1,
                                                         final Function<? super V, ? extends Trampoline<? extends R2>> mapper2) {

        return (PersistentMapX) BiTransformable.super.bitrampoline(mapper1, mapper2);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Transformable#cast(java.lang.Class)
     */
    @Override
    default <U> PersistentMapX<K, U> cast(final Class<? extends U> type) {

        return (PersistentMapX<K, U>) Transformable.super.cast(type);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Transformable#peek(java.util.function.Consumer)
     */
    @Override
    default PersistentMapX<K, V> peek(final Consumer<? super V> c) {

        return (PersistentMapX<K, V>) Transformable.super.peek(c);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Transformable#trampoline(java.util.function.Function)
     */
    @Override
    default <R> PersistentMapX<K, R> trampoline(final Function<? super V, ? extends Trampoline<? extends R>> mapper) {

        return (PersistentMapX<K, R>) Transformable.super.trampoline(mapper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Filters#filter(java.util.function.Predicate)
     */
    @Override
    default PersistentMapX<K, V> filter(final Predicate<? super Tuple2<K, V>> fn) {
        return stream().filter(fn).to()
                .persistentMapX(t -> t._1(), t -> t._2());
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Filters#filterNot(java.util.function.Predicate)
     */
    @Override
    default PersistentMapX<K, V> filterNot(final Predicate<? super Tuple2<K, V>> fn) {

        return (PersistentMapX<K, V>) IterableFilterable.super.filterNot(fn);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Filters#notNull()
     */
    @Override
    default PersistentMapX<K, V> notNull() {

        return (PersistentMapX<K, V>) IterableFilterable.super.notNull();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Filters#removeAll(java.util.stream.Stream)
     */
    @Override
    default PersistentMapX<K, V> removeAllS(final Stream<? extends Tuple2<K, V>> stream) {

        return (PersistentMapX<K, V>) IterableFilterable.super.removeAllS(stream);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Filters#removeAll(java.lang.Iterable)
     */
    @Override
    default PersistentMapX<K, V> removeAllI(final Iterable<? extends Tuple2<K, V>> it) {

        return (PersistentMapX<K, V>) IterableFilterable.super.removeAllI(it);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Filters#removeAll(java.lang.Object[])
     */
    @Override
    default PersistentMapX<K, V> removeAll(final Tuple2<K, V>... values) {

        return (PersistentMapX<K, V>) IterableFilterable.super.removeAll(values);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Filters#retainAllI(java.lang.Iterable)
     */
    @Override
    default PersistentMapX<K, V> retainAllI(final Iterable<? extends Tuple2<K, V>> it) {

        return (PersistentMapX<K, V>) IterableFilterable.super.retainAllI(it);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Filters#retainAllI(java.util.stream.Stream)
     */
    @Override
    default PersistentMapX<K, V> retainAllS(final Stream<? extends Tuple2<K, V>> stream) {

        return (PersistentMapX<K, V>) IterableFilterable.super.retainAllS(stream);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.Filters#retainAllI(java.lang.Object[])
     */
    @Override
    default PersistentMapX<K, V> retainAll(final Tuple2<K, V>... values) {

        return (PersistentMapX<K, V>) IterableFilterable.super.retainAll(values);
    }



    /* (non-Javadoc)
     * @see org.reactivestreams.Publisher#forEachAsync(org.reactivestreams.Subscriber)
     */
    @Override
    default void subscribe(final Subscriber<? super Tuple2<K, V>> s) {
        stream().subscribe(s);

    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.recoverable.OnEmpty#onEmpty(java.lang.Object)
     */
    @Override
    default PersistentMapX<K, V> onEmpty(final Tuple2<K, V> value) {
        return fromStream(stream().onEmpty(value));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.recoverable.OnEmpty#onEmptyGet(java.util.function.Supplier)
     */
    @Override
    default PersistentMapX<K, V> onEmptyGet(final Supplier<? extends Tuple2<K, V>> supplier) {

        return fromStream(stream().onEmptyGet(supplier));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.recoverable.OnEmpty#onEmptyThrow(java.util.function.Supplier)
     */
    @Override
    default <X extends Throwable> PersistentMapX<K, V> onEmptyThrow(final Supplier<? extends X> supplier) {

        return fromStream(stream().onEmptyThrow(supplier));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.recoverable.OnEmptySwitch#onEmptySwitch(java.util.function.Supplier)
     */
    @Override
    default PersistentMapX<K, V> onEmptySwitch(final Supplier<? extends PMap<K, V>> supplier) {
        if (isEmpty())
            return PersistentMapX.fromMap(supplier.get());
        return this;
    }
    /**
     * Convert this MapX to a ListX via the provided transformation function
     * 
     * @param fn Mapping function to transform each Map entry into a singleUnsafe value
     * @return ListX of transformed values
     */
    default <T> ListX<T> toListX(final Function<? super Tuple2<? super K, ? super V>, ? extends T> fn) {
        return ListX.narrow(stream().map(fn)
                                    .toListX());
    }

    /**
     * Convert this MapX to a PersistentSetX via the provided transformation function
     * 
     * @param fn Mapping function to transform each Map entry into a singleUnsafe value
     * @return PersistentSetX of transformed values
     */
    default <T> PersistentSetX<T> toPersistentSetX(final Function<? super Tuple2<? super K, ? super V>, ? extends T> fn) {
        return PersistentSetX.narrow(stream().map(fn).to()
                .persistentSetX());
    }

    /**
     * Convert this MapX to a POrderdSetX via the provided transformation function
     * 
     * @param fn Mapping function to transform each Map entry into a singleUnsafe value
     * @return OrderedSetX of transformed values
     */
    default <T> OrderedSetX<T> toOrderedSetX(final Function<? super Tuple2<? super K, ? super V>, ? extends T> fn) {
        return OrderedSetX.narrow(stream().map(fn).to().orderedSetX());
    }

    /**
     * Convert this MapX to a QueueX via the provided transformation function
     * 
     * @param fn Mapping function to transform each Map entry into a singleUnsafe value
     * @return QueueX of transformed values
     */
    default <T> PersistentQueueX<T> toPersistentQueueX(final Function<? super Tuple2<? super K, ? super V>, ? extends T> fn) {
        return PersistentQueueX.narrow(stream().map(fn).to().persistentQueueX());
    }

    /**
     * Convert this MapX to a LinkedListX via the provided transformation function
     * 
     * @param fn Mapping function to transform each Map entry into a singleUnsafe value
     * @return LinkedListX of transformed values
     */
    default <T> LinkedListX<T> toLinkedListX(final Function<? super Tuple2<? super K, ? super V>, ? extends T> fn) {
        return LinkedListX.narrow(stream().map(fn).to().linkedListX());

    }
    /**
     * Convert this MapX to a VectorX via the provided transformation function
     * 
     * @param fn Mapping function to transform each Map entry into a singleUnsafe value
     * @return VectorX of transformed values
     */
    default <T> VectorX<T> toVectorX(final Function<? super Tuple2<? super K, ? super V>, ? extends T> fn) {
        return VectorX.narrow(stream().map(fn).to().vectorX());
    }
    /**
     * Convert this MapX to a BagX via the provided transformation function
     * 
     * @param fn Mapping function to transform each Map entry into a singleUnsafe value
     * @return BagX of transformed values
     */
    default <T> BagX<T> toBagX(final Function<? super Tuple2<? super K, ? super V>, ? extends T> fn) {
        return BagX.narrow(stream().map(fn).to().bagX());
    }

}
