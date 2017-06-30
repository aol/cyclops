package com.aol.cyclops2.types.anyM;

import com.aol.cyclops2.types.Filters;
import cyclops.function.Fn3;
import cyclops.function.Fn4;
import cyclops.function.Monoid;
import cyclops.monads.AnyM;
import cyclops.monads.WitnessType;
import cyclops.stream.ReactiveSeq;
import cyclops.control.Trampoline;
import cyclops.control.Xor;
import com.aol.cyclops2.types.MonadicValue;
import com.aol.cyclops2.types.Value;
import com.aol.cyclops2.types.Zippable;
import cyclops.function.Predicates;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;
import org.reactivestreams.Publisher;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.Stream;

/**
 * Wrapper around 'Any' scalar 'M'onad
 * 
 * @author johnmcclean
 *
 * @param <T> Data types of elements managed by wrapped scalar Monad.
 */
public interface AnyMValue<W extends WitnessType<W>,T> extends  AnyM<W,T>,
                                                                Value<T>,
                                                                Filters<T>,
                                                                MonadicValue<T> {


    @Override
    default <R, A> R collect(Collector<? super T, A, R> collector) {
        return AnyM.super.collect(collector);
    }

    /**
     * Equivalence test, returns true if this Monad is equivalent to the supplied monad
     * e.g.
     * <pre>
     * {code
     *     Optional.of(1) and CompletableFuture.completedFuture(1) are equivalent
     * }
     * </pre>
     * 
     * 
     * @param t Monad toNested compare toNested
     * @return true if equivalent
     */
    default boolean eqv(final AnyMValue<?,T> t) {
        return Predicates.eqv(t)
                         .test(this);
    }
    

    /* (non-Javadoc)
     * @see cyclops2.monads.AnyM#combine(java.util.function.BinaryOperator, com.aol.cyclops2.types.Applicative)
     */
    @Override
    default AnyMValue<W,T> zip(BinaryOperator<Zippable<T>> combiner, Zippable<T> app) {
        
        return (AnyMValue<W,T>)MonadicValue.super.zip(combiner, app);
    }

    @Override
    default int arity() {
        return 1;
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#coflatMap(java.util.function.Function)
     */
    @Override
    default <R> AnyMValue<W,R> coflatMap(final Function<? super MonadicValue<T>, R> mapper) {
        return mapper.andThen(r -> unit(r))
                     .apply(this);
    }

    /* cojoin
     * (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#nest()
     */
    @Override
    default AnyMValue<W,MonadicValue<T>> nest() {
        return unit(this);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue2#combine(cyclops2.function.Monoid, com.aol.cyclops2.types.MonadicValue2)
     */
    default AnyMValue<W,T> combineEager(final Monoid<T> monoid, final AnyMValue<W,? extends T> v2) {
        return unit(this.<T> flatMap(t1 -> v2.map(t2 -> monoid.apply(t1, t2)))
                        .orElseGet(() -> orElseGet(() -> monoid.zero())));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Value#mkString()
     */
    @Override
    default String mkString() {
        final Optional<T> opt = toOptional();
        return opt.isPresent() ? "AnyMValue[" + get() + "]" : "AnyMValue[]";
    }

    @Override
    default <R> AnyMValue<W,R> zipWith(Iterable<Function<? super T, ? extends R>> fn) {
        return (AnyMValue<W,R>)AnyM.super.zipWith(fn);
    }

    @Override
    default <R> AnyMValue<W,R> zipWithS(Stream<Function<? super T, ? extends R>> fn) {
        return (AnyMValue<W,R>)AnyM.super.zipWithS(fn);
    }

    @Override
    default <R> AnyMValue<W,R> zipWithP(Publisher<Function<? super T, ? extends R>> fn) {
        return (AnyMValue<W,R>)AnyM.super.zipWithP(fn);
    }

    @Override
    default <R> AnyMValue<W,R> retry(final Function<? super T, ? extends R> fn) {
        return (AnyMValue<W,R>)AnyM.super.retry(fn);
    }

    @Override
    default <U> AnyMValue<W,Tuple2<T, U>> zipP(final Publisher<? extends U> other) {
        return (AnyMValue)AnyM.super.zipP(other);
    }

    @Override
    default <R> AnyMValue<W,R> retry(final Function<? super T, ? extends R> fn, final int retries, final long delay, final TimeUnit timeUnit) {
        return (AnyMValue<W,R>)AnyM.super.retry(fn,retries,delay,timeUnit);
    }

    @Override
    default <S, U> AnyMValue<W,Tuple3<T, S, U>> zip3(final Iterable<? extends S> second, final Iterable<? extends U> third) {
        return (AnyMValue)AnyM.super.zip3(second,third);
    }

    @Override
    default <S, U, R> AnyMValue<W,R> zip3(final Iterable<? extends S> second, final Iterable<? extends U> third, final Fn3<? super T, ? super S, ? super U, ? extends R> fn3) {
        return (AnyMValue<W,R>)AnyM.super.zip3(second,third,fn3);
    }

    @Override
    default <T2, T3, T4> AnyMValue<W,Tuple4<T, T2, T3, T4>> zip4(final Iterable<? extends T2> second, final Iterable<? extends T3> third, final Iterable<? extends T4> fourth) {
        return (AnyMValue)AnyM.super.zip4(second,third,fourth);
    }

    @Override
    default <T2, T3, T4, R> AnyMValue<W,R> zip4(final Iterable<? extends T2> second, final Iterable<? extends T3> third, final Iterable<? extends T4> fourth, final Fn4<? super T, ? super T2, ? super T3, ? super T4, ? extends R> fn) {
        return (AnyMValue<W,R>)AnyM.super.zip4(second,third,fourth,fn);
    }



    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Filters#ofType(java.lang.Class)
     */
    @Override
    default <U> AnyMValue<W,U> ofType(final Class<? extends U> type) {

        return (AnyMValue<W,U>) MonadicValue.super.ofType(type);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Filters#filterNot(java.util.function.Predicate)
     */
    @Override
    default AnyMValue<W,T> filterNot(final Predicate<? super T> fn) {

        return (AnyMValue<W,T>) MonadicValue.super.filterNot(fn);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Filters#notNull()
     */
    @Override
    default AnyMValue<W,T> notNull() {

        return (AnyMValue<W,T>) MonadicValue.super.notNull();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Functor#cast(java.lang.Class)
     */
    @Override
    default <U> AnyMValue<W,U> cast(final Class<? extends U> type) {

        return (AnyMValue<W,U>) AnyM.super.cast(type);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Functor#trampoline(java.util.function.Function)
     */
    @Override
    default <R> AnyMValue<W,R> trampoline(final Function<? super T, ? extends Trampoline<? extends R>> mapper) {

        return (AnyMValue<W,R>) AnyM.super.trampoline(mapper);
    }


    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.EmptyUnit#emptyUnit()
     */
    @Override
    default <T> AnyMValue<W,T> emptyUnit(){
        return empty();
    }



   

    /* (non-Javadoc)
     * @see com.aol.cyclops2.monad.AnyM#filter(java.util.function.Predicate)
     */
    @Override
    default AnyMValue<W,T> filter(Predicate<? super T> p){
        return (AnyMValue<W,T>)AnyM.super.filter(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.monad.AnyM#map(java.util.function.Function)
     */
    @Override
    default <R> AnyMValue<W,R> map(Function<? super T, ? extends R> fn){
        return (AnyMValue<W,R>)AnyM.super.map(fn);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.monad.AnyM#peek(java.util.function.Consumer)
     */
    @Override
    default AnyMValue<W,T> peek(Consumer<? super T> c){
        return (AnyMValue<W,T>)AnyM.super.peek(c);
    }


    @Override
    default boolean isPresent() {
        Object unwrap = unwrap();
        if (unwrap instanceof Value) {
            return ((Value<T>) unwrap).isPresent();
        }else if(unwrap instanceof Optional){
            return ((Optional<T>) unwrap).isPresent();
        }else if(unwrap instanceof Iterable){
            return ((Iterable<T>)unwrap).iterator().hasNext();
        }else{
            return this.adapter().toIterable(this).iterator().hasNext();
        }

    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.monad.AnyM#aggregate(com.aol.cyclops2.monad.AnyM)
     */
    @Override
    default AnyMValue<W,List<T>> aggregate(AnyM<W,T> next){
        return (AnyMValue<W,List<T>>)AnyM.super.aggregate(next);
    }


    @Override
    default <R> AnyMValue<W,R> flatMapA(Function<? super T, ? extends AnyM<W,? extends R>> fn){
        return  (AnyMValue<W,R>)AnyM.super.flatMapA(fn);   
        
    }

    default <R> AnyMValue<W,R> flatMapI(final Function<? super T, ? extends Iterable<? extends R>> fn){
        return (AnyMValue<W,R>)MonadicValue.super.flatMapI(fn);
    }
    default <R> AnyMValue<W,R> flatMapP(Function<? super T, ? extends Publisher<? extends R>> fn){
        return (AnyMValue<W,R>)MonadicValue.super.flatMapP(fn);
    }
    default <R> AnyMValue<W,R> flatMapS(Function<? super T, ? extends Stream<? extends R>> fn){
        return (AnyMValue<W,R>)MonadicValue.super.flatMapS(fn);
    }

    @Override
    default <R> AnyMValue<W,R> flatMap(Function<? super T, ? extends MonadicValue<? extends R>> fn){
        if(unwrap() instanceof MonadicValue){
            MonadicValue<T> unwrap = unwrap();
            return AnyM.ofValue(unwrap.flatMap(fn),adapter());
        }
        return flatMapA(fn.andThen(a->this.fromIterable(a)));
    }
    @Override
    default T get() {
        return adapter().visit(e->{ throw new IllegalAccessError("misconfigured adapter : value adapter required");}
                             , v->v.get(this));
    }
   

    /* (non-Javadoc)
     * @see com.aol.cyclops2.monad.AnyM#unit(java.lang.Object)
     */
    @Override
    default <T> AnyMValue<W,T> unit(T value){
        return (AnyMValue<W,T>)AnyM.super.unit(value);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.monad.AnyM#empty()
     */
    @Override
    default <T> AnyMValue<W,T> empty(){
        return (AnyMValue<W,T>)AnyM.super.empty();
    }
    
    @Override
    default Xor<AnyMValue<W,T>, AnyMSeq<W,T>> matchable() {
        return Xor.secondary(this);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.fromEither5.AnyMValue#ap(com.aol.cyclops2.types.Value, java.util.function.BiFunction)
     */
    @Override
    default <T2, R> AnyMValue<W,R> combine(final Value<? extends T2> app, final BiFunction<? super T, ? super T2, ? extends R> fn) {
        if (this.unwrap() instanceof MonadicValue) {
            
            return (AnyMValue<W, R>) AnyM.ofValue( ((MonadicValue<T>) unwrap()).combine(app, fn),adapter());
        }
        return (AnyMValue<W, R>) MonadicValue.super.combine(app, fn);
    }



    @Override
    default <T2, R> AnyMValue<W,R> zip(final Iterable<? extends T2> app, final BiFunction<? super T, ? super T2, ? extends R> fn) {
        if (this.unwrap() instanceof Zippable) {
            return (AnyMValue<W, R>) adapter().unit(((Zippable) unwrap()).zip(app, fn));
        }
        return (AnyMValue<W,R>) MonadicValue.super.zip(app, fn);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.applicative.ApplicativeFunctor#zip(java.util.function.BiFunction, org.reactivestreams.Publisher)
     */
    @Override
    default <T2, R> AnyMValue<W,R> zipP(final Publisher<? extends T2> app,final BiFunction<? super T, ? super T2, ? extends R> fn) {
        if (this.unwrap() instanceof Zippable) {
            return (AnyMValue<W, R>) adapter().unit(((Zippable) unwrap()).zipP(app,fn));
        }
        return (AnyMValue<W,R>) MonadicValue.super.zipP(app,fn);
    }

    

    @Override
    default Iterator<T> iterator() {

        return AnyM.super.iterator();
    }

    @Override
    default ReactiveSeq<T> stream() {
        return AnyM.super.stream();
    }


    @Override
    default <T2, R1, R2, R3, R> AnyMValue<W,R> forEach4(final Function<? super T, ? extends MonadicValue<R1>> value1, final BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2, final Fn3<? super T, ? super R1, ? super R2, ? extends MonadicValue<R3>> value3, final Fn4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        return (AnyMValue<W,R>)MonadicValue.super.forEach4(value1,value2,value3,yieldingFunction);
    }

    @Override
    default <T2, R1, R2, R3, R> AnyMValue<W,R> forEach4(final Function<? super T, ? extends MonadicValue<R1>> value1, final BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2, final Fn3<? super T, ? super R1, ? super R2, ? extends MonadicValue<R3>> value3, final Fn4<? super T, ? super R1, ? super R2, ? super R3, Boolean> filterFunction, final Fn4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        return (AnyMValue<W,R>)MonadicValue.super.forEach4(value1,value2,value3,filterFunction,yieldingFunction);
    }

    @Override
    default <T2, R1, R2, R> AnyMValue<W,R> forEach3(final Function<? super T, ? extends MonadicValue<R1>> value1, final BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2, final Fn3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {
        return (AnyMValue<W,R>)MonadicValue.super.forEach3(value1,value2,yieldingFunction);
    }

    @Override
    default <T2, R1, R2, R> AnyMValue<W,R> forEach3(final Function<? super T, ? extends MonadicValue<R1>> value1, final BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2, final Fn3<? super T, ? super R1, ? super R2, Boolean> filterFunction, final Fn3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {
        return (AnyMValue<W,R>)MonadicValue.super.forEach3(value1,value2,filterFunction,yieldingFunction);
    }

    @Override
    default <R1, R> AnyMValue<W,R> forEach2(Function<? super T, ? extends MonadicValue<R1>> value1, final BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {
        return (AnyMValue<W,R>)MonadicValue.super.forEach2(value1,yieldingFunction);
    }

    @Override
    default <R1, R> AnyMValue<W,R> forEach2(Function<? super T, ? extends MonadicValue<R1>> value1, final BiFunction<? super T, ? super R1, Boolean> filterFunction, final BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {
        return (AnyMValue<W,R>)MonadicValue.super.forEach2(value1,filterFunction,yieldingFunction);
    }

    @Override
    default AnyMValue<W,T> combineEager(final Monoid<T> monoid, final MonadicValue<? extends T> v2) {
        return (AnyMValue<W,T>)MonadicValue.super.combineEager(monoid,v2);
    }
}
