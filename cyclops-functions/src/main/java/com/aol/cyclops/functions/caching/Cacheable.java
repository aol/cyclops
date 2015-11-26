package com.aol.cyclops.functions.caching;

import java.util.function.Function;

public interface Cacheable<OUT> {

	public OUT computeIfAbsent(Object key, Function<Object,OUT> fn);
}
