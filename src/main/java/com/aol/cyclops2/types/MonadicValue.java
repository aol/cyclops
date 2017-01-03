package com.aol.cyclops2.types;

import cyclops.function.Monoid;
import cyclops.control.Maybe;
import com.aol.cyclops2.types.stream.reactive.ValueSubscriber;
import cyclops.function.Curry;
import cyclops.function.Fn3;
import cyclops.function.Fn4;
import org.jooq.lambda.tuple.Tuple;
import org.reactivestreams.Publisher;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * A type that represents a Monad that wraps a single value
 * 
 * @author johnmcclean
 *
 * @param <T> Data type of element stored inside this Monad
 */
public interface MonadicValue<T> extends Value<T>, Unit<T>, Transformable<T>, Filters<T>, Zippable<T>{


    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Filters#filter(java.util.function.Predicate)
     */
    @Override
     MonadicValue<T> filter(Predicate<? super T> predicate) ;

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Pure#unit(java.lang.Object)
     */
    @Override
    public <T> MonadicValue<T> unit(T unit);

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Transformable#map(java.util.function.Function)
     */
    @Override
    <R> MonadicValue<R> map(Function<? super T, ? extends R> fn);

    

    /**
     * Perform a coflatMap operation. The mapping function accepts this MonadicValue and returns
     * a single value to be wrapped inside a Monad.
     * 
     * <pre>
     * {@code 
     *   Maybe.none().coflatMap(m -> m.isPresent() ? m.get() : 10);
     *   //Maybe[10]
     * }
     * </pre>
     * 
     * @param mapper Mapping / transformation function
     * @return MonadicValue wrapping return value from transformation function applied to the value inside this MonadicValue
     */
    default <R> MonadicValue<R> coflatMap(final Function<? super MonadicValue<T>, R> mapper) {
        return mapper.andThen(r -> unit(r))
                     .apply(this);
    }

    //cojoin
    /**
     * cojoin pattern. Nests this Monad inside another.
     * 
     * @return Nested Monad
     */
    default MonadicValue<MonadicValue<T>> nest() {
        return this.map(t -> unit(t));
    }
    /**
     * A flattening transformation operation (@see {@link java.util.Optional#flatMap(Function)}
     * 
     * <pre>
     * {@code 
     *   Eval.now(1).map(i->i+2).flatMap(i->Eval.later(()->i*3);
     *   //Eval[9]
     * 
     * }</pre>
     * 
     * 
     * @param mapper transformation function
     * @return MonadicValue
     */
    <R> MonadicValue<R> flatMap(Function<? super T, ? extends MonadicValue<? extends R>> mapper);
    /**
     * Perform a four level nested internal iteration over this MonadicValue and the
     * supplied MonadicValues
     * 
       * <pre>
     * {@code 
     *  Maybe.of(3)
     *       .forEach4(a->Maybe.just(a+10),
     *                 (a,b)->Maybe.just(a+b),
     *                 (a,b,c)->Maybe.none(),
     *                 (a,b,c,d)->a+b+c+d);
     *                                  
     *  
     *  //Maybe.none
     * }
     * </pre>
     * 
     * @param value1
     *            Nested MonadicValue to iterate over
     * @param value2
     *            Nested MonadicValue to iterate over
     * @param value3
     *            Nested MonadicValue to iterate over
     * @param yieldingFunction
     *            Function with pointers to the current element from both
     *            MonadicValue that generates the new elements
     * @return MonadicValue with elements generated via nested iteration
     */
    default < T2, R1, R2,R3, R>  MonadicValue<R> forEach4(final Function<? super T, ? extends MonadicValue<R1>> value1,
                                                    final BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
                                                    final Fn3<? super T, ? super R1, ? super R2, ? extends MonadicValue<R3>> value3,
                                                    final Fn4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction){
        return this.flatMap(in-> { 
            
            MonadicValue<R1> a = value1.apply(in);
            return a.flatMap(ina-> {
                MonadicValue<R2> b = value2.apply(in,ina);
               return b.flatMap(inb-> {
                   MonadicValue<R3> c= value3.apply(in,ina,inb);
                   return c.map(in2->yieldingFunction.apply(in,ina,inb,in2));
               });
                
            });
            
        });
    }

