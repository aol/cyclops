package com.aol.cyclops.types;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.BaseStream;

import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.For;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.types.anyM.AnyMValue;
import com.aol.cyclops.util.function.QuadFunction;
import com.aol.cyclops.util.function.TriFunction;

/**
 * A type that represents a Monad that wraps a single value
 * 
 * @author johnmcclean
 *
 * @param <T> Data type of element stored inside this Monad
 */
public interface MonadicValue<T> extends Value<T>, Unit<T>, Functor<T> {

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Unit#unit(java.lang.Object)
     */
    @Override
    public <T> MonadicValue<T> unit(T unit);

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Functor#map(java.util.function.Function)
     */
    @Override
    <R> MonadicValue<R> map(Function<? super T, ? extends R> fn);

    /**
     * @return This monad wrapped as an AnyMValue
     */
    default AnyMValue<T> anyM() {
        return AnyM.ofValue(this);
    }

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
                                                    final TriFunction<? super T, ? super R1, ? super R2, ? extends MonadicValue<R3>> value3,
                                                    final QuadFunction<? super T, ? super R1, ? super R2, ? super R3, ? extends R>  yieldingFunction){
        return For.Values.each4(this, value1, value2, value3,yieldingFunction)
                .unwrap();
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
            final TriFunction<? super T, ? super R1, ? super R2, ? extends MonadicValue<R3>> value3,
            final QuadFunction<? super T, ? super R1, ? super R2, ? super R3, Boolean> filterFunction,
            final QuadFunction<? super T, ? super R1, ? super R2, ? super R3, ? extends R>  yieldingFunction){
        return For.Values.each4(this, value1, value2, value3,filterFunction,yieldingFunction)
                .unwrap();
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
                    final TriFunction<? super T, ? super R1, ? super R2, ? extends R>  yieldingFunction){
        return For.Values.each3(this, value1, value2, yieldingFunction)
                .unwrap();
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
                    final TriFunction<? super T, ? super R1, ? super R2, Boolean> filterFunction,
                    final TriFunction<? super T, ? super R1, ? super R2, ? extends R>  yieldingFunction){
        return For.Values.each3(this, value1, value2, filterFunction,yieldingFunction)
                .unwrap();
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
        return For.Values.each2(this, value1,yieldingFunction)
                .unwrap();
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
        return For.Values.each2(this, value1, filterFunction,yieldingFunction)
                    .unwrap();
    }


}
