package cyclops.reactive.data.collections;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.stream.Collectors;

import cyclops.reactive.collections.immutable.PersistentQueueX;
import org.junit.Test;

import cyclops.companion.Reducers;
import cyclops.reactive.ReactiveSeq;

public class PQueuesTest {

	@Test
	public void testOf() {
		assertThat(PersistentQueueX.of("a","b","c")
							.stream()
							.collect(Collectors.toList()),equalTo(Arrays.asList("a","b","c")));
	}

	@Test
	public void testEmpty() {
		assertThat(PersistentQueueX.empty().stream()
				.collect(Collectors.toList()),equalTo(Arrays.asList()));
	}

	@Test
	public void testSingleton() {
		assertThat(PersistentQueueX.of("a").stream()
				.collect(Collectors.toList()),equalTo(Arrays.asList("a")));
	}
	@Test
	public void testFromCollection() {
		assertThat(PersistentQueueX.fromIterable(Arrays.asList("a","b","c")).stream()
				.collect(Collectors.toList()),equalTo(Arrays.asList("a","b","c")));
	}
	@Test
	public void testToPStackstreamOfT() {
		assertThat(PersistentQueueX.fromIterable(ReactiveSeq.of("a","b","c")).stream()
				.collect(Collectors.toList()),
						equalTo(Arrays.asList("a","b","c")));
	}

	@Test
	public void testToPStack() {
		assertThat(ReactiveSeq.of("a","b","c").foldMap(Reducers.toPersistentQueue()).stream()
				.collect(Collectors.toList()),
				equalTo(Arrays.asList("a","b","c")));
	}


}
