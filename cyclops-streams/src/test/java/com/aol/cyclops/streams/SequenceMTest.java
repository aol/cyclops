package com.aol.cyclops.streams;

import static com.aol.cyclops.lambda.api.AsAnyM.anyMIterable;
import static com.aol.cyclops.lambda.monads.AnyMonads.*;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import com.aol.cyclops.sequence.SequenceM;
import com.aol.cyclops.streams.StreamUtils;
import com.nurkiewicz.lazyseq.LazySeq;

public class SequenceMTest {

	
	@Test
	public void splitBy(){
		assertThat( SequenceM.of(1, 2, 3, 4, 5, 6).splitBy(i->i<4).v1.toList(),equalTo(Arrays.asList(1,2,3)));
		assertThat( SequenceM.of(1, 2, 3, 4, 5, 6).splitBy(i->i<4).v2.toList(),equalTo(Arrays.asList(4,5,6)));
	}
	@Test
	public void testLazy(){
		Collection<Integer> col = SequenceM.of(1,2,3,4,5)
											.peek(System.out::println)
											.toLazyCollection();
		System.out.println("first!");
		col.forEach(System.out::println);
	}
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
	public void testOfType() {

		

		assertThat(SequenceM.of(1, "a", 2, "b", 3, null).ofType(Integer.class).toList(),containsInAnyOrder(1, 2, 3));

		assertThat(SequenceM.of(1, "a", 2, "b", 3, null).ofType(Integer.class).toList(),not(containsInAnyOrder("a", "b",null)));

		assertThat(SequenceM.of(1, "a", 2, "b", 3, null)

				.ofType(Serializable.class).toList(),containsInAnyOrder(1, "a", 2, "b", 3));

	}

	@Test
	public void testCastPast() {
		SequenceM.of(1, "a", 2, "b", 3, null).cast(Date.class).map(d -> d.getTime());
	



	}
	
	@Test
	public void flatMapCompletableFuture(){
		assertThat(SequenceM.of(1,2,3).flatMapCompletableFuture(i->CompletableFuture.completedFuture(i+2))
				  								.collect(Collectors.toList()),
				  								equalTo(Arrays.asList(3,4,5)));
	}
	@Test
	public void flatMapOptional(){
		assertThat(SequenceM.of(1,2,3,null).flatMapOptional(Optional::ofNullable)
			      										.collect(Collectors.toList()),
			      										equalTo(Arrays.asList(1,2,3)));
	}
	@Test
	public void testIntersperse() {
		
		assertThat(SequenceM.of(1,2,3).intersperse(0).toList(),equalTo(Arrays.asList(1,0,2,0,3)));
	



	}
	@Test(expected=ClassCastException.class)
	public void cast(){
		SequenceM.of(1,2,3).cast(String.class).collect(Collectors.toList());
	}
	@Test
	public void xMatch(){
		assertTrue(SequenceM.of(1,2,3,5,6,7).xMatch(3, i-> i>4 ));
	}
	@Test
	public void collectIterables(){
		List result = SequenceM.of(1, 2, 3).collectIterable(
				Arrays.asList(Collectors.toList(),
						Collectors.summingInt(Integer::intValue),
						Collectors.averagingInt(Integer::intValue)));

		assertThat(result.get(0), equalTo(Arrays.asList(1, 2, 3)));
		assertThat(result.get(1), equalTo(6));
		assertThat(result.get(2), equalTo(2.0));
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
