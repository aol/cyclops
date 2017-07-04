package com.aol.cyclops2.internal.adapters;

import static cyclops.monads.AnyM.fromStreamable;
import static cyclops.monads.Witness.streamable;

import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import cyclops.monads.AnyM;
import cyclops.stream.Streamable;
import cyclops.monads.Witness;

import com.aol.cyclops2.types.extensability.AbstractFunctionalAdapter;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class StreamableAdapter extends AbstractFunctionalAdapter<streamable> {
    
    private final Supplier<Streamable<?>> empty;
    private final Function<?,Streamable<?>> unit;
    private final Function<Iterator<?>,Streamable<?>> unitIterator;
    public final static StreamableAdapter streamable = new StreamableAdapter(()->Streamable.of(),t->Streamable.of(t),it->Streamable.fromIterator(it));
 
    
    private <U> Supplier<Streamable<U>> getEmpty(){
        return (Supplier)empty;
    }
    private <U> Function<U,Streamable<U>>  getUnit(){
        return (Function)unit;
    }
    private <U> Function<Iterator<U>,Streamable<U>>  getUnitIterator(){
        return (Function)unitIterator;
    }
    @Override
    public <T> Iterable<T> toIterable(AnyM<streamable, T> t) {
        return ()->streamable(t).iterator();
    }
    

    @Override
    public <T> AnyM<streamable, T> filter(AnyM<streamable, T> t, Predicate<? super T> fn) {
        return fromStreamable(streamable(t).filter(fn));
    }


    @Override
    public <T> AnyM<streamable, T> empty() {
        return fromStreamable(this.<T>getEmpty().get());
    }


    @Override
    public <T, R> AnyM<streamable, R> ap(AnyM<streamable,? extends Function<? super T,? extends  R>> fn, AnyM<streamable, T> apply) {
         return fromStreamable(streamable(apply).zip(streamable(fn),(a,b)->b.apply(a)));
    }

    @Override
    public <T, R> AnyM<streamable, R> flatMap(AnyM<streamable, T> t,
            Function<? super T, ? extends AnyM<streamable, ? extends R>> fn) {
        return fromStreamable(streamable(t).flatMap(fn.andThen(Witness::streamable)));
    }

    @Override
    public <T> AnyM<streamable, T> unitIterable(Iterable<T> it)  {
       return fromStreamable(this.<T>getUnitIterator().apply(it.iterator()));
    }
   
    @Override
    public <T> AnyM<streamable, T> unit(T o) {
        return fromStreamable(this.<T>getUnit().apply(o));
    }


    @Override
    public <T, R> AnyM<streamable, R> map(AnyM<streamable, T> t, Function<? super T, ? extends R> fn) {
        return fromStreamable(streamable(t).map(fn));
    }
}
