package com.aol.cyclops2.internal.adapters;

import com.aol.cyclops2.types.anyM.AnyMValue;
import com.aol.cyclops2.types.extensability.AbstractFunctionalAdapter;
import com.aol.cyclops2.types.extensability.FunctionalAdapter;
import com.aol.cyclops2.types.extensability.ValueAdapter;
import cyclops.control.Option;
import cyclops.control.Xor;
import cyclops.control.lazy.Either;
import cyclops.monads.AnyM;
import cyclops.monads.Witness;
import cyclops.monads.Witness.either;
import lombok.AllArgsConstructor;

import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Predicate;

@AllArgsConstructor
public class EitherAdapter extends AbstractFunctionalAdapter<either> implements ValueAdapter<either> {
   
  


    @Override
    public boolean isFilterable(){
        return false;
    }

    
    
    public <T> Option<T> get(AnyMValue<either,T> t){
        return either(t).toOption();
    }
    @Override
    public <T> Iterable<T> toIterable(AnyM<either, T> t) {
        return either(t);
    }

    public <R> R visit(Function<? super FunctionalAdapter<either>,? extends R> fn1, Function<? super ValueAdapter<either>, ? extends R> fn2){
        return fn2.apply(this);
    }

    public <T> Xor<?,T> either(AnyM<either, T> t){
        return (Xor<?,T>)t.unwrap();
    }
    @Override
    public <T> AnyM<either, T> filter(AnyM<either, T> t, Predicate<? super T> fn) {
        return t;
    }


    @Override
    public <T> AnyM<either, T> empty() {
        return AnyM.fromEither(Either.left(null));
      
    }

    @Override
    public <T, R> AnyM<either, R> ap(AnyM<either,? extends Function<? super T, ? extends R>> fn, AnyM<either, T> apply) {
        return flatMap(apply,x->fn.map(fnA->fnA.apply(x)));
         
    }

    @Override
    public <T, R> AnyM<either, R> flatMap(AnyM<either, T> t,
            Function<? super T, ? extends AnyM<either, ? extends R>> fn) {
        
        return AnyM.fromEither(Witness.either(t).flatMap(fn.andThen(Witness::either)));
       
    }

    @Override
    public <T, R> AnyM<either, R> map(AnyM<either, T> t, Function<? super T, ? extends R> fn) {
        return AnyM.fromEither(Witness.either(t).map(fn));
    }

    @Override
    public <T> AnyM<either, T> unitIterable(Iterable<T> it) {
       return AnyM.fromEither(fromIterable(it));
    }
   
    @Override
    public <T> AnyM<either, T> unit(T o) {
        return AnyM.fromEither(Either.right(o));
    }


   private static <ST, T> Either<ST, T> fromIterable(final Iterable<T> iterable) {

        final Iterator<T> it = iterable.iterator();
        return it.hasNext() ? Either.right( it.next()) : Either.left(null);
    }
}
