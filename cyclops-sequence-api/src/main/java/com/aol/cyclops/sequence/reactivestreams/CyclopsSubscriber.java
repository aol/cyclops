package com.aol.cyclops.sequence.reactivestreams;

import org.reactivestreams.Subscriber;

import com.aol.cyclops.sequence.SequenceM;

public interface CyclopsSubscriber<T> extends Subscriber<T>{

	SequenceM<T> sequenceM();
}
