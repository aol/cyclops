package com.aol.cyclops.types.anyM;

import com.aol.cyclops.Monoid;
import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.Trampoline;
import com.aol.cyclops.control.Xor;
import com.aol.cyclops.types.Filterable;
import com.aol.cyclops.types.MonadicValue;
import com.aol.cyclops.types.Value;
import com.aol.cyclops.types.Zippable;
import com.aol.cyclops.util.function.Predicates;
import org.reactivestreams.Publisher;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
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
                                                                Filterable<T>,
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
     * @param t Monad to compare to
     * @return true if equivalent
     */
    default boolean eqv(final AnyMValue<?,T> t) {
        return Predicates.eqv(t)
                         .test(this);
    }
    

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.AnyM#combine(java.util.function.BinaryOperator, com.aol.cyclops.types.Applicative)
     */
    @Override
    default AnyMValue<W,T> zip(BinaryOperator<Zippable<T>> combiner, Zippable<T> app) {
        
        return (AnyMValue<W,T>)MonadicValue.super.zip(combiner, app);
    }


    /* (non-Javadoc)
     * @see com.aol.cyclops.types.MonadicValue#coflatMap(java.util.function.Function)
     */
    @Override
    default <R> AnyMValue<W,R> coflatMap(final Function<? super MonadicValue<T>, R> mapper) {
        return mapper.andThen(r -> unit(r))
                     .apply(this);
    }

    /* cojoin
     * (non-Javadoc)
     * @see com.aol.cyclops.types.MonadicValue#nest()
     */
    @Override
    default AnyMValue<W,MonadicValue<T>> nest() {
        return unit(this);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.MonadicValue2#combine(com.aol.cyclops.Monoid, com.aol.cyclops.types.MonadicValue2)
     */
    default AnyMValue<W,T> combine(final Monoid<T> monoid, final AnyMValue<W,? extends T> v2) {
        return unit(this.<T> flatMap(t1 -> v2.map(t2 -> monoid.apply(t1, t2)))
                        .orElseGet(() -> orElseGet(() -> monoid.zero())));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Value#mkString()
     */
    @Override
    default String mkString() {
        final Optional<T> opt = toOptional();
        return opt.isPresent() ? "AnyMValue[" + get() + "]" : "AnyMValue[]";
    }


    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Filterable#ofType(java.lang.Class)
     */
    @Override
    default <U> AnyMValue<W,U> ofType(final Class<? extends U> type) {

        return (AnyMValue<W,U>) MonadicValue.super.ofType(type);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Filterable#filterNot(java.util.function.Predicate)
     */
    @Override
    default AnyMValue<W,T> filterNot(final Predicate<? super T> fn) {

        return (AnyMValue<W,T>) MonadicValue.super.filterNot(fn);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Filterable#notNull()
     */
    @Override
    default AnyMValue<W,T> notNull() {

        return (AnyMValue<W,T>) MonadicValue.super.notNull();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Functor#cast(java.lang.Class)
     */
    @Override
    default <U> AnyMValue<W,U> cast(final Class<? extends U> type) {

        return (AnyMValue<W,U>) AnyM.super.cast(type);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Functor#trampoline(java.util.function.Function)
     */
    @Override
    default <R> AnyMValue<W,R> trampoline(final Function<? super T, ? extends Trampoline<? extends R>> mapper) {

        return (AnyMValue<W,R>) AnyM.super.trampoline(mapper);
    }


    /* (non-Javadoc)
     * @see com.aol.cyclops.types.EmptyUnit#emptyUnit()
     */
    @Override
    default <T> AnyMValue<W,T> emptyUnit(){
        return empty();
    }



   

    /* (non-Javadoc)
     * @see com.aol.cyclops.monad.AnyM#filter(java.util.function.Predicate)
     */
    @Override
    default AnyMValue<W,T> filter(Predicate<? super T> p){
        return (AnyMValue<W,T>)AnyM.super.filter(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.monad.AnyM#map(java.util.function.Function)
     */
    @Override
    default <R> AnyMValue<W,R> map(Function<? super T, ? extends R> fn){
        return (AnyMValue<W,R>)AnyM.super.map(fn);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.monad.AnyM#peek(java.util.function.Consumer)
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
     * @see com.aol.cyclops.monad.AnyM#aggregate(com.aol.cyclops.monad.AnyM)
     */
    @Override
    default AnyMValue<W,List<T>> aggregate(AnyM<W,T> next){
        return (AnyMValue<W,List<T>>)AnyM.super.aggregate(next);
    }


    @Override
    default <R> AnyMValue<W,R> flatMapA(Function<? super T, ? extends AnyM<W,? extends R>> fn){
        return  (AnyMValue<W,R>)AnyM.super.flatMapA(fn);   
        
    }
    default <R> AnyM<W,R> flatMapI(Function<? super T, ? extends Iterable<? extends R>> fn){
        return (AnyMValue<W,R>)MonadicValue.super.flatMapIterable(fn);
    }
    default <R> AnyM<W,R> flatMapP(Function<? super T, ? extends Publisher<? extends R>> fn){
        return (AnyMValue<W,R>)MonadicValue.super.flatMapPublisher(fn);
    }
    default <R> AnyMValue<W,R> flatMapS(Function<? super T, ? extends Stream<? extends R>> fn){
        return (AnyMValue<W,R>)MonadicValue.super.flatMapS(fn);
    }

    @Override
    default <R> AnyMValue<W,R> flatMap(Function<? super T, ? extends MonadicValue<? extends R>> fn){
        return flatMapA(fn.andThen(this::fromIterable));
    }
    @Override
    default T get() {
        return adapter().visit(e->{ throw new IllegalAccessError("misconfigured adapter : value adapter required");}
                             , v->v.get(this));
    }
   

    /* (non-Javadoc)
     * @see com.aol.cyclops.monad.AnyM#unit(java.lang.Object)
     */
    @Override
    default <T> AnyMValue<W,T> unit(T value){
        return (AnyMValue<W,T>)AnyM.super.unit(value);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.monad.AnyM#empty()
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
     * @see com.aol.cyclops.types.anyM.AnyMValue#ap(com.aol.cyclops.types.Value, java.util.function.BiFunction)
     */
    @Override
    default <T2, R> AnyMValue<W,R> combine(final Value<? extends T2> app, final BiFunction<? super T, ? super T2, ? extends R> fn) {
        if (this.unwrap() instanceof MonadicValue) {
            
            return (AnyMValue<W, R>) adapter().unit(((MonadicValue) unwrap()).combine(app, fn));
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
     * @see com.aol.cyclops.types.applicative.ApplicativeFunctor#zip(java.util.function.BiFunction, org.reactivestreams.Publisher)
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


    
}
