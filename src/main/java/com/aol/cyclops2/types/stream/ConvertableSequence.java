package com.aol.cyclops2.types.stream;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import cyclops.async.LazyReact;
import cyclops.async.SimpleReact;
import cyclops.collections.immutable.*;
import cyclops.control.Eval;
import cyclops.control.Maybe;
import cyclops.stream.ReactiveSeq;
import cyclops.stream.Streamable;
import lombok.AllArgsConstructor;
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
@AllArgsConstructor
public class  ConvertableSequence<T> {
    Iterable<T> iterable;

    public static enum Conversion { MATERIALIZED, LAZY }
    public ReactiveSeq<T> stream() {
        return ReactiveSeq.fromIterable(iterable);
    }

    public Seq<T> seq() {
        if(iterable instanceof Seq){
            return (Seq<T>)iterable;
        }
        return Seq.seq(iterable);
    }

    public FutureStream<T> toFutureStream(final LazyReact reactor) {
        return reactor.fromIterable(iterable);
    }

    public FutureStream<T> toFutureStream() {
        return toFutureStream(new LazyReact());
    }

    public SimpleReactStream<T> toSimpleReact(final SimpleReact reactor) {

        return reactor.fromIterable(iterable);
    }

    public SimpleReactStream<T> toSimpleReact() {
        return toSimpleReact(new SimpleReact());
    }

    public Streamable<T> toStreamable() {

        return Streamable.fromIterable(iterable);
    }
    public PersistentQueueX<T> toPersistentQueueX(){
        return toPersistentQueueX(Conversion.LAZY);
    }
    public PersistentQueueX<T> toPersistentQueueX(Conversion c) {
        PersistentQueueX<T> res = PersistentQueueX.fromIterable(iterable);
        if(c==Conversion.MATERIALIZED)
            return res.materialize();
        return res;
    }

    public PersistentSetX<T> toPersistentSetX(){
        return toPersistentSetX(Conversion.LAZY);
    }

    public PersistentSetX<T> toPersistentSetX(Conversion c) {
        PersistentSetX<T> res = PersistentSetX.fromIterable(iterable);
        if(c==Conversion.MATERIALIZED)
            return res.materialize();
        return res;
    }

    public OrderedSetX<T> toOrderedSetX(){
        return toOrderedSetX(Conversion.LAZY);
    }
    public OrderedSetX<T> toOrderedSetX(Conversion c) {
        OrderedSetX<T> res = OrderedSetX.fromIterable(iterable);
        if(c==Conversion.MATERIALIZED)
            return res.materialize();
        return res;
    }

    public BagX<T> toBagX(){
        return toBagX(Conversion.LAZY);
    }
    public BagX<T> toBagX(Conversion c) {
        BagX<T> res = BagX.fromIterable(iterable);
        if(c==Conversion.MATERIALIZED)
            return res.materialize();
        return res;
    }

    public VectorX<T> toVectorX(){
        return toVectorX(Conversion.LAZY);
    }
    public VectorX<T> toVectorX(Conversion c) {
        VectorX<T> res = VectorX.fromIterable(iterable);
        if(c==Conversion.MATERIALIZED)
            return res.materialize();
        return res;
    }

    public LinkedListX<T> toLinkedListX(){
        return toLinkedListX(Conversion.LAZY);
    }
    public LinkedListX<T> toLinkedListX(Conversion c) {
        LinkedListX<T> res = LinkedListX.fromIterable(iterable);
        if(c==Conversion.MATERIALIZED)
            return res.materialize();
        return res;
    }

    public DequeX<T> toDequeX(){
        return toDequeX(Conversion.LAZY);
    }
    public DequeX<T> toDequeX(Conversion c) {
        DequeX<T> res = DequeX.fromIterable(iterable);
        if(c==Conversion.MATERIALIZED)
            return res.materialize();
        return res;
    }
    public SortedSetX<T> toSortedSetX() {
        return toSortedSetX(Conversion.LAZY);
    }
    public SortedSetX<T> toSortedSetX(Conversion c) {
        SortedSetX<T> res = SortedSetX.fromIterable(iterable);
        if(c==Conversion.MATERIALIZED)
            return res.materialize();
        return res;
    }

    public SetX<T> toSetX(){
        return toSetX(Conversion.LAZY);
    }
    public SetX<T> toSetX(Conversion c) {
        SetX<T> res = SetX.fromIterable(iterable);
        if(c==Conversion.MATERIALIZED)
            return res.materialize();
        return res;
    }

    public ListX<T> toListX(){
        return toListX(Conversion.LAZY);
    }
    public ListX<T> toListX(Conversion c) {
        ListX<T> res = ListX.fromIterable(iterable);
        if(c==Conversion.MATERIALIZED)
            return res.materialize();
        return res;
    }

    public QueueX<T> toQueueX(){
        return toQueueX(Conversion.LAZY);
    }
    public QueueX<T> toQueueX(Conversion c) {
        QueueX<T> res = QueueX.fromIterable(iterable);
        if(c==Conversion.MATERIALIZED)
            return res.materialize();
        return res;
    }



    public <K, V> PersistentMapX<K, V> toPersistentMapX(final Function<? super T, ? extends K> keyMapper, final Function<? super T, ? extends V> valueMapper) {

        final ReactiveSeq<Tuple2<K, V>> stream = stream().map(t -> Tuple.tuple(keyMapper.apply(t), valueMapper.apply(t)));
        return stream.mapReduce(Reducers.toPMapX());
    }

    public <K, V> MapX<K, V> toMapX(final Function<? super T, ? extends K> keyMapper, final Function<? super T, ? extends V> valueMapper) {
        return MapX.fromMap(stream().toMap(keyMapper, valueMapper));
    }
    public Maybe<ListX<T>> maybe() {
        return toValue().toMaybe();

    }


    public Optional<ListX<T>> toOptional() {
        final ListX<T> list = toListX();
        if (list.size() == 0)
            return Optional.empty();
        return Optional.of(list);
    }

    public Value<ListX<T>> toValue() {
        return Eval.later(() -> toListX());
    }
    public Maybe<T> firstValue() {
        return Eval.later(() -> toListX(Conversion.LAZY)).toMaybe()
                                       .flatMap(l->l.size()==0? Maybe.none() : Maybe.just(l.firstValue()));
    }


}
