package com.aol.cyclops.control.monads.transformers.values;

import java.util.Collection;
import java.util.Comparator;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import com.aol.cyclops.Monoid;
import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.types.FilterableFunctor;
import com.aol.cyclops.types.Foldable;
import com.aol.cyclops.types.Sequential;
import com.aol.cyclops.types.Traversable;
import com.aol.cyclops.types.anyM.AnyMSeq;
import com.aol.cyclops.types.anyM.NestedFoldable;
import com.aol.cyclops.types.stream.ConvertableSequence;
import com.aol.cyclops.types.stream.CyclopsCollectable;
import com.aol.cyclops.types.stream.future.FutureOperations;
import com.aol.cyclops.types.stream.lazy.LazyOperations;
import com.aol.cyclops.util.stream.Streamable;

public interface TransformerSeq<T> extends  NestedFoldable<T>,
                                            Traversable<T>,
                                            Sequential<T>,                                
                                            Iterable<T>,
                                            FilterableFunctor<T>,
                                            
                                            Publisher<T> {
    
  
    default <X extends CyclopsCollectable<T> & Foldable<T> & ConvertableSequence<T>> X flattened(){
        return (X)stream();
    }
    
    
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Filterable#filter(java.util.function.Predicate)
     */
    @Override
    TransformerSeq<T> filter(Predicate<? super T> fn) ;

   

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.MonadicValue#map(java.util.function.Function)
     */
    @Override
     <R> TransformerSeq<R> map(Function<? super T, ? extends R> fn);
    
    <T> TransformerSeq<T> unitAnyM(AnyM<Traversable<T>> traversable);
    AnyM<? extends Traversable<T>> transformerStream();
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#combine(java.util.function.BiPredicate, java.util.function.BinaryOperator)
     */
    @Override
    default Traversable<T> combine(BiPredicate<? super T, ? super T> predicate, BinaryOperator<T> op) {
        return unitAnyM(transformerStream().map(s->s.combine(predicate, op)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#subscribe(org.reactivestreams.Subscriber)
     */
    @Override
    default void subscribe(Subscriber<? super T> s) {
        transformerStream().forEach(n->n.subscribe(s));
        
    }

    

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#cycle(int)
     */
    @Override
    default Traversable<T> cycle(int times) {
        return unitAnyM(transformerStream().map(s->s.cycle(times)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#cycle(com.aol.cyclops.Monoid, int)
     */
    @Override
    default Traversable<T> cycle(Monoid<T> m, int times) {
        return unitAnyM(transformerStream().map(s->s.cycle(m,times)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#cycleWhile(java.util.function.Predicate)
     */
    @Override
    default Traversable<T> cycleWhile(Predicate<? super T> predicate) {
        return unitAnyM(transformerStream().map(s->s.cycleWhile(predicate)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#cycleUntil(java.util.function.Predicate)
     */
    @Override
    default Traversable<T> cycleUntil(Predicate<? super T> predicate) {
        return unitAnyM(transformerStream().map(s->s.cycleUntil(predicate)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#zip(java.lang.Iterable, java.util.function.BiFunction)
     */
    @Override
    default <U, R> Traversable<R> zip(Iterable<U> other, BiFunction<? super T, ? super U, ? extends R> zipper) {
        AnyM<Traversable<R>> zipped = transformerStream().map(s->s.zip(other,zipper));
       return unitAnyM(zipped);
       
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#zipStream(java.util.stream.Stream)
     */
    @Override
    default <U> Traversable<Tuple2<T, U>> zipStream(Stream<U> other) {
        Streamable<U> streamable = Streamable.fromStream(other);
        return unitAnyM(transformerStream().map(s->s.zipStream(streamable.stream())));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#zip(org.jooq.lambda.Seq)
     */
    @Override
    default <U> Traversable<Tuple2<T, U>> zip(Seq<U> other) {
       return zipStream(other);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#zip3(java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    default <S, U> Traversable<Tuple3<T, S, U>> zip3(Stream<? extends S> second, Stream<? extends U> third) {
        Streamable<? extends S> streamable2 = Streamable.fromStream(second);
        Streamable<? extends U> streamable3 = Streamable.fromStream(third);
        AnyM<Traversable<Tuple3<T, S, U>>> zipped =transformerStream().map(s->s.zip3(streamable2.stream(),streamable3.stream()));
        return unitAnyM(zipped);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#zip4(java.util.stream.Stream, java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    default <T2, T3, T4> Traversable<Tuple4<T, T2, T3, T4>> zip4(Stream<T2> second, Stream<T3> third,
            Stream<T4> fourth) {
        Streamable<T2> streamable2 = Streamable.fromStream(second);
        Streamable<T3> streamable3 = Streamable.fromStream(third);
        Streamable<T4> streamable4 = Streamable.fromStream(fourth);
        AnyM<Traversable<Tuple4<T, T2, T3, T4>>> zipped =transformerStream().map(s->s.zip4(streamable2.stream(),streamable3.stream(),streamable4.stream()));
        return unitAnyM(zipped);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#zipWithIndex()
     */
    @Override
    default Traversable<Tuple2<T, Long>> zipWithIndex() {
       return unitAnyM(transformerStream().map(s->s.zipWithIndex()));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#sliding(int)
     */
    @Override
    default Traversable<ListX<T>> sliding(int windowSize) {
        return unitAnyM(transformerStream().map(s->s.sliding(windowSize)));
        
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#sliding(int, int)
     */
    @Override
    default Traversable<ListX<T>> sliding(int windowSize, int increment) {
        return unitAnyM(transformerStream().map(s->s.sliding(windowSize,increment)));
    }

    

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#grouped(int, java.util.function.Supplier)
     */
    @Override
    default <C extends Collection<? super T>> Traversable<C> grouped(int size, Supplier<C> supplier) {
        return unitAnyM(transformerStream().map(s->s.grouped(size,supplier)));
        
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#groupedUntil(java.util.function.Predicate)
     */
    @Override
    default Traversable<ListX<T>> groupedUntil(Predicate<? super T> predicate) {
        return unitAnyM(transformerStream().map(s->s.groupedUntil(predicate)));
        
    }

    

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#groupedStatefullyWhile(java.util.function.BiPredicate)
     */
    @Override
    default Traversable<ListX<T>> groupedStatefullyWhile(BiPredicate<ListX<? super T>, ? super T> predicate) {
        return unitAnyM(transformerStream().map(s->s.groupedStatefullyWhile(predicate)));
       
    }

    
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#groupedWhile(java.util.function.Predicate)
     */
    @Override
    default Traversable<ListX<T>> groupedWhile(Predicate<? super T> predicate) {
        return unitAnyM(transformerStream().map(s->s.groupedWhile(predicate)));
    }

   

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#groupedWhile(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
    default <C extends Collection<? super T>> Traversable<C> groupedWhile(Predicate<? super T> predicate,
            Supplier<C> factory) {
        return unitAnyM(transformerStream().map(s->s.groupedWhile(predicate,factory)));
       
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#groupedUntil(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
    default <C extends Collection<? super T>> Traversable<C> groupedUntil(Predicate<? super T> predicate,
            Supplier<C> factory) {
        return unitAnyM(transformerStream().map(s->s.groupedUntil(predicate,factory)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#grouped(int)
     */
    @Override
    default Traversable<ListX<T>> grouped(int groupSize) {
        return unitAnyM(transformerStream().map(s->s.grouped(groupSize)));
    }

   
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#grouped(java.util.function.Function, java.util.stream.Collector)
     */
    @Override
    default <K, A, D> Traversable<Tuple2<K, D>> grouped(Function<? super T, ? extends K> classifier,
            Collector<? super T, A, D> downstream) {
        return unitAnyM(transformerStream().map(s->s.grouped(classifier,downstream)));
        
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#grouped(java.util.function.Function)
     */
    @Override
    default <K> Traversable<Tuple2<K, Seq<T>>> grouped(Function<? super T, ? extends K> classifier) {
       return unitAnyM(transformerStream().map(s->s.grouped(classifier)));
       
    }

    

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#distinct()
     */
    @Override
    default Traversable<T> distinct() {
        return unitAnyM(transformerStream().map(s->s.distinct()));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#scanLeft(com.aol.cyclops.Monoid)
     */
    @Override
    default Traversable<T> scanLeft(Monoid<T> monoid) {
        return unitAnyM(transformerStream().map(s->s.scanLeft(monoid)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#scanLeft(java.lang.Object, java.util.function.BiFunction)
     */
    @Override
    default <U> Traversable<U> scanLeft(U seed, BiFunction<U, ? super T, U> function) {
        return unitAnyM(transformerStream().map(s->s.scanLeft(seed,function)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#scanRight(com.aol.cyclops.Monoid)
     */
    @Override
    default Traversable<T> scanRight(Monoid<T> monoid) {
        return unitAnyM(transformerStream().map(s->s.scanRight(monoid)));
    }

    /* (non-Javadoc)
     * @see com.aol).cyclops.types.Traversable#scanRight(java.lang.Object, java.util.function.BiFunction)
     */
    @Override
    default <U> Traversable<U> scanRight(U identity, BiFunction<? super T, U, U> combiner) {
        return unitAnyM(transformerStream().map(s->s.scanRight(identity,combiner)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#sorted()
     */
    @Override
    default Traversable<T> sorted() {
        return unitAnyM(transformerStream().map(s->s.sorted()));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#sorted(java.util.Comparator)
     */
    @Override
    default Traversable<T> sorted(Comparator<? super T> c) {
        return unitAnyM(transformerStream().map(s->s.sorted(c)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#takeWhile(java.util.function.Predicate)
     */
    @Override
    default Traversable<T> takeWhile(Predicate<? super T> p) {
        return unitAnyM(transformerStream().map(s->s.takeWhile(p)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#dropWhile(java.util.function.Predicate)
     */
    @Override
    default Traversable<T> dropWhile(Predicate<? super T> p) {
        return unitAnyM(transformerStream().map(s->s.dropWhile(p)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#takeUntil(java.util.function.Predicate)
     */
    @Override
    default Traversable<T> takeUntil(Predicate<? super T> p) {
        return unitAnyM(transformerStream().map(s->s.takeUntil(p)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#dropUntil(java.util.function.Predicate)
     */
    @Override
    default Traversable<T> dropUntil(Predicate<? super T> p) {
        return unitAnyM(transformerStream().map(s->s.dropUntil(p)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#dropRight(int)
     */
    @Override
    default Traversable<T> dropRight(int num) {
        return unitAnyM(transformerStream().map(s->s.dropRight(num)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#takeRight(int)
     */
    @Override
    default Traversable<T> takeRight(int num) {
        return unitAnyM(transformerStream().map(s->s.takeRight(num)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#skip(long)
     */
    @Override
    default Traversable<T> skip(long num) {
        return unitAnyM(transformerStream().map(s->s.skip(num)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#skipWhile(java.util.function.Predicate)
     */
    @Override
    default Traversable<T> skipWhile(Predicate<? super T> p) {
        return unitAnyM(transformerStream().map(s->s.skipWhile(p)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#skipUntil(java.util.function.Predicate)
     */
    @Override
    default Traversable<T> skipUntil(Predicate<? super T> p) {
        return unitAnyM(transformerStream().map(s->s.skipUntil(p)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#limit(long)
     */
    @Override
    default Traversable<T> limit(long num) {
        return unitAnyM(transformerStream().map(s->s.limit(num)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#limitWhile(java.util.function.Predicate)
     */
    @Override
    default Traversable<T> limitWhile(Predicate<? super T> p) {
        return unitAnyM(transformerStream().map(s->s.limitWhile(p)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#limitUntil(java.util.function.Predicate)
     */
    @Override
    default Traversable<T> limitUntil(Predicate<? super T> p) {
        return unitAnyM(transformerStream().map(s->s.limitUntil(p)));
    }

    

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#intersperse(java.lang.Object)
     */
    @Override
    default Traversable<T> intersperse(T value) {
        return unitAnyM(transformerStream().map(s->s.intersperse(value)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#reverse()
     */
    @Override
    default Traversable<T> reverse() {
        return unitAnyM(transformerStream().map(s->s.reverse()));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#shuffle()
     */
    @Override
    default Traversable<T> shuffle() {
        return unitAnyM(transformerStream().map(s->s.shuffle()));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#futureOperations(java.util.concurrent.Executor)
     */
    @Override
    default FutureOperations<T> futureOperations(Executor exec) {
        // TODO Auto-generated method stub
        return Traversable.super.futureOperations(exec);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#lazyOperations()
     */
    @Override
    default LazyOperations<T> lazyOperations() {
        // TODO Auto-generated method stub
        return Traversable.super.lazyOperations();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#skipLast(int)
     */
    @Override
    default Traversable<T> skipLast(int num) {
        return unitAnyM(transformerStream().map(s->s.skipLast(num)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#limitLast(int)
     */
    @Override
    default Traversable<T> limitLast(int num) {
        return unitAnyM(transformerStream().map(s->s.limitLast(num)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#onEmpty(java.lang.Object)
     */
    @Override
    default Traversable<T> onEmpty(T value) {
        return unitAnyM(transformerStream().map(s->s.onEmpty(value)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#onEmptyGet(java.util.function.Supplier)
     */
    @Override
    default Traversable<T> onEmptyGet(Supplier<T> supplier) {
        return unitAnyM(transformerStream().map(s->s.onEmptyGet(supplier)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#onEmptyThrow(java.util.function.Supplier)
     */
    @Override
    default <X extends Throwable> Traversable<T> onEmptyThrow(Supplier<X> supplier) {
       return unitAnyM(transformerStream().map(s->s.onEmptyThrow(supplier)));
       
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#shuffle(java.util.Random)
     */
    @Override
    default Traversable<T> shuffle(Random random) {
        return unitAnyM(transformerStream().map(s->s.shuffle(random)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#slice(long, long)
     */
    @Override
    default Traversable<T> slice(long from, long to) {
        return unitAnyM(transformerStream().map(s->s.slice(from, to)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#sorted(java.util.function.Function)
     */
    @Override
    default <U extends Comparable<? super U>> Traversable<T> sorted(Function<? super T, ? extends U> function) {
        return unitAnyM(transformerStream().map(s->s.sorted(function)));
    }


   


    

    
}
