package com.aol.cyclops.data.collections.extensions.persistent;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;
import org.pcollections.AmortizedPQueue;
import org.pcollections.PQueue;
import org.reactivestreams.Publisher;

import com.aol.cyclops.Monoid;
import com.aol.cyclops.Reducers;
import com.aol.cyclops.control.Matchable.CheckValue1;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.Trampoline;
import com.aol.cyclops.data.collections.extensions.FluentCollectionX;
import com.aol.cyclops.data.collections.extensions.standard.ListX;

public interface PQueueX<T> extends PQueue<T>, FluentCollectionX<T> {

    public static <T> PQueueX<T> of(T... values) {
        PQueue<T> result = empty();
        for(T value : values){
            result = result.plus(value);
        }
        
        return new PQueueXImpl<>(result);
    }

    public static <T> PQueueX<T> empty() {
        return new PQueueXImpl<>(AmortizedPQueue.empty());
    }

    public static <T> PQueueX<T> singleton(T value) {
        return PQueueX.<T>empty().plus(value);
    }

    /**
     * Construct a PQueueX from an Publisher
     * 
     * @param publisher
     *            to construct PQueueX from
     * @return PQueueX
     */
    public static <T> PQueueX<T> fromPublisher(Publisher<? extends T> publisher) {
        return ReactiveSeq.fromPublisher((Publisher<T>)publisher).toPQueueX();
    }
    public static <T> PQueueX<T> fromIterable(Iterable<T> iterable) {
        if (iterable instanceof PQueueX)
            return (PQueueX) iterable;
        if (iterable instanceof PQueue)
            return new PQueueXImpl<>((PQueue) (iterable));
        PQueue<T> res =  empty();
        Iterator<T> it = iterable.iterator();
        while (it.hasNext())
            res = res.plus(it.next());

        return new PQueueXImpl<>(res);
    }

    public static <T> PQueueX<T> fromCollection(Collection<T> stream) {
        if (stream instanceof PQueueX)
            return (PQueueX) (stream);
        if (stream instanceof PQueue)
            return new PQueueXImpl<>((PQueue) (stream));
        return PQueueX.<T>empty().plusAll(stream);
    }

