package com.aol.cyclops.internal.comprehensions.comprehenders;

import static com.aol.cyclops.control.AnyM.fromStreamable;
import static com.aol.cyclops.types.anyM.Witness.streamable;

import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.Streamable;
import com.aol.cyclops.types.anyM.Witness;

import com.aol.cyclops.types.extensability.AbstractFunctionalAdapter;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class StreamableAdapter extends AbstractFunctionalAdapter<Witness.streamable> {
    
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
    public <T, R> AnyM<streamable, R> ap(AnyM<streamable, Function<T, R>> fn, AnyM<streamable, T> apply) {
         return fromStreamable(streamable(apply).zip(streamable(fn),(a,b)->b.apply(a)));
    }

    @Override
    public <T, R> AnyM<streamable, R> flatMap(AnyM<streamable, T> t,
            Function<? super T, ? extends AnyM<streamable, ? extends R>> fn) {
        return fromStreamable(Witness.streamable(t).flatMap(fn.andThen(Witness::streamable)));
    }

    @Override
    public <T> AnyM<streamable, T> unitIterator(Iterator<T> it) {
       return fromStreamable(this.<T>getUnitIterator().apply(it));
    }
   
    @Override
    public <T> AnyM<com.aol.cyclops.types.anyM.Witness.streamable, T> unit(T o) {
        return fromStreamable(this.<T>getUnit().apply(o));
    }

   
   
}
