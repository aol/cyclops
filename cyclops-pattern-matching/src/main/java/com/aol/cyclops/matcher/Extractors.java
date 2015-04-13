package com.aol.cyclops.matcher;
import static java.util.Spliterator.ORDERED;
import static java.util.Spliterators.spliteratorUnknownSize;

import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;


public class Extractors {
	
	
	
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
