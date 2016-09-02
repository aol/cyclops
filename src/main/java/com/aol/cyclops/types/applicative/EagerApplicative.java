package com.aol.cyclops.types.applicative;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import com.aol.cyclops.control.FutureW;
import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.types.ConvertableFunctor;
import com.aol.cyclops.types.Functor;

@FunctionalInterface
public interface EagerApplicative<T, R, D extends ConvertableFunctor<R>> extends Functor<Function<? super T, ? extends R>> {

    /**
     * @return true if a value is present, false otherwise
     */
    default boolean isPresent() {
        return delegate().isPresent();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Functor#map(java.util.function.Function)
     */
    @Override
    default <U> Functor<U> map(Function<? super Function<? super T, ? extends R>, ? extends U> fn) {
        return delegate().map(fn);
    }

    ConvertableFunctor<Function<? super T, ? extends R>> delegate();

    default D ap(ConvertableFunctor<T> f) {

        return (D) delegate().toOptional()
                             .map(myFn -> f.map(t -> myFn.apply(t)))
                             .orElse(Maybe.none());
    }

    default D ap(Optional<T> f) {
        return ap(Maybe.fromOptional(f));
    }

    default D ap(CompletableFuture<T> f) {
        return ap(FutureW.of(f));
    }

}
