package com.aol.cyclops.api;

import java.util.function.Function;

public interface Function2Converter<T,R> {
	R λ2(T fn);
}
