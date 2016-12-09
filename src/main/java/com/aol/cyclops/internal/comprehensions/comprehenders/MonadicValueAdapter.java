package com.aol.cyclops.internal.comprehensions.comprehenders;

import static com.aol.cyclops.control.AnyM.fromMonadicValue;
import static com.aol.cyclops.types.anyM.Witness.monadicValue;

import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.types.MonadicValue;
import com.aol.cyclops.types.anyM.Witness;
import com.aol.cyclops.types.extensability.AbstractFunctionalAdapter;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class MonadicValueAdapter<W extends Witness.MonadicValueWitness<W>> extends AbstractFunctionalAdapter<W> {
   
    private final Supplier<MonadicValue<?>> empty;
    private final Function<?,MonadicValue<?>> unit;
   
    private final boolean filter;
    private final W witness;
    
    
    private <U> Supplier<MonadicValue<U>> getEmpty(){
        return (Supplier)empty;
    }
    private <U> Function<U,MonadicValue<U>>  getUnit(){
        return (Function)unit;
    }
    private <U> Function<Iterator<U>,MonadicValue<U>>  getUnitIterator(){
        return  it->it.hasNext() ? this.<U>getUnit().apply(it.next()) : this.<U>getEmpty().get();
    }
    @Override
    public <T> Iterable<T> toIterable(AnyM<W, T> t) {
        return monadicValue(t);
    }
    

    @Override
    public <T> AnyM<W, T> filter(AnyM<W, T> t, Predicate<? super T> fn) {
        if(filter)
            return fromMonadicValue(monadicValue(t).filter(fn),witness);
        return super.filter(t, fn);
    }


    @Override
    public <T> AnyM<W, T> empty() {
        return fromMonadicValue(this.<T>getEmpty().get(),witness);
      
    }

    @Override
    public <T, R> AnyM<W, R> ap(AnyM<W,? extends Function<T, R>> fn, AnyM<W, T> apply) {
         return fromMonadicValue(monadicValue(apply).combine(monadicValue(fn),(a,b)->b.apply(a)),witness);
         
    }

    @Override
    public <T, R> AnyM<W, R> flatMap(AnyM<W, T> t,
            Function<? super T, ? extends AnyM<W, ? extends R>> fn) {
        return fromMonadicValue(monadicValue(t).flatMap(fn.andThen(Witness::monadicValue)),witness);
    }

    @Override
    public <T> AnyM<W, T> unitIterator(Iterator<T> it) {
       return fromMonadicValue(this.<T>getUnitIterator().apply(it),witness);
    }
   
    @Override
    public <T> AnyM<W, T> unit(T o) {
        return fromMonadicValue(this.<T>getUnit().apply(o),witness);
    }

   
   
}
