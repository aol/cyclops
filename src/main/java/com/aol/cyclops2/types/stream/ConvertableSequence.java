package com.aol.cyclops2.types.stream;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import cyclops.async.LazyReact;
import cyclops.async.SimpleReact;
import cyclops.control.lazy.Eval;
import cyclops.stream.ReactiveSeq;
import cyclops.stream.Streamable;
import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;

import cyclops.Reducers;
import cyclops.Streams;
import cyclops.collections.immutable.PBagX;
import cyclops.collections.immutable.PMapX;
import cyclops.collections.immutable.POrderedSetX;
import cyclops.collections.immutable.PQueueX;
import cyclops.collections.immutable.PSetX;
import cyclops.collections.immutable.PStackX;
import cyclops.collections.immutable.PVectorX;
import cyclops.collections.DequeX;
import cyclops.collections.ListX;
import cyclops.collections.MapX;
import cyclops.collections.QueueX;
import cyclops.collections.SetX;
import cyclops.collections.SortedSetX;
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

    default <K, V> PMapX<K, V> toPMapX(final Function<? super T, ? extends K> keyMapper, final Function<? super T, ? extends V> valueMapper) {

        final ReactiveSeq<Tuple2<K, V>> stream = stream().map(t -> Tuple.tuple(keyMapper.apply(t), valueMapper.apply(t)));
        return stream.mapReduce(Reducers.toPMapX());
    }

    default <K, V> MapX<K, V> toMapX(final Function<? super T, ? extends K> keyMapper, final Function<? super T, ? extends V> valueMapper) {
        return MapX.fromMap(stream().toMap(keyMapper, valueMapper));
    }

    default PStackX<T> toPStackX() {
        return PStackX.fromIterable(this);
    }

    default PVectorX<T> toPVectorX() {
        return PVectorX.fromIterable(this);
    }

    default PQueueX<T> toPQueueX() {
        return PQueueX.fromIterable(this);
    }

    default PBagX<T> toPBagX() {
        return PBagX.fromIterable(this);
    }

    default PSetX<T> toPSetX() {
        return PSetX.fromIterable(this);
    }

    default POrderedSetX<T> toPOrderedSetX() {
        return POrderedSetX.fromIterable(this);
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
