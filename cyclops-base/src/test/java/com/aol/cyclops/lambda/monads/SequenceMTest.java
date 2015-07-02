package com.aol.cyclops.lambda.monads;

import static com.aol.cyclops.lambda.api.AsAnyM.anyMIterable;
import static com.aol.cyclops.lambda.monads.AnyMonads.*;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

public class SequenceMTest {

	int peek = 0;
	@Test
	public void testPeek() {
		peek = 0 ;
		   anyM(Stream.of(asList(1,3)))
				  				.flatMap(c->anyM(c.stream()))
				  				.asSequence()
				  				.map(i->i*2)
				  				.peek(i-> peek=i)
				  				.collect(Collectors.toList());
		assertThat(peek,equalTo(6));
	}
	@Test
	public void testMap() {
		  List<Integer> list = anyM(Stream.of(asList(1,3)))
				  				.flatMap(c->anyM(c.stream()))
				  				.asSequence()
				  				.map(i->i*2)
				  				.peek(System.out::println)
				  				.collect(Collectors.toList());
		assertThat(Arrays.asList(2,6),equalTo(list));
	}
	@Test
	public void testFlatMap() {
		  List<Integer> list = anyM(Stream.of(asList(1,3)))
				  				.asSequence()
				  				.flatMap(c->anyM(c.stream()).asSequence())
				  				.map(i->i*2)
				  				.peek(System.out::println)
				  				.collect(Collectors.toList());
		assertThat(Arrays.asList(2,6),equalTo(list));
	}
}
