package com.aol.cyclops.lambda.monads;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.Value;
import lombok.val;
import lombok.experimental.Wither;

import org.junit.Ignore;
import org.junit.Test;
public class MonadTest {

	@Test
	public void test() {
		val list = MonadImpl.<List<Integer>,Stream>of(Stream.of(Arrays.asList(1,3)))
				.flatMap(Collection::stream).<Stream<Integer>>get()
				.map(i->i*2)
				.peek(System.out::println)
				.collect(Collectors.toList());
		assertThat(Arrays.asList(2,6),equalTo(list));
	}
	@Test @Ignore //WIP for raw Monads
	public void testMixed() {
		val list = MonadImpl.<List<Integer>,Stream>of(Stream.of(Arrays.asList(1,3)))
				.bind(Optional::of).<Stream<Integer>>get()
				.map(i->i*2)
				.peek(System.out::println)
				.collect(Collectors.toList());
		assertThat(Arrays.asList(2,6),equalTo(list));
	}
	
	@Value
	static class MonadImpl<T,MONAD> implements Monad<T,MONAD>{
		@Wither
		private final Object monad;
		public static <T,MONAD> Monad<T,MONAD>  of(Object of) {
			return new MonadImpl(of);
			
		}
		
		
	}

}
