package com.aol.cyclops2.internal.adapters;

import com.aol.cyclops2.types.anyM.AnyMValue;
import com.aol.cyclops2.types.extensability.AbstractFunctionalAdapter;
import com.aol.cyclops2.types.extensability.FunctionalAdapter;
import com.aol.cyclops2.types.extensability.ValueAdapter;
import cyclops.control.Option;
import cyclops.control.Either;
import cyclops.control.LazyEither3;
import cyclops.control.anym.AnyM;
import cyclops.control.anym.Witness;
import cyclops.control.anym.Witness.lazyEither3;
import lombok.AllArgsConstructor;

import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Predicate;

@AllArgsConstructor
public class LazyEither3Adapter extends AbstractFunctionalAdapter<lazyEither3> implements ValueAdapter<lazyEither3> {
   


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

    public <R> R visit(Function<? super FunctionalAdapter<lazyEither3>,? extends R> fn1, Function<? super ValueAdapter<lazyEither3>, ? extends R> fn2){
        return fn2.apply(this);
    }

    public <T> Either<?,T> either3(AnyM<lazyEither3, T> t){
        return (Either<?,T>)t.unwrap();
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
        
        return AnyM.fromEither3(Witness.lazyEither3(t).flatMap(fn.andThen(Witness::lazyEither3)));
       
    }

    @Override
    public <T, R> AnyM<lazyEither3, R> map(AnyM<lazyEither3, T> t, Function<? super T, ? extends R> fn) {
        return AnyM.fromEither3(Witness.lazyEither3(t).map(fn));
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
