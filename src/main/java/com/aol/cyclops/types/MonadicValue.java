package com.aol.cyclops.types;

import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.types.anyM.AnyMValue;

public interface MonadicValue<T> extends Value<T> {

       default AnyMValue<T> anyM(){
           return AnyM.ofValue(this);
       }
    
}
