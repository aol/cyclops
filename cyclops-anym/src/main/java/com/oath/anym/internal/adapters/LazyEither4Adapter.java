package com.oath.anym.internal.adapters;


import com.oath.anym.AnyMValue;
import com.oath.anym.extensability.AbstractFunctionalAdapter;
import com.oath.anym.extensability.FunctionalAdapter;
import com.oath.anym.extensability.ValueAdapter;
import cyclops.control.Option;
import cyclops.control.Either;
import cyclops.control.LazyEither4;
import cyclops.monads.AnyM;
import cyclops.monads.Witness;
import cyclops.monads.Witness.lazyEither4;
import lombok.AllArgsConstructor;

import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Predicate;

@AllArgsConstructor
public class LazyEither4Adapter extends AbstractFunctionalAdapter<lazyEither4> implements ValueAdapter<lazyEither4> {


    @Override
    public boolean isFilterable(){
        return false;
    }



    public <T> Option<T> get(AnyMValue<lazyEither4,T> t){
        return either4(t).toOption();
    }
    @Override
    public <T> Iterable<T> toIterable(AnyM<lazyEither4, T> t) {
        return either4(t);
    }

    public <R> R visit(Function<? super FunctionalAdapter<lazyEither4>,? extends R> fn1, Function<? super ValueAdapter<lazyEither4>, ? extends R> fn2){
        return fn2.apply(this);
    }

    public <T> Either<?,T> either4(AnyM<lazyEither4, T> t){
        return (Either<?,T>)t.unwrap();
    }
    @Override
    public <T> AnyM<lazyEither4, T> filter(AnyM<lazyEither4, T> t, Predicate<? super T> fn) {
        return t;
    }


    @Override
    public <T> AnyM<lazyEither4, T> empty() {
        return AnyM.fromEither4(LazyEither4.left1(null));

    }

    @Override
    public <T, R> AnyM<lazyEither4, R> ap(AnyM<lazyEither4,? extends Function<? super T, ? extends R>> fn, AnyM<lazyEither4, T> apply) {
        return flatMap(apply,x->fn.map(fnA->fnA.apply(x)));

    }

    @Override
    public <T, R> AnyM<lazyEither4, R> flatMap(AnyM<lazyEither4, T> t,
                                               Function<? super T, ? extends AnyM<lazyEither4, ? extends R>> fn) {

        return AnyM.fromEither4(Witness.lazyEither4(t).flatMap(fn.andThen(Witness::lazyEither4)));

    }

    @Override
    public <T, R> AnyM<lazyEither4, R> map(AnyM<lazyEither4, T> t, Function<? super T, ? extends R> fn) {
        return AnyM.fromEither4(Witness.lazyEither4(t).map(fn));
    }

    @Override
    public <T> AnyM<lazyEither4, T> unitIterable(Iterable<T> it) {
       return AnyM.fromEither4(fromIterable(it));
    }

    @Override
    public <T> AnyM<lazyEither4, T> unit(T o) {
        return AnyM.fromEither4(LazyEither4.right(o));
    }


   private static <ST, ST2, ST3,T> LazyEither4<ST,ST2,ST3, T> fromIterable(final Iterable<T> iterable) {

        final Iterator<T> it = iterable.iterator();
        return it.hasNext() ? LazyEither4.right( it.next()) : LazyEither4.left1(null);
    }
}
