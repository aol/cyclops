package cyclops.futurestream.react.simple;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import cyclops.data.ImmutableList;
import org.junit.Test;

import cyclops.futurestream.SimpleReact;



public class ResultCollectionTest {



	@Test
	public void testBlock() throws InterruptedException, ExecutionException {


        ImmutableList<String> strings = new SimpleReact()
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
        ImmutableList<String> strings = new SimpleReact()
				.<Integer> ofAsync(() -> 1, () -> 2, () -> 3)
				.then(it -> it * 100)
				.then(it -> {
					if (it == 100)
						throw new RuntimeException("boo!");

					return it;
				}).onFail(e -> 1).then((it) -> "*" + it)
				.block(status -> status.getCompleted() > 1);

		assertThat(strings.size(), greaterThan(1));

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
				}).onFail(e -> 1).then((it) -> "*" + it)

				.block(Collectors.toSet(),status -> status.getCompleted() > 1);

		assertThat(strings.size(), is(2));

	}

	@Test
	public void testBreakoutException() throws InterruptedException,
			ExecutionException {
		Throwable[] error = { null };
        ImmutableList<String> strings = new SimpleReact()
				.<Integer> ofAsync(() -> 1, () -> 2, () -> 3)
				.then(it -> it * 100)
				.<String>then(it -> {

					throw new RuntimeException("boo!");

				}).capture(e -> error[0] = e)

				.block(status -> status.getCompleted() >= 1);

		assertThat(strings.size(), is(0));
		assertThat(error[0], instanceOf(RuntimeException.class));
	}
	volatile int count =0;
	@Test
	public void testBreakoutExceptionTimes() throws InterruptedException,
			ExecutionException {
		count =0;
        ImmutableList<String> strings = new SimpleReact()
				.<Integer> ofAsync(() -> 1, () -> 2, () -> 3)
				.then(it -> it * 100)
				.<String>then(it -> {

					throw new RuntimeException("boo!");

				}).capture(e -> count++)

				.block(status -> status.getCompleted() >= 1);


		assertThat(strings.size(), is(0));
		assertThat(count, is(3));
	}
	@Test
	public void testBreakoutAllCompleted() throws InterruptedException,
			ExecutionException {
		count =0;
        ImmutableList<Integer> results = new SimpleReact()
				.<Integer> ofAsync(() -> 1, () -> 2, () -> 3)
				.then(it -> it * 100)
				.then(it -> {
					if(it==100)
						throw new RuntimeException("boo!");
					else
						sleep(it);
					return it;

				}).capture(e -> count++)
				.block(status -> status.getAllCompleted() >0);

		assertThat(results.size(), is(0));
		assertThat(count, is(1));
	}
	@Test
	public void testBreakoutAllCompletedAndTime() throws InterruptedException,
			ExecutionException {
		count =0;
        ImmutableList<Integer> results = new SimpleReact()
				.<Integer> ofAsync(() -> 1, () -> 2, () -> 3)
				.then(it -> it * 100)
				.then(it -> {
					sleep(it);
					return it;

				}).capture(e -> count++)
				.block(status -> status.getAllCompleted() >1 && status.getElapsedMillis()>200);

		assertThat(results.size(), greaterThan(1));
		assertThat(count, is(0));
	}


	@Test
	public void testBreakoutInEffective() throws InterruptedException,
			ExecutionException {
		Throwable[] error = { null };
        ImmutableList<String> strings = new SimpleReact()
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

	private Object sleep(Integer it) {
		try {
			Thread.sleep(it);
		} catch (InterruptedException e) {

			e.printStackTrace();
		}
		return it;
	}
}
