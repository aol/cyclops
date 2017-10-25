package com.aol.cyclops2.internal.adapters;

import static cyclops.monads.AnyM.fromCompletableFuture;
import static cyclops.monads.Witness.completableFuture;
import static cyclops.companion.CompletableFutures.combine;

import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

import cyclops.control.Option;
import cyclops.monads.AnyM;
import cyclops.async.Future;
import com.aol.cyclops2.types.anyM.AnyMValue;
import cyclops.monads.Witness;
import com.aol.cyclops2.types.extensability.AbstractFunctionalAdapter;
import com.aol.cyclops2.types.extensability.ValueAdapter;
import cyclops.companion.CompletableFutures;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class FutureAdapter extends AbstractFunctionalAdapter<completableFuture> implements ValueAdapter<completableFuture> {
    
    private final Supplier<CompletableFuture<?>> empty;
    private final Function<?,CompletableFuture<?>> unit;
    
    
    public final static FutureAdapter completableFuture = new FutureAdapter(()->new CompletableFuture(),
                                                                            t->CompletableFuture.completedFuture(t));
    
    private <U> Supplier<CompletableFuture<U>> getEmpty(){
        return (Supplier)empty;
    }
    private <U> Function<U,CompletableFuture<U>>  getUnit(){
        return (Function)unit;
    }
    private <U> Function<Iterator<U>,CompletableFuture<U>>  getUnitIterator(){
        return  it->it.hasNext() ? this.<U>getUnit().apply(it.next()) : this.<U>getEmpty().get();
    }
    public <T> Option<T> get(AnyMValue<completableFuture,T> t){
        CompletableFuture<T> cf = (CompletableFuture<T>)t;
        if(cf.isCompletedExceptionally())
            return Option.none();
        try {
            return Option.some(cf.join());
        }
        catch(Throwable x){
            return Option.none();
        }
    }


    @Override
    public <T> Iterable<T> toIterable(AnyM<completableFuture, T> t) {
        return Future.of(completableFuture(t));
    }
    



    @Override
    public <T> AnyM<completableFuture, T> empty() {
        return fromCompletableFuture(this.<T>getEmpty().get());
    }


    @Override
    public <T, R> AnyM<completableFuture, R> ap(AnyM<completableFuture, ? extends Function<? super T, ? extends R>> fn, AnyM<completableFuture, T> apply) {
         return fromCompletableFuture(combine(completableFuture(apply), completableFuture(fn),(a,b)->b.apply(a)));
    }

    @Override
    public <T, R> AnyM<completableFuture, R> flatMap(AnyM<completableFuture, T> t,
            Function<? super T, ? extends AnyM<completableFuture, ? extends R>> fn) {
        return fromCompletableFuture(completableFuture(t).<R>thenCompose(fn.andThen(Witness::completableFuture).andThen(CompletableFutures::narrow)));
    }

    @Override
    public <T, R> AnyM<Witness.completableFuture, R> map(AnyM<Witness.completableFuture, T> t, Function<? super T, ? extends R> fn) {
        return fromCompletableFuture(completableFuture(t).<R>thenApply(fn));
    }

    @Override
    public <T> AnyM<completableFuture, T> unitIterable(Iterable<T> it) {
       return fromCompletableFuture(this.<T>getUnitIterator().apply(it.iterator()));
    }
   
    @Override
    public <T> AnyM<completableFuture, T> unit(T o) {
        return fromCompletableFuture(this.<T>getUnit().apply(o));
    }

   
   
}
