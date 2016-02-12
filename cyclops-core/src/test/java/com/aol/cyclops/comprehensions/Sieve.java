package com.aol.cyclops.comprehensions;

import org.junit.Test;

import com.aol.cyclops.control.SequenceM;
import com.aol.cyclops.types.stream.HeadAndTail;
import com.aol.cyclops.util.stream.Streamable;

public class Sieve {

	@Test
	public void sieveTest(){
		sieve(SequenceM.range(2, 1_000)).forEach(System.out::println);
	}
	SequenceM<Integer> from(int n) {
	    return SequenceM.iterate(n, m -> m + 1);
	}
	SequenceM<Integer> sieve(SequenceM<Integer> s){
		HeadAndTail<Integer> headAndTail = s.headAndTail();
		
		return headAndTail.headStream().appendStream(sieve(headAndTail.tail().filter(n -> n % headAndTail.head() != 0)));
		
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
