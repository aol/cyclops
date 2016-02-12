package com.aol.cyclops.internal.sequence.streamable;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.stream.Collector;

import com.aol.cyclops.util.Streamable;

import lombok.Getter;
import lombok.Value;



@Value
public  class StreamableImpl<T> implements Streamable<T>{
	@Getter
	private final T streamable;

	
	
}