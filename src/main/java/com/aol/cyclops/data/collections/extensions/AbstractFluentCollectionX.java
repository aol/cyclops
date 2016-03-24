package com.aol.cyclops.data.collections.extensions;

import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;
import java.util.Random;
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

import com.aol.cyclops.Monoid;
import com.aol.cyclops.control.Matchable.CheckValue1;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.Trampoline;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.util.stream.StreamUtils;

public abstract class AbstractFluentCollectionX<T> implements FluentCollectionX<T>{
    abstract public ReactiveSeq<T> streamInternal();
    abstract public <X> FluentCollectionX<X> stream(Stream<X> stream);
    @Override
    public FluentCollectionX<T> plusLazy(T e){
        return stream(streamInternal().append(e));
        
    }
    @Override
    public FluentCollectionX<T> plus(T e){
        add(e);
        return this;
    }
    
    @Override
    public FluentCollectionX<T> plusAll(Collection<? extends T> list){
        addAll(list);
        return this;
    }
    
    @Override
    public FluentCollectionX<T> minus(Object e){
        remove(e);
        return this;
    }
    
    @Override
    public FluentCollectionX<T> minusAll(Collection<?> list){
        removeAll(list);
        return this;
    }
    @Override
    public FluentCollectionX<T> plusAllLazy(Collection<? extends T> list){
        return stream(streamInternal().appendStream((Stream<T>)list.stream()));
    }
    
    @Override
    public FluentCollectionX<T> minusLazy(Object e){
        
        return stream(streamInternal().filterNot(t-> Objects.equals(t,e)));
        
    }
    @Override
    public FluentCollectionX<T> minusAllLazy(Collection<?> list){
        return stream(streamInternal().removeAll((Collection)list));
      
    }
    
    @Override
    public FluentCollectionX<T> combine(BiPredicate<? super T, ? super T> predicate, BinaryOperator<T> op){
        return stream(streamInternal().combine(predicate, op)); 
    }
    @Override
    public FluentCollectionX<T> reverse(){
        return stream(streamInternal().reverse()); 
    }
    @Override
    public FluentCollectionX<T> filter(Predicate<? super T> pred){
        return stream(streamInternal().filter(pred));
    }
    @Override
    public <R> CollectionX<R> map(Function<? super T, ? extends R> mapper){
        return stream(streamInternal().map(mapper));
    }
    @Override
    public <R> CollectionX<R> flatMap(Function<? super T, ? extends Iterable<? extends R>> mapper){
        return stream(streamInternal().flatMap(mapper.andThen(StreamUtils::stream)));
    }
    @Override
    public FluentCollectionX<T> limit(long num){
        return stream(streamInternal().limit(num));
    }
    @Override
    public FluentCollectionX<T> skip(long num){
        return stream(streamInternal().skip(num));
    }
    @Override
    public FluentCollectionX<T> takeRight(int num){
        return stream(streamInternal().limitLast(num));
    }
    @Override
    public FluentCollectionX<T> dropRight(int num){
        return stream(streamInternal().skipLast(num));
    }
    @Override
    public FluentCollectionX<T> takeWhile(Predicate<? super T> p){
        return stream(streamInternal().limitWhile(p));
    }
    @Override
    public FluentCollectionX<T> dropWhile(Predicate<? super T> p){
        return stream(streamInternal().skipWhile(p));
    }
    @Override
    public FluentCollectionX<T> takeUntil(Predicate<? super T> p){
        return stream(streamInternal().limitUntil(p));
    }
    @Override
    public FluentCollectionX<T> dropUntil(Predicate<? super T> p){
        return stream(streamInternal().skipUntil(p));
    }
     /**
      * Performs a map operation that can call a recursive method without running out of stack space
      * <pre>
      * {@code
      * ReactiveSeq.of(10,20,30,40)
                 .trampoline(i-> fibonacci(i))
                 .forEach(System.out::println); 
                 
        Trampoline<Long> fibonacci(int i){
            return fibonacci(i,1,0);
        }
        Trampoline<Long> fibonacci(int n, long a, long b) {
            return n == 0 ? Trampoline.done(b) : Trampoline.more( ()->fibonacci(n-1, a+b, a));
        }        
                 
      * 55
        6765
        832040
        102334155
      * 
      * 
      * ReactiveSeq.of(10_000,200_000,3_000_000,40_000_000)
                 .trampoline(i-> fibonacci(i))
                 .forEach(System.out::println);
                 
                 
      * completes successfully
      * }
      * 
     * @param mapper
     * @return
     */
    public <R> FluentCollectionX<R> trampoline(Function<? super T, ? extends Trampoline<? extends R>> mapper){
        
         return  stream(streamInternal().trampoline(mapper));    
    }
    /*
     * (non-Javadoc)
     * 
     * @see org.jooq.lambda.Seq#slice(long, long)
     */
    public FluentCollectionX<T> slice(long from, long to){
        return stream(streamInternal().slice(from,to));  
    }
    
    

