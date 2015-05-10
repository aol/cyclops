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
		val list = MonadWrapper.<List<Integer>,Stream>of(Stream.of(Arrays.asList(1,3)))
				.flatMap(Collection::stream).<Stream<Integer>>get()
				.map(i->i*2)
				.peek(System.out::println)
				.collect(Collectors.toList());
		assertThat(Arrays.asList(2,6),equalTo(list));
	}
	@Test
	public void testMixed() {
		val list = MonadWrapper.<List<Integer>,Stream>of(Stream.of(Arrays.asList(1,3)))
				.bind(Optional::of).<Stream<List<Integer>>>get()
				.map(i->i.size())
				.peek(System.out::println)
				.collect(Collectors.toList());
		assertThat(Arrays.asList(2),equalTo(list));
	}
	
	

}
