package com.oath.cyclops.types.foldable;

import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.oath.cyclops.data.collections.extensions.CollectionX;
import com.oath.cyclops.types.stream.ToStream;
import cyclops.companion.Streams;
import cyclops.control.Option;
import cyclops.control.Eval;
import cyclops.control.Maybe;
import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.Streamable;
import cyclops.reactive.collections.immutable.*;
import lombok.AllArgsConstructor;
import cyclops.data.tuple.Tuple;
import cyclops.data.tuple.Tuple2;

import cyclops.companion.Reducers;
import cyclops.reactive.collections.mutable.DequeX;
import cyclops.reactive.collections.mutable.ListX;
import cyclops.reactive.collections.mutable.MapX;
import cyclops.reactive.collections.mutable.QueueX;
import cyclops.reactive.collections.mutable.SetX;
import cyclops.reactive.collections.mutable.SortedSetX;
import com.oath.cyclops.types.Value;


/**
 * Represents a non-scalar Data Structure that can be converted to other types
 *
 * @author johnmcclean
 *
 * @param <T> Data types of elements in this ConvertableSequence
 */
@AllArgsConstructor
public class  ConvertableSequence<T> implements ToStream<T> {
    Iterable<T> iterable;

    @Override
    public Iterator<T> iterator() {
        return iterable.iterator();
    }





    public ReactiveSeq<T> stream() {
        return ReactiveSeq.fromIterable(iterable);
    }




    public Streamable<T> streamable() {

        return Streamable.fromIterable(iterable);
    }
    public PersistentQueueX<T> persistentQueueX(){
        return persistentQueueX(Evaluation.EAGER);
    }
    public PersistentQueueX<T> persistentQueueX(Evaluation c) {
        PersistentQueueX<T> res = PersistentQueueX.fromIterable(iterable);
        if(c== Evaluation.EAGER)
            return res.materialize();
        return res;
    }

    public PersistentSetX<T> persistentSetX(){
        return persistentSetX(Evaluation.EAGER);
    }

    public PersistentSetX<T> persistentSetX(Evaluation c) {
        PersistentSetX<T> res = PersistentSetX.fromIterable(iterable);
        if(c== Evaluation.EAGER)
            return res.materialize();
        return res;
    }

    public OrderedSetX<T> orderedSetX(){
        return orderedSetX(Evaluation.EAGER);
    }
    public OrderedSetX<T> orderedSetX(Evaluation c) {
        OrderedSetX<T> res = OrderedSetX.fromIterable(iterable);
        if(c== Evaluation.EAGER)
            return res.materialize();
        return res;
    }

    public BagX<T> bagX(){
        return bagX(Evaluation.LAZY);
    }
    public BagX<T> bagX(Evaluation c) {
        BagX<T> res = BagX.fromIterable(iterable);
        if(c== Evaluation.EAGER)
            return res.materialize();
        return res;
    }

    public VectorX<T> vectorX(){
        return vectorX(Evaluation.EAGER);
    }
    public VectorX<T> vectorX(Evaluation c) {
        VectorX<T> res = VectorX.fromIterable(iterable);
        if(c== Evaluation.EAGER)
            return res.materialize();
        return res;
    }

    public LinkedListX<T> linkedListX(){
        return linkedListX(Evaluation.EAGER);
    }
    public LinkedListX<T> linkedListX(Evaluation c) {
        LinkedListX<T> res = LinkedListX.fromIterable(iterable);
        if(c== Evaluation.EAGER)
            return res.materialize();
        return res;
    }

    public DequeX<T> dequeX(){
        return dequeX(Evaluation.EAGER);
    }
    public DequeX<T> dequeX(Evaluation c) {
        DequeX<T> res = DequeX.fromIterable(iterable);
        if(c== Evaluation.EAGER)
            return res.materialize();
        return res;
    }
    public SortedSetX<T> sortedSetX() {
        return sortedSetX(Evaluation.EAGER);
    }
    public SortedSetX<T> sortedSetX(Evaluation c) {
        SortedSetX<T> res = SortedSetX.fromIterable(iterable);
        if(c== Evaluation.EAGER)
            return res.materialize();
        return res;
    }

    public SetX<T> setX(){
        return setX(Evaluation.EAGER);
    }
    public SetX<T> setX(Evaluation c) {
        SetX<T> res = SetX.fromIterable(iterable);
        if(c== Evaluation.EAGER)
            return res.materialize();
        return res;
    }

