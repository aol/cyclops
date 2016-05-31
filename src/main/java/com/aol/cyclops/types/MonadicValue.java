package com.aol.cyclops.types;

import java.util.function.Function;

import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.types.anyM.AnyMValue;

public interface MonadicValue<T> extends Value<T>, Unit<T>,Functor<T> {

    /**
     * @return true if this value is present
     
    default boolean isPresent(){
        return toOptional().isPresent();
    }*/
    public <T> MonadicValue<T> unit(T unit);
    <R> MonadicValue<R>  map(Function<? super T,? extends R> fn);
    
    default AnyMValue<T> anyM(){
        return AnyM.ofValue(this);
    }
    default <R> MonadicValue<R> coflatMap(Function<? super MonadicValue<T>,R> mapper){
        return mapper.andThen(r->unit(r)).apply(this);
    }
    //cojoin
    default  MonadicValue<MonadicValue<T>> nest(){
        return this.map(t->unit(t));
    }
    
    
}
