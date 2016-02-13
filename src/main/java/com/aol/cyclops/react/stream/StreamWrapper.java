package com.aol.cyclops.react.stream;

import java.util.List;
import java.util.stream.Stream;

public interface StreamWrapper<U> {
	public Stream<U> stream();

	//public List<U> list();
}
