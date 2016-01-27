package com.aol.cyclops.sequence;

import static com.aol.cyclops.sequence.SequenceM.fromStream;

import java.util.stream.Stream;

import org.jooq.lambda.Seq;

public interface JoolManipulation<T> extends Seq<T>{
	
	
	default SequenceM<T> removeAll(Stream<T> stream){
		return fromStream(Seq.super.removeAll(stream));
	}
	default  SequenceM<T> removeAll(Iterable<T> it){
		return fromStream(Seq.super.removeAll(it));
	}
	default  SequenceM<T> removeAll(Seq<T> seq){
		return fromStream(Seq.super.removeAll(seq));
	}
	default  SequenceM<T> removeAll(T... values){
		return fromStream(Seq.super.removeAll(values));
		
	}
	default  SequenceM<T> retainAll(Iterable<T> it){
		return fromStream(Seq.super.retainAll(it));
	}
	default  SequenceM<T> retainAll(Seq<T> seq){
		return fromStream(Seq.super.retainAll(seq));
	}
	default  SequenceM<T> retainAll(Stream<T> stream){
		return fromStream(Seq.super.retainAll(stream));
	}
	default  SequenceM<T> retainAll(T... values){
		return fromStream(Seq.super.retainAll(values));
	}
}
