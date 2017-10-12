package com.aol.cyclops2.internal.adapters;

import com.aol.cyclops2.types.anyM.AnyMValue;
import com.aol.cyclops2.types.extensability.AbstractFunctionalAdapter;
import com.aol.cyclops2.types.extensability.ValueAdapter;
import cyclops.control.Identity;
import cyclops.control.Option;
import cyclops.control.anym.AnyM;
import cyclops.control.anym.Witness;
import cyclops.control.anym.Witness.identity;

import java.util.Iterator;
import java.util.function.Function;


public class IdentityAdapter extends AbstractFunctionalAdapter<identity> implements ValueAdapter<identity> {

    @Override
    public <T, R> AnyM<identity, R> map(AnyM<identity, T> t, Function<? super T, ? extends R> fn) {
        return AnyM.fromIdentity(Witness.identity(t).map(fn));
    }

    @Override
    public <T, R> AnyM<identity, R> ap(AnyM<identity, ? extends Function<? super T, ? extends R>> fn, AnyM<identity, T> apply) {
        Identity<? extends Function<? super T, ? extends R>> f = Witness.identity(fn);
        Identity<T> ap = Witness.identity(apply);
        return AnyM.fromIdentity(f.flatMap(x -> ap.map(x)));
    }

    @Override
    public <T, R> AnyM<identity, R> flatMap(AnyM<identity, T> t, Function<? super T, ? extends AnyM<identity, ? extends R>> fn) {
        return AnyM.fromIdentity(Witness.identity(t).flatMap(x->Witness.identity(fn.apply(x))));
    }

    @Override
    public <T> Iterable<T> toIterable(AnyM<identity, T> t) {
        return Witness.identity(t);
    }

    @Override
    public <T> AnyM<identity, T> unitIterable(Iterable<T> it) {
        Iterator<T> i = it.iterator();
        return AnyM.identity(i.hasNext() ? i.next() :  null);
    }

    @Override
    public <T> Option<T> get(AnyMValue<identity, T> t) {
        return Option.some(Witness.identity(t).get());
    }
}
