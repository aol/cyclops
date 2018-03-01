package com.oath.cyclops.types.foldable;

import com.oath.cyclops.types.stream.ToStream;
import cyclops.companion.Streams;
import cyclops.control.Eval;
import cyclops.control.Maybe;
import cyclops.control.Option;
import cyclops.data.*;
import cyclops.data.tuple.Tuple;
import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.Streamable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
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
@AllArgsConstructor
public class  ConvertableSequence<T> implements ToStream<T> {
    @Getter(AccessLevel.PROTECTED)
    private final Iterable<T> iterable;


    @Override
    public Iterator<T> iterator() {
        return iterable.iterator();
    }





    public ReactiveSeq<T> stream() {
        return ReactiveSeq.fromIterable(iterable);
    }



    public BankersQueue<T> bankersQueue(){
        return BankersQueue.fromIterable(iterable);
    }
    public Seq<T> seq(){
        return Seq.fromIterable(iterable);
    }
    public Vector<T> vector(){
        return Vector.fromIterable(iterable);
    }
    public LazySeq<T> lazySeq(){
        return LazySeq.fromIterable(iterable);
    }
    public HashSet<T> hashSet(){
        return HashSet.fromIterable(iterable);
    }
    public Bag<T> bag(){
        return Bag.fromIterable(iterable);
    }
    public <K,V> HashMap<K,V> hashMap(Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends V> valueMapper){
        return HashMap.fromStream(stream().map(t-> Tuple.tuple(keyMapper.apply(t),valueMapper.apply(t))));
    }
    public TreeSet<T> treeSet(Comparator<T> comp){
        return TreeSet.fromIterable(iterable,comp);
    }


    public Maybe<LazySeq<T>> maybe() {
        return Maybe.fromEval(Eval.later(()->
            iterator().hasNext() ? Maybe.just(lazySeq()) : Maybe.<LazySeq<T>>nothing()
        )).flatMap(i->i);

    }
    public Option<LazySeq<T>> option() {
        return iterator().hasNext() ? Option.<LazySeq<T>>some(lazySeq()) : Option.<LazySeq<T>>none();
    }



    public Maybe<T> firstValue() {
       return Maybe.fromIterable(iterable);
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
    public Collection<T> lazyCollection() {
        return Streams.toLazyCollection(ReactiveSeq.fromIterable(iterable));
    }


    public Streamable<T> streamable() {
        return Streams.toLazyStreamable(ReactiveSeq.fromIterable(iterable));
    }


    public LazyString lazyString(){
        LazySeq<Character> x = lazySeq().flatMap(i -> LazyString.of(i.toString() + ", "));
        return LazyString.fromLazySeq(x).dropRight(2);
    }


    public <C extends Collection<T>> C collection(final Supplier<C> factory) {
        return ReactiveSeq.fromIterable(iterable).collect(Collectors.toCollection(factory));
    }


}
