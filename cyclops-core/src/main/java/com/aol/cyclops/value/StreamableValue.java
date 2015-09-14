package com.aol.cyclops.value;

import com.aol.cyclops.comprehensions.donotation.Doable;
import com.aol.cyclops.sequence.streamable.Streamable;


public interface StreamableValue<T> extends ValueObject, Streamable<T>, Doable<T> {
	
	
}
