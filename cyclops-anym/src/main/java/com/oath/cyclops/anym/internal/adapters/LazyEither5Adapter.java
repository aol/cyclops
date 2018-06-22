package com.oath.cyclops.anym.internal.adapters;

import com.oath.cyclops.anym.AnyMValue;
import com.oath.cyclops.anym.extensability.AbstractMonadAdapter;
import com.oath.cyclops.anym.extensability.MonadAdapter;
import com.oath.cyclops.anym.extensability.ValueAdapter;
import cyclops.control.LazyEither5;
import cyclops.control.Option;
import cyclops.monads.AnyM;
import cyclops.monads.Witness;
import cyclops.monads.Witness.lazyEither5;
import lombok.AllArgsConstructor;

import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Predicate;

@AllArgsConstructor
public class LazyEither5Adapter extends AbstractMonadAdapter<lazyEither5> implements ValueAdapter<lazyEither5> {

    @Override
    public boolean isFilterable(){
        return false;
    }

    public <T> Option<T> get(AnyMValue<lazyEither5,T> t){
        return either5(t).toOption();
    }

    @Override
    public <T> Iterable<T> toIterable(AnyM<lazyEither5, T> t) {
        return either5(t);
    }

    public <R> R fold(Function<? super MonadAdapter<lazyEither5>,? extends R> fn1, Function<? super ValueAdapter<lazyEither5>, ? extends R> fn2){
        return fn2.apply(this);
    }

    public <T> LazyEither5<?, ?, ?, ?, T> either5(AnyM<lazyEither5, T> t){
        return (LazyEither5<?, ?, ?, ?, T>)t.unwrap();
    }
    @Override
    public <T> AnyM<lazyEither5, T> filter(AnyM<lazyEither5, T> t, Predicate<? super T> fn) {
        return t;
    }


    @Override
    public <T> AnyM<lazyEither5, T> empty() {
        return AnyM.fromEither5(LazyEither5.left1(null));

    }

    @Override
    public <T, R> AnyM<lazyEither5, R> ap(AnyM<lazyEither5,? extends Function<? super T, ? extends R>> fn, AnyM<lazyEither5, T> apply) {
        return flatMap(apply,x->fn.map(fnA->fnA.apply(x)));

    }

    @Override
    public <T, R> AnyM<lazyEither5, R> flatMap(AnyM<lazyEither5, T> t,
                                               Function<? super T, ? extends AnyM<lazyEither5, ? extends R>> fn) {

        return AnyM.fromEither5(Witness.lazyEither5(t).flatMap(fn.andThen(Witness::lazyEither5)));

    }

    @Override
    public <T, R> AnyM<lazyEither5, R> map(AnyM<lazyEither5, T> t, Function<? super T, ? extends R> fn) {
        return AnyM.fromEither5(Witness.lazyEither5(t).map(fn));
    }

    @Override
    public <T> AnyM<lazyEither5, T> unitIterable(Iterable<T> it) {
       return AnyM.fromEither5(fromIterable(it));
    }

    @Override
    public <T> AnyM<lazyEither5, T> unit(T o) {
        return AnyM.fromEither5(LazyEither5.right(o));
    }

    private static <ST, ST2, ST3, ST4, T> LazyEither5<ST,ST2,ST3,ST4,T> fromIterable(final Iterable<T> iterable) {

        final Iterator<T> it = iterable.iterator();
        return it.hasNext() ? LazyEither5.right( it.next()) : LazyEither5.left1(null);
    }
}
