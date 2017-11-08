package com.oath.anym;

import com.oath.cyclops.types.Filters;
import com.oath.cyclops.types.MonadicValue;
import com.oath.cyclops.types.Value;
import com.oath.cyclops.types.Zippable;
import com.oath.anym.extensability.MonadAdapter;
import cyclops.control.Option;
import cyclops.control.Trampoline;
import cyclops.control.Either;
import cyclops.function.*;
import cyclops.monads.AnyM;
import cyclops.monads.AnyM2;
import cyclops.monads.WitnessType;
import cyclops.reactive.ReactiveSeq;
import cyclops.data.tuple.Tuple2;
import cyclops.data.tuple.Tuple3;
import cyclops.data.tuple.Tuple4;
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
   * @see com.oath.cyclops.types.MonadicValue#coflatMap(java.util.function.Function)
   */
    @Override
    default <R> AnyMValue2<W,T2,R> coflatMap(final Function<? super MonadicValue<T>, R> mapper) {
        return mapper.andThen(r -> unit(r))
                     .apply(this);
    }

    /* cojoin
     * (non-Javadoc)
     * @see com.oath.cyclops.types.MonadicValue#nest()
     */
    @Override
    default AnyMValue<W,MonadicValue<T>> nest() {
        return unit(this);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.MonadicValue2#combine(cyclops2.function.Monoid, com.oath.cyclops.types.MonadicValue2)
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
    default <R> AnyMValue2<W,T2,R> retry(final Function<? super T, ? extends R> fn) {
        return (AnyMValue2<W,T2,R>)AnyM2.super.retry(fn);
    }



    @Override
    default <R> AnyMValue2<W,T2,R> retry(final Function<? super T, ? extends R> fn, final int retries, final long delay, final TimeUnit timeUnit) {
        return (AnyMValue2<W,T2,R>)AnyM2.super.retry(fn,retries,delay,timeUnit);
    }






    /* (non-Javadoc)
     * @see com.oath.cyclops.types.Filters#ofType(java.lang.Class)
     */
    @Override
    default <U> AnyMValue2<W,T2,U> ofType(final Class<? extends U> type) {

        return (AnyMValue2<W,T2,U>) AnyMValue.super.ofType(type);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.Filters#filterNot(java.util.function.Predicate)
     */
    @Override
    default AnyMValue2<W,T2,T> filterNot(final Predicate<? super T> fn) {

        return (AnyMValue2<W,T2,T>) AnyMValue.super.filterNot(fn);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.Filters#notNull()
     */
    @Override
    default AnyMValue2<W,T2,T> notNull() {

        return (AnyMValue2<W,T2,T>) AnyMValue.super.notNull();
    }


    /* (non-Javadoc)
     * @see com.oath.cyclops.types.Functor#trampoline(java.util.function.Function)
     */
    @Override
    default <R> AnyMValue2<W,T2,R> trampoline(final Function<? super T, ? extends Trampoline<? extends R>> mapper) {

        return (AnyMValue2<W,T2,R>) AnyMValue.super.trampoline(mapper);
    }


    /* (non-Javadoc)
     * @see com.oath.cyclops.types.EmptyUnit#emptyUnit()
     */
    @Override
    default <T> AnyMValue2<W,T2,T> emptyUnit(){
        return empty();
    }





    /* (non-Javadoc)
     * @see com.oath.cyclops.monad.AnyM#filter(java.util.function.Predicate)
     */
    @Override
    default AnyMValue2<W,T2,T> filter(Predicate<? super T> p){
        return (AnyMValue2<W,T2,T>)AnyM2.super.filter(p);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.monad.AnyM#transform(java.util.function.Function)
     */
    @Override
    default <R> AnyMValue2<W,T2,R> map(Function<? super T, ? extends R> fn){
        return (AnyMValue2<W,T2,R>)AnyM2.super.map(fn);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.monad.AnyM#peek(java.util.function.Consumer)
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
     * @see com.oath.cyclops.monad.AnyM#aggregate(com.oath.cyclops.monad.AnyM)
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
        return AnyMValue.super.foldLeft(identity,accumulator);
    }

    @Override
    default <R> AnyMValue2<W,T2,R> flatMap(Function<? super T, ? extends MonadicValue<? extends R>> fn){
        if(unwrap() instanceof MonadicValue){
            MonadicValue<T> unwrap = unwrap();
            MonadAdapter<W> a= adapter();
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
     * @see com.oath.cyclops.monad.AnyM#unit(java.lang.Object)
     */
    @Override
    default <T> AnyMValue2<W,T2,T> unit(T value){
        return (AnyMValue2<W,T2,T>)AnyM2.super.unit(value);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.monad.AnyM#zero()
     */
    @Override
    default <T> AnyMValue2<W,T2,T> empty(){
        return (AnyMValue2<W,T2,T>)AnyM2.super.empty();
    }

    @Override
    default Either<AnyMValue<W,T>, AnyMSeq<W,T>> matchable() {
        return Either.left(this);
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


}
