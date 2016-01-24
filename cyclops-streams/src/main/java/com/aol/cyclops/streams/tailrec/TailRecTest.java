package com.aol.cyclops.streams.tailrec;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Spliterator;

import org.junit.Test;

import com.aol.cyclops.streams.StreamUtils;

public class TailRecTest {
	@Test
	public void tailRecTest(){
		sieve(TailRec.iterate(2, x -> x+1),0).forEach(System.out::println);
	}
	public static TailRec<Integer> sieve(TailRec<Integer> input,int depth) {
		System.out.println("depth:"+depth);
	/**	for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
		    System.out.println(ste);
		}**/
	    return input.headTail( (head,tail) -> 
	    		sieve(tail.filter(n -> n %  head != 0),depth+1).tailRec().prepend(head));
	}
	
	@Test
	public void simple(){
		Iterator<Integer> it = Arrays.asList(1,2,3,4).iterator();
		System.out.println(StreamUtils.stream(it).iterator() == it);
	}
	@Test
	public void simple2(){
		Spliterator<Integer> it = Arrays.asList(1,2,3,4).stream().spliterator();
		System.out.println(StreamUtils.stream(it).spliterator() == it);
	}
}