    public FluentCollectionX<ListX<T>> grouped(int groupSize){
        return stream(streamInternal().grouped(groupSize).map(ListX::fromIterable));     
    }
    public <K, A, D> FluentCollectionX<Tuple2<K, D>> grouped(Function<? super T, ? extends K> classifier, Collector<? super T, A, D> downstream){
        return stream(streamInternal().grouped(classifier,downstream));  
    }
    public <K> FluentCollectionX<Tuple2<K, Seq<T>>> grouped(Function<? super T, ? extends K> classifier){
        return stream(streamInternal().grouped(classifier));     
    }
    public <U> FluentCollectionX<Tuple2<T, U>> zip(Iterable<U> other){
        return stream(streamInternal().zip(other));
    }
    public <U, R> FluentCollectionX<R> zip(Iterable<U> other, BiFunction<? super T, ? super U, ? extends R> zipper){
        return stream(streamInternal().zip(other,zipper));
    }
    public FluentCollectionX<ListX<T>> sliding(int windowSize){
        return stream(streamInternal().sliding(windowSize).map(ListX::fromIterable));   
    }
    public FluentCollectionX<ListX<T>> sliding(int windowSize, int increment){
        return stream(streamInternal().sliding(windowSize,increment).map(ListX::fromIterable)); 
    }
    public FluentCollectionX<T> scanLeft(Monoid<T> monoid){
        return stream(streamInternal().scanLeft(monoid));   
    }
    public <U> FluentCollectionX<U> scanLeft(U seed, BiFunction<U, ? super T, U> function){
        return stream(streamInternal().scanLeft(seed,function));    
    }
    public FluentCollectionX<T> scanRight(Monoid<T> monoid){
        return stream(streamInternal().scanRight(monoid));  
    }
    public <U> FluentCollectionX<U> scanRight(U identity, BiFunction<? super T, U, U> combiner){
        return stream(streamInternal().scanRight(identity,combiner));
    }
    

