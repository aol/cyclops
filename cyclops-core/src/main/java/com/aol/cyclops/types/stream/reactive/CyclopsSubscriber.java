package com.aol.cyclops.types.stream.reactive;

import org.reactivestreams.Subscriber;

import com.aol.cyclops.control.SequenceM;

public interface CyclopsSubscriber<T> extends Subscriber<T>{

	

	SequenceM<T> sequenceM();
}
