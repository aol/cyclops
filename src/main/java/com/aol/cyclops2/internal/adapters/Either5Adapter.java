package com.aol.cyclops2.internal.adapters;

import com.aol.cyclops2.types.anyM.AnyMValue;
import com.aol.cyclops2.types.extensability.AbstractFunctionalAdapter;
import com.aol.cyclops2.types.extensability.FunctionalAdapter;
import com.aol.cyclops2.types.extensability.ValueAdapter;
import cyclops.control.Option;
import cyclops.control.Xor;
import cyclops.control.lazy.Either5;
import cyclops.monads.AnyM;
import cyclops.monads.Witness;
import cyclops.monads.Witness.either5;
import lombok.AllArgsConstructor;

import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Predicate;

@AllArgsConstructor
public class Either5Adapter extends AbstractFunctionalAdapter<either5> implements ValueAdapter<either5> {
   
  



    @Override
    public boolean isFilterable(){
        return false;
    }

    
    public <T> Option<T> get(AnyMValue<either5,T> t){
        return either5(t).toOption();
    }
    @Override
    public <T> Iterable<T> toIterable(AnyM<either5, T> t) {
        return either5(t);
    }

    public <R> R visit(Function<? super FunctionalAdapter<either5>,? extends R> fn1, Function<? super ValueAdapter<either5>, ? extends R> fn2){
        return fn2.apply(this);
    }

    public <T> Xor<?,T> either5(AnyM<either5, T> t){
        return (Xor<?,T>)t.unwrap();
    }
    @Override
    public <T> AnyM<either5, T> filter(AnyM<either5, T> t, Predicate<? super T> fn) {
        return t;
    }


    @Override
    public <T> AnyM<either5, T> empty() {
        return AnyM.fromEither5(Either5.left1(null));
      
    }

    @Override
    public <T, R> AnyM<either5, R> ap(AnyM<either5,? extends Function<? super T, ? extends R>> fn, AnyM<either5, T> apply) {
        return flatMap(apply,x->fn.map(fnA->fnA.apply(x)));
         
    }

    @Override
    public <T, R> AnyM<either5, R> flatMap(AnyM<either5, T> t,
            Function<? super T, ? extends AnyM<either5, ? extends R>> fn) {
        
        return AnyM.fromEither5(Witness.either5(t).flatMap(fn.andThen(Witness::either5)));
       
    }

    @Override
    public <T, R> AnyM<either5, R> map(AnyM<either5, T> t, Function<? super T, ? extends R> fn) {
        return AnyM.fromEither5(Witness.either5(t).map(fn));
    }

    @Override
    public <T> AnyM<either5, T> unitIterable(Iterable<T> it) {
       return AnyM.fromEither5(fromIterable(it));
    }
   
    @Override
    public <T> AnyM<either5, T> unit(T o) {
        return AnyM.fromEither5(Either5.right(o));
    }


   private static <ST, ST2, ST3, ST4, T> Either5<ST,ST2,ST3,ST4,T> fromIterable(final Iterable<T> iterable) {

        final Iterator<T> it = iterable.iterator();
        return it.hasNext() ? Either5.right( it.next()) : Either5.left1(null);
    }
}
