package com.aol.cyclops.comprehensions;

import java.util.stream.Stream;

public interface StreamConverter<T,X> {

	abstract X convert(Stream<T> t);
}
