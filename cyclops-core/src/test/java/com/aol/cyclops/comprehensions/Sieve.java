package com.aol.cyclops.comprehensions;

import java.util.Optional;

import lombok.val;

import org.junit.Test;

import com.aol.cyclops.sequence.HeadAndTail;
import com.aol.cyclops.sequence.SequenceM;
import com.aol.cyclops.sequence.streamable.Streamable;

public class Sieve {

	@Test
	public void sieveTest(){
		sieve(SequenceM.range(2, 1_000)).forEach(System.out::println);
	}
	SequenceM<Integer> from(int n) {
	    return SequenceM.iterate(n, m -> m + 1);
	}
	SequenceM<Integer> sieve(SequenceM<Integer> s){
		Optional<HeadAndTail<Integer>> headAndTail = s.headAndTailOptional();
		
		return headAndTail.map(ht ->SequenceM.of(ht.head())
	    						.appendStream(sieve(ht.tail().filter(n -> n % ht.head() != 0))))
	    				.orElse(SequenceM.of());
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
