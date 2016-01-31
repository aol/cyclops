package com.aol.cyclops.comprehensions.converters;

import java.util.stream.Stream;

public interface StreamUpscaler<T,X> {

	default Object upscaleIfStream(Object o){
		if(o instanceof Stream)
			return convert((Stream)o);
		return o;
	}
	abstract X convert(Stream<T> t);
}
