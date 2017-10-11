package com.aol.cyclops2.types.anyM;

import com.aol.cyclops2.types.Filters;
import com.aol.cyclops2.types.MonadicValue;
import com.aol.cyclops2.types.Value;
import com.aol.cyclops2.types.Zippable;
import com.aol.cyclops2.types.extensability.FunctionalAdapter;
import cyclops.control.Option;
import cyclops.control.lazy.Trampoline;
import cyclops.control.Either;
import cyclops.function.*;
import cyclops.monads.AnyM;
import cyclops.monads.AnyM2;
import cyclops.monads.WitnessType;
import cyclops.stream.ReactiveSeq;
import cyclops.collections.tuple.Tuple2;
import cyclops.collections.tuple.Tuple3;
import cyclops.collections.tuple.Tuple4;
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
public interface AnyMValue2<W extends WitnessType<W>,T2,T> extends AnyM2<W,T2,T>,
                                                                AnyMValue<W,T>,
                                                                Value<T>,
                                                                Filters<T>,
                                                                MonadicValue<T> {


    @Override
    default <R, A> R collect(Collector<? super T, A, R> collector) {
        return AnyM2.super.collect(collector);
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
     * @param t Monad to compare to
     * @return true if equivalent
     */
    default boolean eqv(final AnyMValue2<?, T2,T> t) {
        return Predicates.eqv(t)
                         .test(this);
    }
    

    /* (non-Javadoc)
     * @see cyclops2.monads.AnyM#combine(java.util.function.BinaryOperator, com.aol.cyclops2.types.Applicative)
     */
    @Override
    default AnyMValue2<W,T2,T> zip(BinaryOperator<Zippable<T>> combiner, Zippable<T> app) {
        
        return (AnyMValue2<W,T2,T>)AnyMValue.super.zip(combiner, app);
    }


    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.MonadicValue#coflatMap(java.util.function.Function)
     */
    @Override
    default <R> AnyMValue2<W,T2,R> coflatMap(final Function<? super MonadicValue<T>, R> mapper) {
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
    default AnyMValue2<W,T2,T> combineEager(final Monoid<T> monoid, final AnyMValue<W, ? extends T> v2) {
        return unit(this.<T> flatMap(t1 -> v2.map(t2 -> monoid.apply(t1, t2)))
                        .orElseGet(() -> orElseGet(() -> monoid.zero())));
    }


    @Override
    default String mkString() {
        return visit(s->"AnyMValue2[" + s + "]",()->"AnyMValue2[]");

    }

    @Override
    default <R> AnyMValue2<W,T2,R> zipWith(Iterable<Function<? super T, ? extends R>> fn) {
        return (AnyMValue2<W,T2,R>)AnyM2.super.zipWith(fn);
    }

    @Override
    default <R> AnyMValue2<W,T2,R> zipWithS(Stream<Function<? super T, ? extends R>> fn) {
        return (AnyMValue2<W,T2,R>)AnyM2.super.zipWithS(fn);
    }

    @Override
    default <R> AnyMValue2<W,T2,R> zipWithP(Publisher<Function<? super T, ? extends R>> fn) {
        return (AnyMValue2<W,T2,R>)AnyM2.super.zipWithP(fn);
    }

    @Override
    default <R> AnyMValue2<W,T2,R> retry(final Function<? super T, ? extends R> fn) {
        return (AnyMValue2<W,T2,R>)AnyM2.super.retry(fn);
    }

    @Override
    default <U> AnyMValue2<W,T2,Tuple2<T, U>> zipP(final Publisher<? extends U> other) {
        return (AnyMValue2)AnyM2.super.zipP(other);
    }

    @Override
    default <R> AnyMValue2<W,T2,R> retry(final Function<? super T, ? extends R> fn, final int retries, final long delay, final TimeUnit timeUnit) {
        return (AnyMValue2<W,T2,R>)AnyM2.super.retry(fn,retries,delay,timeUnit);
    }

    @Override
    default <S, U> AnyMValue2<W,T2,Tuple3<T, S, U>> zip3(final Iterable<? extends S> second, final Iterable<? extends U> third) {
        return (AnyMValue2)AnyM2.super.zip3(second,third);
    }

    @Override
    default <S, U, R> AnyMValue2<W,T2,R> zip3(final Iterable<? extends S> second, final Iterable<? extends U> third, final Function3<? super T, ? super S, ? super U, ? extends R> fn3) {
        return (AnyMValue2<W,T2,R>)AnyM2.super.zip3(second,third,fn3);
    }

    @Override
    default <T2, T3, T4> AnyMValue2<W,T2,Tuple4<T, T2, T3, T4>> zip4(final Iterable<? extends T2> second, final Iterable<? extends T3> third, final Iterable<? extends T4> fourth) {
        return (AnyMValue2)AnyM2.super.zip4(second,third,fourth);
    }

    @Override
    default <T2, T3, T4, R> AnyMValue2<W,T2,R> zip4(final Iterable<? extends T2> second, final Iterable<? extends T3> third, final Iterable<? extends T4> fourth, final Function4<? super T, ? super T2, ? super T3, ? super T4, ? extends R> fn) {
        return (AnyMValue2<W,T2,R>)AnyM2.super.zip4(second,third,fourth,fn);
    }



    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Filters#ofType(java.lang.Class)
     */
    @Override
    default <U> AnyMValue2<W,T2,U> ofType(final Class<? extends U> type) {

        return (AnyMValue2<W,T2,U>) AnyMValue.super.ofType(type);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Filters#filterNot(java.util.function.Predicate)
     */
    @Override
    default AnyMValue2<W,T2,T> filterNot(final Predicate<? super T> fn) {

        return (AnyMValue2<W,T2,T>) AnyMValue.super.filterNot(fn);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Filters#notNull()
     */
    @Override
    default AnyMValue2<W,T2,T> notNull() {

        return (AnyMValue2<W,T2,T>) AnyMValue.super.notNull();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Functor#cast(java.lang.Class)
     */
    @Override
    default <U> AnyMValue2<W,T2,U> cast(final Class<? extends U> type) {

        return (AnyMValue2<W,T2,U>) AnyMValue.super.cast(type);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Functor#trampoline(java.util.function.Function)
     */
    @Override
    default <R> AnyMValue2<W,T2,R> trampoline(final Function<? super T, ? extends Trampoline<? extends R>> mapper) {

        return (AnyMValue2<W,T2,R>) AnyMValue.super.trampoline(mapper);
    }


    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.EmptyUnit#emptyUnit()
     */
    @Override
    default <T> AnyMValue2<W,T2,T> emptyUnit(){
        return empty();
    }





    /* (non-Javadoc)
     * @see com.aol.cyclops2.monad.AnyM#filter(java.util.function.Predicate)
     */
    @Override
    default AnyMValue2<W,T2,T> filter(Predicate<? super T> p){
        return (AnyMValue2<W,T2,T>)AnyM2.super.filter(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.monad.AnyM#transform(java.util.function.Function)
     */
    @Override
    default <R> AnyMValue2<W,T2,R> map(Function<? super T, ? extends R> fn){
        return (AnyMValue2<W,T2,R>)AnyM2.super.map(fn);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.monad.AnyM#peek(java.util.function.Consumer)
     */
    @Override
    default AnyMValue2<W,T2,T> peek(Consumer<? super T> c){
        return (AnyMValue2<W,T2,T>)AnyM2.super.peek(c);
    }
    @Override
    default int arity() {
        return 1;
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
    default AnyMValue<W,List<T>> aggregate(AnyM<W, T> next){
        return (AnyMValue<W,List<T>>)AnyMValue.super.aggregate(next);
    }


    @Override
    default <R> AnyMValue2<W,T2,R> flatMapA(Function<? super T, ? extends AnyM<W, ? extends R>> fn){
        return  (AnyMValue2<W,T2,R>)AnyMValue.super.flatMapA(fn);

    }

    default <R> AnyMValue2<W,T2,R> flatMapI(final Function<? super T, ? extends Iterable<? extends R>> fn){
        return (AnyMValue2<W,T2,R>)AnyMValue.super.flatMapI(fn);
    }
    default <R> AnyMValue2<W,T2,R> flatMapP(Function<? super T, ? extends Publisher<? extends R>> fn){
        return (AnyMValue2<W,T2,R>)AnyMValue.super.flatMapP(fn);
    }
    default <R> AnyMValue2<W,T2,R> flatMapS(Function<? super T, ? extends Stream<? extends R>> fn){
        return (AnyMValue2<W,T2,R>)AnyMValue.super.flatMapS(fn);
    }

    @Override
    default T foldLeft(final T identity, final BinaryOperator<T> accumulator) {
        return null;
    }

    @Override
    default <R> AnyMValue2<W,T2,R> flatMap(Function<? super T, ? extends MonadicValue<? extends R>> fn){
        if(unwrap() instanceof MonadicValue){
            MonadicValue<T> unwrap = unwrap();
            FunctionalAdapter<W> a= adapter();
            MonadicValue<? extends R> mapped = unwrap.flatMap(fn);
            return AnyM.<W,T2,R>ofValue2(mapped,a);
           
        }

        return flatMapA(fn.andThen(a->this.fromIterable(a)));
    }
    @Override
    default Option<T> get() {
        return adapter().visit(e->{ throw new IllegalAccessError("misconfigured adapter : value adapter required");}
                             , v->v.get(this));
    }


    /* (non-Javadoc)
     * @see com.aol.cyclops2.monad.AnyM#unit(java.lang.Object)
     */
    @Override
    default <T> AnyMValue2<W,T2,T> unit(T value){
        return (AnyMValue2<W,T2,T>)AnyM2.super.unit(value);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.monad.AnyM#zero()
     */
    @Override
    default <T> AnyMValue2<W,T2,T> empty(){
        return (AnyMValue2<W,T2,T>)AnyM2.super.empty();
    }

    @Override
    default Either<AnyMValue<W,T>, AnyMSeq<W,T>> matchable() {
        return Either.left(this);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.fromEither5.AnyMValue2#ap(com.aol.cyclops2.types.Value, java.util.function.BiFunction)
     */
    @Override
    default <T2, R> AnyMValue2<W,T2,R> combine(final Value<? extends T2> app, final BiFunction<? super T, ? super T2, ? extends R> fn) {
        if (this.unwrap() instanceof MonadicValue) {

            return (AnyMValue2<W, T2,R>) AnyM.ofValue( ((MonadicValue<T>) unwrap()).combine(app, fn),adapter());
        }
        return (AnyMValue2<W, T2,R>) AnyMValue.super.combine(app, fn);
    }



    @Override
    default <T2, R> AnyMValue2<W,T2,R> zip(final Iterable<? extends T2> app, final BiFunction<? super T, ? super T2, ? extends R> fn) {
        if (this.unwrap() instanceof Zippable) {
            return (AnyMValue2<W, T2,R>) adapter().unit(((Zippable) unwrap()).zip(app, fn));
        }
        return (AnyMValue2<W,T2,R>) AnyMValue.super.zip(app, fn);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.applicative.ApplicativeFunctor#zip(java.util.function.BiFunction, org.reactivestreams.Publisher)
     */
    @Override
    default <T2, R> AnyMValue2<W,T2,R> zipP(final Publisher<? extends T2> app, final BiFunction<? super T, ? super T2, ? extends R> fn) {
        if (this.unwrap() instanceof Zippable) {
            return (AnyMValue2<W, T2,R>) adapter().unit(((Zippable) unwrap()).zipP(app,fn));
        }
        return (AnyMValue2<W,T2,R>) AnyMValue.super.zipP(app,fn);
    }

    

    @Override
    default Iterator<T> iterator() {

        return AnyM2.super.iterator();
    }

    @Override
    default ReactiveSeq<T> stream() {
        return AnyM2.super.stream();
    }

    @Override
    default <T2, R1, R2, R3, R> AnyMValue2<W,T2,R> forEach4(final Function<? super T, ? extends MonadicValue<R1>> value1, final BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2, final Function3<? super T, ? super R1, ? super R2, ? extends MonadicValue<R3>> value3, final Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        return (AnyMValue2<W,T2,R>)AnyMValue.super.forEach4(value1,value2,value3,yieldingFunction);
    }

    @Override
    default <T2, R1, R2, R3, R> AnyMValue2<W,T2,R> forEach4(final Function<? super T, ? extends MonadicValue<R1>> value1, final BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2, final Function3<? super T, ? super R1, ? super R2, ? extends MonadicValue<R3>> value3, final Function4<? super T, ? super R1, ? super R2, ? super R3, Boolean> filterFunction, final Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        return (AnyMValue2<W,T2,R>)AnyMValue.super.forEach4(value1,value2,value3,filterFunction,yieldingFunction);
    }

    @Override
    default <T2, R1, R2, R> AnyMValue2<W,T2,R> forEach3(final Function<? super T, ? extends MonadicValue<R1>> value1, final BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2, final Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {
        return (AnyMValue2<W,T2,R>)AnyMValue.super.forEach3(value1,value2,yieldingFunction);
    }

    @Override
    default <T2, R1, R2, R> AnyMValue2<W,T2,R> forEach3(final Function<? super T, ? extends MonadicValue<R1>> value1, final BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2, final Function3<? super T, ? super R1, ? super R2, Boolean> filterFunction, final Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {
        return (AnyMValue2<W,T2,R>)AnyMValue.super.forEach3(value1,value2,filterFunction,yieldingFunction);
    }

    @Override
    default <R1, R> AnyMValue2<W,T2,R> forEach2(Function<? super T, ? extends MonadicValue<R1>> value1, final BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {
        return (AnyMValue2<W,T2,R>)AnyMValue.super.forEach2(value1,yieldingFunction);
    }

    @Override
    default <R1, R> AnyMValue2<W,T2,R> forEach2(Function<? super T, ? extends MonadicValue<R1>> value1, final BiFunction<? super T, ? super R1, Boolean> filterFunction, final BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {
        return (AnyMValue2<W,T2,R>)AnyMValue.super.forEach2(value1,filterFunction,yieldingFunction);
    }

    @Override
    default AnyMValue2<W,T2,T> combineEager(final Monoid<T> monoid, final MonadicValue<? extends T> v2) {
        return (AnyMValue2<W,T2,T>)AnyMValue.super.combineEager(monoid,v2);
    }


}
