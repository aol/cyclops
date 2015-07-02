package com.aol.cyclops.lambda.monads;

import static com.aol.cyclops.lambda.api.AsAnyM.anyMIterable;
import static com.aol.cyclops.lambda.monads.AnyMonads.*;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import com.aol.cyclops.streams.StreamUtils;
import com.nurkiewicz.lazyseq.LazySeq;

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
	public void headAndTailTest(){
		Stream<String> s = Stream.of("hello","world");
		Iterator<String> it = s.iterator();
		String head = it.next();
		Stream<String> tail = StreamUtils.stream(it);
		tail.forEach(System.out::println);
	}
	@Test
	public void testFlatMap() {
		System.out.println(LazySeq.of(1)
					.flatMap(i-> LazySeq.of( asList(1,3)))
					.toList());
		System.out.println(Stream.of(1)
				.flatMap(i-> Stream.of((asList(1,3))))
				.collect(Collectors.toList()));
		
		/**
		
		System.out.println( anyM(Stream.of(asList(1,3)))
				  				.asSequence().toList());
		  List<Integer> list = anyM(Stream.of(asList(1,3)))
				  				.asSequence()
				  				.flatMap(c->anyM(c.stream()).asSequence())
				  				.map(i->i*2)
				  				.peek(System.out::println)
				  				.collect(Collectors.toList());
		assertThat(Arrays.asList(2,6),equalTo(list)); **/
	}
}
