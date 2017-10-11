package com.aol.cyclops2.types.reactive;

import cyclops.control.Either;

public enum BufferOverflowPolicy{ DROP, BLOCK;
    public <T> Either<Void,T> match(T value){
        return this==DROP? Either.left(null) : Either.right(value);
    }
}