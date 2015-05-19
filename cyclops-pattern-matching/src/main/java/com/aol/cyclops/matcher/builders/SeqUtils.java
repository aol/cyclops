package com.aol.cyclops.matcher.builders;

import java.util.Iterator;
import java.util.Map;
import java.util.stream.Stream;

import com.nurkiewicz.lazyseq.LazySeq;

class SeqUtils {

	public final static class EMPTY { }
	private static final EMPTY EMPTY = new EMPTY();
	public static LazySeq<Object> seq(Object t){
		if(t instanceof Iterable){
			return LazySeq.of((Iterable)t).continually(EMPTY);
		}
		if(t instanceof Stream){
			return LazySeq.of((Stream)t).continually(EMPTY);
		}
		if(t instanceof Iterator){
			return LazySeq.of((Iterator)t).continually(EMPTY);
		}
		if(t instanceof Map){
			return LazySeq.of((Map)t).continually(EMPTY);
		}
		return LazySeq.of(t).continually(EMPTY);
	}
}
