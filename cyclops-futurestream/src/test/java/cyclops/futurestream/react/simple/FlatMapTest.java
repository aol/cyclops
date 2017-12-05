package cyclops.futurestream.react.simple;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import org.junit.Test;

import cyclops.async.SimpleReact;

public class FlatMapTest {

	@Test
	public void flatMapCf(){
		assertThat( new SimpleReact()
										.of(1,2,3)
										.flatMapToCompletableFuture(i->CompletableFuture.completedFuture(i))
										.block(),equalTo(Arrays.asList(1,2,3)));
	}
	@Test
	public void flatMapCfSync(){
		assertThat( new SimpleReact()
										.of(1,2,3)
										.sync()
										.flatMapToCompletableFuture(i->CompletableFuture.completedFuture(i))
										.block(),equalTo(Arrays.asList(1,2,3)));
	}
	@Test
	public void flatMapCfSync2(){
		assertThat( new SimpleReact()
										.of(1,2,3)
										.flatMapToCompletableFutureSync(i->CompletableFuture.completedFuture(i))
										.block(),equalTo(Arrays.asList(1,2,3)));
	}
}
