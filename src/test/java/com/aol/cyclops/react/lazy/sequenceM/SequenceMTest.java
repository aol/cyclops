package com.aol.cyclops.react.lazy.sequenceM;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.react.stream.traits.LazyFutureStream;
import com.aol.cyclops.util.stream.StreamUtils;
import com.aol.cyclops.util.stream.Streamable;

public class SequenceMTest {
	@Test
	public void startsWith(){
		assertTrue(LazyFutureStream.of(1,2,3,4)
						.startsWith(Arrays.asList(1,2,3)));
	}
	@Test
	public void startsWithIterator(){
		assertTrue(LazyFutureStream.of(1,2,3,4).startsWith(Arrays.asList(1,2,3).iterator()));
	}
	@Test
	public void onEmptySwitchEmpty(){
		assertThat(LazyFutureStream.of()
							.onEmptySwitch(()->LazyFutureStream.of(1,2,3))
							.toList(),
							equalTo(Arrays.asList(1,2,3)));
				
	}
	@Test
	public void onEmptySwitch(){
		assertThat(LazyFutureStream.of(4,5,6)
							.onEmptySwitch(()->LazyFutureStream.of(1,2,3))
							.toList(),
							equalTo(Arrays.asList(4,5,6)));
				
	}
	
	@Test
	public void elapsedIsPositive(){
		
		
		assertTrue(LazyFutureStream.of(1,2,3,4,5).elapsed().noneMatch(t->t.v2<0));
	}
	@Test
	public void timeStamp(){
		
		
		assertTrue(LazyFutureStream.of(1,2,3,4,5)
							.timestamp()
							.allMatch(t-> t.v2 <= System.currentTimeMillis()));
		

	}
	@Test
	public void get0(){
		assertThat(LazyFutureStream.of(1).elementAt(0).v1,equalTo(1));
	}
	@Test
	public void getMultple(){
		assertThat(LazyFutureStream.of(1,2,3,4,5).elementAt(2).v1,equalTo(3));
	}
	@Test
	public void getMultpleStream(){
		assertThat(LazyFutureStream.of(1,2,3,4,5).elementAt(2).v2.toList(),equalTo(Arrays.asList(1,2,3,4,5)));
	}
	@Test(expected=NoSuchElementException.class)
	public void getMultiple1(){
		LazyFutureStream.of(1).elementAt(1);
	}
	@Test(expected=NoSuchElementException.class)
	public void getEmpty(){
		LazyFutureStream.of().elementAt(0);
	}
	@Test
	public void elementAt0(){
		assertTrue(LazyFutureStream.of(1).get(0).isPresent());
	}
	@Test
	public void elementAtMultple(){
		assertThat(LazyFutureStream.of(1,2,3,4,5).get(2).get(),equalTo(3));
	}
	@Test
	public void elementAt1(){
		assertFalse(LazyFutureStream.of(1).get(1).isPresent());
	}
	@Test
	public void elementAtEmpty(){
		assertFalse(LazyFutureStream.of().get(0).isPresent());
	}
	@Test
	public void singleTest(){
		assertThat(LazyFutureStream.of(1).single(),equalTo(1));
	}
	@Test(expected=UnsupportedOperationException.class)
	public void singleEmpty(){
		LazyFutureStream.of().single();
	}
	@Test(expected=UnsupportedOperationException.class)
	public void single2(){
		LazyFutureStream.of(1,2).single();
	}
	@Test
	public void limitTime(){
		List<Integer> result = LazyFutureStream.of(1,2,3,4,5,6)
										.peek(i->sleep(i*100))
										.limit(1000,TimeUnit.MILLISECONDS)
										.toList();
		
		
		assertThat(result,equalTo(Arrays.asList(1,2,3,4)));
	}
	@Test
	public void limitTimeEmpty(){
		List<Integer> result = ReactiveSeq.<Integer>of()
										.peek(i->sleep(i*100))
										.limit(1000,TimeUnit.MILLISECONDS)
										.toList();
		
		
		assertThat(result,equalTo(Arrays.asList()));
	}
	@Test
	public void skipTime(){
		List<Integer> result = LazyFutureStream.of(1,2,3,4,5,6)
										.peek(i->sleep(i*100))
										.skip(1000,TimeUnit.MILLISECONDS)
										.toList();
		
		
		assertThat(result,equalTo(Arrays.asList(4,5,6)));
	}
	@Test
	public void skipTimeEmpty(){
		List<Integer> result = ReactiveSeq.<Integer>of()
										.peek(i->sleep(i*100))
										.skip(1000,TimeUnit.MILLISECONDS)
										.toList();
		
		
		assertThat(result,equalTo(Arrays.asList()));
	}
	private int sleep(Integer i) {
		try {
			Thread.currentThread().sleep(i);
		} catch (InterruptedException e) {
			
		}
		return i;
	}
	@Test
	public void testSkipLast(){
		assertThat(LazyFutureStream.of(1,2,3,4,5)
							.skipLast(2)
							.collect(Collectors.toList()),equalTo(Arrays.asList(1,2,3)));
	}
	@Test
	public void testSkipLastEmpty(){
		assertThat(LazyFutureStream.of()
							.skipLast(2)
							.collect(Collectors.toList()),equalTo(Arrays.asList()));
	}
	@Test
	public void testLimitLast(){
		assertThat(LazyFutureStream.of(1,2,3,4,5)
							.limitLast(2)
							.collect(Collectors.toList()),equalTo(Arrays.asList(4,5)));
	}
	@Test
	public void testLimitLastEmpty(){
		assertThat(LazyFutureStream.of()
							.limitLast(2)
							.collect(Collectors.toList()),equalTo(Arrays.asList()));
	}
	@Test
	public void endsWith(){
		assertTrue(LazyFutureStream.of(1,2,3,4,5,6)
				.endsWith(Arrays.asList(5,6)));
	}
	@Test
	public void endsWithFalse(){
		assertFalse(LazyFutureStream.of(1,2,3,4,5,6)
				.endsWith(Arrays.asList(5,6,7)));
	}
	@Test
	public void endsWithToLong(){
		assertFalse(LazyFutureStream.of(1,2,3,4,5,6)
				.endsWith(Arrays.asList(0,1,2,3,4,5,6)));
	}
	@Test
	public void endsWithEmpty(){
		assertTrue(LazyFutureStream.of(1,2,3,4,5,6)
				.endsWith(Arrays.asList()));
	}
	@Test
	public void endsWithWhenEmpty(){
		assertFalse(LazyFutureStream.of()
				.endsWith(Arrays.asList(1,2,3,4,5,6)));
	}
	@Test
	public void endsWithBothEmpty(){
		assertTrue(ReactiveSeq.<Integer>of()
				.endsWith(Arrays.asList()));
	}
	@Test
	public void endsWithStream(){
		assertTrue(LazyFutureStream.of(1,2,3,4,5,6)
				.endsWith(Stream.of(5,6)));
	}
	@Test
	public void endsWithFalseStream(){
		assertFalse(LazyFutureStream.of(1,2,3,4,5,6)
				.endsWith(Stream.of(5,6,7)));
	}
	@Test
	public void endsWithToLongStream(){
		assertFalse(LazyFutureStream.of(1,2,3,4,5,6)
				.endsWith(Stream.of(0,1,2,3,4,5,6)));
	}
	@Test
	public void endsWithEmptyStream(){
		assertTrue(LazyFutureStream.of(1,2,3,4,5,6)
				.endsWith(Stream.of()));
	}
	@Test
	public void endsWithWhenEmptyStream(){
		assertFalse(LazyFutureStream.of()
				.endsWith(Stream.of(1,2,3,4,5,6)));
	}
	@Test
	public void endsWithBothEmptyStream(){
		assertTrue(ReactiveSeq.<Integer>of()
				.endsWith(Stream.of()));
	}
	@Test
	public void anyMTest(){
		List<Integer> list = LazyFutureStream.of(1,2,3,4,5,6)
								.anyM().filter(i->i>3).asSequence().toList();
		
		assertThat(list,equalTo(Arrays.asList(4,5,6)));
	}
	@Test
	public void streamable(){
		Streamable<Integer> repeat = LazyFutureStream.of(1,2,3,4,5,6)
												.map(i->i*2)
												.toStreamable();
		
		assertThat(repeat.sequenceM().toList(),equalTo(Arrays.asList(2,4,6,8,10,12)));
		assertThat(repeat.sequenceM().toList(),equalTo(Arrays.asList(2,4,6,8,10,12)));
	}
	
