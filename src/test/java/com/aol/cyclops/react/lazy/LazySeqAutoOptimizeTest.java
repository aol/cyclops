package com.aol.cyclops.react.lazy;
import static org.hamcrest.Matchers.greaterThan;
import java.util.Set;
import java.util.function.Supplier;
import static org.junit.Assert.assertThat;
import org.junit.Test;

import com.aol.cyclops.control.LazyReact;
import com.aol.cyclops.react.stream.traits.LazyFutureStream;

public class LazySeqAutoOptimizeTest extends LazySeqTest {
	@Override
	protected <U> LazyFutureStream<U> of(U... array) {
		return new LazyReact()
							.autoOptimizeOn()
							.of(array);
	}
	@Override
	protected <U> LazyFutureStream<U> ofThread(U... array) {
		return new LazyReact()
							.autoOptimizeOn()
							.of(array);
	}

	@Override
	protected <U> LazyFutureStream<U> react(Supplier<U>... array) {
		return new LazyReact().autoOptimizeOn()
								.react(array);
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
}
