package cyclops.futurestream.react.completablefuture;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;

import com.oath.cyclops.types.futurestream.BaseSimpleReactStream;
import org.junit.Test;

import cyclops.futurestream.SimpleReact;

public class CompletableFutureTest {
	@Test
	public void asyncEventRecieverTest() throws InterruptedException,
			ExecutionException {

		Queue<CompletableFuture<Integer>> queue = buildQueueOfAsyncEvents();



		BaseSimpleReactStream<String> convertedToStrings = new SimpleReact()
								.fromStream(queue.stream())
								.<String>then(it ->  it + "*");



		convertedToStrings.streamCompletableFutures().forEach(f -> assertFalse(f.isDone()));

		new SimpleReact(new ForkJoinPool(3)).ofAsync( ()-> 100, ()->200, ()->400).then( it-> sleep(it)).then(it -> queue.poll().complete(it));

		List<String> result = convertedToStrings.block();

		assertThat(result.size(),is(3));
		assertThat(result,hasItem("400*"));

	}
	private Queue<CompletableFuture<Integer>> buildQueueOfAsyncEvents() {
		CompletableFuture<Integer> future1 = new CompletableFuture<>();
		CompletableFuture<Integer> future2 = new CompletableFuture<>();
		CompletableFuture<Integer> future3 = new CompletableFuture<>();
		Queue<CompletableFuture<Integer>> queue = new ConcurrentLinkedQueue(Arrays.asList(future1,future2,future3));
		return queue;
	}
	private Integer sleep(Integer it) {
		try {
			Thread.currentThread().sleep(it);
		} catch (InterruptedException e) {

		}
		return it;
	}

}
