package com.oath.cyclops.anym.internal.adapters;

import com.oath.cyclops.anym.AnyMValue;
import com.oath.cyclops.anym.extensability.AbstractMonadAdapter;
import com.oath.cyclops.anym.extensability.MonadAdapter;
import com.oath.cyclops.anym.extensability.ValueAdapter;
import cyclops.control.LazyEither3;
import cyclops.control.Option;
import cyclops.monads.AnyM;
import cyclops.monads.Witness;
import cyclops.monads.Witness.lazyEither3;
import lombok.AllArgsConstructor;

import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Predicate;

import static cyclops.monads.Witness.lazyEither3;

@AllArgsConstructor
public class LazyEither3Adapter extends AbstractMonadAdapter<lazyEither3> implements ValueAdapter<lazyEither3> {

    @Override
    public boolean isFilterable(){
        return false;
    }

    public <T> Option<T> get(AnyMValue<lazyEither3,T> t){
        return either3(t).toOption();
    }

    @Override
    public <T> Iterable<T> toIterable(AnyM<lazyEither3, T> t) {
        return either3(t);
    }

    public <R> R fold(Function<? super MonadAdapter<lazyEither3>,? extends R> fn1, Function<? super ValueAdapter<lazyEither3>, ? extends R> fn2){
        return fn2.apply(this);
    }

    public <T> LazyEither3<?, ?, T> either3(AnyM<lazyEither3, T> t){
        return (LazyEither3<?, ?, T>)t.unwrap();
    }

    @Override
    public <T> AnyM<lazyEither3, T> filter(AnyM<lazyEither3, T> t, Predicate<? super T> fn) {
        return t;
    }


    @Override
    public <T> AnyM<lazyEither3, T> empty() {
        return AnyM.fromEither3(LazyEither3.left1(null));

    }

    @Override
    public <T, R> AnyM<lazyEither3, R> ap(AnyM<lazyEither3,? extends Function<? super T, ? extends R>> fn, AnyM<lazyEither3, T> apply) {
        return flatMap(apply,x->fn.map(fnA->fnA.apply(x)));

    }

    @Override
    public <T, R> AnyM<lazyEither3, R> flatMap(AnyM<lazyEither3, T> t,
                                               Function<? super T, ? extends AnyM<lazyEither3, ? extends R>> fn) {

        return AnyM.fromEither3(lazyEither3(t).flatMap(fn.andThen(Witness::lazyEither3)));

    }

    @Override
    public <T, R> AnyM<lazyEither3, R> map(AnyM<lazyEither3, T> t, Function<? super T, ? extends R> fn) {
        return AnyM.fromEither3(lazyEither3(t).map(fn));
    }

    @Override
    public <T> AnyM<lazyEither3, T> unitIterable(Iterable<T> it) {
       return AnyM.fromEither3(fromIterable(it));
    }

    @Override
    public <T> AnyM<lazyEither3, T> unit(T o) {
        return AnyM.fromEither3(LazyEither3.right(o));
    }

    private static <ST, ST2, T> LazyEither3<ST,ST2, T> fromIterable(final Iterable<T> iterable) {

        final Iterator<T> it = iterable.iterator();
        return it.hasNext() ? LazyEither3.right( it.next()) : LazyEither3.left1(null);
    }
}
