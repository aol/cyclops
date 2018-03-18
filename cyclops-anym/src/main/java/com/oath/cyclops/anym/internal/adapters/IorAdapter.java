package com.oath.cyclops.anym.internal.adapters;


import com.oath.cyclops.anym.AnyMValue;
import com.oath.cyclops.anym.extensability.AbstractMonadAdapter;
import com.oath.cyclops.anym.extensability.MonadAdapter;
import com.oath.cyclops.anym.extensability.ValueAdapter;
import cyclops.control.Ior;
import cyclops.control.Option;
import cyclops.monads.AnyM;
import cyclops.monads.Witness;
import cyclops.monads.Witness.ior;
import lombok.AllArgsConstructor;

import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Predicate;

@AllArgsConstructor
public class IorAdapter extends AbstractMonadAdapter<ior> implements ValueAdapter<ior> {






    @Override
    public boolean isFilterable(){
        return false;
    }


    public <T> Option<T> get(AnyMValue<ior,T> t){
        return ior(t).toOption();
    }
    @Override
    public <T> Iterable<T> toIterable(AnyM<ior, T> t) {
        return ior(t);
    }

    public <R> R visit(Function<? super MonadAdapter<ior>,? extends R> fn1, Function<? super ValueAdapter<ior>, ? extends R> fn2){
        return fn2.apply(this);
    }

    public <T> Ior<?,T> ior(AnyM<ior, T> t){
        return (Ior<?,T>)t.unwrap();
    }
    @Override
    public <T> AnyM<ior, T> filter(AnyM<ior, T> t, Predicate<? super T> fn) {
        return t;
    }


    @Override
    public <T> AnyM<ior, T> empty() {
        return AnyM.fromIor(Ior.left(null));

    }

    @Override
    public <T, R> AnyM<ior, R> ap(AnyM<ior,? extends Function<? super T, ? extends R>> fn, AnyM<ior, T> apply) {
        return flatMap(apply,x->fn.map(fnA->fnA.apply(x)));

    }

    @Override
    public <T, R> AnyM<ior, R> flatMap(AnyM<ior, T> t,
            Function<? super T, ? extends AnyM<ior, ? extends R>> fn) {

        return AnyM.fromIor(Witness.ior(t).flatMap(fn.andThen(Witness::ior)));

    }

    @Override
    public <T, R> AnyM<ior, R> map(AnyM<ior, T> t, Function<? super T, ? extends R> fn) {
        return AnyM.fromIor(Witness.ior(t).map(fn));
    }

    @Override
    public <T> AnyM<ior, T> unitIterable(Iterable<T> it) {
       return AnyM.fromIor(fromIterable(it));
    }

    @Override
    public <T> AnyM<ior, T> unit(T o) {
        return AnyM.fromIor(Ior.right(o));
    }


    static <ST, T> Ior<ST, T> fromIterable(final Iterable<T> iterable) {
        final Iterator<T> it = iterable.iterator();
        return it.hasNext() ? Ior.right(it.next()) : Ior.left(null);
    }
}
