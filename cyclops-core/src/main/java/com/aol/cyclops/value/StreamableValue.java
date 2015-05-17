package com.aol.cyclops.value;

import com.aol.cyclops.comprehensions.donotation.Doable;
import com.aol.cyclops.lambda.api.Streamable;


public interface StreamableValue<T> extends Value, Streamable<T>, Doable<T> {
	
	default  T getValue(){
		return (T)this;
	}
}
