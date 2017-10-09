package com.aol.cyclops2.types;

public interface OrElseValue<T, X extends OrElseValue<T,?>> extends Value<T> {
    default X orElseUse(X opt){
        if(isPresent())
            return (X)this;
        return opt;
    }

}