    public static <T> PQueueX<T> fromStream(Stream<T> stream) {
        return Reducers.<T>toPQueueX().mapReduce(stream);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#unit(java.util.Collection)
     */
    @Override
    <R> PQueueX<R> unit(Collection<R> col);
    
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Unit#unit(java.lang.Object)
     */
    @Override
    <R> PQueueX<R> unit(R value);
    
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.IterableFunctor#unitIterator(java.util.Iterator)
     */
    @Override
    <R> PQueueX<R> unitIterator(Iterator<R> it);
    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#patternMatch(java.util.function.Function, java.util.function.Supplier)
     */
    @Override
    <R> PQueueX<R> patternMatch(
            Function<CheckValue1<T, R>, CheckValue1<T, R>> case1,Supplier<? extends R> otherwise);
    
    /* (non-Javadoc)
     * @see java.util.Collection#stream()
     */
    @Override
    ReactiveSeq<T> stream();


    
    
    

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#reverse()
     */
    @Override
    PQueueX<T> reverse();
    /**
     * Combine two adjacent elements in a PQueueX using the supplied BinaryOperator
     * This is a stateful grouping & reduction operation. The output of a combination may in turn be combined
     * with it's neighbor
     * <pre>
     * {@code 
     *  PQueueX.of(1,1,2,3)
                   .combine((a, b)->a.equals(b),Semigroups.intSum)
                   .toPQueueX()
                   
     *  //PQueueX(3,4) 
     * }</pre>
     * 
     * @param predicate Test to see if two neighbors should be joined
     * @param op Reducer to combine neighbors
     * @return Combined / Partially Reduced PQueueX
     */
    @Override
    PQueueX<T> combine(BiPredicate<? super T, ? super T> predicate, BinaryOperator<T> op);
    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#filter(java.util.function.Predicate)
     */
    @Override
    PQueueX<T> filter(Predicate<? super T> pred);

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#map(java.util.function.Function)
     */
    @Override
    <R> PQueueX<R> map(Function<? super T, ? extends R> mapper);

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#flatMap(java.util.function.Function)
     */
    @Override
    <R> PQueueX<R> flatMap(Function<? super T, ? extends Iterable<? extends R>> mapper);

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#limit(long)
     */
    @Override
    PQueueX<T> limit(long num);

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#skip(long)
     */
    @Override
    PQueueX<T> skip(long num);
    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#takeRight(int)
     */
    @Override
    PQueueX<T> takeRight(int num);
    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#dropRight(int)
     */
    @Override
    PQueueX<T> dropRight(int num);
    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#takeWhile(java.util.function.Predicate)
     */
    @Override
    PQueueX<T> takeWhile(Predicate<? super T> p);

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#dropWhile(java.util.function.Predicate)
     */
    @Override
    PQueueX<T> dropWhile(Predicate<? super T> p);

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#takeUntil(java.util.function.Predicate)
     */
    @Override
    PQueueX<T> takeUntil(Predicate<? super T> p);

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#dropUntil(java.util.function.Predicate)
     */
    @Override
    PQueueX<T> dropUntil(Predicate<? super T> p);

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#trampoline(java.util.function.Function)
     */
    @Override
    <R> PQueueX<R> trampoline(Function<? super T, ? extends Trampoline<? extends R>> mapper);

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#slice(long, long)
     */
    @Override
    PQueueX<T> slice(long from, long to);

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#sorted(java.util.function.Function)
     */
    @Override
    <U extends Comparable<? super U>> PQueueX<T> sorted(Function<? super T, ? extends U> function);
    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#grouped(int)
     */
    @Override
    PQueueX<ListX<T>> grouped(int groupSize);
    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#grouped(java.util.function.Function, java.util.stream.Collector)
     */
    @Override 
    <K, A, D> PQueueX<Tuple2<K, D>> grouped(Function<? super T, ? extends K> classifier, Collector<? super T, A, D> downstream);
    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#grouped(java.util.function.Function)
     */
    @Override
    <K> PQueueX<Tuple2<K, Seq<T>>> grouped(Function<? super T, ? extends K> classifier);
    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#zip(java.lang.Iterable)
     */
    @Override
    <U> PQueueX<Tuple2<T, U>> zip(Iterable<U> other);
    
    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#zip(java.lang.Iterable, java.util.function.BiFunction)
     */
    @Override
    <U, R> PQueueX<R> zip(Iterable<U> other, BiFunction<? super T, ? super U, ? extends R> zipper);

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#sliding(int)
     */
    @Override
    PQueueX<ListX<T>> sliding(int windowSize);
    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#sliding(int, int)
     */
    @Override
    PQueueX<ListX<T>> sliding(int windowSize, int increment);
    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#scanLeft(com.aol.cyclops.Monoid)
     */
    @Override
    PQueueX<T> scanLeft(Monoid<T> monoid);
    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#scanLeft(java.lang.Object, java.util.function.BiFunction)
     */
    @Override
    <U> PQueueX<U> scanLeft(U seed, BiFunction<U, ? super T, U> function);
    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#scanRight(com.aol.cyclops.Monoid)
     */
    @Override
    PQueueX<T> scanRight(Monoid<T> monoid);
    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#scanRight(java.lang.Object, java.util.function.BiFunction)
     */
    @Override
    <U> PQueueX<U> scanRight(U identity, BiFunction<? super T, U, U> combiner);
    

    /* Lazy operation
     * 
     * (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#plus(java.lang.Object)
     */
    @Override
    PQueueX<T> plus(T e);
    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#plusAll(java.util.Collection)
     */
    @Override
    PQueueX<T> plusAll(Collection<? extends T> list);
    

    
    /*
     * (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#minus(java.lang.Object)
     */
    PQueueX<T> minus(Object e);
    
    /* 
     * (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#minusAll(java.util.Collection)
     */
    PQueueX<T> minusAll(Collection<?> list);
    


    @Override
    int size();

   

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.CollectionX#peek(java.util.function.Consumer)
     */
    @Override
    default PQueueX<T> peek(Consumer<? super T> c) {
        return (PQueueX<T>)FluentCollectionX.super.peek(c);
    }
    



    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#cycle(int)
     */
    @Override
    PQueueX<T> cycle(int times);

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#cycle(com.aol.cyclops.Monoid, int)
     */
    @Override
    PQueueX<T> cycle(Monoid<T> m, int times);

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Traversable#cycleWhile(java.util.function.Predicate)
     */
    @Override
    PQueueX<T> cycleWhile(Predicate<? super T> predicate);

    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#cycleUntil(java.util.function.Predicate)
     */
    @Override
    PQueueX<T> cycleUntil(Predicate<? super T> predicate) ;

    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#zipStream(java.util.stream.Stream)
     */
    @Override
    <U> PQueueX<Tuple2<T, U>> zipStream(Stream<U> other);

    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#zip(org.jooq.lambda.Seq)
     */
    @Override
    <U> PQueueX<Tuple2<T, U>> zip(Seq<U> other) ;

    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#zip3(java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    <S, U> PQueueX<Tuple3<T, S, U>> zip3(Stream<? extends S> second, Stream<? extends U> third);

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#zip4(java.util.stream.Stream, java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    <T2, T3, T4> PQueueX<Tuple4<T, T2, T3, T4>> zip4(Stream<T2> second, Stream<T3> third,
            Stream<T4> fourth) ;

    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#zipWithIndex()
     */
    @Override
    PQueueX<Tuple2<T, Long>> zipWithIndex();

    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#sorted()
     */
    @Override
    PQueueX<T> sorted();

    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#sorted(java.util.Comparator)
     */
    @Override
    PQueueX<T> sorted(Comparator<? super T> c);

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#skipWhile(java.util.function.Predicate)
     */
    @Override
    PQueueX<T> skipWhile(Predicate<? super T> p);
    

    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#skipUntil(java.util.function.Predicate)
     */
    @Override
    PQueueX<T> skipUntil(Predicate<? super T> p);

    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#shuffle()
     */
    @Override
    PQueueX<T> shuffle();

    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#skipLast(int)
     */
    @Override
    PQueueX<T> skipLast(int num) ;

    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#shuffle(java.util.Random)
     */
    @Override
    PQueueX<T> shuffle(Random random);

    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#permutations()
     */
    @Override
    PQueueX<ReactiveSeq<T>> permutations();

    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#combinations(int)
     */
    @Override
    PQueueX<ReactiveSeq<T>> combinations(int size);

    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#combinations()
     */
    @Override
    PQueueX<ReactiveSeq<T>> combinations();
    

    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#cast(java.lang.Class)
     */
    @Override
    public <U> PQueueX<U> cast(Class<U> type);

    

    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#distinct()
     */
    @Override
    PQueueX<T> distinct();

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#limitWhile(java.util.function.Predicate)
     */
    @Override
    PQueueX<T> limitWhile(Predicate<? super T> p);

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#limitUntil(java.util.function.Predicate)
     */
    @Override
    PQueueX<T> limitUntil(Predicate<? super T> p);

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#intersperse(java.lang.Object)
     */
    @Override
    PQueueX<T> intersperse(T value);

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#limitLast(int)
     */
    @Override
    PQueueX<T> limitLast(int num);
    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#onEmpty(java.lang.Object)
     */
    @Override
    PQueueX<T> onEmpty(T value);
    

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#onEmptyGet(java.util.function.Supplier)
     */
    @Override
    PQueueX<T> onEmptyGet(Supplier<T> supplier);

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#onEmptyThrow(java.util.function.Supplier)
     */
    @Override
    <X extends Throwable> PQueueX<T> onEmptyThrow(Supplier<X> supplier);

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#ofType(java.lang.Class)
     */
    @Override
    <U> PQueueX<U> ofType(Class<U> type);

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#filterNot(java.util.function.Predicate)
     */
    @Override
    PQueueX<T> filterNot(Predicate<? super T> fn);

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#notNull()
     */
    @Override
    PQueueX<T> notNull();

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#removeAll(java.util.stream.Stream)
     */
    @Override
    PQueueX<T> removeAll(Stream<T> stream);

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#removeAll(java.lang.Iterable)
     */
    @Override
    PQueueX<T> removeAll(Iterable<T> it);

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#removeAll(java.lang.Object[])
     */
    @Override
    PQueueX<T> removeAll(T... values);

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#retainAll(java.lang.Iterable)
     */
    @Override
    PQueueX<T> retainAll(Iterable<T> it);

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#retainAll(java.util.stream.Stream)
     */
    @Override
    PQueueX<T> retainAll(Stream<T> stream);

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#retainAll(java.lang.Object[])
     */
    @Override
    PQueueX<T> retainAll(T... values);

    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#grouped(int, java.util.function.Supplier)
     */
    @Override
     <C extends Collection<? super T>> PQueueX<C> grouped(int size, Supplier<C> supplier);

     /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#groupedUntil(java.util.function.Predicate)
     */
    @Override
     PQueueX<ListX<T>> groupedUntil(Predicate<? super T> predicate);


    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#groupedWhile(java.util.function.Predicate)
     */
    @Override
     PQueueX<ListX<T>> groupedWhile(Predicate<? super T> predicate);


    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#groupedWhile(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
     <C extends Collection<? super T>> PQueueX<C> groupedWhile(Predicate<? super T> predicate,
                Supplier<C> factory);


    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#groupedUntil(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
     <C extends Collection<? super T>> PQueueX<C> groupedUntil(Predicate<? super T> predicate,
                Supplier<C> factory);


    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#groupedStatefullyWhile(java.util.function.BiPredicate)
     */
    @Override
    PQueueX<ListX<T>> groupedStatefullyWhile(BiPredicate<ListX<? super T>, ? super T> predicate);
        
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#removeAll(org.jooq.lambda.Seq)
     */
    @Override
    PQueueX<T> removeAll(Seq<T> stream);


     /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#retainAll(org.jooq.lambda.Seq)
     */
    @Override
     PQueueX<T> retainAll(Seq<T> stream);
}