    /**
     * Perform a four level nested internal iteration over this MonadicValue and the
     * supplied MonadicValues
     * 
     * {@code 
     *  Maybe.of(3)
     *       .forEach4(a->Maybe.just(a+10),
     *                 (a,b)->Maybe.just(a+b),
     *                 (a,b,c)->Maybe.none(),
     *                 (a,b,c,d)->a+b+c<100,
     *                 (a,b,c,d)->a+b+c+d);
     *                                  
     *  
     *  //Maybe.none
     * }
     * </pre>
     * 
     * 
     * @param value1
     *            Nested MonadicValue to iterate over
     * @param value2
     *            Nested MonadicValue to iterate over
     * @param value3
     *            Nested MonadicValue to iterate over
     * @param filterFunction
     *            Filter to apply over elements before passing non-filtered
     *            values to the yielding function
     * @param yieldingFunction
     *            Function with pointers to the current element from both
     *            MonadicValue that generates the new elements
     * @return MonadicValue with elements generated via nested iteration
     */
    default < T2, R1, R2,R3, R>  MonadicValue<R> forEach4(final Function<? super T, ? extends MonadicValue<R1>> value1,
            final BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
            final Fn3<? super T, ? super R1, ? super R2, ? extends MonadicValue<R3>> value3,
            final Fn4<? super T, ? super R1, ? super R2, ? super R3, Boolean> filterFunction,
            final Fn4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction){
        return this.flatMap(in-> { 
            
            MonadicValue<R1> a = value1.apply(in);
            return a.flatMap(ina-> {
                MonadicValue<R2> b = value2.apply(in,ina);
               return b.flatMap(inb-> {
                   MonadicValue<R3> c= value3.apply(in,ina,inb);
                   return c.filter(in2-> filterFunction.apply(in,ina,inb,in2)).map(in2->yieldingFunction.apply(in,ina,inb,in2));
               });
                
            });
            
        });
    }
    /**
     * Perform a three level nested internal iteration over this MonadicValue and the
     * supplied MonadicValues
     * 
       * <pre>
     * {@code 
     *  Maybe.of(3)
     *       .forEach3(a->Maybe.just(a+10),
     *                 (a,b)->Maybe.just(a+b),
     *                 (a,b,c)->a+b+c<100,
     *                 (a,b,c)->a+b+c);
     *                                  
     *  
     *  //Maybe[32]
     * }
     * </pre>
     * 
     * @param value1
     *            Nested MonadicValue to iterate over
     * @param value2
     *            Nested MonadicValue to iterate over
     * @param yieldingFunction
     *            Function with pointers to the current element from both
     *            MonadicValue that generates the new elements
     * @return MonadicValue with elements generated via nested iteration
     */
    default < T2, R1, R2, R>  MonadicValue<R> forEach3(final Function<? super T, ? extends MonadicValue<R1>> value1,
                                                    final BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
                    final Fn3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction){
        return this.flatMap(in-> { 
            
            MonadicValue<R1> a = value1.apply(in);
            return a.flatMap(ina-> {
                MonadicValue<R2> b = value2.apply(in,ina);
                return b.map(in2->yieldingFunction.apply(in,ina, in2));
            });
            
        });
    }

    /**
     * Perform a three level nested internal iteration over this MonadicValue and the
     * supplied MonadicValues
     * 
       * <pre>
     * {@code 
     *  Maybe.of(3)
     *       .forEach3(a->Maybe.just(a+10),
     *                 (a,b)->Maybe.just(a+b),
     *                 (a,b,c)->a+b+c<100,
     *                 (a,b,c)->a+b+c);
     *                                  
     *  
     *  //Maybe[32]
     * }
     * </pre>
     * 
     * 
     * @param value1
     *            Nested MonadicValue to iterate over
     * @param value2
     *            Nested MonadicValue to iterate over
     * @param filterFunction
     *            Filter to apply over elements before passing non-filtered
     *            values to the yielding function
     * @param yieldingFunction
     *            Function with pointers to the current element from both
     *            MonadicValue that generates the new elements
     * @return MonadicValue with elements generated via nested iteration
     */
    default < T2, R1, R2, R>  MonadicValue<R> forEach3(final Function<? super T, ? extends MonadicValue<R1>> value1,
            final BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
                    final Fn3<? super T, ? super R1, ? super R2, Boolean> filterFunction,
                    final Fn3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction){
        return this.flatMap(in-> { 
            
            MonadicValue<R1> a = value1.apply(in);
            return a.flatMap(ina-> {
                MonadicValue<R2> b = value2.apply(in,ina);
                return b.filter(in2-> filterFunction.apply(in,ina,in2)).map(in2->yieldingFunction.apply(in,ina, in2));
            });
            
        });
       
    }

