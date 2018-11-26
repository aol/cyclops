package cyclops.futurestream.react.lazy;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Set;
import java.util.function.Supplier;

import cyclops.futurestream.FutureStream;
import cyclops.reactive.ReactiveSeq;
import org.junit.Test;

import cyclops.futurestream.LazyReact;

public class LazySeqAutoOptimizeTest extends LazySeqTest {
	@Override
	protected <U> FutureStream<U> of(U... array) {
		return new LazyReact()
							.autoOptimizeOn()
							.of(array);
	}
	@Override
	protected <U> FutureStream<U> ofThread(U... array) {
		return new LazyReact()
							.autoOptimizeOn()
							.of(array);
	}

	@Override
	protected <U> FutureStream<U> react(Supplier<U>... array) {
		return new LazyReact().autoOptimizeOn()
								.ofAsync(array);
	}
	@Test
	public void multi(){
		Set<Long> threads = of(1,2,3,4)
								.map(i->i+2)
								.map(i->i*3)
								.peek(i-> sleep(50))
								.map(i->Thread.currentThread().getId())
								.toSet();

		assertThat(threads.size(),greaterThan(0));
	}
	@Test
	public void longRunForEach(){
		new LazyReact().autoOptimizeOn().range(0, 1_000_000)
						.map(i->i+2)
						.map(i->Thread.currentThread().getId())
					//	.peek(System.out::println)
						.forEach(a-> {});
		System.out.println("Finished!");
	}
	@Test
	public void longRun(){
		new LazyReact().autoOptimizeOn().range(0, 1_000_000)
						.map(i->i+2)
						.map(i->Thread.currentThread().getId())
					//	.peek(System.out::println)
						.runOnCurrent();
		System.out.println("Finished!");
	}
    @Override
    public void testSkipUntilWithNullsInclusive() {
        Supplier<FutureStream<Integer>> s = () -> of(1, 2, null, 3, 4, 5);

        assertTrue(s.get().dropUntilInclusive(i -> true).toList().size()==5);
    }
    @Test
    public void testSkipUntilInclusive() {
        Supplier<ReactiveSeq<Integer>> s = () -> of(1, 2, 3, 4, 5);

        assertEquals(asList(), s.get().dropUntil(i -> false).toList());
        assertTrue(s.get().dropUntilInclusive(i -> true).toList().size()==4);
    }
}
