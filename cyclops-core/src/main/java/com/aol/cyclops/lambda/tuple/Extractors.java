package com.aol.cyclops.lambda.tuple;

import java.util.Iterator;

import com.aol.cyclops.matcher.Extractor;

public class Extractors {
	/**
	 * @return An extractor that generates a PTuple from an iterable, extracts the first 2 values to create a PTuple2
	 */
	public final static <V1,V2> Extractor<Iterable,PTuple2<V1,V2>> toPTuple2(){
		return  ( Iterable itable)-> {
			Iterator it = itable.iterator();
			return new TupleImpl(it,2);//(it.next(),it.next());
		};
		
	}
	
	/**
	 *  @return An extractor that generates a PTuple from an iterable, extracts the first 3 values to create a PTuple3
	 */
	public final static <V1,V2,V3> Extractor<Iterable,PTuple3<V1,V2,V3>> toPTuple3(){
		return  ( Iterable itable)-> {
			Iterator it = itable.iterator();
			return  new TupleImpl(it,3);
		};
		
	}
	/**
	 *  @return An extractor that generates a PTuple from an iterable, extracts the first 4 values to create a PTuple4
	 */
	public final static <V1,V2,V3,V4> Extractor<Iterable,PTuple4<V1,V2,V3,V4>> toPTuple4(){
		return  ( Iterable itable)-> {
			Iterator it = itable.iterator();
			return new TupleImpl(it,4);
		};
		
	}
	/**
	 *  @return An extractor that generates a PTuple from an iterable, extracts the first 5 values to create a PTuple5
	 */
	public final static <V1,V2,V3,V4,V5> Extractor<Iterable,PTuple5<V1,V2,V3,V4,V5>> toPTuple5(){
		return  ( Iterable itable)-> {
			Iterator it = itable.iterator();
			return new TupleImpl(it,5);
		};
		
	}
	/**
	 *  @return An extractor that generates a PTuple from an iterable, extracts the first 6 values to create a PTuple6
	 */
	public final static <V1,V2,V3,V4,V5,V6> Extractor<Iterable,PTuple6<V1,V2,V3,V4,V5,V6>> toPTuple6(){
		return  ( Iterable itable)-> {
			Iterator it = itable.iterator();
			return new TupleImpl(it,6);
		};
		
	}
	/**
	 *  @return An extractor that generates a PTuple from an iterable, extracts the first 7 values to create a PTuple7
	 */
	public final static <V1,V2,V3,V4,V5,V6,V7> Extractor<Iterable,PTuple7<V1,V2,V3,V4,V5,V6,V7>> toPTuple7(){
		return  ( Iterable itable)-> {
			Iterator it = itable.iterator();
			return new TupleImpl(it,7);
		};
		
	}
	/**
	 *  @return An extractor that generates a PTuple from an iterable, extracts the first 8 values to create a PTuple8
	 */
	public final static <V1,V2,V3,V4,V5,V6,V7,V8> Extractor<Iterable,PTuple8<V1,V2,V3,V4,V5,V6,V7,V8>> toPTuple8(){
		return  ( Iterable itable)-> {
			Iterator it = itable.iterator();
			return new TupleImpl(it,8);
		};
		
	}
	
}
