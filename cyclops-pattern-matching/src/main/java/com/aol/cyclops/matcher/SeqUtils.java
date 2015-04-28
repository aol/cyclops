package com.aol.cyclops.matcher;

import java.util.Iterator;
import java.util.Map;
import java.util.stream.Stream;

import org.jooq.lambda.Seq;

public class SeqUtils {

	public static Seq<Object> seq(Object t){
		if(t instanceof Iterable){
			return Seq.seq((Iterable)t);
		}
		if(t instanceof Stream){
			return Seq.seq((Stream)t);
		}
		if(t instanceof Iterator){
			return Seq.seq((Iterator)t);
		}
		if(t instanceof Map){
			return Seq.seq((Map)t);
		}
		return Seq.of(t);
	}
}
