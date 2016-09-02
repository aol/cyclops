package com.aol.cyclops.comprehensions;

import org.junit.Test;

import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.Streamable;
import com.aol.cyclops.types.stream.HeadAndTail;

public class Sieve {

	@Test
	public void sieveTest(){
		sieve(ReactiveSeq.range(2, 1_000)).forEach(System.out::println);
	}
	ReactiveSeq<Integer> from(int n) {
	    return ReactiveSeq.iterate(n, m -> m + 1);
	}
	ReactiveSeq<Integer> sieve(ReactiveSeq<Integer> s){
		HeadAndTail<Integer> headAndTail = s.headAndTail();
		
		return headAndTail.isHeadPresent() ? ReactiveSeq.of() : 
							ReactiveSeq.of(headAndTail.head())
				.appendStream(sieve(headAndTail.tail().filter(n -> n % headAndTail.head() != 0)));

	}
	@Test
	public void sieveTest2(){
		sieveStreamable(Streamable.range(2, 1_000)).forEach(System.out::println);
	}
	
	Streamable<Integer> sieveStreamable(Streamable<Integer> s){
		
		return s.size()==0? Streamable.of() : Streamable.of(s.head())
	    												.appendStreamable(sieveStreamable(s.tail().filter(n -> n % s.head() != 0)));
	}
	
	

}
