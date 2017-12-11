package cyclops.futurestream.react.lazy.futures;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.oath.cyclops.async.LazyReact;
import cyclops.reactive.FutureStream;
import org.junit.Test;

import cyclops.reactive.ReactiveSeq;
import com.oath.cyclops.types.stream.HeadAndTail;

public class HeadTailFuturesTest {

	@Test
	public void headTailReplay() {

		FutureStream<String> helloWorld = LazyReact.sequentialBuilder().of("hello",
				"world", "last");
		HeadAndTail<String> headAndTail = helloWorld.actOnFutures()
				.headAndTail();
		String head = headAndTail.head();
		assertThat(head, equalTo("hello"));

		ReactiveSeq<String> tail = headAndTail.tail();
		assertThat(tail.headAndTail().head(), equalTo("world"));

	}


}
