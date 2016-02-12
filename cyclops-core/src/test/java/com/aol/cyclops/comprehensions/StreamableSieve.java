package com.aol.cyclops.comprehensions;

import org.junit.Test;

import com.aol.cyclops.util.stream.Streamable;

public class StreamableSieve {
	@Test
	public void sieveTest2(){
		sieve(Streamable.range(2, 1_000)).forEach(System.out::println);
	}
	
	Streamable<Integer> sieve(Streamable<Integer> s){
		
		return s.size()==0? Streamable.of() : Streamable.of(s.head())
	    												.appendStreamable(sieve(s.tail()
	    																.filter(n -> n % s.head() != 0)));
	}
}
