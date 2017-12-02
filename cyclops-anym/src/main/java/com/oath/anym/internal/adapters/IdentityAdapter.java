package com.oath.anym.internal.adapters;

import com.oath.anym.AnyMValue;
import com.oath.anym.extensability.AbstractFunctionalAdapter;
import com.oath.anym.extensability.ValueAdapter;
import cyclops.control.Identity;
import cyclops.control.Maybe;
import cyclops.control.Option;
import cyclops.monads.AnyM;
import cyclops.monads.Witness.identity;

import java.util.Iterator;
import java.util.function.BiFunction;
import java.util.function.Function;

import static cyclops.monads.Witness.identity;


public class IdentityAdapter extends AbstractFunctionalAdapter<identity> implements ValueAdapter<identity> {

    @Override
    public <T, R> AnyM<identity, R> map(AnyM<identity, T> t, Function<? super T, ? extends R> fn) {
        return AnyM.fromIdentity(identity(t).map(fn));
    }


  @Override
    public <T, R> AnyM<identity, R> ap(AnyM<identity, ? extends Function<? super T, ? extends R>> fn, AnyM<identity, T> apply) {
        Identity<? extends Function<? super T, ? extends R>> f = identity(fn);
        Identity<T> ap = identity(apply);
        return AnyM.fromIdentity(f.flatMap(x -> ap.map(x)));
    }

    @Override
    public <T, R> AnyM<identity, R> flatMap(AnyM<identity, T> t, Function<? super T, ? extends AnyM<identity, ? extends R>> fn) {
        return AnyM.fromIdentity(identity(t).flatMap(x-> identity(fn.apply(x))));
    }

    @Override
    public <T> Iterable<T> toIterable(AnyM<identity, T> t) {
        return identity(t);
    }

    @Override
    public <T> AnyM<identity, T> unitIterable(Iterable<T> it) {
        Iterator<T> i = it.iterator();
        return AnyM.identity(i.hasNext() ? i.next() :  null);
    }

    @Override
    public <T> Option<T> get(AnyMValue<identity, T> t) {
        return Option.some(identity(t).get());
    }
}
