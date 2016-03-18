package com.aol.cyclops.types;

import java.util.function.Function;

public interface MonadicValue2<T1,T2> extends MonadicValue<T2>{
    <R1,R2> MonadicValue2<R1,R2> flatMap(Function<? super T2,? extends MonadicValue2<? extends R1,? extends R2>> mapper);
}
