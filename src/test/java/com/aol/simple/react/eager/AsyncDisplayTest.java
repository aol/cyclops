package com.aol.simple.react.eager;

import java.util.stream.Stream;

import org.junit.Test;

import com.aol.simple.react.stream.traits.EagerFutureStream;
import com.aol.simple.react.stream.traits.LazyFutureStream;

public class AsyncDisplayTest {

	long mainThread = Thread.currentThread().getId();

	private String threadDetails(long threadId) {
		if (mainThread == threadId)
			return "Running on main thread :" + threadId;
		else
			return "Running on separate thread : " + threadId;
	}

	@Test
	public void test3Streams() {

		System.out.println("Main thread is : " + mainThread);

		LazyFutureStream.of(1, 2, 3, 4)
				.map(it -> Thread.currentThread().getId())
				.map(this::threadDetails)
				.peek(System.out::println)
				.runOnCurrent();
		

	}
}
