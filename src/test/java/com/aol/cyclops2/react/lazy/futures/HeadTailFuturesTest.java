package com.aol.cyclops2.react.lazy.futures;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import cyclops.stream.FutureStream;
import org.junit.Test;

import cyclops.stream.ReactiveSeq;
import com.aol.cyclops2.types.stream.HeadAndTail;

public class HeadTailFuturesTest {

	@Test
	public void headTailReplay() {

		FutureStream<String> helloWorld = FutureStream.of("hello",
				"world", "last");
		HeadAndTail<String> headAndTail = helloWorld.actOnFutures()
				.headAndTail();
		String head = headAndTail.head();
		assertThat(head, equalTo("hello"));

		ReactiveSeq<String> tail = headAndTail.tail();
		assertThat(tail.headAndTail().head(), equalTo("world"));

	}

	
}
