package com.aol.cyclops.lambda.tuple;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import com.aol.cyclops.data.LazyImmutable;

public class LazyMap2PTuple8<T,T1,T2,T3,T4,T5,T6,T7,T8> extends TupleImpl<T1,T,T3,T4,T5,T6,T7,T8> {
	 private final LazyImmutable<T> value = LazyImmutable.def();
	 private final Function<T2, T> fn;
	 private final PTuple8<T1,T2,T3,T4,T5,T6,T7,T8> host;
	public LazyMap2PTuple8( Function<T2, T> fn,PTuple8<T1,T2,T3,T4,T5,T6,T7,T8> host){
		super(host.arity());
		this.host = host;
		this.fn = fn;
	}
		public T v2(){
			return value.computeIfAbsent(()->fn.apply(host.v2())); 
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