package com.aol.cyclops.types.stream.reactive;

import org.reactivestreams.Subscriber;

import com.aol.cyclops.control.ReactiveSeq;

public interface CyclopsSubscriber<T> extends Subscriber<T>{

	

	ReactiveSeq<T> sequenceM();
}
