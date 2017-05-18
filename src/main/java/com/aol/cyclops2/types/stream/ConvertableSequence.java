package com.aol.cyclops2.types.stream;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import cyclops.async.LazyReact;
import cyclops.async.SimpleReact;
import cyclops.collections.immutable.*;
import cyclops.control.Eval;
import cyclops.stream.ReactiveSeq;
import cyclops.stream.Streamable;
import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;

import cyclops.companion.Reducers;
import cyclops.companion.Streams;
import cyclops.collections.immutable.OrderedSetX;
import cyclops.collections.mutable.DequeX;
import cyclops.collections.mutable.ListX;
import cyclops.collections.mutable.MapX;
import cyclops.collections.mutable.QueueX;
import cyclops.collections.mutable.SetX;
import cyclops.collections.mutable.SortedSetX;
import com.aol.cyclops2.types.Value;
import cyclops.stream.FutureStream;
import com.aol.cyclops2.types.futurestream.SimpleReactStream;

/**
 * Represents a non-scalar Data Structure that can be converted to other types
 * 
 * @author johnmcclean
 *
 * @param <T> Data types of elements in this ConvertableSequence
 */
public interface ConvertableSequence<T> extends Iterable<T> {

    default ReactiveSeq<T> stream() {
        return ReactiveSeq.fromIterable(this);
    }

    default Seq<T> seq() {
        return Seq.seq(this);
    }

    default FutureStream<T> toFutureStream(final LazyReact reactor) {
        return reactor.fromIterable(this);
    }

    default FutureStream<T> toFutureStream() {
        return new LazyReact().fromIterable(this);
    }

    default SimpleReactStream<T> toSimpleReact(final SimpleReact reactor) {
        return reactor.fromIterable(this);
    }

    default SimpleReactStream<T> toSimpleReact() {
        return new SimpleReact().fromIterable(this);
    }

    default Streamable<T> toStreamable() {
        return stream().toStreamable();
    }

    default DequeX<T> toDequeX() {
        return DequeX.fromIterable(this);
    }

    default QueueX<T> toQueueX() {
        return QueueX.fromIterable(this);
    }

    default SetX<T> toSetX() {
        return SetX.fromIterable(this);

    }

    default SortedSetX<T> toSortedSetX() {
        return SortedSetX.fromIterable(this);
    }

    default ListX<T> toListX() {
        return ListX.fromIterable(this);
    }

    default <K, V> PersistentMapX<K, V> toPMapX(final Function<? super T, ? extends K> keyMapper, final Function<? super T, ? extends V> valueMapper) {

        final ReactiveSeq<Tuple2<K, V>> stream = stream().map(t -> Tuple.tuple(keyMapper.apply(t), valueMapper.apply(t)));
        return stream.mapReduce(Reducers.toPMapX());
    }

    default <K, V> MapX<K, V> toMapX(final Function<? super T, ? extends K> keyMapper, final Function<? super T, ? extends V> valueMapper) {
        return MapX.fromMap(stream().toMap(keyMapper, valueMapper));
    }

    default LinkedListX<T> toPStackX() {
        return LinkedListX.fromIterable(this);
    }

    default VectorX<T> toPVectorX() {
        return VectorX.fromIterable(this);
    }

    default PersistentQueueX<T> toPQueueX() {
        return PersistentQueueX.fromIterable(this);
    }

    default BagX<T> toPBagX() {
        return BagX.fromIterable(this);
    }

    default PersistentSetX<T> toPSetX() {
        return PersistentSetX.fromIterable(this);
    }

    default OrderedSetX<T> toPOrderedSetX() {
        return OrderedSetX.fromIterable(this);
    }


    default Optional<ListX<T>> toOptional() {
        final ListX<T> list = toListX();
        if (list.size() == 0)
            return Optional.empty();
        return Optional.of(list);
    }

    default Value<ListX<T>> toValue() {
        return Eval.later(() -> ListX.fromIterable(Streams.stream(this)
                                                              .collect(Collectors.toList())));
    }


}
