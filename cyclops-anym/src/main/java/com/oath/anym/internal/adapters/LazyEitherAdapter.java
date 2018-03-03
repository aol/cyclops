package com.oath.anym.internal.adapters;


import com.oath.anym.AnyMValue;
import com.oath.anym.extensability.AbstractMonadAdapter;
import com.oath.anym.extensability.MonadAdapter;
import com.oath.anym.extensability.ValueAdapter;
import cyclops.control.Option;
import cyclops.control.Either;
import cyclops.control.LazyEither;
import cyclops.monads.AnyM;
import cyclops.monads.Witness;
import cyclops.monads.Witness.lazyEither;
import lombok.AllArgsConstructor;

import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Predicate;

@AllArgsConstructor
public class LazyEitherAdapter extends AbstractMonadAdapter<lazyEither> implements ValueAdapter<lazyEither> {




    @Override
    public boolean isFilterable(){
        return false;
    }



    public <T> Option<T> get(AnyMValue<lazyEither,T> t){
        return either(t).toOption();
    }
    @Override
    public <T> Iterable<T> toIterable(AnyM<lazyEither, T> t) {
        return either(t);
    }

    public <R> R visit(Function<? super MonadAdapter<lazyEither>,? extends R> fn1, Function<? super ValueAdapter<lazyEither>, ? extends R> fn2){
        return fn2.apply(this);
    }

    public <T> Either<?,T> either(AnyM<lazyEither, T> t){
        return (Either<?,T>)t.unwrap();
    }
    @Override
    public <T> AnyM<lazyEither, T> filter(AnyM<lazyEither, T> t, Predicate<? super T> fn) {
        return t;
    }


    @Override
    public <T> AnyM<lazyEither, T> empty() {
        return AnyM.fromLazyEither(LazyEither.left(null));

    }

    @Override
    public <T, R> AnyM<lazyEither, R> ap(AnyM<lazyEither,? extends Function<? super T, ? extends R>> fn, AnyM<lazyEither, T> apply) {
        return flatMap(apply,x->fn.map(fnA->fnA.apply(x)));

    }

    @Override
    public <T, R> AnyM<lazyEither, R> flatMap(AnyM<lazyEither, T> t,
                                              Function<? super T, ? extends AnyM<lazyEither, ? extends R>> fn) {

        return AnyM.fromLazyEither(Witness.lazyEither(t).flatMap(fn.andThen(Witness::lazyEither)));

    }

    @Override
    public <T, R> AnyM<lazyEither, R> map(AnyM<lazyEither, T> t, Function<? super T, ? extends R> fn) {
        return AnyM.fromLazyEither(Witness.lazyEither(t).map(fn));
    }

    @Override
    public <T> AnyM<lazyEither, T> unitIterable(Iterable<T> it) {
       return AnyM.fromLazyEither(fromIterable(it));
    }

    @Override
    public <T> AnyM<lazyEither, T> unit(T o) {
        return AnyM.fromLazyEither(LazyEither.right(o));
    }


   private static <ST, T> LazyEither<ST, T> fromIterable(final Iterable<T> iterable) {

        final Iterator<T> it = iterable.iterator();
        return it.hasNext() ? LazyEither.right( it.next()) : LazyEither.left(null);
    }
}
