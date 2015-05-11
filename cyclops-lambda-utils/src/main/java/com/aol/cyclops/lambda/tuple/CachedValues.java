package com.aol.cyclops.lambda.tuple;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import com.aol.cyclops.lambda.api.Decomposable;

public interface CachedValues extends Iterable, Decomposable{

	public List<Object> getCachedValues();
	
	@Override
	default Iterator iterator(){
		return getCachedValues().iterator();
	}
	
	default Stream stream(){
		return getCachedValues().stream();
	}
}
