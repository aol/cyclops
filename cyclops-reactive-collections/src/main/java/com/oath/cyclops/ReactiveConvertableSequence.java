package com.oath.cyclops;

import com.oath.cyclops.data.collections.extensions.CollectionX;
import com.oath.cyclops.data.collections.extensions.CollectionXImpl;
import com.oath.cyclops.types.Value;
import com.oath.cyclops.types.foldable.ConvertableSequence;
import com.oath.cyclops.types.foldable.Evaluation;
import com.oath.cyclops.types.stream.ToStream;
import cyclops.companion.Reducers;
import cyclops.companion.Streams;
import cyclops.control.Eval;
import cyclops.control.Maybe;
import cyclops.control.Option;
import cyclops.data.tuple.Tuple;
import cyclops.data.tuple.Tuple2;
import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.Streamable;
import cyclops.reactive.collections.immutable.*;
import cyclops.reactive.collections.mutable.*;
import lombok.AllArgsConstructor;

import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;


/**
 * Represents a non-scalar Data Structure that can be converted to other types
 *
 * @author johnmcclean
 *
 * @param <T> Data types of elements in this ConvertableSequence
 */
public class ReactiveConvertableSequence<T> extends ConvertableSequence<T> {

    public static <T> ReactiveConvertableSequence<T> converter(Iterable<T> it){
        return new ReactiveConvertableSequence<>(it);
    }

    public ReactiveConvertableSequence(Iterable<T> iterable) {
        super(iterable);
    }

    public PersistentQueueX<T> persistentQueueX(){
        return persistentQueueX(Evaluation.EAGER);
    }
    public PersistentQueueX<T> persistentQueueX(Evaluation c) {
        PersistentQueueX<T> res = PersistentQueueX.fromIterable(getIterable());
        if(c== Evaluation.EAGER)
            return res.materialize();
        return res;
    }

    public PersistentSetX<T> persistentSetX(){
        return persistentSetX(Evaluation.EAGER);
    }

    public PersistentSetX<T> persistentSetX(Evaluation c) {
        PersistentSetX<T> res = PersistentSetX.fromIterable(getIterable());
        if(c== Evaluation.EAGER)
            return res.materialize();
        return res;
    }

    public OrderedSetX<T> orderedSetX(){
        return orderedSetX(Evaluation.EAGER);
    }
    public OrderedSetX<T> orderedSetX(Evaluation c) {
        OrderedSetX<T> res = OrderedSetX.fromIterable(getIterable());
        if(c== Evaluation.EAGER)
            return res.materialize();
        return res;
    }

    public BagX<T> bagX(){
        return bagX(Evaluation.LAZY);
    }
    public BagX<T> bagX(Evaluation c) {
        BagX<T> res = BagX.fromIterable(getIterable());
        if(c== Evaluation.EAGER)
            return res.materialize();
        return res;
    }

    public VectorX<T> vectorX(){
        return vectorX(Evaluation.EAGER);
    }
    public VectorX<T> vectorX(Evaluation c) {
        VectorX<T> res = VectorX.fromIterable(getIterable());
        if(c== Evaluation.EAGER)
            return res.materialize();
        return res;
    }

    public LinkedListX<T> linkedListX(){
        return linkedListX(Evaluation.EAGER);
    }
    public LinkedListX<T> linkedListX(Evaluation c) {
        LinkedListX<T> res = LinkedListX.fromIterable(getIterable());
        if(c== Evaluation.EAGER)
            return res.materialize();
        return res;
    }

    public DequeX<T> dequeX(){
        return dequeX(Evaluation.EAGER);
    }
    public DequeX<T> dequeX(Evaluation c) {
        DequeX<T> res = DequeX.fromIterable(getIterable());
        if(c== Evaluation.EAGER)
            return res.materialize();
        return res;
    }
    public SortedSetX<T> sortedSetX() {
        return sortedSetX(Evaluation.EAGER);
    }
    public SortedSetX<T> sortedSetX(Evaluation c) {
        SortedSetX<T> res = SortedSetX.fromIterable(getIterable());
        if(c== Evaluation.EAGER)
            return res.materialize();
        return res;
    }

    public SetX<T> setX(){
        return setX(Evaluation.EAGER);
    }
    public SetX<T> setX(Evaluation c) {
        SetX<T> res = SetX.fromIterable(getIterable());
        if(c== Evaluation.EAGER)
            return res.materialize();
        return res;
    }

    public ListX<T> listX(){
        return listX(Evaluation.EAGER);
    }
    public ListX<T> listX(Evaluation c) {
        ListX<T> res = ListX.fromIterable(getIterable());
        if(Evaluation.EAGER ==c) {
            return res.materialize();
        }
        return res;
    }

    public QueueX<T> queueX(){
        return queueX(Evaluation.EAGER);
    }
    public QueueX<T> queueX(Evaluation c) {
        QueueX<T> res = QueueX.fromIterable(getIterable());
        if(c== Evaluation.EAGER)
            return res.materialize();
        return res;
    }


    public Maybe<ListX<T>> maybeListX() {
        return value().toMaybe();

    }
    public Option<ListX<T>> optionListX() {
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
        return new CollectionXImpl<>(Streams.toLazyCollection(ReactiveSeq.fromIterable(getIterable())));
    }


    public Streamable<T> lazyStreamable() {
        return Streams.toLazyStreamable(ReactiveSeq.fromIterable(getIterable()));
    }





    public <C extends Collection<T>> C collection(final Supplier<C> factory) {
        return ReactiveSeq.fromIterable(getIterable()).collect(Collectors.toCollection(factory));
    }


}