	@Test
	public void concurrentLazyStreamable(){
		Streamable<Integer> repeat = LazyFutureStream.of(1,2,3,4,5,6)
												.map(i->i*2)
												.toConcurrentLazyStreamable();
		
		assertThat(repeat.sequenceM().toList(),equalTo(Arrays.asList(2,4,6,8,10,12)));
		assertThat(repeat.sequenceM().toList(),equalTo(Arrays.asList(2,4,6,8,10,12)));
	}
	@Test
	public void splitBy(){
		assertThat( LazyFutureStream.of(1, 2, 3, 4, 5, 6).splitBy(i->i<4).v1.toList(),equalTo(Arrays.asList(1,2,3)));
		assertThat( LazyFutureStream.of(1, 2, 3, 4, 5, 6).splitBy(i->i<4).v2.toList(),equalTo(Arrays.asList(4,5,6)));
	}
	@Test
	public void testLazy(){
		Collection<Integer> col = LazyFutureStream.of(1,2,3,4,5)
											.peek(System.out::println)
											.toLazyCollection();
		System.out.println("first!");
		col.forEach(System.out::println);
		assertThat(col.size(),equalTo(5));
	}
	@Test
	public void testLazyCollection(){
		Collection<Integer> col = LazyFutureStream.of(1,2,3,4,5)
											.peek(System.out::println)
											.toConcurrentLazyCollection();
		System.out.println("first!");
		col.forEach(System.out::println);
		assertThat(col.size(),equalTo(5));
	}
	int peek = 0;
	@Test
	public void testPeek() {
		peek = 0 ;
		AnyM.fromStream(Stream.of(asList(1,3)))
				  				.flatMap(c->AnyM.fromStream(c.stream()))
				  				.asSequence()
				  				.map(i->i*2)
				  				.peek(i-> peek=i)
				  				.collect(Collectors.toList());
		assertThat(peek,equalTo(6));
	}
	@Test
	public void testMap() {
		  List<Integer> list = AnyM.fromStream(Stream.of(asList(1,3)))
				  				.flatMap(c->AnyM.fromStream(c.stream()))
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

		

		assertThat(LazyFutureStream.of(1, "a", 2, "b", 3, null).ofType(Integer.class).toList(),containsInAnyOrder(1, 2, 3));

		assertThat(LazyFutureStream.of(1, "a", 2, "b", 3, null).ofType(Integer.class).toList(),not(containsInAnyOrder("a", "b",null)));

		assertThat(LazyFutureStream.of(1, "a", 2, "b", 3, null)

				.ofType(Serializable.class).toList(),containsInAnyOrder(1, "a", 2, "b", 3));

	}

