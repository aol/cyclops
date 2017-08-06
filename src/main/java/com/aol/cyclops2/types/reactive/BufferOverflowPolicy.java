package com.aol.cyclops2.types.reactive;

import cyclops.control.Xor;

public enum BufferOverflowPolicy{ DROP, BLOCK;
    public <T> Xor<Void,T> match(T value){
        return this==DROP? Xor.secondary(null) : Xor.primary(value);
    }
}