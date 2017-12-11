package cyclops.futurestream.react.simple;


import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.junit.Test;

import com.oath.cyclops.async.LazyReact;
import cyclops.reactive.FutureStream;


public class LazyReactTest {

	@Test
	public void testReactListWithExtendedSuppliers() throws InterruptedException, ExecutionException {

		class DummySupplier implements Supplier<Integer> {

			private Integer i;
			public DummySupplier(Integer i) {
				this.i = i;
			}

			@Override
			public Integer get() {
				return i;
			}

		}

		DummySupplier s1 = new DummySupplier(1);
		DummySupplier s2 = new DummySupplier(2);
		DummySupplier s3 = new DummySupplier(3);

		Iterable<DummySupplier> iterable = Arrays.asList(s1, s2, s3);
		FutureStream<Integer> futures = new LazyReact()
				.<Integer> fromIterableAsync(iterable)
				.withAsync(false);

		assertThat(futures.elementAt(0).toOptional().get(), is(lessThan(99)));
	}

	@Test
	public void testFromStreamAsyncWithExtendedSuppliers() throws InterruptedException, ExecutionException {

		class DummySupplier implements Supplier<Integer> {

			private Integer i;
			public DummySupplier(Integer i) {
				this.i = i;
			}

			@Override
			public Integer get() {
				return i;
			}

		}

		DummySupplier s1 = new DummySupplier(1);
		DummySupplier s2 = new DummySupplier(2);
		DummySupplier s3 = new DummySupplier(3);

		Stream<DummySupplier> stream = Arrays.asList(s1, s2, s3).stream();
		FutureStream<Integer> futures = new LazyReact()
				.<Integer> fromStreamAsync(stream).withAsync(false);

		assertThat(futures.elementAt(0).toOptional().get(), is(lessThan(99)));
	}

	@Test
	public void testReactListFromIteratorAsync() throws InterruptedException, ExecutionException {

		class DummySupplier implements Supplier<Integer> {

			private Integer i;
			public DummySupplier(Integer i) {
				this.i = i;
			}

			@Override
			public Integer get() {
				return i;
			}

		}

		DummySupplier s1 = new DummySupplier(1);
		DummySupplier s2 = new DummySupplier(2);
		DummySupplier s3 = new DummySupplier(3);

		Iterator<DummySupplier> iterator = Arrays.asList(s1, s2, s3).iterator();
		FutureStream<Integer> futures = new LazyReact()
				.<Integer> fromIteratorAsync(iterator)
				.withAsync(false);

		assertThat(futures.elementAt(0).toOptional().get(), is(lessThan(99)));
	}
}
