package com.aol.cyclops2.types.recoverable;


import java.util.function.Function;
import java.util.function.Supplier;

public interface Recoverable<U> {

    Recoverable<U> recover(Supplier<? extends U> value);


}
