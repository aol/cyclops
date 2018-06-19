package cyclops.reactive.data.collections;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.stream.Collectors;

import cyclops.reactive.collections.immutable.PersistentSetX;
import org.junit.Test;

import cyclops.companion.Reducers;
import cyclops.reactive.ReactiveSeq;

public class PSetsTest {

	@Test
	public void testOf() {
		assertThat(PersistentSetX.of("a","b","c")
							.stream()
							.collect(Collectors.toList()),hasItems("a","b","c"));
	}

	@Test
	public void testEmpty() {
		assertThat(PersistentSetX.empty().stream()
				.collect(Collectors.toList()),equalTo(Arrays.asList()));
	}

	@Test
	public void testSingleton() {
		assertThat(PersistentSetX.of("a").stream()
				.collect(Collectors.toList()),equalTo(Arrays.asList("a")));
	}
	@Test
	public void testFromCollection() {
		assertThat(PersistentSetX.fromIterable(Arrays.asList("a","b","c")).stream()
				.collect(Collectors.toList()),hasItems("a","b","c"));
	}
	@Test
	public void testToPSetstreamOfT() {
		assertThat(PersistentSetX.fromIterable(ReactiveSeq.of("a","b","c")).stream()
				.collect(Collectors.toList()),
						hasItems("a","b","c"));
	}

	@Test
	public void testToPSets() {
		assertThat(ReactiveSeq.of("a","b","c").foldMap(Reducers.toPersistentSet()).stream()
				.collect(Collectors.toList()),
				hasItems("a","b","c"));
	}


}
