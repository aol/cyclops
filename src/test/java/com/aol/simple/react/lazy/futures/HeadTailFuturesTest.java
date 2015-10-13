package com.aol.simple.react.lazy.futures;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.junit.Test;

import com.aol.cyclops.sequence.HeadAndTail;
import com.aol.cyclops.sequence.SequenceM;
import com.aol.simple.react.stream.traits.LazyFutureStream;

public class HeadTailFuturesTest {

	@Test
	public void headTailReplay() {

		LazyFutureStream<String> helloWorld = LazyFutureStream.of("hello",
				"world", "last");
		HeadAndTail<String> headAndTail = helloWorld.actOnFutures()
				.headAndTail();
		String head = headAndTail.head();
		assertThat(head, equalTo("hello"));

		SequenceM<String> tail = headAndTail.tail();
		assertThat(tail.headAndTail().head(), equalTo("world"));

	}

	@Test
	public void headTailOptional() {

		LazyFutureStream<String> helloWorld = LazyFutureStream.of();
		Optional<HeadAndTail<String>> headAndTail = helloWorld.actOnFutures()
				.headAndTailOptional();
		assertTrue(!headAndTail.isPresent());

	}
}