    public ListX<T> listX(){
        return listX(Evaluation.EAGER);
    }
    public ListX<T> listX(Evaluation c) {
        ListX<T> res = ListX.fromIterable(iterable);
        if(Evaluation.EAGER ==c) {
            return res.materialize();
        }
        return res;
    }

    public QueueX<T> queueX(){
        return queueX(Evaluation.EAGER);
    }
    public QueueX<T> queueX(Evaluation c) {
        QueueX<T> res = QueueX.fromIterable(iterable);
        if(c== Evaluation.EAGER)
            return res.materialize();
        return res;
    }



    public <K, V> PersistentMapX<K, V> persistentMapX(final Function<? super T, ? extends K> keyMapper, final Function<? super T, ? extends V> valueMapper) {

        final ReactiveSeq<Tuple2<K, V>> stream = stream().map(t -> Tuple.tuple(keyMapper.apply(t), valueMapper.apply(t)));
        return stream.mapReduce(Reducers.toPMapX());
    }

    public <K, V> MapX<K, V> mapX(final Function<? super T, ? extends K> keyMapper, final Function<? super T, ? extends V> valueMapper) {
        return MapX.fromMap(stream().collect(Collectors.toMap(keyMapper, valueMapper)));
    }

    public Maybe<ListX<T>> maybe() {
        return value().toMaybe();

    }
    public Option<ListX<T>> option() {
        final ListX<T> list = listX();
        if (list.size() == 0)
            return Option.none();
        return Option.of(list);
    }

    public Optional<ListX<T>> optional() {
        final ListX<T> list = listX();
        if (list.size() == 0)
            return Optional.empty();
        return Optional.of(list);
    }

    public Value<ListX<T>> value() {
        return Eval.later(() -> listX());
    }
    public Maybe<T> firstValue() {
        return Eval.later(() -> listX(Evaluation.LAZY)).toMaybe()
                                       .flatMap(l->l.size()==0? Maybe.nothing() : Maybe.just(l.firstValue(null)));
    }
    /**
     * Lazily converts this ReactiveSeq into a Collection. This does not trigger
     * the Stream. E.g. Collection is not thread safe on the first iteration.
     *
     * <pre>
     * {@code
     *  Collection<Integer> col = ReactiveSeq.of(1, 2, 3, 4, 5)
     *                                       .peek(System.out::println)
     *                                       .lazyCollection();
     *
     *  col.forEach(System.out::println);
     * }
     *
     * // Will print out "first!" before anything else
     * </pre>
     *
     * @return
     */
    public CollectionX<T> lazyCollection() {
        return Streams.toLazyCollection(ReactiveSeq.fromIterable(iterable));
    }

    /**
     * Lazily converts this ReactiveSeq into a Collection. This does not trigger
     * the Stream. E.g.
     *
     * <pre>
     * {@code
     *  Collection<Integer> col = ReactiveSeq.of(1, 2, 3, 4, 5)
     *                                       .peek(System.out::println)
     *                                       .lazyCollectionSynchronized();
     *
     *  col.forEach(System.out::println);
     * }
     *
     * // Will print out "first!" before anything else
     * </pre>
     *
     * @return
     */
    public CollectionX<T> lazyCollectionSynchronized() {
        return Streams.toConcurrentLazyCollection(ReactiveSeq.fromIterable(iterable));
    }
    public Streamable<T> lazyStreamable() {
        return Streams.toLazyStreamable(ReactiveSeq.fromIterable(iterable));
    }


    /**
     * <pre>
     * {@code
     *  Streamable<Integer> repeat = ReactiveSeq.of(1, 2, 3, 4, 5, 6).map(i -> i + 2).lazyStreamableSynchronized();
     *
     *  assertThat(repeat.stream().toList(), equalTo(Arrays.asList(2, 4, 6, 8, 10, 12)));
     *  assertThat(repeat.stream().toList(), equalTo(Arrays.asList(2, 4, 6, 8, 10, 12)));
     * }
     * </pre>
     *
     * @return Streamable that replay this ReactiveSeq, populated lazily and can
     *         be populated across threads
     */
    public Streamable<T> lazyStreamableSynchronized() {
        return Streams.toConcurrentLazyStreamable(ReactiveSeq.fromIterable(iterable));

    }


    public <C extends Collection<T>> C collection(final Supplier<C> factory) {
        return ReactiveSeq.fromIterable(iterable).collect(Collectors.toCollection(factory));
    }


}
