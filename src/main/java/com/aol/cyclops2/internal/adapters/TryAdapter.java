package com.aol.cyclops2.internal.adapters;

import com.aol.cyclops2.types.anyM.AnyMValue;
import com.aol.cyclops2.types.extensability.AbstractFunctionalAdapter;
import com.aol.cyclops2.types.extensability.FunctionalAdapter;
import com.aol.cyclops2.types.extensability.ValueAdapter;
import cyclops.control.Option;
import cyclops.control.Try;

import cyclops.control.anym.AnyM;
import cyclops.control.anym.Witness.tryType;
import lombok.AllArgsConstructor;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.function.Predicate;

@AllArgsConstructor
public class TryAdapter extends AbstractFunctionalAdapter<tryType> implements ValueAdapter<tryType> {
   


    @Override
    public boolean isFilterable(){
        return false;
    }

    
    
    public <T> Option<T> get(AnyMValue<tryType,T> t){
        return tryType(t).toOption();
    }
    @Override
    public <T> Iterable<T> toIterable(AnyM<tryType, T> t) {
        return tryType(t);
    }

    public <R> R visit(Function<? super FunctionalAdapter<tryType>,? extends R> fn1, Function<? super ValueAdapter<tryType>, ? extends R> fn2){
        return fn2.apply(this);
    }

    public <T> Try<T,Throwable> tryType(AnyM<tryType, T> t){
        return (Try<T,Throwable>)t.unwrap();
    }
    @Override
    public <T> AnyM<tryType, T> filter(AnyM<tryType, T> t, Predicate<? super T> fn) {
        return t;
    }


    @Override
    public <T> AnyM<tryType, T> empty() {
        return AnyM.fromTry(Try.failure(new NoSuchElementException()));
      
    }

    @Override
    public <T, R> AnyM<tryType, R> ap(AnyM<tryType,? extends Function<? super T, ? extends R>> fn, AnyM<tryType, T> apply) {
        return flatMap(apply,x->fn.map(fnA->fnA.apply(x)));
         
    }

    @Override
    public <T, R> AnyM<tryType, R> flatMap(AnyM<tryType, T> t,
            Function<? super T, ? extends AnyM<tryType, ? extends R>> fn) {
        
        return AnyM.fromTry(toTry(t).flatMap(fn.andThen(TryAdapter::toTry)));
       
    }

    @Override
    public <T, R> AnyM<tryType, R> map(AnyM<tryType, T> t, Function<? super T, ? extends R> fn) {
        return AnyM.fromTry(toTry(t).map(fn));
    }

    private static <T> Try<T, Throwable> toTry(AnyM<tryType, T> t) {
        return (Try<T,Throwable>)t.unwrap();
    }

    @Override
    public <T> AnyM<tryType, T> unitIterable(Iterable<T> it) {
       return AnyM.fromTry(fromIterable(it));
    }
   
    @Override
    public <T> AnyM<tryType, T> unit(T o) {
        return AnyM.fromTry(Try.success(o));
    }


   private static <T> Try<T,Throwable> fromIterable(final Iterable<T> iterable) {

        final Iterator<T> it = iterable.iterator();
        return it.hasNext() ? Try.success( it.next()) : Try.failure(new NoSuchElementException());
    }
}
