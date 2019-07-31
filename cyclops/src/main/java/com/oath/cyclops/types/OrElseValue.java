package com.oath.cyclops.types;

import com.oath.cyclops.types.recoverable.OnEmptySwitch;

import java.util.function.Supplier;

@Deprecated
public interface OrElseValue<T, X extends OrElseValue<T,?>> extends Value<T>, OnEmptySwitch<T,X> {
    @Deprecated
    default X orElseUse(X opt){
        if(isPresent())
            return (X)this;
        return opt;
    }
    @Deprecated
    default X orElseUse(Supplier<X> opt){
      return onEmptySwitch(opt);
    }

    @Override
    default X onEmptySwitch(Supplier<? extends X> supplier){
        if(isPresent())
            return (X)this;
        return supplier.get();
    }
}
