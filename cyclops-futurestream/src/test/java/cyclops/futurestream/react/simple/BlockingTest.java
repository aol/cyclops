package cyclops.futurestream.react.simple;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.oath.cyclops.types.futurestream.SimpleReactStream;
import org.junit.Test;

import cyclops.futurestream.SimpleReact;

public class BlockingTest {



	@Test
	public void testBlockStreamsSeparateExecutors() throws InterruptedException,
			ExecutionException {

		Integer result = new SimpleReact()
				.<Integer> ofAsync(() -> 1, () -> 2, () -> 3)
				.then(it -> it * 200)
				.block()
				.parallelStream()
				.filter(f -> f > 300)
				.map(m -> m - 5)
				.reduce(0, (acc, next) -> acc + next);

		assertThat(result, is(990));
	}

	@Test
	public void testTypeInferencingCapture(){
		List<String> result = new SimpleReact().ofAsync(() -> "World",()-> "Hello").then( in -> "hello")
				.capture(e -> e.printStackTrace()).block();
		assertThat(result.size(),is(2));

	}
	@Test
	public void testTypeInferencingThen(){
		List<String> result = new SimpleReact().ofAsync(() -> "World",()-> "Hello").then( in -> "hello")
				.block();
		assertThat(result.size(),is(2));

	}
	@Test
	public void testTypeInferencingThenPredicate(){
		List<String> result = new SimpleReact().ofAsync(() -> "World",()-> "Hello").then( in -> "hello")
				.block(state -> state.getCompleted()>3);
		assertThat(result.size(),is(2));

	}



	@Test
	public void testBlock() throws InterruptedException, ExecutionException {

		List<String> strings = new SimpleReact()
				.<Integer> ofAsync(() -> 1, () -> 2, () -> 3)
				.then(it -> it * 100)
				.then(it -> "*" + it)
				.block();

		assertThat(strings.size(), is(3));

	}

	@Test
	public void testBlockToSet() throws InterruptedException, ExecutionException {

		Set<String> strings = new SimpleReact()
				.<Integer> ofAsync(() -> 1, () -> 1, () -> 3)
				.then(it -> it * 100)
				.then(it -> "*" + it)
				.block(Collectors.toSet());

		assertThat(strings.size(), is(2));

	}

	@Test
	public void testBreakout() throws InterruptedException, ExecutionException {
		Throwable[] error = { null };
		List<String> strings = new SimpleReact()
				.<Integer> ofAsync(() -> 1, () -> 2, () -> 3)
				.then(it -> it * 100)
				.then(it -> {
					if (it == 100)
						throw new RuntimeException("boo!");

					return it;
				})
				.onFail(e -> 1)
				.then(it -> "*" + it)
				.block(status -> status.getCompleted() > 1);

		assertThat(strings.size(), is(greaterThan(1)));

	}
	@Test
	public void testBreakoutToSet() throws InterruptedException, ExecutionException {
		Throwable[] error = { null };
		Set<String> strings = new SimpleReact()
				.<Integer> ofAsync(() -> 1, () -> 2, () -> 3)
				.then(it -> it * 100)
				.then(it -> {
					if (it == 100)
						throw new RuntimeException("boo!");

					return it;
				})
				.onFail(e -> 1)
				.then(it -> "*" + it)
				.block(Collectors.toSet(),status -> status.getCompleted() > 1);

		assertThat(strings.size(), greaterThan(1));

	}

