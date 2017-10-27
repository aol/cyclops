package com.oath.cyclops.types;


public interface Present<T> {

    public T orElse(T alt);
}