    /**
     * Perform a two level nested internal iteration over this MonadicValue and the
     * supplied MonadicValue
     * 
     * <pre>
     * {@code 
     *  Maybe.of(3)
     *       .forEach2(a->Maybe.none(),
     *                 (a,b)->a+b);
     *                                  
     * 
     *  //Maybe.none()
     * }
     * </pre>
     * 
     * 
     * @param value1
     *            Nested Monadic Type to iterate over
     * @param yieldingFunction
     *            Function with pointers to the current element from both
     *            monad types that generates the new elements
     * @return MonadicValue with elements generated via nested iteration
     */
    default <R1, R> MonadicValue<R> forEach2(Function<? super T, ? extends MonadicValue<R1>> value1,
            final BiFunction<? super T, ? super R1, ? extends R> yieldingFunction){
        return this.flatMap(in-> { 
            MonadicValue<R1> b = value1.apply(in);
            return b.map(in2->yieldingFunction.apply(in, in2));
        });
       
    }

    /**
     * Perform a two level nested internal iteration over this MonadicValue and the
     * supplied stream
     * 
     * <pre>
     * {@code 
     *  Maybe.of(3)
     *       .forEach2(a->Maybe.none(),
     *                 a->b-> a<3 && b>10,
     *                 (a,b)->a+b);
     *                                  
     * 
     *  //Maybe.none()
     * }
     * </pre>
     * 
     * @param monad1
     *            Nested monadic type to iterate over
     * @param filterFunction
     *            Filter to apply over elements before passing non-filtered
     *            values to the yielding function
     * @param yieldingFunction
     *            Function with pointers to the current element from both
     *            Streams that generates the new elements
     * @return MonadicValue with elements generated via nested iteration
     */
    default <R1, R> MonadicValue<R> forEach2(Function<? super T, ? extends MonadicValue<R1>> value1,
            final BiFunction<? super T, ? super R1, Boolean> filterFunction,
            final BiFunction<? super T, ? super R1, ? extends R> yieldingFunction){
        return this.flatMap(in-> { 
           
            MonadicValue<R1> b = value1.apply(in);
            return b.filter(in2-> filterFunction.apply(in,in2)).map(in2->yieldingFunction.apply(in, in2));
        });
    }
    
    

    /**
     * Eagerly combine two MonadicValues using the supplied monoid (@see ApplicativeFunctor for type appropraite i.e. lazy / async alternatives)
     * 
     * <pre>
     * {@code 
     * 
     *  Monoid<Integer> add = Monoid.of(1,Semigroups.intSum);
     *  Maybe.of(10).combineEager(add,Maybe.none());
     *  //Maybe[10]
     *  
     *  Maybe.none().combineEager(add,Maybe.of(10));
     *  //Maybe[10]
     *  
     *  Maybe.none().combineEager(add,Maybe.none());
     *  //Maybe.none()
     *  
     *  Maybe.of(10).combineEager(add,Maybe.of(10));
     *  //Maybe[20]
     *  
     *  Monoid<Integer> firstNonNull = Monoid.of(null , Semigroups.firstNonNull());
     *  Maybe.of(10).combineEager(firstNonNull,Maybe.of(10));
     *  //Maybe[10]
     * }</pre>
     * 
     * @param monoid
     * @param v2
     * @return
     */
    default MonadicValue<T> combineEager(final Monoid<T> monoid, final MonadicValue<? extends T> v2) {
        return unit(this.<T> flatMap(t1 -> v2.map(t2 -> monoid
                                                              .apply(t1, t2)))
                        .orElseGet(() -> orElseGet(() -> monoid.zero())));
    }

