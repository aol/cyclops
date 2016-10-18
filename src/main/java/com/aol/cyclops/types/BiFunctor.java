package com.aol.cyclops.types;

import java.util.function.Consumer;
import java.util.function.Function;

import com.aol.cyclops.control.Trampoline;

/* 
 * A type that can perform transformations across a domain with two types. 
 * For example a BiFunctor for Java Map may allow both the Keys and Values to be transformed together (via the bimap operator).
 * 
 * @author johnmcclean
 *
 * @param <T1> The first input type this BiFunctor accepts
 * @param <T2> The second input type this BiFunctor accepts
 */
public interface BiFunctor<T1, T2> {

    /**
     * Transform this BiFunctor, changing two value types at once.
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
     * @return New BiFunctor containing transformed data
     */
    <R1, R2> BiFunctor<R1, R2> bimap(Function<? super T1, ? extends R1> fn1, Function<? super T2, ? extends R2> fn2);

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
     * @return New BiFunctor with the same data
     */
    default BiFunctor<T1, T2> bipeek(Consumer<? super T1> c1, Consumer<? super T2> c2) {
        return bimap(input -> {
            c1.accept(input);
            return input;
        } , input -> {
            c2.accept(input);
            return input;
        });
    }

    /**
     * Cast two data types simulatanously.
     * <pre>
     * {@code
     *     MapX<Animal,Food> map = MapXs.of(cow,grass);
     *     MapX<Mamaml,Vegitation> herbervoreMammals = map.bicast(Mammal.class,Vegitation.class);
     * }
     * </pre>
     * @param type1
     * @param type2
     * @return
     */
    default <U1, U2> BiFunctor<U1, U2> bicast(Class<U1> type1, Class<U2> type2) {
        return bimap(type1::cast, type2::cast);
    }

    /**
     * Perform a tail-call optimized recursive transformation operation across two data points simultaneously
     * 
     * @param mapper1 transformation function for the first type
     * @param mapper2 transformation function for the second type
     * @return New BiFunctor containing transformed data
     */
    default <R1, R2> BiFunctor<R1, R2> bitrampoline(Function<? super T1, ? extends Trampoline<? extends R1>> mapper1,
            Function<? super T2, ? extends Trampoline<? extends R2>> mapper2) {
        return bimap(in -> mapper1.apply(in)
                                  .result(),
                     in -> mapper2.apply(in)
                                  .result());
    }

}
