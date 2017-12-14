package cyclops.futurestream.react.simple;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;

import org.junit.Test;

import cyclops.futurestream.SimpleReact;

public class PeekTest {

	@Test
	public void testPeek() throws InterruptedException,
			ExecutionException {
		Queue<String> peeked = new  ConcurrentLinkedQueue<String>();
		List<String> strings = new SimpleReact()
				.<Integer> ofAsync(() -> 1, () -> 2, () -> 3)
				.then(it -> it * 100)
				.<String>then(it -> "*" + it)
				.peek((String it) -> peeked.add(it))
				.block();

		assertThat(peeked.size(), is(strings.size()));


		assertThat(strings,hasItem(peeked.peek()));

	}

	@Test
	public void testPeekFirst() throws InterruptedException,
			ExecutionException {
		Queue<Integer> peeked = new  ConcurrentLinkedQueue<Integer>();
		List<String> strings = new SimpleReact()
				.<Integer> ofAsync(() -> 1, () -> 2, () -> 3)
				.peek(it -> peeked.add(it))
				.then(it -> it * 100)
				.then(it -> "*" + it)
				.block();

		assertThat(peeked.size(), is(strings.size()));



	}
	@Test
	public void testPeekMultiple() throws InterruptedException,
			ExecutionException {
		Queue<Integer> peeked = new  ConcurrentLinkedQueue<Integer>();
		List<String> strings = new SimpleReact()
				.<Integer> ofAsync(() -> 1, () -> 2, () -> 3)
				.peek(it -> peeked.add(it))
				.peek(it -> peeked.add(it))
				.then(it -> it * 100)
				.then(it -> "*" + it)
				.block();

		assertThat(peeked.size(), is(strings.size()*2));



	}
}
