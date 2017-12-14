package com.oath.cyclops.types.reactive;

import cyclops.control.Either;
import cyclops.control.Option;

public enum BufferOverflowPolicy{ DROP, BLOCK;
    public <T> Option<T> match(T value){
        return this==DROP? Option.none() : Option.some(value);
    }
}
