package com.aol.cyclops.types;

import java.util.function.Consumer;
import java.util.function.Function;

import com.aol.cyclops.control.Trampoline;

/* 
 * @author johnmcclean
 *
 * @param <T>
 */

public interface BiFunctor<T1, T2> {

    <R1, R2> BiFunctor<R1, R2> bimap(Function<? super T1, ? extends R1> fn1, Function<? super T2, ? extends R2> fn2);

    default BiFunctor<T1, T2> bipeek(Consumer<? super T1> c1, Consumer<? super T2> c2) {
        return bimap(input -> {
            c1.accept(input);
            return input;
        } , input -> {
            c2.accept(input);
            return input;
        });
    }

   
    default <U1, U2> BiFunctor<U1, U2> bicast(Class<U1> type1, Class<U2> type2) {
        return bimap(type1::cast, type2::cast);
    }

    default <R1, R2> BiFunctor<R1, R2> bitrampoline(Function<? super T1, ? extends Trampoline<? extends R1>> mapper1,
            Function<? super T2, ? extends Trampoline<? extends R2>> mapper2) {
        return bimap(in -> mapper1.apply(in)
                                  .result(),
                     in -> mapper2.apply(in)
                                  .result());
    }

}