	@Test
	public void testBreakoutException() throws InterruptedException,
			ExecutionException {
		Throwable[] error = { null };
		List<Integer> results = new SimpleReact()
				.<Integer> ofAsync(() -> 1, () -> 2, () -> 3)
				.then(it -> it * 100)
				.<Integer>then(it -> {

					throw new RuntimeException("boo!");


				}).capture(e -> error[0] = e.getCause())
				.block(status -> status.getCompleted() >= 1);

		assertThat(results.size(), is(0));
		assertThat(error[0], instanceOf(RuntimeException.class));
	}
	AtomicInteger count = new AtomicInteger(0);
	@Test
	public void testBreakoutExceptionTimes() throws InterruptedException,
			ExecutionException {
	    count = new AtomicInteger(0);
		List<Integer> results = new SimpleReact()
				.<Integer> ofAsync(() -> 1, () -> 2, () -> 3)
				.then(it -> it * 100)
				.<Integer>then(it -> {

					throw new RuntimeException("boo!");

				}).capture(e -> count.incrementAndGet())
				.block(status -> status.getCompleted() >= 1);

		assertThat(results.size(), is(0));
		assertThat(count.get(), is(3));
	}
	@Test
	public void testBreakoutAllCompleted() throws InterruptedException,
			ExecutionException {
	    count = new AtomicInteger(0);
		List<Integer> results = new SimpleReact()
				.<Integer> ofAsync(() -> 1, () -> 2, () -> 3)
				.then(it -> it * 100)
				.then(it -> {
					if(it==100)
						throw new RuntimeException("boo!");
					else
						sleep(it);
					return it;

				}).capture(e -> count.incrementAndGet())
				.block(status -> status.getAllCompleted() >0);

		assertThat(results.size(), is(0));
		assertThat(count.get(), is(1));
	}
	@Test
	public void testBreakoutAllCompletedStrings() throws InterruptedException,
			ExecutionException {
		count = new AtomicInteger(0);
		List<String> strings = new SimpleReact()
				.<Integer> ofAsync(() -> 1, () -> 2, () -> 3)
				.then(it -> it * 100)
				.then(it -> {
					if(it==100)
						throw new RuntimeException("boo!");
					else
						sleep(it);
					return it;

				})
				.then( it -> "*" + it)
				.capture(e -> count.incrementAndGet())
				.block(status -> status.getAllCompleted() >0);

		assertThat(strings.size(), is(0));
		assertThat(count.get(), is(1));
	}
	@Test
	public void testBreakoutAllCompletedAndTime() throws InterruptedException,
			ExecutionException {
	        count = new AtomicInteger(0);
			List<Integer> result = new SimpleReact()
					.<Integer> ofAsync(() -> 1, () -> 2, () -> 3)
					.then(it -> it * 100)
					.then(it -> {
						sleep(it);
						return it;

					}).capture(e -> count.incrementAndGet())
					.block(status -> status.getAllCompleted() >1 && status.getElapsedMillis()>20);

			assertThat(result.size(), is(2));
			assertThat(count.get(), is(0));
	}


	@Test
	public void testBreakoutInEffective() throws InterruptedException,
			ExecutionException {
		Throwable[] error = { null };
		List<String> strings = new SimpleReact()
				.<Integer> ofAsync(() -> 1, () -> 2, () -> 3)
				.then(it -> it * 100)
				.then(it -> {
					if (it == 100)
						throw new RuntimeException("boo!");

					return it;
				}).onFail(e -> 1)
				.then(it -> "*" + it)
				.block(status -> status.getCompleted() > 5);

		assertThat(strings.size(), is(3));

	}
	@Test
	public void testLast() throws InterruptedException, ExecutionException {

		Integer result = new SimpleReact()
		.<Integer> ofAsync(() -> 1, () -> 2, () -> 3, () -> 5)
		.then( it -> it*100)
		.then( it -> sleep(it))
		.block().takeRight(1).get(0);

		assertThat(result,is(500));
	}
	@Test
	public void testFirstSimple() throws InterruptedException, ExecutionException {

		SimpleReactStream<Integer> stage = new SimpleReact()
		.<Integer> ofAsync(() -> 1, () -> 2, () -> 3, () -> 5)
		.then( it -> it*100)
		.then( it -> sleep(it));

		int result = stage.block().firstValue(null);

		assertThat(result,is(100));

		stage.block();
	}



	@Test
	public void testFirstAllOf() throws InterruptedException, ExecutionException {

		Set<Integer> result = new SimpleReact()
		.<Integer> ofAsync(() -> 1, () -> 2, () -> 3, () -> 5)
		.then( it -> it*100)
		.<Set<Integer>,Set<Integer>>allOf(Collectors.toSet(), it -> {
			assertThat (it,is( Set.class));
			return it;
		}).block().firstValue(null);

		assertThat(result.size(),is(4));
	}
	@Test
	public void testLastAllOf() throws InterruptedException, ExecutionException {

		Set<Integer> result = new SimpleReact()
		.<Integer> ofAsync(() -> 1, () -> 2, () -> 3, () -> 5)
		.then( it -> it*100)
		.<Set<Integer>,Set<Integer>>allOf(Collectors.toSet(), it -> {
			assertThat (it,is( Set.class));
			return it;
		}).block().takeRight(1).get(0);

		assertThat(result.size(),is(4));
	}

	private Integer sleep(Integer it) {
		try {
			Thread.sleep(it);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return it;
	}
}