    /*
     * (non-Javadoc)
     * 
     * @see org.jooq.lambda.Seq#sorted(java.util.function.Function)
     */
    public <U extends Comparable<? super U>> FluentCollectionX<T> sorted(Function<? super T, ? extends U> function){
        return stream(streamInternal().sorted(function));
    }
   



    


    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Traversable#cycle(int)
     */
    @Override
    public FluentCollectionX<T> cycle(int times) {
        
        return stream(streamInternal().cycle(times));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Traversable#cycle(com.aol.cyclops.sequence.Monoid, int)
     */
    @Override
    public FluentCollectionX<T> cycle(Monoid<T> m, int times) {
        
        return stream(streamInternal().cycle(m, times));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Traversable#cycleWhile(java.util.function.Predicate)
     */
    @Override
    public FluentCollectionX<T> cycleWhile(Predicate<? super T> predicate) {
        
        return stream(streamInternal().cycleWhile(predicate));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Traversable#cycleUntil(java.util.function.Predicate)
     */
    @Override
    public FluentCollectionX<T> cycleUntil(Predicate<? super T> predicate) {
        
        return stream(streamInternal().cycleUntil(predicate));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Traversable#zipStream(java.util.stream.Stream)
     */
    @Override
    public <U> FluentCollectionX<Tuple2<T, U>> zipStream(Stream<U> other) {
        
        return stream(streamInternal().zipStream(other));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Traversable#zip(org.jooq.lambda.Seq)
     */
    @Override
    public <U> FluentCollectionX<Tuple2<T, U>> zip(Seq<U> other) {
        
        return stream(streamInternal().zip(other));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Traversable#zip3(java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    public <S, U> FluentCollectionX<Tuple3<T, S, U>> zip3(Stream<? extends S> second, Stream<? extends U> third) {
        
        return stream(streamInternal().zip3(second, third));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Traversable#zip4(java.util.stream.Stream, java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    public <T2, T3, T4> FluentCollectionX<Tuple4<T, T2, T3, T4>> zip4(Stream<T2> second, Stream<T3> third,
            Stream<T4> fourth) {
        
        return stream(streamInternal().zip4(second, third, fourth));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Traversable#zipWithIndex()
     */
    @Override
    public FluentCollectionX<Tuple2<T, Long>> zipWithIndex() {
        
        return stream(streamInternal().zipWithIndex());
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Traversable#distinct()
     */
    @Override
    public FluentCollectionX<T> distinct() {
        
        return stream(streamInternal().distinct());
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Traversable#sorted()
     */
    @Override
    public FluentCollectionX<T> sorted() {
        
        return stream(streamInternal().sorted());
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Traversable#sorted(java.util.Comparator)
     */
    @Override
    public FluentCollectionX<T> sorted(Comparator<? super T> c) {
        
        return stream(streamInternal().sorted(c));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Traversable#skipWhile(java.util.function.Predicate)
     */
    @Override
    public FluentCollectionX<T> skipWhile(Predicate<? super T> p) {
        
        return stream(streamInternal().skipWhile(p));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Traversable#skipUntil(java.util.function.Predicate)
     */
    @Override
    public FluentCollectionX<T> skipUntil(Predicate<? super T> p) {
        
        return stream(streamInternal().skipUntil(p));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Traversable#limitWhile(java.util.function.Predicate)
     */
    @Override
    public FluentCollectionX<T> limitWhile(Predicate<? super T> p) {
        
        return stream(streamInternal().limitWhile(p));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Traversable#limitUntil(java.util.function.Predicate)
     */
    @Override
    public FluentCollectionX<T> limitUntil(Predicate<? super T> p) {
        
        return stream(streamInternal().limitUntil(p));
    }

    
    

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Traversable#intersperse(java.lang.Object)
     */
    @Override
    public FluentCollectionX<T> intersperse(T value) {
        
        return stream(streamInternal().intersperse(value));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Traversable#shuffle()
     */
    @Override
    public FluentCollectionX<T> shuffle() {
        
        return stream(streamInternal().shuffle());
    }

    

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Traversable#skipLast(int)
     */
    @Override
    public FluentCollectionX<T> skipLast(int num) {
        
        return stream(streamInternal().skipLast(num));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Traversable#limitLast(int)
     */
    @Override
    public FluentCollectionX<T> limitLast(int num) {
    
        return stream(streamInternal().limitLast(num));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Traversable#onEmpty(java.lang.Object)
     */
    @Override
    public FluentCollectionX<T> onEmpty(T value) {
        return stream(streamInternal().onEmpty(value));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Traversable#onEmptyGet(java.util.function.Supplier)
     */
    @Override
    public FluentCollectionX<T> onEmptyGet(Supplier<T> supplier) {
        return stream(streamInternal().onEmptyGet(supplier));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Traversable#onEmptyThrow(java.util.function.Supplier)
     */
    @Override
    public <X extends Throwable> FluentCollectionX<T> onEmptyThrow(Supplier<X> supplier) {
        return stream(streamInternal().onEmptyThrow(supplier));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Traversable#shuffle(java.util.Random)
     */
    @Override
    public FluentCollectionX<T> shuffle(Random random) {
        return stream(streamInternal().shuffle(random));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Filterable#ofType(java.lang.Class)
     */
    @Override
    public <U> FluentCollectionX<U> ofType(Class<U> type) {
        
        return stream(streamInternal().ofType(type));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Filterable#filterNot(java.util.function.Predicate)
     */
    @Override
    public FluentCollectionX<T> filterNot(Predicate<? super T> fn) {
        return stream(streamInternal().filterNot(fn));
        
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Filterable#notNull()
     */
    @Override
    public FluentCollectionX<T> notNull() {
        return stream(streamInternal().notNull());
        
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Filterable#removeAll(java.util.stream.Stream)
     */
    @Override
    public FluentCollectionX<T> removeAll(Stream<T> stream) {
        
        return stream(streamInternal().removeAll(stream));
    }
    @Override
    public FluentCollectionX<T> removeAll(Seq<T> stream) {
        
        return stream(streamInternal().removeAll(stream));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Filterable#removeAll(java.lang.Iterable)
     */
    @Override
    public FluentCollectionX<T> removeAll(Iterable<T> it) {
        return stream(streamInternal().removeAll(it));
        
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Filterable#removeAll(java.lang.Object[])
     */
    @Override
    public FluentCollectionX<T> removeAll(T... values) {
        return stream(streamInternal().removeAll(values));
        
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Filterable#retainAll(java.lang.Iterable)
     */
    @Override
    public FluentCollectionX<T> retainAll(Iterable<T> it) {
        return stream(streamInternal().retainAll(it));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Filterable#retainAll(java.util.stream.Stream)
     */
    @Override
    public FluentCollectionX<T> retainAll(Stream<T> stream) {
        return stream(streamInternal().retainAll(stream));
    }
    @Override
    public FluentCollectionX<T> retainAll(Seq<T> stream) {
        return stream(streamInternal().retainAll(stream));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Filterable#retainAll(java.lang.Object[])
     */
    @Override
    public FluentCollectionX<T> retainAll(T... values) {
        return stream(streamInternal().retainAll(values));
    }

    

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Functor#cast(java.lang.Class)
     */
    @Override
    public <U> FluentCollectionX<U> cast(Class<U> type) {
        return stream(streamInternal().cast(type));
    }

    
    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Functor#patternMatch(java.lang.Object, java.util.function.Function)
     */
    @Override
    public <R> FluentCollectionX<R> patternMatch(
            Function<CheckValue1<T, R>, CheckValue1<T, R>> case1,Supplier<? extends R> otherwise) {
        
        return stream(streamInternal().patternMatch(case1, otherwise));
    }

    

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.ExtendedTraversable#permutations()
     */
    @Override
    public FluentCollectionX<ReactiveSeq<T>> permutations() {
        return stream(streamInternal().permutations());
        
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.ExtendedTraversable#combinations(int)
     */
    @Override
    public FluentCollectionX<ReactiveSeq<T>> combinations(int size) {
        return stream(streamInternal().combinations(size));
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.ExtendedTraversable#combinations()
     */
    @Override
    public FluentCollectionX<ReactiveSeq<T>> combinations() {
        return stream(streamInternal().combinations());
    }

    @Override
    public <C extends Collection<? super T>> FluentCollectionX<C> grouped(int size, Supplier<C> supplier) {
        
        return stream(streamInternal().grouped(size,supplier));
    }

    @Override
    public FluentCollectionX<ListX<T>> groupedUntil(Predicate<? super T> predicate) {
        
        return stream(streamInternal().groupedUntil(predicate));
    }

    @Override
    public FluentCollectionX<ListX<T>> groupedWhile(Predicate<? super T> predicate) {
        
        return stream(streamInternal().groupedWhile(predicate));
    }

    @Override
    public <C extends Collection<? super T>> FluentCollectionX<C> groupedWhile(Predicate<? super T> predicate,
            Supplier<C> factory) {
        
        return stream(streamInternal().groupedWhile(predicate,factory));
    }

    @Override
    public <C extends Collection<? super T>> FluentCollectionX<C> groupedUntil(Predicate<? super T> predicate,
            Supplier<C> factory) {
        
        return stream(streamInternal().groupedUntil(predicate,factory));
    }

    @Override
    public FluentCollectionX<ListX<T>> groupedStatefullyWhile(BiPredicate<ListX<? super T>, ? super T> predicate) {
        return stream(streamInternal().groupedStatefullyWhile(predicate));
    }
    
}
