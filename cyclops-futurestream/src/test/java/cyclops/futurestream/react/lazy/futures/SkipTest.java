package cyclops.futurestream.react.lazy.futures;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.stream.Collectors;

import cyclops.futurestream.LazyReact;
import org.junit.Test;

public class SkipTest {
	@Test
	public void testSkipLast(){
		assertThat(LazyReact.sequentialBuilder().of(1,2,3,4,5)
							.actOnFutures()
							.dropRight(2)
							.collect(Collectors.toList()),equalTo(Arrays.asList(1,2,3)));
	}
	@Test
	public void testSkipLastEmpty(){
		assertThat(LazyReact.sequentialBuilder().of()
							.actOnFutures()
							.dropRight(2)
							.collect(Collectors.toList()),equalTo(Arrays.asList()));
	}
	@Test
	public void testLimitLast(){
		assertThat(LazyReact.sequentialBuilder().of(1,2,3,4,5)
							.actOnFutures()
							.takeRight(2)
							.collect(Collectors.toList()),equalTo(Arrays.asList(4,5)));
	}
	@Test
	public void testLimitLastEmpty(){
		assertThat(LazyReact.sequentialBuilder().of()
							.actOnFutures()
							.takeRight(2)
							.collect(Collectors.toList()),equalTo(Arrays.asList()));
	}
}
