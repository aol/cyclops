package com.oath.cyclops.types;

import com.oath.cyclops.types.factory.EmptyUnit;
import com.oath.cyclops.types.factory.Unit;
import com.oath.cyclops.types.functor.Transformable;
import com.oath.cyclops.types.reactive.ValueSubscriber;
import cyclops.control.Future;
import cyclops.control.Maybe;
import cyclops.control.Option;
import cyclops.control.Try;
import cyclops.function.Function3;
import cyclops.function.Function4;
import org.reactivestreams.Publisher;

import java.util.NoSuchElementException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A type that represents a Monad that wraps a single value
 *
 * @author johnmcclean
 *
 * @param <T> Data type of element stored inside this Monad
 */
public interface MonadicValue<T> extends Value<T>, Unit<T>, Transformable<T>, Filters<T>, EmptyUnit<T>{

    default <R> Future<R> mapAsync(Function<? super T,? extends R> fn, Executor ex){
        return Future.of(()->map(fn),ex).flatMap(a->a.fold(s->Future.ofResult(s),()->Future.ofError(new NoSuchElementException())));
    }

    default <R> Option<R> attemptMap(Function<? super T,? extends R> fn){
        Try<R,Throwable> t = mapTry(fn);
        return t.toOption();
    }

    default <R> Try<R,Throwable> mapRetry(final Function<? super T, ? extends R> fn) {
        return mapRetry(fn,7, 2, TimeUnit.SECONDS);
    }

    default <R> Try<R,Throwable> mapRetry(final Function<? super T, ? extends R> fn, final int retries, final long delay, final TimeUnit timeUnit) {
        return this.toTry().retry(fn,retries,delay,timeUnit);
    }


    default <X extends Throwable,R> Try<R,X> mapTry(Function<? super T,? extends R> fn, Class<X>... exceptionTypes){
        Try<? extends MonadicValue<? extends R>, X> x = Try.withCatch(() -> map(fn),exceptionTypes);
        return x.flatMap(a->a.toTry(exceptionTypes));
    }

    default <R> Try<R,Throwable> mapTry(Function<? super T,? extends R> fn){
        Try<? extends MonadicValue<? extends R>, Throwable> x = Try.withCatch(() -> map(fn));
        return x.flatMap(a->a.toTry());
    }


    @Override
    MonadicValue<T> filter(Predicate<? super T> predicate) ;


    @Override
    public <T> MonadicValue<T> unit(T unit);

    @Override
    <T> MonadicValue<T> emptyUnit();


    @Override
    <R> MonadicValue<R> map(Function<? super T, ? extends R> fn);



    default int arity(){
        return 1;
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
                                                    final Function3<? super T, ? super R1, ? super R2, ? extends MonadicValue<R3>> value3,
                                                    final Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction){
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
     *            Filter to applyHKT over elements before passing non-filtered
     *            values to the yielding function
     * @param yieldingFunction
     *            Function with pointers to the current element from both
     *            MonadicValue that generates the new elements
     * @return MonadicValue with elements generated via nested iteration
     */
    default < T2, R1, R2,R3, R>  MonadicValue<R> forEach4(final Function<? super T, ? extends MonadicValue<R1>> value1,
            final BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
            final Function3<? super T, ? super R1, ? super R2, ? extends MonadicValue<R3>> value3,
            final Function4<? super T, ? super R1, ? super R2, ? super R3, Boolean> filterFunction,
            final Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction){
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
                    final Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction){
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
     *            Filter to applyHKT over elements before passing non-filtered
     *            values to the yielding function
     * @param yieldingFunction
     *            Function with pointers to the current element from both
     *            MonadicValue that generates the new elements
     * @return MonadicValue with elements generated via nested iteration
     */
    default < T2, R1, R2, R>  MonadicValue<R> forEach3(final Function<? super T, ? extends MonadicValue<R1>> value1,
            final BiFunction<? super T, ? super R1, ? extends MonadicValue<R2>> value2,
                    final Function3<? super T, ? super R1, ? super R2, Boolean> filterFunction,
                    final Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction){
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
     * @param value1
     *            Nested monadic type to iterate over
     * @param filterFunction
     *            Filter to applyHKT over elements before passing non-filtered
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
     * A flattening transformation operation that takes the first value from the returned Iterable.
     *
     * <pre>
     * {@code
     *   Maybe.just(1).map(i->i+2).concatMap(i->Arrays.asList(()->i*3,20);
     *   //Maybe[9]
     *
     * }</pre>
     *
     *
     * @param mapper  transformation function
     * @return  MonadicValue
     */
    default <R> MonadicValue<R> concatMap(final Function<? super T, ? extends Iterable<? extends R>> mapper) {
        return this.flatMap(a -> {
            return Maybe.fromIterable(mapper.apply(a));
        });
    }


    /**
     * A flattening transformation operation that takes the first value from the returned Publisher.
     * <pre>
     * {@code
     *   Future.ofResult(1).map(i->i+2).mergeMap(i->Flux.just(()->i*3,20);
     *   //Future[9]
     *
     * }</pre>
     *
     * @param mapper transformation function
     * @return  MonadicValue
     */
    default <R> MonadicValue<R> mergeMap(final Function<? super T, ? extends Publisher<? extends R>> mapper) {

        return this.flatMap(a -> {
            final Publisher<? extends R> publisher = mapper.apply(a);
            final ValueSubscriber<R> sub = ValueSubscriber.subscriber();
            publisher.subscribe(sub);

            final Maybe<R> maybe = sub.toMaybe();
            return maybe.fold(in->unit(in),__->emptyUnit());

        });


    }



}
