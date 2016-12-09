package com.aol.cyclops.types.applicative.zipping;

import java.util.function.BiFunction;
import java.util.function.Function;

import com.aol.cyclops.types.IterableFunctor;
import com.aol.cyclops.types.Unit;
import com.aol.cyclops.util.function.F4;
import com.aol.cyclops.util.function.F5;
import com.aol.cyclops.util.function.F3;

public interface ZippingApplicativable<T> extends IterableFunctor<T>, Unit<T> {

    default <R> ApplyingZippingApplicativeBuilder<T, R, ZippingApplicativable<R>> applicatives() {
        return new ApplyingZippingApplicativeBuilder<T, R, ZippingApplicativable<R>>(
                                                                                     this, this);
    }

    default <R> ZippingApplicativable<R> ap1(final Function<? super T, ? extends R> fn) {
        return this.<R> applicatives()
                   .applicative(fn)
                   .ap(this);

    }

    default <T2, R> ZippingApplicative<T2, R, ?> ap2(final BiFunction<? super T, ? super T2, ? extends R> fn) {
        return this.<R> applicatives()
                   .applicative2(fn);
    }

    default <T2, T3, R> ZippingApplicative2<T2, T3, R, ?> ap3(final F3<? super T, ? super T2, ? super T3, ? extends R> fn) {
        return this.<R> applicatives()
                   .applicative3(fn);
    }

    default <T2, T3, T4, R> ZippingApplicative3<T2, T3, T4, R, ?> ap4(
            final F4<? super T, ? super T2, ? super T3, ? super T4, ? extends R> fn) {
        return this.<R> applicatives()
                   .applicative4(fn);
    }

    default <T2, T3, T4, T5, R> ZippingApplicative4<T2, T3, T4, T5, R, ?> ap5(
            final F5<? super T, ? super T2, ? super T3, ? super T4, ? super T5, ? extends R> fn) {
        return this.<R> applicatives()
                   .applicative5(fn);
    }

}
