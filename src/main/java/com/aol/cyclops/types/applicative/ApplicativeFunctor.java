package com.aol.cyclops.types.applicative;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.jooq.lambda.tuple.Tuple;
import org.reactivestreams.Publisher;

import com.aol.cyclops.Semigroup;
import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.types.Combiner;
import com.aol.cyclops.types.ConvertableFunctor;
import com.aol.cyclops.types.Functor;
import com.aol.cyclops.types.Unit;
import com.aol.cyclops.types.Value;
import com.aol.cyclops.util.function.Curry;
import com.aol.cyclops.util.function.F4;
import com.aol.cyclops.util.function.F5;
import com.aol.cyclops.util.function.F3;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.Wither;

/**
 * @author johnmcclean
 *
 * Interface for applicative-like behavior (via Applicative) otherise allows (mostly eager)
 * partial application of functions inside wrapped contexts. 
 * 
 * 
 * Allows the application of functions within a wrapped context, support both 
 * abscence / error short-circuiting and error accumulation
 * 
 * <pre> {@code ap(BiFunction<T,T,T>)}</pre> and <pre>{@code ap(Semigroup<T>}</pre> for accumulation despite absence
 * use ap1..5 for absence short-circuiting
 *
 * @param <T> Data type of element/s inside this Applicative Functo
 */
public interface ApplicativeFunctor<T> extends Combiner<T>,ConvertableFunctor<T>, Unit<T> {

    public static class Applicatives {
        public static <T, R> ApplyingApplicativeBuilder<T, R, ApplicativeFunctor<R>> applicatives(final Unit unit, final Functor functor) {
            return new ApplyingApplicativeBuilder<T, R, ApplicativeFunctor<R>>(
                                                                               unit, functor);
        }
    }

   

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Zippable#zip(java.lang.Iterable, java.util.function.BiFunction)
     */
    @Override
    default <T2, R> ApplicativeFunctor<R> zip(final Iterable<? extends T2> app, final BiFunction<? super T, ? super T2, ? extends R> fn) {

        return (ApplicativeFunctor<R>) map(v -> Tuple.tuple(v, Curry.curry2(fn)
                                                                    .apply(v))).map(tuple -> Maybe.fromIterable(app)
                                                                                                  .visit(i -> tuple.v2.apply(i), () -> tuple.v1));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Zippable#zip(java.util.function.BiFunction, org.reactivestreams.Publisher)
     */
    @Override
    default <T2, R> ApplicativeFunctor<R> zip(final BiFunction<? super T, ? super T2, ? extends R> fn, final Publisher<? extends T2> app) {

        return (ApplicativeFunctor<R>) map(v -> Tuple.tuple(v, Curry.curry2(fn)
                                                                    .apply(v))).map(tuple -> Maybe.fromPublisher(app)
                                                                                                  .visit(i -> tuple.v2.apply(i), () -> tuple.v1));
    }

    /**
     * Eagerly apply functions across one or more Functor instances
     * 
     * @return ApplyFunctions builder
     */
    default ApplyFunctions<T> applyFunctions() {
        return new ApplyFunctions<T>(
                                     this);
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    static class ApplyFunctions<T> {
        ApplicativeFunctor<T> app;

        @AllArgsConstructor(access = AccessLevel.PROTECTED)
        public static class SemigroupApplyer<T> {
            protected BiFunction<T, T, T> combiner;
            @Wither(AccessLevel.PROTECTED)
            protected ConvertableFunctor<T> functor;

            public SemigroupApplyer<T> ap(final ConvertableFunctor<T> fn) {

                return functor.visit(p -> fn.visit(p2 -> withFunctor(functor.map(v1 -> combiner.apply(v1, fn.get()))), () -> this),
                                     () -> withFunctor(fn));

            }

            public Value<T> convertable() {
                return functor;
            }
        }

        /**
          * Apply the provided function to combine multiple different Applicatives, wrapping the same type.
          * 
        
          * We can combine Applicative types together without unwrapping the values.
          * 
          * <pre>
          * {@code
          *   Xor<String,String> fail1 = Xor.secondary("failed1");
             
             fail1.swap().ap(Semigroups.stringConcat)
                         .ap(Xor.secondary("failed2").swap())
                         .ap(Xor.<String,String>primary("success").swap())
                                     .
                                     
             // [failed1failed2]
          *  }
          *  </pre>
          * 
          * @param fn
          * @return
          */
        public SemigroupApplyer<T> ap(final BiFunction<T, T, T> fn) {
            return new SemigroupApplyer<T>(
                                           fn, app);
        }

        public SemigroupApplyer<T> ap(final Semigroup<T> fn) {
            return new SemigroupApplyer<>(
                                          fn, app);
        }

        public <R> ApplicativeFunctor<R> ap1(final Function<? super T, ? extends R> fn) {
            return Applicatives.<T, R> applicatives(app, app)
                               .applicative(fn)
                               .ap(app);

        }

        /**
         * Apply the provided function to two different Applicatives. e.g. given a method add
         * 
         * <pre>
         * {@code 
         * 	public int add(Integer a,Integer b){
         * 			return a+b;
         * 	}
         * 
         * }
         * </pre>
         * We can add two Applicative types together without unwrapping the values
         * 
         * <pre>
         * {@code 
         *  Maybe.of(10).ap2(this::add).ap(Maybe.of(20))
         *  
         *  //Maybe[30];
         *  }
         *  </pre>
         * 
         * @param fn
         * @return
         */
        public <T2, R> EagerApplicative<T2, R, ?> ap2(final BiFunction<? super T, ? super T2, ? extends R> fn) {
            return Applicatives.<T, R> applicatives(app, app)
                               .applicative2(fn);
        }

        public <T2, T3, R> Applicative2<T2, T3, R, ?> ap3(final F3<? super T, ? super T2, ? super T3, ? extends R> fn) {
            return Applicatives.<T, R> applicatives(app, app)
                               .applicative3(fn);
        }

        public <T2, T3, T4, R> Applicative3<T2, T3, T4, R, ?> ap4(final F4<? super T, ? super T2, ? super T3, ? super T4, ? extends R> fn) {
            return Applicatives.<T, R> applicatives(app, app)
                               .applicative4(fn);
        }

        public <T2, T3, T4, T5, R> Applicative4<T2, T3, T4, T5, R, ?> ap5(
                final F5<? super T, ? super T2, ? super T3, ? super T4, ? super T5, ? extends R> fn) {
            return Applicatives.<T, R> applicatives(app, app)
                               .applicative5(fn);
        }
    }

}
