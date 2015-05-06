package com.aol.cyclops.matcher.builders;

import java.util.Iterator;
import java.util.Map;
import java.util.stream.Stream;

import org.jooq.lambda.Seq;

class SeqUtils {

	public final static class EMPTY { }
	private static final EMPTY EMPTY = new EMPTY();
	public static Seq<Object> seq(Object t){
		if(t instanceof Iterable){
			return Seq.seq((Iterable)t).concat(Seq.cycle(Stream.of(EMPTY)));
		}
		if(t instanceof Stream){
			return Seq.seq((Stream)t).concat(Seq.cycle(Stream.of(EMPTY)));
		}
		if(t instanceof Iterator){
			return Seq.seq((Iterator)t).concat(Seq.cycle(Stream.of(EMPTY)));
		}
		if(t instanceof Map){
			return Seq.seq((Map)t).concat(Seq.cycle(Stream.of(EMPTY)));
		}
		return Seq.of(t).concat(Seq.cycle(Stream.of(EMPTY)));
	}
}
