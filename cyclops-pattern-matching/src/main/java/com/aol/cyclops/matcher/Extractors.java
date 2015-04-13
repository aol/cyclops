package com.aol.cyclops.matcher;
import static java.util.Spliterator.ORDERED;
import static java.util.Spliterators.spliteratorUnknownSize;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import lombok.val;

import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;


public class Extractors {
	
	public final static <V1,V2> PatternMatcher.Extractor<Iterable,Tuple2<V1,V2>> of(int v1,int v2){
		val l1 = new Long(v1);
		val l2 = new Long(v2);
		return  ( Iterable it)-> {
		
			List l  = (List)Seq.seq(it).zipWithIndex().skip(Math.min(v1,v2))
								.limit(Math.max(v1,v2)+1)
								.filter(t -> ((Tuple2<Object,Long>)t).v2.equals(l1) || ((Tuple2<Object,Long>)t).v2.equals(l2))
								.map(t->((Tuple2)t).v1)
								.collect(Collectors.toList());
			return Tuple.tuple((V1)l.get(0),(V2)l.get(1));
			
		};
		
	}
	
	public final static <R> PatternMatcher.Extractor<Iterable<R>,R> at(int pos){
	
		return  ( Iterable<R> it)-> {
			return StreamSupport.stream(spliteratorUnknownSize(it.iterator(), ORDERED), false).skip(pos).limit(1).findFirst().get();
			
		};
		
	}
	public final static <R> PatternMatcher.Extractor<List<R>,R> get(int pos){
		return (List<R> c) ->{
			return c.get(pos);
		};
		
	}
	public final static <K,R> PatternMatcher.Extractor<Map<K,R>,R> get(K key){
		return (Map<K,R> c) ->{
			return c.get(key);
		};
		
	}
	
}
