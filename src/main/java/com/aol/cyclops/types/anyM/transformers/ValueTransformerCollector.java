package com.aol.cyclops.types.anyM.transformers;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;

import com.aol.cyclops.Monoid;
import com.aol.cyclops.Reducer;
import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.Eval;
import com.aol.cyclops.control.FutureW;
import com.aol.cyclops.control.Ior;
import com.aol.cyclops.control.LazyReact;
import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.control.SimpleReact;
import com.aol.cyclops.control.Streamable;
import com.aol.cyclops.control.Try;
import com.aol.cyclops.control.Xor;
import com.aol.cyclops.data.LazyImmutable;
import com.aol.cyclops.data.Mutable;
import com.aol.cyclops.data.collections.extensions.CollectionX;
import com.aol.cyclops.data.collections.extensions.persistent.PBagX;
import com.aol.cyclops.data.collections.extensions.persistent.POrderedSetX;
import com.aol.cyclops.data.collections.extensions.persistent.PQueueX;
import com.aol.cyclops.data.collections.extensions.persistent.PSetX;
import com.aol.cyclops.data.collections.extensions.persistent.PStackX;
import com.aol.cyclops.data.collections.extensions.persistent.PVectorX;
import com.aol.cyclops.data.collections.extensions.standard.DequeX;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.data.collections.extensions.standard.QueueX;
import com.aol.cyclops.data.collections.extensions.standard.SetX;
import com.aol.cyclops.data.collections.extensions.standard.SortedSetX;
import com.aol.cyclops.types.Foldable;
import com.aol.cyclops.types.anyM.WitnessType;
import com.aol.cyclops.types.futurestream.LazyFutureStream;
import com.aol.cyclops.types.futurestream.SimpleReactStream;
import com.aol.cyclops.types.stream.HotStream;
import com.aol.cyclops.types.stream.reactive.ValueSubscriber;
import com.aol.cyclops.util.ExceptionSoftener;

public interface ValueTransformerCollector<W extends WitnessType<W>> {
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Value#mapReduce(com.aol.cyclops.Reducer)
     */
   
