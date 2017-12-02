package com.oath.anym;

import com.oath.cyclops.types.Filters;
import cyclops.control.Option;
import cyclops.function.Function3;
import cyclops.function.Function4;
import cyclops.function.Monoid;
import cyclops.monads.AnyM;
import cyclops.monads.WitnessType;
import cyclops.reactive.ReactiveSeq;
import cyclops.control.Trampoline;
import cyclops.control.Either;
import com.oath.cyclops.types.MonadicValue;
import com.oath.cyclops.types.Value;
import com.oath.cyclops.types.Zippable;
import cyclops.function.Predicates;
import cyclops.data.tuple.Tuple2;
import cyclops.data.tuple.Tuple3;
import cyclops.data.tuple.Tuple4;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import java.io.PrintStream;
import java.io.PrintWriter;
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
    default void print(final PrintStream str) {
        MonadicValue.super.print(str);
    }

    @Override
    default void print(final PrintWriter writer) {
        MonadicValue.super.print(writer);
    }

    @Override
    default void printOut() {
        MonadicValue.super.printOut();
    }

    @Override
    default void printErr() {
        MonadicValue.super.printErr();
    }

    @Override
    default <R> R visit(Function<? super T, ? extends R> present, Supplier<? extends R> absent){
        return get().visit(present,absent);
    }

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
     * @param t Monad to compare to
     * @return true if equivalent
     */
    default boolean eqv(final AnyMValue<?,T> t) {
        return Predicates.eqv(t)
                         .test(this);
    }


  @Override
    default int arity() {
        return 1;
    }
    /* (non-Javadoc)
     * @see com.oath.cyclops.types.MonadicValue#coflatMap(java.util.function.Function)
     */
    @Override
    default <R> AnyMValue<W,R> coflatMap(final Function<? super MonadicValue<T>, R> mapper) {
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

    default <R> R print(R r){
        System.out.println(r);
        return r;
    }
    /* (non-Javadoc)
     * @see com.oath.cyclops.types.MonadicValue2#combine(cyclops2.function.Monoid, com.oath.cyclops.types.MonadicValue2)
     */
    default AnyMValue<W,T> combineEager(final Monoid<T> monoid, final AnyMValue<W,? extends T> v2) {
        return unit(this.<T> flatMap(t1 -> print(v2.map(t2 -> monoid.apply(t1, t2))))
                        .orElseGet(() -> orElseGet(() -> monoid.zero())));
    }

    @Override
    default void subscribe(final Subscriber<? super T> sub) {
        Object o = unwrap();
        if(o instanceof  Publisher){
            ((Publisher)o).subscribe(sub);
        }
        MonadicValue.super.subscribe(sub);

    }

    /* (non-Javadoc)
         * @see com.oath.cyclops.types.Value#mkString()
         */
    @Override
    default String mkString() {
        return visit(s->"AnyMValue[" + s + "]",()->"AnyMValue[]");

    }

  @Override
    default <R> AnyMValue<W,R> retry(final Function<? super T, ? extends R> fn) {
        return (AnyMValue<W,R>)AnyM.super.retry(fn);
    }


    @Override
    default <R> AnyMValue<W,R> retry(final Function<? super T, ? extends R> fn, final int retries, final long delay, final TimeUnit timeUnit) {
        return (AnyMValue<W,R>)AnyM.super.retry(fn,retries,delay,timeUnit);
    }





    /* (non-Javadoc)
     * @see com.oath.cyclops.types.Filters#ofType(java.lang.Class)
     */
    @Override
    default <U> AnyMValue<W,U> ofType(final Class<? extends U> type) {

        return (AnyMValue<W,U>) AnyM.super.ofType(type);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.Filters#filterNot(java.util.function.Predicate)
     */
    @Override
    default AnyMValue<W,T> filterNot(final Predicate<? super T> fn) {

        return (AnyMValue<W,T>) AnyM.super.filterNot(fn);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.Filters#notNull()
     */
    @Override
    default AnyMValue<W,T> notNull() {

        return (AnyMValue<W,T>) AnyM.super.notNull();
    }



    /* (non-Javadoc)
     * @see com.oath.cyclops.types.Functor#trampoline(java.util.function.Function)
     */
    @Override
    default <R> AnyMValue<W,R> trampoline(final Function<? super T, ? extends Trampoline<? extends R>> mapper) {

        return (AnyMValue<W,R>) AnyM.super.trampoline(mapper);
    }


    /* (non-Javadoc)
     * @see com.oath.cyclops.types.EmptyUnit#emptyUnit()
     */
    @Override
    default <T> AnyMValue<W,T> emptyUnit(){
        return empty();
    }





    /* (non-Javadoc)
     * @see com.oath.cyclops.monad.AnyM#filter(java.util.function.Predicate)
     */
    @Override
    default AnyMValue<W,T> filter(Predicate<? super T> p){
        return (AnyMValue<W,T>)AnyM.super.filter(p);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.monad.AnyM#transform(java.util.function.Function)
     */
    @Override
    default <R> AnyMValue<W,R> map(Function<? super T, ? extends R> fn){
        return (AnyMValue<W,R>)AnyM.super.map(fn);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.monad.AnyM#peek(java.util.function.Consumer)
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
     * @see com.oath.cyclops.monad.AnyM#aggregate(com.oath.cyclops.monad.AnyM)
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

    default Option<T> get() {
        return adapter().visit(e->{ throw new IllegalAccessError("misconfigured adapter : value adapter required");}
                             , v->v.get(this));
    }


    /* (non-Javadoc)
     * @see com.oath.cyclops.monad.AnyM#unit(java.lang.Object)
     */
    @Override
    default <T> AnyMValue<W,T> unit(T value){
        return (AnyMValue<W,T>)AnyM.super.unit(value);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.monad.AnyM#zero()
     */
    @Override
    default <T> AnyMValue<W,T> empty(){
        return (AnyMValue<W,T>)AnyM.super.empty();
    }

    @Override
    default Either<AnyMValue<W,T>, AnyMSeq<W,T>> matchable() {
        return Either.left(this);
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
    default <T2, R1, R2, R3, R> AnyMValue<W,R> forEach4(final Function<? super T, ? extends MonadicValue<R1>> value1, final BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2, final Function3<? super T, ? super R1, ? super R2, ? extends MonadicValue<R3>> value3, final Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        return (AnyMValue<W,R>)MonadicValue.super.forEach4(value1,value2,value3,yieldingFunction);
    }

    @Override
    default <T2, R1, R2, R3, R> AnyMValue<W,R> forEach4(final Function<? super T, ? extends MonadicValue<R1>> value1, final BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2, final Function3<? super T, ? super R1, ? super R2, ? extends MonadicValue<R3>> value3, final Function4<? super T, ? super R1, ? super R2, ? super R3, Boolean> filterFunction, final Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        return (AnyMValue<W,R>)MonadicValue.super.forEach4(value1,value2,value3,filterFunction,yieldingFunction);
    }

    @Override
    default <T2, R1, R2, R> AnyMValue<W,R> forEach3(final Function<? super T, ? extends MonadicValue<R1>> value1, final BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2, final Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {
        return (AnyMValue<W,R>)MonadicValue.super.forEach3(value1,value2,yieldingFunction);
    }

    @Override
    default <T2, R1, R2, R> AnyMValue<W,R> forEach3(final Function<? super T, ? extends MonadicValue<R1>> value1, final BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2, final Function3<? super T, ? super R1, ? super R2, Boolean> filterFunction, final Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {
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
  default <T2, R> AnyMValue<W, R> zip(final AnyM<W, ? extends T2> anyM, final BiFunction<? super T, ? super T2, ? extends R> fn) {
    return (AnyMValue<W, R>)AnyM.super.zip(anyM,fn);
  }

  @Override
  default <U> AnyMValue<W, Tuple2<T, U>> zip(final AnyM<W, ? extends U> other) {
    return (AnyMValue)AnyM.super.zip(other);
  }
}
