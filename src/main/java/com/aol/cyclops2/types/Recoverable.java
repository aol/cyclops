package com.aol.cyclops2.types;


import cyclops.control.Maybe;

import java.util.function.Function;
import java.util.function.Supplier;

public interface Recoverable<T,U> {

    Recoverable<T,U> recover(Supplier<? extends U> value);
    Recoverable<T,U> recover(Function<? super T,? extends U> fn);

}