    /**
     * A flattening transformation operation (@see {@link java.util.Optional#flatMap(Function)}
     * 
     * <pre>
     * {@code 
     *   Eval.now(1).map(i->i+2).flatMap(i->Eval.later(()->i*3);
     *   //Eval[9]
     * 
     * }</pre>
     * 
     * 
     * @param mapper transformation function
     * @return MonadicValue
     
    <R> MonadicValue<R> flatMap(Function<? super T, ? extends MonadicValue<? extends R>> mapper);*/

    /**
     * A flattening transformation operation that takes the first value from the returned Iterable.
     * 
     * <pre>
     * {@code 
     *   Maybe.just(1).map(i->i+2).flatMapI(i->Arrays.asList(()->i*3,20);
     *   //Maybe[9]
     * 
     * }</pre>
     * 
     * 
     * @param mapper  transformation function
     * @return  MonadicValue
     */
    default <R> MonadicValue<R> flatMapI(final Function<? super T, ? extends Iterable<? extends R>> mapper) {
        return this.flatMap(a -> {
            return Maybe.fromIterable(mapper.apply(a));
        });
    }

    default <R> MonadicValue<R> flatMapS(final Function<? super T, ? extends Stream<? extends R>> mapper) {
        return this.flatMap(a -> {
            return Maybe.fromStream(mapper.apply(a));
        });
    }

    /**
     * A flattening transformation operation that takes the first value from the returned Publisher.
     * <pre>
     * {@code 
     *   FutureW.ofResult(1).map(i->i+2).flatMapP(i->Flux.just(()->i*3,20);
     *   //FutureW[9]
     * 
     * }</pre>
     * 
     * @param mapper transformation function
     * @return  MonadicValue
     */
    default <R> MonadicValue<R> flatMapP(final Function<? super T, ? extends Publisher<? extends R>> mapper) {

        return this.flatMap(a -> {
            final Publisher<? extends R> publisher = mapper.apply(a);
            final ValueSubscriber<R> sub = ValueSubscriber.subscriber();
            publisher.subscribe(sub);

            final Maybe<R> maybe = sub.toMaybe();
            return unit(maybe.get());

        });


    }
    /**
     * Lazily combine this ApplicativeFunctor with the supplied value via the supplied BiFunction
     *
     * Example
     * <pre>
     * {@code
     *   Maybe<Integer> some = Maybe.just(10);
     *   just.combine(Eval.now(20), this::add);
     *   //Some[30]
     *
     *   Maybe<Integer> none = Maybe.none();
     *   none.combine(Eval.now(20), this::add);
     *   //None
     *
     * }
     * </pre>
     *
     * @param app Value to combine with this one.
     * @param fn BiFunction to combine them
     * @return New Applicativefunctor that represents the combined values
     */
    default <T2, R> MonadicValue<R> combine(final Value<? extends T2> app, final BiFunction<? super T, ? super T2, ? extends R> fn) {

        return (MonadicValue<R>) map(v -> Tuple.tuple(v, Curry.curry2(fn)
                .apply(v))).map(tuple -> app.visit(i -> tuple.v2.apply(i), () -> tuple.v1));
    }
    /* (non-Javadoc)
    * @see com.aol.cyclops2.types.Zippable#zip(java.lang.Iterable, java.util.function.BiFunction)
    */
    @Override
    default <T2, R> MonadicValue<R> zip(final Iterable<? extends T2> app, final BiFunction<? super T, ? super T2, ? extends R> fn) {

        return (MonadicValue<R>) map(v -> Tuple.tuple(v, Curry.curry2(fn)
                .apply(v))).map(tuple -> Maybe.fromIterable(app)
                .visit(i -> tuple.v2.apply(i), () -> tuple.v1));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Zippable#zip(java.util.function.BiFunction, org.reactivestreams.Publisher)
     */
    @Override
    default <T2, R> MonadicValue<R> zipP(final Publisher<? extends T2> app,final BiFunction<? super T, ? super T2, ? extends R> fn) {

        return (MonadicValue<R>) map(v -> Tuple.tuple(v, Curry.curry2(fn)
                .apply(v))).map(tuple -> Maybe.fromPublisher(app)
                .visit(i -> tuple.v2.apply(i), () -> tuple.v1));
    }



}
