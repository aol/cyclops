package com.aol.cyclops2.internal.adapters;

import com.aol.cyclops2.types.anyM.AnyMValue;
import com.aol.cyclops2.types.extensability.AbstractFunctionalAdapter;
import com.aol.cyclops2.types.extensability.FunctionalAdapter;
import com.aol.cyclops2.types.extensability.ValueAdapter;
import cyclops.control.Option;
import cyclops.control.Xor;
import cyclops.control.lazy.Either;
import cyclops.control.lazy.Either3;
import cyclops.monads.AnyM;
import cyclops.monads.Witness;
import cyclops.monads.Witness.either3;
import lombok.AllArgsConstructor;

import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Predicate;

@AllArgsConstructor
public class Either3Adapter extends AbstractFunctionalAdapter<either3> implements ValueAdapter<either3> {
   


    @Override
    public boolean isFilterable(){
        return false;
    }

    
    
    public <T> Option<T> get(AnyMValue<either3,T> t){
        return either3(t).toOption();
    }
    @Override
    public <T> Iterable<T> toIterable(AnyM<either3, T> t) {
        return either3(t);
    }

    public <R> R visit(Function<? super FunctionalAdapter<either3>,? extends R> fn1, Function<? super ValueAdapter<either3>, ? extends R> fn2){
        return fn2.apply(this);
    }

    public <T> Xor<?,T> either3(AnyM<either3, T> t){
        return (Xor<?,T>)t.unwrap();
    }
    @Override
    public <T> AnyM<either3, T> filter(AnyM<either3, T> t, Predicate<? super T> fn) {
        return t;
    }


    @Override
    public <T> AnyM<either3, T> empty() {
        return AnyM.fromEither3(Either3.left1(null));
      
    }

    @Override
    public <T, R> AnyM<either3, R> ap(AnyM<either3,? extends Function<? super T, ? extends R>> fn, AnyM<either3, T> apply) {
        return flatMap(apply,x->fn.map(fnA->fnA.apply(x)));
         
    }

    @Override
    public <T, R> AnyM<either3, R> flatMap(AnyM<either3, T> t,
            Function<? super T, ? extends AnyM<either3, ? extends R>> fn) {
        
        return AnyM.fromEither3(Witness.either3(t).flatMap(fn.andThen(Witness::either3)));
       
    }

    @Override
    public <T, R> AnyM<either3, R> map(AnyM<either3, T> t, Function<? super T, ? extends R> fn) {
        return AnyM.fromEither3(Witness.either3(t).map(fn));
    }

    @Override
    public <T> AnyM<either3, T> unitIterable(Iterable<T> it) {
       return AnyM.fromEither3(fromIterable(it));
    }
   
    @Override
    public <T> AnyM<either3, T> unit(T o) {
        return AnyM.fromEither3(Either3.right(o));
    }


   private static <ST, ST2, T> Either3<ST,ST2, T> fromIterable(final Iterable<T> iterable) {

        final Iterator<T> it = iterable.iterator();
        return it.hasNext() ? Either3.right( it.next()) : Either3.left1(null);
    }
}
