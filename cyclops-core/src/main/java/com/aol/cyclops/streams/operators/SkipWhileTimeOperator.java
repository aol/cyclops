package com.aol.cyclops.streams.operators;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Stream;

import lombok.Value;

import com.aol.cyclops.sequence.SequenceM;
import com.aol.cyclops.util.StreamUtils;
@Value
public class SkipWhileTimeOperator<U> {
	Stream<U> stream;
	public  Stream<U>  skipWhile(long time, TimeUnit unit){
		long start = System.nanoTime();
		long allowed = unit.toNanos(time);
		return stream.filter(a-> System.nanoTime()-start > allowed);
		
	}
}
