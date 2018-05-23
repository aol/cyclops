package com.oath.cyclops.types.functor;

import java.util.function.Consumer;
import java.util.function.Function;

import cyclops.control.Trampoline;

/*
 * A type that can perform transformations across a domain with two types.
 * For example a BiTransformable for Java Map may allow both the Keys and Values to be transformed together (via the bimap operator).
 *
 * @author johnmcclean
 *
 * @param <T1> The first input type this BiTransformable accepts
 * @param <T2> The second input type this BiTransformable accepts
 */
public interface BiTransformable<T1, T2> {

    /**
     * Transform this BiTransformable, changing two value types at once.
     *
     * <pre>
     * {@code
     *     MapX<String,Integer> map = MapXs.of("hello",2);
     *     MapX<String,Integer> transformedMap = map.bimap(s->s+" world",i->i*4);
     *     //["hello world",8]
     * }
     * </pre>
     *
     * @param fn1 transformation function for the first type
     * @param fn2 transformation function for the second type
     * @return New BiTransformable containing transformed data
     */
    <R1, R2> BiTransformable<R1, R2> bimap(Function<? super T1, ? extends R1> fn1, Function<? super T2, ? extends R2> fn2);

    /**
     * Peek at two data types simulatanously (typically to perform a side-effect with each data point)
     *
     * <pre>
     * {@code
     *     MapX<String,Integer> map = MapXs.of("hello",2);
     *     map.bipeek(s->System.out.pritnln("key = " + s),System.out::println);
     * }
     * </pre>
     *
     * @param c1 consumer for the first type
     * @param c2 consumer for the second type
     * @return New BiTransformable with the same data
     */
    default BiTransformable<T1, T2> bipeek(final Consumer<? super T1> c1, final Consumer<? super T2> c2) {
        return bimap(input -> {
            c1.accept(input);
            return input;
        } , input -> {
            c2.accept(input);
            return input;
        });
    }



}
