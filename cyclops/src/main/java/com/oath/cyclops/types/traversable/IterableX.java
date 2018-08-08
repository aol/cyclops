package com.oath.cyclops.types.traversable;

import com.oath.cyclops.types.foldable.ConvertableSequence;
import com.oath.cyclops.types.foldable.Folds;
import com.oath.cyclops.types.persistent.PersistentCollection;
import com.oath.cyclops.types.reactive.ReactiveStreamsTerminalOperations;
import cyclops.control.Eval;
import cyclops.data.Seq;
import cyclops.data.HashSet;
import cyclops.data.Vector;
import cyclops.data.tuple.Tuple2;
import cyclops.data.tuple.Tuple3;
import cyclops.data.tuple.Tuple4;
import cyclops.control.Future;
import cyclops.function.Function3;
import cyclops.function.Function4;
import cyclops.function.Monoid;
import cyclops.reactive.ReactiveSeq;
import cyclops.control.Try;
import cyclops.function.Function1;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;

import java.util.*;
import java.util.concurrent.Executor;
import java.util.function.*;
import java.util.stream.Stream;

/**
 * Iterable on steroids.
 * Created by johnmcclean on 17/12/2016.
 */
@FunctionalInterface
public interface IterableX<T> extends Traversable<T>,
                                                Folds<T>,
                                                Iterable<T>,
                                                ReactiveStreamsTerminalOperations<T> {



    default int size(){
        return (int)count();
    }
    default boolean equalToIteration(Iterable<T> iterable){

        Iterator<T> it2 = iterable.iterator();
        Iterator<T> it1 = iterator();
        while(it2.hasNext() && it1.hasNext()){
            if(!Objects.equals(it2.next(),it1.next()))
                return false;
        }
        return it2.hasNext() == it1.hasNext();
    }

    default boolean isEmpty(){
        return !(iterator().hasNext());
    }


    @Override
    default <U> IterableX<U> unitIterable(Iterable<U> U) {
        return ReactiveSeq.fromIterable(U);
    }

    @Override
    default IterableX<T> filter(Predicate<? super T> fn) {
        return stream().filter(fn);
    }

    @Override
    default <R> IterableX<R> map(Function<? super T, ? extends R> fn) {
        return stream().map(fn);
    }

    @Override
    default ReactiveSeq<T> stream() {
        return ReactiveSeq.fromIterator(iterator());
    }



    default ConvertableSequence<T> to(){
        return new ConvertableSequence<>(this);
    }

    default Seq<T> seq(){
        return to().seq();
    }
    default HashSet<T> toHashSet(){
        return HashSet.fromIterable(this);
    }
    /**
     * Perform an async fold on the provided executor
     *
     *  <pre>
     *  {@code
     *    Future<Integer> sum =  Seq.of(1,2,3)
     *                                 .map(this::load)
     *                                 .foldFuture(exec,list->list.reduce(0,(a,b)->a+b))
     *
     *  }
     *  </pre>
     *
     *
     *
     * @param fn Folding function
     * @param ex Executor to perform fold on
     * @return Future that will contain the result when complete
     */
    default <R> Future<R> foldFuture(Executor ex,Function<? super IterableX<T>,? extends R> fn){

        return Future.of(()->fn.apply(this),ex);
    }
    default Future<Void> runFuture(Executor ex, Consumer<? super IterableX<T>> fn){
        return Future.of(()-> { fn.accept(this); return null;},ex);
    }

    /**
     * Perform a maybe caching fold (results are memoized)
     *  <pre>
     *  {@code
     *    Eval<Integer> sum =  Seq.of(1,2,3)
     *                                 .map(this::load)
     *                                 .foldLazy(list->list.reduce(0,(a,b)->a+b))
     *
     *  }
     *  </pre>
     *
     *
     * @param fn Folding function
     * @return Eval that lazily performs the fold once
     */
    default <R> Eval<R> foldLazy(Function<? super IterableX<T>,? extends R> fn){
        return Eval.later(()->fn.apply(this));
    }
    default Eval<Void> runLazy(Consumer<? super IterableX<T>> fn){
        return Eval.later(()->{ fn.accept(this); return null;});
    }

    /**
     * Try a fold, capturing any unhandling execution exceptions (that fold the provided classes)
     *  <pre>
     *  {@code
     *    Try<Integer,Throwable> sum =  Seq.of(1,2,3)
     *                                       .map(this::load)
     *                                       .foldLazy(list->list.reduce(0,(a,b)->a+b),IOException.class)
     *
     *  }
     *  </pre>
     * @param fn Folding function
     * @param classes Unhandled Exception types to capture in Try
     * @return Try that eagerly executes the fold and captures specified unhandled exceptions
     */
    default <R, X extends Throwable> Try<R, X> foldTry(Function<? super IterableX<T>,? extends R> fn,
                                                       final Class<X>... classes){
        return Try.withCatch(()->fn.apply(this),classes);
    }

    default Function1<Long,T> asFunction(){
        return index->this.elementAt(index).orElse(null);
    }







    @Override
    default <X extends Throwable> Subscription forEachSubscribe(Consumer<? super T> consumer){
        Subscription result = ReactiveStreamsTerminalOperations.super.forEachSubscribe(consumer, e->e.printStackTrace(),()->{});
        return result;
    }

    @Override
    default <X extends Throwable> Subscription forEachSubscribe(Consumer<? super T> consumer, Consumer<? super Throwable> consumerError){
        Subscription result = ReactiveStreamsTerminalOperations.super.forEachSubscribe(consumer,consumerError,()->{});
        return result;
    }

    @Override
    default <X extends Throwable> Subscription forEachSubscribe(Consumer<? super T> consumer, Consumer<? super Throwable> consumerError, Runnable onComplete){
        Subscription result = ReactiveStreamsTerminalOperations.super.forEachSubscribe(consumer,consumerError,onComplete);
        return result;
    }
    @Override
    default <X extends Throwable> Subscription forEach(long numberOfElements, Consumer<? super T> consumer){
        return stream().forEach(numberOfElements,consumer);
    }

    @Override
    default <X extends Throwable> Subscription forEach(long numberOfElements, Consumer<? super T> consumer, Consumer<? super Throwable> consumerError){
        return stream().forEach(numberOfElements,consumer,consumerError);
    }

    @Override
    default <X extends Throwable> Subscription forEach(long numberOfElements, Consumer<? super T> consumer, Consumer<? super Throwable> consumerError, Runnable onComplete){
        return stream().forEach(numberOfElements,consumer,consumerError,onComplete);
    }

    @Override
    default <X extends Throwable> void forEach(Consumer<? super T> consumerElement, Consumer<? super Throwable> consumerError){
        stream().forEach(consumerElement,consumerError);
    }

    @Override
    default <X extends Throwable> void forEach(Consumer<? super T> consumerElement, Consumer<? super Throwable> consumerError, Runnable onComplete){
        stream().forEach(consumerElement, consumerError, onComplete);
    }

    @Override
    default <U> IterableX<U> ofType(final Class<? extends U> type) {
        return (IterableX<U>)Traversable.super.ofType(type);
    }

    @Override
    default IterableX<T> filterNot(final Predicate<? super T> predicate) {
        return (IterableX<T>)Traversable.super.filterNot(predicate);
    }

    @Override
    default IterableX<T> notNull() {
        return (IterableX<T>)Traversable.super.notNull();
    }

    @Override
    default IterableX<T> removeStream(final Stream<? extends T> stream) {
        return (IterableX<T>)Traversable.super.removeStream(stream);
    }

    default IterableX<T> removeAll(final Iterable<? extends T> it) {
        return (IterableX<T>)Traversable.super.removeAll(it);
    }

    @Override
    default IterableX<T> removeAll(final T... values) {
        return (IterableX<T>)Traversable.super.removeAll(values);
    }

    @Override
    default IterableX<T> retainAll(final Iterable<? extends T> it) {
        return (IterableX<T>)Traversable.super.retainAll(it);
    }

    @Override
    default IterableX<T> retainStream(final Stream<? extends T> stream) {
        return (IterableX<T>)Traversable.super.retainStream(stream);
    }

    @Override
    default IterableX<T> retainAll(final T... values) {
        return (IterableX<T>)Traversable.super.retainAll(values);
    }

    @Override
    default <T2, R> IterableX<R> zip(final BiFunction<? super T, ? super T2, ? extends R> fn, final Publisher<? extends T2> publisher) {
      return (IterableX<R>)Traversable.super.zip(fn, publisher);
    }

    @Override
    default <U> IterableX<Tuple2<T, U>> zipWithStream(final Stream<? extends U> other) {
      return (IterableX)Traversable.super.zipWithStream(other);
    }

    @Override
    default <T2, R> IterableX<R> zipWithStream(final Stream<? extends T2> other,final BiFunction<? super T, ? super T2, ? extends R> fn) {
      return (IterableX<R>)Traversable.super.zipWithStream(other,fn);
    }

    @Override
    default <U> IterableX<Tuple2<T, U>> zipWithPublisher(final Publisher<? extends U> other) {
        return (IterableX)Traversable.super.zipWithPublisher(other);
    }

    @Override
    default <U> IterableX<Tuple2<T, U>> zip(final Iterable<? extends U> other) {
        return (IterableX)Traversable.super.zip(other);
    }

    @Override
    default <S, U, R> IterableX<R> zip3(final Iterable<? extends S> second, final Iterable<? extends U> third, final Function3<? super T, ? super S, ? super U, ? extends R> fn3) {
        return (IterableX)Traversable.super.zip3(second,third,fn3);
    }

    @Override
    default <T2, T3, T4, R> IterableX<R> zip4(final Iterable<? extends T2> second, final Iterable<? extends T3> third, final Iterable<? extends T4> fourth, final Function4<? super T, ? super T2, ? super T3, ? super T4, ? extends R> fn) {
        return (IterableX)Traversable.super.zip4(second,third,fourth,fn);
    }


    @Override
    default IterableX<T> combine(final BiPredicate<? super T, ? super T> predicate, final BinaryOperator<T> op) {
        return (IterableX)Traversable.super.combine(predicate,op);
    }

    @Override
    default IterableX<T> combine(final Monoid<T> op, final BiPredicate<? super T, ? super T> predicate) {
        return (IterableX)Traversable.super.combine(op,predicate);
    }

    @Override
    default IterableX<T> cycle(final long times) {
        return (IterableX)Traversable.super.cycle(times);
    }

    @Override
    default IterableX<T> cycle(final Monoid<T> m, final long times) {
        return (IterableX)Traversable.super.cycle(m,times);
    }

    @Override
    default IterableX<T> cycleWhile(final Predicate<? super T> predicate) {
        return (IterableX)Traversable.super.cycleWhile(predicate);
    }

    @Override
    default IterableX<T> cycleUntil(final Predicate<? super T> predicate) {
        return (IterableX)Traversable.super.cycleUntil(predicate);
    }

    @Override
    default <U, R> IterableX<R> zip(final Iterable<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {
        return (IterableX<R>)Traversable.super.zip(other,zipper);
    }

    @Override
    default <S, U> IterableX<Tuple3<T, S, U>> zip3(final Iterable<? extends S> second, final Iterable<? extends U> third) {
        return (IterableX)Traversable.super.zip3(second,third);
    }

    @Override
    default <T2, T3, T4> IterableX<Tuple4<T, T2, T3, T4>> zip4(final Iterable<? extends T2> second, final Iterable<? extends T3> third, final Iterable<? extends T4> fourth) {
        return (IterableX)Traversable.super.zip4(second,third,fourth);
    }

    @Override
    default IterableX<Tuple2<T, Long>> zipWithIndex() {
        return (IterableX)Traversable.super.zipWithIndex();
    }

    @Override
    default IterableX<Seq<T>> sliding(final int windowSize) {
        return (IterableX<Seq<T>>)Traversable.super.sliding(windowSize);
    }

    @Override
    default IterableX<Seq<T>> sliding(final int windowSize, final int increment) {
        return (IterableX<Seq<T>>)Traversable.super.sliding(windowSize,increment);
    }

    @Override
    default <C extends PersistentCollection<? super T>> IterableX<C> grouped(final int size, final Supplier<C> supplier) {
        return (IterableX<C>)Traversable.super.grouped(size,supplier);
    }

    @Override
    default IterableX<Vector<T>> groupedUntil(final Predicate<? super T> predicate) {
        return (IterableX<Vector<T>>)Traversable.super.groupedUntil(predicate);
    }

    @Override
    default IterableX<Vector<T>> groupedUntil(final BiPredicate<Vector<? super T>, ? super T> predicate) {
        return (IterableX<Vector<T>>)Traversable.super.groupedUntil(predicate);
    }

  @Override
    default IterableX<Vector<T>> groupedWhile(final Predicate<? super T> predicate) {
        return (IterableX<Vector<T>>)Traversable.super.groupedWhile(predicate);
    }

    @Override
    default <C extends PersistentCollection<? super T>> IterableX<C> groupedWhile(final Predicate<? super T> predicate, final Supplier<C> factory) {
        return (IterableX<C>)Traversable.super.groupedWhile(predicate,factory);
    }

    @Override
    default <C extends PersistentCollection<? super T>> IterableX<C> groupedUntil(final Predicate<? super T> predicate, final Supplier<C> factory) {
        return (IterableX<C>)Traversable.super.groupedUntil(predicate,factory);
    }

    @Override
    default IterableX<Vector<T>> grouped(final int groupSize) {
        return (IterableX<Vector<T>>)Traversable.super.grouped(groupSize);
    }

    @Override
    default IterableX<T> distinct() {
        return (IterableX<T>)Traversable.super.distinct();
    }

    @Override
    default IterableX<T> scanLeft(final Monoid<T> monoid) {
        return (IterableX<T>)Traversable.super.scanLeft(monoid);
    }

    @Override
    default <U> IterableX<U> scanLeft(final U seed, final BiFunction<? super U, ? super T, ? extends U> function) {
        return (IterableX<U>)Traversable.super.scanLeft(seed,function);
    }

    @Override
    default IterableX<T> scanRight(final Monoid<T> monoid) {
        return (IterableX<T>)Traversable.super.scanRight(monoid);
    }

    @Override
    default <U> IterableX<U> scanRight(final U identity, final BiFunction<? super T, ? super U, ? extends U> combiner) {
        return (IterableX<U>)Traversable.super.scanRight(identity,combiner);
    }

    @Override
    default IterableX<T> sorted() {
        return (IterableX<T>)Traversable.super.sorted();
    }

    @Override
    default IterableX<T> sorted(final Comparator<? super T> c) {
        return (IterableX<T>)Traversable.super.sorted(c);
    }

    @Override
    default IterableX<T> takeWhile(final Predicate<? super T> p) {
        return (IterableX<T>)Traversable.super.takeWhile(p);
    }

    @Override
    default IterableX<T> dropWhile(final Predicate<? super T> p) {
        return (IterableX<T>)Traversable.super.dropWhile(p);
    }

    @Override
    default IterableX<T> takeUntil(final Predicate<? super T> p) {
        return (IterableX<T>)Traversable.super.takeUntil(p);
    }

    @Override
    default IterableX<T> dropUntil(final Predicate<? super T> p) {
        return (IterableX<T>)Traversable.super.dropUntil(p);
    }

    @Override
    default IterableX<T> dropRight(final int num) {
        return (IterableX<T>)Traversable.super.dropRight(num);
    }

    @Override
    default IterableX<T> takeRight(final int num) {
        return (IterableX<T>)Traversable.super.takeRight(num);
    }

    @Override
    default IterableX<T> drop(final long num) {
        return (IterableX<T>)Traversable.super.drop(num);
    }

    @Override
    default IterableX<T> skip(final long num) {
        return (IterableX<T>)Traversable.super.skip(num);
    }

    @Override
    default IterableX<T> skipWhile(final Predicate<? super T> p) {
        return (IterableX<T>)Traversable.super.skipWhile(p);
    }

    @Override
    default IterableX<T> skipUntil(final Predicate<? super T> p) {
        return (IterableX<T>)Traversable.super.skipUntil(p);
    }

    @Override
    default IterableX<T> take(final long num) {
        return (IterableX<T>)Traversable.super.take(num);
    }

    @Override
    default IterableX<T> limit(final long num) {
        return (IterableX<T>)Traversable.super.limit(num);
    }

    @Override
    default IterableX<T> limitWhile(final Predicate<? super T> p) {
        return (IterableX<T>)Traversable.super.limitWhile(p);
    }

    @Override
    default IterableX<T> limitUntil(final Predicate<? super T> p) {
        return (IterableX<T>)Traversable.super.limitUntil(p);
    }

    @Override
    default IterableX<T> intersperse(final T value) {
        return (IterableX<T>)Traversable.super.intersperse(value);
    }

    @Override
    default IterableX<T> reverse() {
        return (IterableX<T>)Traversable.super.reverse();
    }

    @Override
    default IterableX<T> shuffle() {
        return (IterableX<T>)Traversable.super.shuffle();
    }

    @Override
    default IterableX<T> skipLast(final int num) {
        return (IterableX<T>)Traversable.super.skipLast(num);
    }

    @Override
    default IterableX<T> limitLast(final int num) {
        return (IterableX<T>)Traversable.super.limitLast(num);
    }

    @Override
    default IterableX<T> onEmpty(final T value) {
        return (IterableX<T>)Traversable.super.onEmpty(value);
    }

    @Override
    default IterableX<T> onEmptyGet(final Supplier<? extends T> supplier) {
        return (IterableX<T>)Traversable.super.onEmptyGet(supplier);
    }



    @Override
    default IterableX<T> shuffle(final Random random) {
        return (IterableX<T>)Traversable.super.shuffle(random);
    }

    @Override
    default IterableX<T> slice(final long from, final long to) {
        return (IterableX<T>) Traversable.super.slice(from,to);
    }

    @Override
    default <U extends Comparable<? super U>> IterableX<T> sorted(final Function<? super T, ? extends U> function) {
        return (IterableX<T>)Traversable.super.sorted(function);
    }


    @Override
    default IterableX<T> prependStream(Stream<? extends T> stream) {
        return (IterableX<T>)Traversable.super.prependStream(stream);
    }

    default IterableX<T> plusAll(Iterable<? extends T> list){
        IterableX<T> res = this;
        for(T next : list){
            res = res.append(next);
        }
        return res;
    }


    default IterableX<T> plus(T value){
        return append(value);
    }



    default IterableX<T> removeValue(T value){
        return unitIterable(stream().removeValue(value));
    }
    default IterableX<T> removeAt(long pos){
        return (IterableX<T>)Traversable.super.removeAt(pos);
    }
    default IterableX<T> removeAt(int pos){
        return (IterableX<T>)Traversable.super.removeAt(pos);
    }


    default IterableX<T> removeFirst(Predicate<? super T> pred){
        return (IterableX<T>)Traversable.super.removeFirst(pred);
    }


    @Override
    default IterableX<T> appendAll(T... values) {
        return (IterableX<T>)Traversable.super.appendAll(values);
    }

    @Override
    default IterableX<T> append(T value) {
        return (IterableX<T>)Traversable.super.append(value);
    }
    @Override
    default IterableX<T> appendAll(Iterable<? extends T> value){
        return (IterableX<T>)Traversable.super.appendAll(value);
    }
    @Override
    default IterableX<T> prependAll(Iterable<? extends T> value){
        return (IterableX<T>)Traversable.super.prependAll(value);
    }
    @Override
    default IterableX<T> prepend(T value) {
        return (IterableX<T>)Traversable.super.prepend(value);
    }

    @Override
    default IterableX<T> prependAll(T... values) {
        return (IterableX<T>)Traversable.super.prependAll(values);
    }
    @Override
    default IterableX<T> updateAt(int pos, T value) {
        return (IterableX<T>)Traversable.super.updateAt(pos,value);
    }

    @Override
    default IterableX<T> deleteBetween(int start, int end) {
        return (IterableX<T>)Traversable.super.deleteBetween(start,end);
    }


    @Override
    default IterableX<T> insertStreamAt(int pos, Stream<T> stream) {
        return (IterableX<T>)Traversable.super.insertStreamAt(pos,stream);
    }




    @Override
    default IterableX<T> peek(final Consumer<? super T> c) {
        return (IterableX<T>)Traversable.super.peek(c);
    }




    /**
     * Perform a flatMap operation on this IterableX. Results from the returned Iterables (from the
     * provided transformation function) are flattened into the resulting toX.
     *
     * @param mapper Transformation function to be applied (and flattened)
     * @return An IterableX containing the flattened results of the transformation function
     */
    default <R> IterableX<R> concatMap(Function<? super T, ? extends Iterable<? extends R>> mapper){
        return stream().concatMap(mapper);
    }
    default <R> IterableX<R> mergeMap(Function<? super T, ? extends Publisher<? extends R>> fn){
      return stream().mergeMap(fn);
    }
    default <R> IterableX<R> mergeMap(int maxConcurecy, Function<? super T, ? extends Publisher<? extends R>> fn){
      return stream().mergeMap(maxConcurecy,fn);
    }

    default IterableX<T> insertAt(int i, T value){
        IterableX<T> front = take(i);
        IterableX<T> back = drop(i);


        return back.prepend(value).prependAll(front);
    }
    @Override
    default IterableX<T> insertAt(int pos, T... values) {
        IterableX<T> front = take(pos);
        IterableX<T> back = drop(pos);
        for(int i=values.length-1;i>=0;--i){
           back = back.prepend(values[i]);
        }


        return back.prependAll(front);
    }

    @Override
    default IterableX<T> insertAt(int pos, Iterable<? extends T> values) {
        IterableX<T> front = take(pos);
        IterableX<T> back = drop(pos);
        List<T> list = new ArrayList<>();
        for(T next : values){
          list.add(next);
        }
        for(int i=list.size()-1;i>=0;--i){
          back = back.prepend(list.get(i));
        }

      return back.prependAll(front);
    }


    default boolean containsValue(T v){
        return stream().filter(t->Objects.equals(t,v)).findFirst().isPresent();
    }



    default IterableX<ReactiveSeq<T>> permutations() {
        return stream().permutations();
    }


    default IterableX<ReactiveSeq<T>> combinations(final int size) {
        return stream().combinations(size);
    }


    default IterableX<ReactiveSeq<T>> combinations() {
        return unitIterable(stream().combinations());

    }


}