	@Test
	public void testCastPast() {
		LazyFutureStream.of(1, "a", 2, "b", 3, null).cast(Date.class).map(d -> d.getTime());
	



	}
	
	@Test
	public void flatMapCompletableFuture(){
		assertThat(LazyFutureStream.of(1,2,3).flatMapCompletableFuture(i->CompletableFuture.completedFuture(i+2))
				  								.collect(Collectors.toList()),
				  								equalTo(Arrays.asList(3,4,5)));
	}
	@Test
	public void flatMapOptional(){
		assertThat(LazyFutureStream.of(1,2,3,null).flatMapOptional(Optional::ofNullable)
			      										.collect(Collectors.toList()),
			      										equalTo(Arrays.asList(1,2,3)));
	}
	@Test
	public void testIntersperse() {
		
		assertThat(LazyFutureStream.of(1,2,3)
						.intersperse(0)
						.toList(),
						equalTo(Arrays.asList(1,0,2,0,3)));
	



	}
	@Test
	public void cast(){
		LazyFutureStream.of(1,2,3).cast(String.class).collect(Collectors.toList())
		.stream().map(i->i.getClass())
		.allMatch(c->Integer.class.equals(c));
	}
	@Test
	public void xMatch(){
		assertTrue(LazyFutureStream.of(1,2,3,5,6,7).xMatch(3, i-> i>4 ));
	}
	
}
