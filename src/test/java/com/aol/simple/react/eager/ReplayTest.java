package com.aol.simple.react.eager;

import org.junit.Test;

import com.aol.simple.react.stream.eager.EagerFutureStream;

public class ReplayTest {

	@Test
	public void replay() {
		EagerFutureStream<Integer> stream = EagerFutureStream.of(1, 2, 3, 4);  // <-- cached state!

		stream.map(it -> it + "*")
			   .forEach(System.out::println); //map the stream 1 time
		stream.map(it -> it + "!")
			  .forEach(System.out::println); //map the stream again & print out
	}
}