    default <E> AnyM<W,? extends E> mapReduce(Reducer<E> monoid) {
        
        return this.transformerStream().map(v->v.mapReduce(monoid));
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Value#fold(com.aol.cyclops.Monoid)
     */
   
    default AnyM<W,? extends T> fold(Monoid<T> monoid) {
        return this.transformerStream().map(v->v.fold(monoid));
     
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Value#fold(java.lang.Object, java.util.function.BinaryOperator)
     */
   
    default AnyM<W,? extends T> fold(T identity, BinaryOperator<T> accumulator) {
        return this.transformerStream().map(v->v.fold(identity, accumulator));
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Value#toLazyImmutable()
     */
   
    default AnyM<W,? extends LazyImmutable<T>> toLazyImmutable() {
        return this.transformerStream().map(v->v.toLazyImmutable());

    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Value#toMutable()
     */
   
    default AnyM<W,? extends Mutable<T>> toMutable() {
        return this.transformerStream().map(v->v.toMutable());
       
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Value#toXor()
     */
   
    default AnyM<W,? extends Xor<?, T>> toXor() {
        return this.transformerStream().map(v->v.toXor());
       
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Value#toXor(java.lang.Object)
     */
   
    default <ST> AnyM<W,? extends Xor<ST, T>> toXor(ST secondary) {
        return this.transformerStream().map(v->v.toXor(secondary));
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Value#toTry(java.lang.Throwable)
     */
   
    default <X extends Throwable> AnyM<W,? extends Try<T, X>> toTry(X throwable) {
        return this.transformerStream().map(v->v.toTry(throwable));
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Value#toTry()
     */
   
    default AnyM<W,? extends Try<T, Throwable>> toTry() {
        return this.transformerStream().map(v->v.toTry());
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Value#toTry(java.lang.Class[])
     */
   
    default <X extends Throwable> AnyM<W,? extends Try<T, X>> toTry(Class<X>... classes) {
        return this.transformerStream().map(v->v.toTry(classes));
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Value#toIor()
     */
    default AnyM<W,? extends Ior<?, T>> toIor() {
        return this.transformerStream().map(v->v.toIor());
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Value#toEvalNow()
     */
   
    default AnyM<W,? extends Eval<T>> toEvalNow() {
        return this.transformerStream().map(v->v.toEvalNow());
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Value#toEvalLater()
     */
   
    default AnyM<W,? extends Eval<T>> toEvalLater() {
        return this.transformerStream().map(v->v.toEvalLater());
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Value#toEvalAlways()
     */
   
    default AnyM<W,? extends Eval<T>> toEvalAlways() {
        return this.transformerStream().map(v->v.toEvalAlways());
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Value#toMaybe()
     */
   
    default AnyM<W,? extends Maybe<T>> toMaybe() {
        return this.transformerStream().map(v->v.toMaybe());
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Value#toListX()
     */
   
    default AnyM<W,? extends ListX<T>> toListX() {
        
        return this.transformerStream().map(v->v.toListX());
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Value#toSetX()
     */
   
    default AnyM<W,? extends SetX<T>> toSetX() {
        return this.transformerStream().map(v->v.toSetX());
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Value#toSortedSetX()
     */
   
    default AnyM<W,? extends SortedSetX<T>> toSortedSetX() {
        return this.transformerStream().map(v->v.toSortedSetX());
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Value#toQueueX()
     */
   
    default AnyM<W,? extends QueueX<T>> toQueueX() {
        return this.transformerStream().map(v->v.toQueueX());
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Value#toDequeX()
     */
   
    default AnyM<W,? extends DequeX<T>> toDequeX() {
        return this.transformerStream().map(v->v.toDequeX());
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Value#toPStackX()
     */
   
    default AnyM<W,? extends PStackX<T>> toPStackX() {
        return this.transformerStream().map(v->v.toPStackX());
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Value#toPVectorX()
     */
   
    default AnyM<W,? extends PVectorX<T>> toPVectorX() {
        return this.transformerStream().map(v->v.toPVectorX());
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Value#toPQueueX()
     */
   
    default AnyM<W,? extends PQueueX<T>> toPQueueX() {
        return this.transformerStream().map(v->v.toPQueueX());
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Value#toPSetX()
     */
   
    default AnyM<W,? extends PSetX<T>> toPSetX() {
        return this.transformerStream().map(v->v.toPSetX());
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Value#toPOrderedSetX()
     */
   
    default AnyM<W,? extends POrderedSetX<T>> toPOrderedSetX() {
        return this.transformerStream().map(v->v.toPOrderedSetX());
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Value#toPBagX()
     */
   
    default AnyM<W,? extends PBagX<T>> toPBagX() {
        return this.transformerStream().map(v->v.toPBagX());
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Value#mkString()
     */
   
    default AnyM<W,String> mkString() {
        return this.transformerStream().map(v->v.mkString());
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Value#toFutureStream(com.aol.cyclops.control.LazyReact)
     */
   
    default AnyM<W,? extends LazyFutureStream<T>> toFutureStream(LazyReact reactor) {
        return this.transformerStream().map(v->v.toFutureStream(reactor));
        
    }
   
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Value#toSimpleReact(com.aol.cyclops.control.SimpleReact)
     */
   
    default AnyM<W,? extends SimpleReactStream<T>> toSimpleReact(SimpleReact reactor) {
        return this.transformerStream().map(v->v.toSimpleReact(reactor));
    }
    
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Value#collect(java.util.stream.Collector)
     */
   
    default <R, A> AnyM<W,? extends R> collect(Collector<? super T, A, R> collector) {
        return this.transformerStream().map(v->v.collect(collector));
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Foldable#foldable()
     */
   
    default AnyM<W,? extends Foldable<T>> foldable() {
        return this.transformerStream().map(v->v.foldable());
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Foldable#reduce(java.lang.Object, java.util.function.BiFunction)
     */
   
    default <U> AnyM<W,? extends U> reduce(U identity, BiFunction<U, ? super T, U> accumulator) {
        return this.transformerStream().map(v->v.reduce(identity,accumulator));
    }

    
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Foldable#print(java.io.PrintStream)
     */
   
    default void print(PrintStream str) {
         this.transformerStream().forEach(v->v.print(str));
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Foldable#print(java.io.PrintWriter)
     */
   
    default void print(PrintWriter writer) {
        this.transformerStream().forEach(v->v.print(writer));
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Foldable#printOut()
     */
   
    default void printOut() {
        this.transformerStream().forEach(v->v.printOut());
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Foldable#printErr()
     */
   
    default void printErr() {
        this.transformerStream().forEach(v->v.printErr());
    }
    
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Foldable#toLazyCollection()
     */
   
    default AnyM<W,? extends CollectionX<T>> toLazyCollection() {
        return this.transformerStream().map(v->v.toLazyCollection());
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Foldable#toConcurrentLazyCollection()
     */
   
    default AnyM<W,? extends CollectionX<T>> toConcurrentLazyCollection() {
        return this.transformerStream().map(v->v.toConcurrentLazyCollection());
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Foldable#toConcurrentLazyStreamable()
     */
   
    default AnyM<W,? extends Streamable<T>> toConcurrentLazyStreamable() {
        return this.transformerStream().map(v->v.toConcurrentLazyStreamable());
       
    }
    
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Foldable#single()
     */
   
    default AnyM<W,? extends T> single() {
        return this.transformerStream().map(v->v.single());
        
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Foldable#single(java.util.function.Predicate)
     */
   
    default AnyM<W,? extends T> single(Predicate<? super T> predicate) {
        
        return this.transformerStream().map(v->v.single(predicate));
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Foldable#singleOptional()
     */
   
    default AnyM<W,? extends Optional<T>> singleOptional() {
        return this.transformerStream().map(v->v.singleOptional());
    }
   
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Foldable#schedule(java.lang.String, java.util.concurrent.ScheduledExecutorService)
     */
   
    default AnyM<W,? extends HotStream<T>> schedule(String cron, ScheduledExecutorService ex) {
        return this.transformerStream().map(v->v.schedule(cron, ex));
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Foldable#scheduleFixedDelay(long, java.util.concurrent.ScheduledExecutorService)
     */
   
    default AnyM<W,? extends HotStream<T>> scheduleFixedDelay(long delay, ScheduledExecutorService ex) {
        return this.transformerStream().map(v->v.scheduleFixedDelay(delay, ex));
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Foldable#scheduleFixedRate(long, java.util.concurrent.ScheduledExecutorService)
     */
   
    default AnyM<W,? extends HotStream<T>> scheduleFixedRate(long rate, ScheduledExecutorService ex) {
        return this.transformerStream().map(v->v.scheduleFixedDelay(rate, ex));
       
    }
    
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Convertable#visit(java.util.function.Function, java.util.function.Supplier)
     */
   
    default <R> AnyM<W,? extends R> visit(Function<? super T, ? extends R> present, Supplier<? extends R> absent) {
        return this.transformerStream().map(v->v.visit(present, absent));
       
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Convertable#isPresent()
     */
   
    default AnyM<W,Boolean> isPresent() {
        return this.transformerStream().map(v->v.isPresent());
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Convertable#orElseGet(java.util.function.Supplier)
     */
   
    default AnyM<W,? extends T> orElseGet(Supplier<? extends T> value) {
        return this.transformerStream().map(v->v.orElseGet(value));
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Convertable#toOptional()
     */
   
    default AnyM<W,? extends Optional<T>> toOptional() {
        return this.transformerStream().map(v->v.toOptional());
    }
    
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Convertable#orElse(java.lang.Object)
     */
    default AnyM<W,? extends T> orElse(T value) {
        return this.transformerStream().map(v->v.orElse(value));
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Convertable#orElseThrow(java.util.function.Supplier)
     */
   
    default <X extends Throwable> AnyM<W,? extends T> orElseThrow(Supplier<? extends X> ex) throws X {
        return this.transformerStream().<T>map(v-> { 
            try {
                return v.orElseThrow(ex);
            } catch (Throwable e) {
               throw ExceptionSoftener.throwSoftenedException(e);
            }
        });
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Convertable#toFutureW()
     */
   
    default AnyM<W,? extends FutureW<T>> toFutureW() {
        return this.transformerStream().map(v->v.toFutureW());
    }
    
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Convertable#toFutureWAsync(java.util.concurrent.Executor)
     */
   
    default AnyM<W,? extends FutureW<T>> toFutureW(Executor ex) {
        return this.transformerStream().map(v->v.toFutureWAsync(ex));
    }
    
    /* (non-Javadoc)
     * @see java.util.function.Supplier#get()
     */
   
    default AnyM<W,? extends T> get() {
        return this.transformerStream().map(v->v.get());
    }
    
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Value#iterator()
     */
   
    default AnyM<W,? extends Iterator<T>> iterator() {
        
        return this.transformerStream().map(v->v.iterator());
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Value#test(java.lang.Object)
     */
   
    default AnyM<W,Boolean> test(T t) {
        
        return this.transformerStream().map(v->v.test(t));
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Value#newSubscriber()
     */
   
    default AnyM<W,? extends ValueSubscriber<T>> newSubscriber() {
        return this.transformerStream().map(v->v.newSubscriber());
    }
    
}
