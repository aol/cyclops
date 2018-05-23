package com.oath.cyclops.types;

import java.util.function.Supplier;

public interface OrElseValue<T, X extends OrElseValue<T,?>> extends Value<T> {
    default X orElseUse(X opt){
        if(isPresent())
            return (X)this;
        return opt;
    }
    default X orElseUse(Supplier<X> opt){
        if(isPresent())
            return (X)this;
        return opt.get();
    }

}
