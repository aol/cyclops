package com.aol.cyclops.streams.rec;

import one.util.streamex.IntStreamEx;
import one.util.streamex.StreamEx;

import org.junit.Test;

import com.aol.cyclops.sequence.SequenceM;

public class RecTest {

	@Test
	public void rec(){
		
		sieve(SequenceM.iterate(2, x -> x+1),0).forEach(System.out::println);
	}
	public static SequenceM<Integer> sieve(SequenceM<Integer> input,int depth) {
		System.out.println("depth:"+depth);
		for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
		    System.out.println(ste);
		}
	    return input.rec( ht -> 
	        sieve(ht.tail()
	        		.filter(n -> n %  ht.head() != 0),depth+1).prepend2(ht.head()));
	}
	
	@Test
	public void streamExTest(){
		sieve(StreamEx.iterate(2, x -> x+1),0).forEach(System.out::println);
	}
	public static StreamEx<Integer> sieve(StreamEx<Integer> input,int depth) {
		System.out.println("depth:"+depth);
		for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
		    System.out.println(ste);
		}
	    return input.headTail( (head,tail) -> 
	    sieve(tail.filter(n -> n %  head != 0),depth+1).prepend(head));
	}
	
}
