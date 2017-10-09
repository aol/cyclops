package com.aol.cyclops2.internal.adapters;

import com.aol.cyclops2.types.anyM.AnyMValue;
import com.aol.cyclops2.types.extensability.AbstractFunctionalAdapter;
import com.aol.cyclops2.types.extensability.FunctionalAdapter;
import com.aol.cyclops2.types.extensability.ValueAdapter;
import cyclops.control.Option;
import cyclops.control.Xor;
import cyclops.control.lazy.Either4;
import cyclops.monads.AnyM;
import cyclops.monads.Witness;
import cyclops.monads.Witness.either4;
import lombok.AllArgsConstructor;

import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Predicate;

@AllArgsConstructor
public class Either4Adapter extends AbstractFunctionalAdapter<either4> implements ValueAdapter<either4> {
   

    @Override
    public boolean isFilterable(){
        return false;
    }

    
    
    public <T> Option<T> get(AnyMValue<either4,T> t){
        return either4(t).toOption();
    }
    @Override
    public <T> Iterable<T> toIterable(AnyM<either4, T> t) {
        return either4(t);
    }

    public <R> R visit(Function<? super FunctionalAdapter<either4>,? extends R> fn1, Function<? super ValueAdapter<either4>, ? extends R> fn2){
        return fn2.apply(this);
    }

    public <T> Xor<?,T> either4(AnyM<either4, T> t){
        return (Xor<?,T>)t.unwrap();
    }
    @Override
    public <T> AnyM<either4, T> filter(AnyM<either4, T> t, Predicate<? super T> fn) {
        return t;
    }


    @Override
    public <T> AnyM<either4, T> empty() {
        return AnyM.fromEither4(Either4.left1(null));
      
    }

    @Override
    public <T, R> AnyM<either4, R> ap(AnyM<either4,? extends Function<? super T, ? extends R>> fn, AnyM<either4, T> apply) {
        return flatMap(apply,x->fn.map(fnA->fnA.apply(x)));
         
    }

    @Override
    public <T, R> AnyM<either4, R> flatMap(AnyM<either4, T> t,
            Function<? super T, ? extends AnyM<either4, ? extends R>> fn) {
        
        return AnyM.fromEither4(Witness.either4(t).flatMap(fn.andThen(Witness::either4)));
       
    }

    @Override
    public <T, R> AnyM<either4, R> map(AnyM<either4, T> t, Function<? super T, ? extends R> fn) {
        return AnyM.fromEither4(Witness.either4(t).map(fn));
    }

    @Override
    public <T> AnyM<either4, T> unitIterable(Iterable<T> it) {
       return AnyM.fromEither4(fromIterable(it));
    }
   
    @Override
    public <T> AnyM<either4, T> unit(T o) {
        return AnyM.fromEither4(Either4.right(o));
    }


   private static <ST, ST2, ST3,T> Either4<ST,ST2,ST3, T> fromIterable(final Iterable<T> iterable) {

        final Iterator<T> it = iterable.iterator();
        return it.hasNext() ? Either4.right( it.next()) : Either4.left1(null);
    }
}
