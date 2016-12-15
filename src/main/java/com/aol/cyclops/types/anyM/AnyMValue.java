package com.aol.cyclops.types.anyM;

import static com.aol.cyclops.internal.Utils.firstOrNull;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.jooq.lambda.function.Function3;
import org.jooq.lambda.function.Function4;
import org.jooq.lambda.function.Function5;
import org.reactivestreams.Publisher;

import com.aol.cyclops.Monoid;
import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.Trampoline;
import com.aol.cyclops.control.Xor;
import com.aol.cyclops.types.Combiner;
import com.aol.cyclops.types.Filterable;
import com.aol.cyclops.types.MonadicValue;
import com.aol.cyclops.types.Value;
import com.aol.cyclops.types.applicative.ApplicativeFunctor;
import com.aol.cyclops.util.function.Predicates;
import com.aol.cyclops.util.function.F4;
import com.aol.cyclops.util.function.F5;
import com.aol.cyclops.util.function.F3;

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
                                                                Combiner<T>,
                                                                ApplicativeFunctor<T>,
                                                                MonadicValue<T> {

    
    
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
    default AnyMValue<W,T> combine(BinaryOperator<Combiner<T>> combiner, Combiner<T> app) {
        
        return (AnyMValue<W,T>)MonadicValue.super.combine(combiner, app);
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
        if (unwrap() instanceof Value) {
            return ((Value<T>) unwrap()).isPresent();
        }
        return this.toOptional().isPresent();
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
        if (this.unwrap() instanceof ApplicativeFunctor) {
            
            return (AnyMValue<W, R>) adapter().unit(((ApplicativeFunctor) unwrap()).combine(app, fn));
        }
        return (AnyMValue<W, R>) MonadicValue.super.combine(app, fn);
    }



    @Override
    default <T2, R> AnyMValue<W,R> zip(final Iterable<? extends T2> app, final BiFunction<? super T, ? super T2, ? extends R> fn) {
        if (this.unwrap() instanceof ApplicativeFunctor) {
            return (AnyMValue<W, R>) adapter().unit(((ApplicativeFunctor) unwrap()).zip(app, fn));
        }
        return (AnyMValue<W,R>) MonadicValue.super.zip(app, fn);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.applicative.ApplicativeFunctor#zip(java.util.function.BiFunction, org.reactivestreams.Publisher)
     */
    @Override
    default <T2, R> AnyMValue<W,R> zip(final BiFunction<? super T, ? super T2, ? extends R> fn, final Publisher<? extends T2> app) {
        if (this.unwrap() instanceof ApplicativeFunctor) {
            return (AnyMValue<W, R>) adapter().unit(((ApplicativeFunctor) unwrap()).zip(fn, app));
        }
        return (AnyMValue<W,R>) MonadicValue.super.zip(fn, app);
    }

    

    @Override
    default Iterator<T> iterator() {

        return AnyM.super.iterator();
    }

    @Override
    default ReactiveSeq<T> stream() {
        return AnyM.super.stream();
    }


    /**
     * Lift a function so it accepts an AnyM and returns an AnyM (any monad)
     * AnyM view simplifies type related challenges.
     * 
     * @param fn
     * @return
     */
    public static <W extends WitnessType<W>,U, R> Function<AnyMValue<W,U>, AnyMValue<W,R>> liftM(final Function<? super U, ? extends R> fn) {
        return u -> u.map(input -> fn.apply(input));
    }

    /**
     * Lift a function so it accepts a Monad and returns a Monad (simplex view of a wrapped Monad)
     * AnyM view simplifies type related challenges. The actual native type is not specified here.
     * 
     * e.g.
     * 
     * <pre>{@code
     * 	BiFunction<AnyMValue<Integer>,AnyMValue<Integer>,AnyMValue<Integer>> add = Monads.liftM2(this::add);
     *   
     *  Optional<Integer> result = add.apply(getBase(),getIncrease());
     *  
     *   private Integer add(Integer a, Integer b){
    			return a+b;
    	}
     * }</pre>
     * The add method has no null handling, but we can lift the method to Monadic form, and use Optionals to automatically handle null / empty value cases.
     * 
     * 
     * @param fn BiFunction to lift
     * @return Lifted BiFunction
     */
    public static <W extends WitnessType<W>,U1, U2, R> BiFunction<AnyMValue<W,U1>, AnyMValue<W,U2>, AnyMValue<W,R>> liftM2(
            final BiFunction<? super U1, ? super U2, ? extends R> fn) {

        return (u1, u2) -> u1.flatMap(input1 -> u2.map(input2 -> fn.apply(input1, input2)));
    }


    /**
     * Lift a TriFunction into Monadic form. A good use case it to take an existing method and lift it so it can accept and return monads
     * 
     * <pre>
     * {@code
     * TriFunction<AnyMValue<Double>,AnyMValue<Entity>,AnyMValue<W,String>,AnyMValue<Integer>> fn = liftM3(this::myMethod);
     *    
     * }
     * </pre>
     * 
     * Now we can execute the Method with Streams, Optional, Futures, Try's etc to transparently inject iteration, null handling, async execution and / or error handling
     * 
     * @param fn Function to lift
     * @return Lifted function
     */
    public static <W extends WitnessType<W>,U1, U2, U3, R> F3<AnyMValue<W,U1>, AnyMValue<W,U2>, AnyMValue<W,U3>, AnyMValue<W,R>> liftM3(
            final Function3<? super U1, ? super U2, ? super U3, ? extends R> fn) {
        return (u1, u2, u3) -> u1.flatMap(input1 -> u2.flatMap(input2 -> u3.map(input3 -> fn.apply(input1, input2, input3))));
    }


    /**
     * Lift a QuadFunction into Monadic form.
     * 
     * @param fn Quad funciton to lift
     * @return Lifted Quad function
     */
    public static <W extends WitnessType<W>,U1, U2, U3, U4, R> F4<AnyMValue<W,U1>, AnyMValue<W,U2>, AnyMValue<W,U3>, AnyMValue<W,U4>, AnyMValue<W,R>> liftM4(
            final Function4<? super U1, ? super U2, ? super U3, ? super U4, ? extends R> fn) {

        return (u1, u2, u3, u4) -> u1.flatMap(input1 -> u2.flatMap(input2 -> u3.flatMap(input3 -> u4.map(input4 -> fn.apply(input1, input2, input3, input4)))));
    }

    /**
     * Lift a  jOOλ Function5 (5 parameters) into Monadic form
     * 
     * @param fn Function to lift
     * @return Lifted Function
     */
    public static <W extends WitnessType<W>,U1, U2, U3, U4, U5, R> F5<AnyMValue<W,U1>, AnyMValue<W,U2>, AnyMValue<W,U3>, AnyMValue<W,U4>, AnyMValue<W,U5>, AnyMValue<W,R>> liftM5(
            final Function5<? super U1, ? super U2, ? super U3, ? super U4, ? super U5, ? extends R> fn) {

        return (u1, u2, u3, u4,
                u5) -> u1.flatMap(input1 -> u2.flatMap(input2 -> u3.flatMap(input3 -> u4.flatMap(input4 -> u5.map(input5 -> fn.apply(input1, input2, input3,
                                                                                                                         input4, input5))))));
    }

    

    /**
     * Lift a Curried Function {@code(2 levels a->b->fn.apply(a,b) )} into Monadic form
     * 
     * @param fn Function to lift
     * @return Lifted function 
     */
    public static <W extends WitnessType<W>,U1, U2, R> Function<AnyMValue<W,U1>, Function<AnyMValue<W,U2>, AnyMValue<W,R>>> liftM2(final Function<U1, Function<U2, R>> fn) {
        return u1 -> u2 -> u1.flatMap(input1 -> u2.map(input2 -> fn.apply(input1)
                                                                .apply(input2)));

    }

    /**
     * Lift a Curried Function {@code(3 levels a->b->c->fn.apply(a,b,c) )} into Monadic form
     * 
     * @param fn Function to lift
     * @return Lifted function 
     */
    public static <W extends WitnessType<W>,U1, U2, U3, R> Function<AnyMValue<W,U1>, Function<AnyMValue<W,U2>, Function<AnyMValue<W,U3>, AnyMValue<W,R>>>> liftM3(
            final Function<? super U1, Function<? super U2, Function<? super U3, ? extends R>>> fn) {
        return u1 -> u2 -> u3 -> u1.flatMap(input1 -> u2.flatMap(input2 -> u3.map(input3 -> fn.apply(input1)
                                                                                        .apply(input2)
                                                                                        .apply(input3))));
    }

    /**
     * Lift a Curried Function {@code(4 levels a->b->c->d->fn.apply(a,b,c,d) )} into Monadic form
     * 
     * @param fn Function to lift
     * @return Lifted function 
     */
    public static <W extends WitnessType<W>,U1, U2, U3, U4, R> Function<AnyMValue<W,U1>, Function<AnyMValue<W,U2>, Function<AnyMValue<W,U3>, Function<AnyMValue<W,U4>, AnyMValue<W,R>>>>> liftM4(
            final Function<? super U1, Function<? super U2, Function<? super U3, Function<? super U4, ? extends R>>>> fn) {

        return u1 -> u2 -> u3 -> u4 -> u1.flatMap(input1 -> u2.flatMap(input2 -> u3.flatMap(input3 -> u4.map(input4 -> fn.apply(input1)
                                                                                                                .apply(input2)
                                                                                                                .apply(input3)
                                                                                                                .apply(input4)))));
    }

    /**
     * Lift a Curried Function {@code (5 levels a->b->c->d->e->fn.apply(a,b,c,d,e) ) }into Monadic form
     * 
     * @param fn Function to lift
     * @return Lifted function 
     */
    public static <W extends WitnessType<W>,U1, U2, U3, U4, U5, R> Function<AnyMValue<W,U1>, Function<AnyMValue<W,U2>, Function<AnyMValue<W,U3>, Function<AnyMValue<W,U4>, Function<AnyMValue<W,U5>, AnyMValue<W,R>>>>>> liftM5(
            final Function<? super U1, Function<? super U2, Function<? super U3, Function<? super U4, Function<? super U5, ? extends R>>>>> fn) {

        return u1 -> u2 -> u3 -> u4 -> u5 -> u1.flatMap(input1 -> u2.flatMap(input2 -> u3.flatMap(input3 -> u4.flatMap(input4 -> u5.map(input5 -> fn.apply(input1)
                                                                                                                                        .apply(input2)
                                                                                                                                        .apply(input3)
                                                                                                                                        .apply(input4)
                                                                                                                                        .apply(input5))))));
    }
    
}
