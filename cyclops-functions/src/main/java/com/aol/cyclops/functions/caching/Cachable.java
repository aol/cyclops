package com.aol.cyclops.functions.caching;

import java.util.function.Function;

public interface Cachable<OUT> {

	public OUT computeIfAbsent(Object key, Function<Object,OUT> fn);
}
