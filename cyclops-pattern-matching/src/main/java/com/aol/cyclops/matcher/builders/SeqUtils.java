package com.aol.cyclops.matcher.builders;

import java.util.Iterator;
import java.util.Map;
import java.util.stream.Stream;

import com.aol.cyclops.streams.StreamUtils;
import com.nurkiewicz.lazyseq.LazySeq;

class SeqUtils {

	public final static class EMPTY { }
	private static final EMPTY EMPTY = new EMPTY();
	public static LazySeq<Object> seq(Object t){
		return LazySeq.of(stream(t));
	}
		public static Stream<Object> stream(Object t){
		
			if(t instanceof Iterable){
				return Stream.concat(StreamUtils.stream((Iterable)t),(StreamUtils.cycle(Stream.of(EMPTY))));
			}
			if(t instanceof Stream){
				return Stream.concat( (Stream)t,(StreamUtils.cycle(Stream.of(EMPTY))));
			}
			if(t instanceof Iterator){
				return Stream.concat( StreamUtils.stream((Iterator)t),(StreamUtils.cycle(Stream.of(EMPTY))));
			}
			if(t instanceof Map){
				return Stream.concat(StreamUtils.stream((Map)t),(StreamUtils.cycle(Stream.of(EMPTY))));
			}
			return Stream.concat(Stream.of(t),(StreamUtils.cycle(Stream.of(EMPTY))));
		}
}
