package com.aol.cyclops;

import com.aol.cyclops.types.MonadicValue;

public interface MonadPlus<T> {
    
    public MonadicValue<T> zero();
    public MonadicValue<T> plus(MonadicValue<T> a, MonadicValue<T> b);

}
