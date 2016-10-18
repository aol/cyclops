package com.aol.cyclops.types.applicative.zipping;

import java.util.function.BiFunction;
import java.util.function.Function;

import com.aol.cyclops.types.IterableFunctor;
import com.aol.cyclops.types.Unit;
import com.aol.cyclops.util.function.CurryVariance;
import com.aol.cyclops.util.function.QuadFunction;
import com.aol.cyclops.util.function.QuintFunction;
import com.aol.cyclops.util.function.TriFunction;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ApplyingZippingApplicativeBuilder<T, R, A extends ZippingApplicativable<R>> {

    private final Unit unit;
    private final IterableFunctor functor;

    private ZippingApplicativable unit(final Function fn) {
        return (ZippingApplicativable) unit.unit(fn);
    }

    public ZippingApplicative<T, R, A> applicative(final ZippingApplicativable<Function<? super T, ? extends R>> fn) {

        return () -> fn.stream()
                       .cycle();
    }

    public ZippingApplicative<T, R, A> applicative(final Function<? super T, ? extends R> fn) {

        return applicative(unit(fn));
    }

    public <T2> ZippingApplicative<T2, R, A> applicative2(final ZippingApplicativable<Function<? super T, Function<? super T2, ? extends R>>> fn) {
        return ((ZippingApplicative2<T, T2, R, A>) () -> fn.stream()
                                                           .cycle()).ap(functor);

    }

    public <T2> ZippingApplicative<T2, R, A> applicative2(final Function<? super T, Function<? super T2, ? extends R>> fn) {

        return applicative2(unit(fn));
    }

    public <T2> ZippingApplicative<T2, R, A> applicative2(final BiFunction<? super T, ? super T2, ? extends R> fn) {

        return applicative2(unit(CurryVariance.curry2(fn)));
    }

    public <T2, T3> ZippingApplicative2<T2, T3, R, A> applicative3(
            final ZippingApplicativable<Function<? super T, Function<? super T2, Function<? super T3, ? extends R>>>> fn) {
        return ((ZippingApplicative3<T, T2, T3, R, A>) () -> fn.stream()
                                                               .cycle()).ap(functor);
    }

    public <T2, T3> ZippingApplicative2<T2, T3, R, A> applicative3(
            final Function<? super T, Function<? super T2, Function<? super T3, ? extends R>>> fn) {

        return applicative3(unit(fn));
    }

    public <T2, T3> ZippingApplicative2<T2, T3, R, A> applicative3(final TriFunction<? super T, ? super T2, ? super T3, ? extends R> fn) {

        return applicative3(unit(CurryVariance.curry3(fn)));
    }

    public <T2, T3, T4> ZippingApplicative3<T2, T3, T4, R, A> applicative4(
            final ZippingApplicativable<Function<? super T, Function<? super T2, Function<? super T3, Function<? super T4, ? extends R>>>>> fn) {

        return ((ZippingApplicative4<T, T2, T3, T4, R, A>) () -> fn.stream()
                                                                   .cycle()).ap(functor);

    }

    public <T2, T3, T4> ZippingApplicative3<T2, T3, T4, R, A> applicative4(
            final Function<? super T, Function<? super T2, Function<? super T3, Function<? super T4, ? extends R>>>> fn) {

        return applicative4(unit(fn));
    }

    public <T2, T3, T4> ZippingApplicative3<T2, T3, T4, R, A> applicative4(
            final QuadFunction<? super T, ? super T2, ? super T3, ? super T4, ? extends R> fn) {

        return applicative4(unit(CurryVariance.curry4(fn)));
    }

    public <T2, T3, T4, T5> ZippingApplicative4<T2, T3, T4, T5, R, A> applicative5(
            final ZippingApplicativable<Function<? super T, Function<? super T2, Function<? super T3, Function<? super T4, Function<? super T5, ? extends R>>>>>> fn) {

        return ((ZippingApplicative5<T, T2, T3, T4, T5, R, A>) () -> fn.stream()
                                                                       .cycle()).ap(functor);
    }

    public <T2, T3, T4, T5> ZippingApplicative4<T2, T3, T4, T5, R, A> applicative5(
            final Function<? super T, Function<? super T2, Function<? super T3, Function<? super T4, Function<? super T5, ? extends R>>>>> fn) {

        return applicative5(unit(fn));
    }

    public <T2, T3, T4, T5> ZippingApplicative4<T2, T3, T4, T5, R, A> applicative5(
            final QuintFunction<? super T, ? super T2, ? super T3, ? super T4, ? super T5, ? extends R> fn) {

        return applicative5(unit(CurryVariance.curry5(fn)));
    }

}
