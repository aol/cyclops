package com.aol.cyclops2.types;


public interface Present<T> {

    public T orElse(T alt);
}
