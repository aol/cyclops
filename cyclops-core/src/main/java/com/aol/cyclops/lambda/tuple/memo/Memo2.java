package com.aol.cyclops.lambda.tuple.memo;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.aol.cyclops.lambda.tuple.PTuple2;
import com.aol.cyclops.lambda.tuple.TupleImpl;

public class Memo2 <T1,T2> extends TupleImpl{
	 private final Map<Integer,Object> values = new ConcurrentHashMap<>();
	    private final PTuple2<T1,T2> host;
		public Memo2(PTuple2<T1,T2> host){
			super(Arrays.asList(),2);
			this.host= host;
		}
		
		
		public T1 v1(){
			return ( T1)values.computeIfAbsent(new Integer(0), key -> host.v1());
		}

		public T2 v2(){
			return ( T2)values.computeIfAbsent(new Integer(1), key -> host.v2());
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