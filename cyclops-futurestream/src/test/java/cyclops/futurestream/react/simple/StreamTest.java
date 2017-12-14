package cyclops.futurestream.react.simple;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import org.junit.Test;

import cyclops.futurestream.SimpleReact;

public class StreamTest {



	@Test
	public void testStreamFrom() throws InterruptedException,
			ExecutionException {


		List<String> strings = new SimpleReact()
								.<String>fromStream(new SimpleReact()
												.<Integer> ofAsync(() -> 1, () -> 2, () -> 3)
												.with(it -> "*" + it).stream())
								.then(it ->  it + "*")
								.block();

		assertThat(strings.size(), is(3));


		assertThat(strings,hasItem("*1*"));

	}
	@Test
	public void testStreamOf() throws InterruptedException,
			ExecutionException {

		Stream<CompletableFuture<String>> stream = new SimpleReact()
													.<Integer> ofAsync(() -> 1, () -> 2, () -> 3)
													.then(it -> "*" + it).streamCompletableFutures();

		List<String> strings = new SimpleReact()
								.<String>fromStream(stream)
								.then(it ->  it + "*")
								.block();

		assertThat(strings.size(), is(3));


		assertThat(strings,hasItem("*1*"));

	}

}
