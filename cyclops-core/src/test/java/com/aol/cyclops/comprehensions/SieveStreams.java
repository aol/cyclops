package com.aol.cyclops.comprehensions;

import static com.aol.cyclops.streams.StreamUtils.headAndTailOptional;

import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.Test;

import com.aol.cyclops.sequence.HeadAndTail;

public class SieveStreams {

	@Test
	public void sieveTest(){
		sieve(IntStream.range(2, 1_000).boxed()).forEach(System.out::println);
	}
	
	Stream<Integer> sieve(Stream<Integer> s){
		Optional<HeadAndTail<Integer>> headAndTail = headAndTailOptional(s);
		
		return headAndTail.map(ht ->Stream.concat(Stream.of(ht.head())
	    						,sieve(ht.tail().filter(n -> n % ht.head() != 0))))
	    				.orElse(Stream.of());
	}
	
	
	

}
