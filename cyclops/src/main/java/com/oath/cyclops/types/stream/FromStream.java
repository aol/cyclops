package com.oath.cyclops.types.stream;

import cyclops.reactive.ReactiveSeq;

public interface FromStream<T> {

  public <R> R fromStream(ReactiveSeq<T> t);
}
