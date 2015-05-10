package com.aol.cyclops.lambda.monads;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.val;
import lombok.experimental.Wither;

import org.junit.Test;
public class FunctorTest {

	@Test
	public void testFunctor() {
		val list = FunctorImpl.<Integer>of(Stream.of(1))
					.map(i->i*2)
					.peek(System.out::println).<Stream>get().collect(Collectors.toList());
		
		assertThat(Arrays.asList(2),equalTo(list));
	}

	@AllArgsConstructor
	static class FunctorImpl<T> implements Functor<T>{
		@Wither
		@Getter
		private final Object functor;
		public static <T> Functor<T> of(Object of) {
			return new FunctorImpl(of);
		}
	}

}
