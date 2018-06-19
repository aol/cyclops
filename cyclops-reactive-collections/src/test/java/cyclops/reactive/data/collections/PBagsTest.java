package cyclops.reactive.data.collections;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.stream.Collectors;

import cyclops.reactive.collections.immutable.BagX;
import cyclops.data.Seq;
import org.junit.Test;

import cyclops.companion.Reducers;
import cyclops.reactive.ReactiveSeq;

public class PBagsTest {

	@Test
	public void testOf() {
		assertThat(BagX.of("a","b","c")
							.stream()
							.collect(Collectors.toList()),hasItems("a","b","c"));
	}

	@Test
	public void testEmpty() {
		assertThat(BagX.empty().stream()
				.collect(Collectors.toList()),equalTo(Arrays.asList()));
	}

	@Test
	public void testSingleton() {
		assertThat(BagX.of("a").stream()
				.collect(Collectors.toList()),equalTo(Arrays.asList("a")));
	}
	@Test
	public void testFromCollection() {
		assertThat(BagX.fromIterable(Arrays.asList("a","b","c")).stream()
				.collect(Collectors.toList()),hasItems("a","b","c"));
	}
	@Test
	public void testToPBagXtreamOfT() {
		assertThat(BagX.bagX(ReactiveSeq.of("a","b","c")).stream()
				.collect(Collectors.toList()),
						hasItems("a","b","c"));
	}

	@Test
	public void testToPBagX() {
		assertThat(ReactiveSeq.of("a","b","c").foldMap(Reducers.toPersistentBag()).stream()
				.collect(Collectors.toList()),
				hasItems("a","b","c"));
	}

	@Test
    public void testReducer(){
	    System.out.println(Reducers.<Integer>toPersistentBag().foldMap(Seq.of(1,2,3).stream()));
    }


}
