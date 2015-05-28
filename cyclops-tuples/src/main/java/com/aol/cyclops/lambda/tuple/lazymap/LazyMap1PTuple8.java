package com.aol.cyclops.lambda.tuple.lazymap;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import com.aol.cyclops.lambda.tuple.PTuple2;
import com.aol.cyclops.lambda.tuple.PTuple8;
import com.aol.cyclops.lambda.tuple.TupleImpl;
import com.aol.cyclops.lambda.utils.LazyImmutable;

public class LazyMap1PTuple8<T,T1,T2,T3,T4,T5,T6,T7,T8> extends TupleImpl<T,T2,T3,T4,T5,T6,T7,T8> {

	private final LazyImmutable<T> value = LazyImmutable.def();
	private final Function<T1, T> fn;
	private final PTuple8<T1,T2,T3,T4,T5,T6,T7,T8> host;

	public LazyMap1PTuple8( Function<T1, T> fn,PTuple8<T1,T2,T3,T4,T5,T6,T7,T8> host){
		super(host.arity());
		this.host = host;
		this.fn = fn;
	}
	public T v1(){
			return value.getOrSet(()->fn.apply(host.v1())); 
		}

	@Override
	public List<Object> getCachedValues() {
			return Arrays.asList(v1(),v2());
	}

	@Override
	public Iterator iterator() {
			return getCachedValues().iterator();
	}

		
};