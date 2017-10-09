package com.aol.cyclops2.internal.adapters;

import com.aol.cyclops2.types.MonadicValue;
import com.aol.cyclops2.types.anyM.AnyMValue;
import com.aol.cyclops2.types.extensability.AbstractFunctionalAdapter;
import com.aol.cyclops2.types.extensability.FunctionalAdapter;
import com.aol.cyclops2.types.extensability.ValueAdapter;
import cyclops.control.Option;
import cyclops.control.Xor;
import cyclops.monads.AnyM;
import cyclops.monads.Witness;
import cyclops.monads.Witness.xor;
import lombok.AllArgsConstructor;

import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static cyclops.monads.AnyM.fromMonadicValue;
import static cyclops.monads.Witness.monadicValue;

@AllArgsConstructor
public class XorAdapter extends AbstractFunctionalAdapter<xor> implements ValueAdapter<xor> {
   





    @Override
    public boolean isFilterable(){
        return false;
    }

    

    public <T> Option<T> get(AnyMValue<xor,T> t){
        return xor(t).toOption();
    }
    @Override
    public <T> Iterable<T> toIterable(AnyM<xor, T> t) {
        return xor(t);
    }

    public <R> R visit(Function<? super FunctionalAdapter<xor>,? extends R> fn1, Function<? super ValueAdapter<xor>, ? extends R> fn2){
        return fn2.apply(this);
    }

    public <T> Xor<?,T> xor(AnyM<xor, T> t){
        return (Xor<?,T>)t.unwrap();
    }
    @Override
    public <T> AnyM<xor, T> filter(AnyM<xor, T> t, Predicate<? super T> fn) {
        return t;
    }


    @Override
    public <T> AnyM<xor, T> empty() {
        return AnyM.fromXor(Xor.secondary(null));
      
    }

    @Override
    public <T, R> AnyM<xor, R> ap(AnyM<xor,? extends Function<? super T, ? extends R>> fn, AnyM<xor, T> apply) {
        return flatMap(apply,x->fn.map(fnA->fnA.apply(x)));
         
    }

    @Override
    public <T, R> AnyM<xor, R> flatMap(AnyM<xor, T> t,
            Function<? super T, ? extends AnyM<xor, ? extends R>> fn) {

        return AnyM.fromXor(Witness.xor(t).flatMap(fn.andThen(Witness::xor)));

    }

    @Override
    public <T, R> AnyM<xor, R> map(AnyM<xor, T> t, Function<? super T, ? extends R> fn) {
        return AnyM.fromXor(Witness.xor(t).map(fn));
    }

    @Override
    public <T> AnyM<xor, T> unitIterable(Iterable<T> it) {
       return AnyM.fromXor(fromIterable(it));
    }
   
    @Override
    public <T> AnyM<xor, T> unit(T o) {
        return AnyM.fromXor(Xor.primary(o));
    }

    public static <ST, T> Xor<ST, T> fromIterable(final Iterable<T> iterable) {

        final Iterator<T> it = iterable.iterator();
        return it.hasNext() ? Xor.primary( it.next()) :Xor.secondary(null);
    }
   
}
