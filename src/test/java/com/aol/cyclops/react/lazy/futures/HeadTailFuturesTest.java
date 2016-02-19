package com.aol.cyclops.react.lazy.futures;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.junit.Test;

import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.types.futurestream.LazyFutureStream;
import com.aol.cyclops.types.stream.HeadAndTail;

public class HeadTailFuturesTest {

	@Test
	public void headTailReplay() {

		LazyFutureStream<String> helloWorld = LazyFutureStream.of("hello",
				"world", "last");
		HeadAndTail<String> headAndTail = helloWorld.actOnFutures()
				.headAndTail();
		String head = headAndTail.head();
		assertThat(head, equalTo("hello"));

		ReactiveSeq<String> tail = headAndTail.tail();
		assertThat(tail.headAndTail().head(), equalTo("world"));

	}

	
}
