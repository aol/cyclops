package com.aol.cyclops.streams;


import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.*;

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
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import com.aol.cyclops.monad.AnyM;
import com.aol.cyclops.sequence.SequenceM;
import com.aol.cyclops.sequence.streamable.Streamable;

public class SequenceMTest {
	
	@Test
	public void subStream(){
		List<Integer> list = SequenceM.of(1,2,3,4,5,6).subStream(1,3).toList();
		assertThat(list,equalTo(Arrays.asList(2,3)));
	}
	@Test
    public void emptyPermutations() {
        assertThat(SequenceM.of().permutations().map(s->s.toList()).toList(),equalTo(Arrays.asList()));
    }

    @Test
    public void permuations3() {
    	System.out.println(SequenceM.of(1, 2, 3).permutations().map(s->s.toList()).toList());
        assertThat(SequenceM.of(1, 2, 3).permutations().map(s->s.toList()).toList(),
        		equalTo(SequenceM.of(SequenceM.of(1, 2, 3),
        		SequenceM.of(1, 3, 2), SequenceM.of(2, 1, 3), SequenceM.of(2, 3, 1), SequenceM.of(3, 1, 2), SequenceM.of(3, 2, 1)).map(s->s.toList()).toList()));
    }
    
    @Test
    public void emptyAllCombinations() {
        assertThat(SequenceM.of().combinations().map(s->s.toList()).toList(),equalTo(Arrays.asList(Arrays.asList())));
    }

    @Test
    public void allCombinations3() {
        assertThat(SequenceM.of(1, 2, 3).combinations().map(s->s.toList()).toList(),equalTo(Arrays.asList(Arrays.asList(), Arrays.asList(1), Arrays.asList(2),
        		Arrays.asList(3), Arrays.asList(1, 2), Arrays.asList(1, 3), Arrays.asList(2, 3), Arrays.asList(1, 2, 3))));
    }

  

    @Test
    public void emptyCombinations() {
        assertThat(SequenceM.of().combinations(2).toList(),equalTo(Arrays.asList()));
    }

    @Test
    public void combinations2() {
        assertThat(SequenceM.of(1, 2, 3).combinations(2).map(s->s.toList()).toList(),
                equalTo(Arrays.asList(Arrays.asList(1, 2), Arrays.asList(1, 3), Arrays.asList(2, 3))));
    }
	@Test
	public void onEmptySwitchEmpty(){
		assertThat(SequenceM.of()
							.onEmptySwitch(()->SequenceM.of(1,2,3))
							.toList(),
							equalTo(Arrays.asList(1,2,3)));
				
	}
	@Test
	public void onEmptySwitch(){
		assertThat(SequenceM.of(4,5,6)
							.onEmptySwitch(()->SequenceM.of(1,2,3))
							.toList(),
							equalTo(Arrays.asList(4,5,6)));
				
	}
	
	@Test
	public void elapsedIsPositive(){
		
		
		assertTrue(SequenceM.of(1,2,3,4,5).elapsed().noneMatch(t->t.v2<0));
	}
	@Test
	public void timeStamp(){
		
		
		assertTrue(SequenceM.of(1,2,3,4,5)
							.timestamp()
							.allMatch(t-> t.v2 <= System.currentTimeMillis()));
		

	}
	@Test
	public void get0(){
		assertThat(SequenceM.of(1).get(0).v1,equalTo(1));
	}
	@Test
	public void getMultple(){
		assertThat(SequenceM.of(1,2,3,4,5).get(2).v1,equalTo(3));
	}
	@Test
	public void getMultpleStream(){
		assertThat(SequenceM.of(1,2,3,4,5).get(2).v2.toList(),equalTo(Arrays.asList(1,2,3,4,5)));
	}
	@Test(expected=NoSuchElementException.class)
	public void getMultiple1(){
		SequenceM.of(1).get(1);
	}
	@Test(expected=NoSuchElementException.class)
	public void getEmpty(){
		SequenceM.of().get(0);
	}
	@Test
	public void elementAt0(){
		assertTrue(SequenceM.of(1).elementAt(0).isPresent());
	}
	@Test
	public void elementAtMultple(){
		assertThat(SequenceM.of(1,2,3,4,5).elementAt(2).get(),equalTo(3));
	}
	@Test
	public void elementAt1(){
		assertFalse(SequenceM.of(1).elementAt(1).isPresent());
	}
	@Test
	public void elementAtEmpty(){
		assertFalse(SequenceM.of().elementAt(0).isPresent());
	}
	@Test
	public void singleTest(){
		assertThat(SequenceM.of(1).single(),equalTo(1));
	}
	@Test(expected=UnsupportedOperationException.class)
	public void singleEmpty(){
		SequenceM.of().single();
	}
	@Test(expected=UnsupportedOperationException.class)
	public void single2(){
		SequenceM.of(1,2).single();
	}
	@Test
	public void singleOptionalTest(){
		assertThat(SequenceM.of(1).singleOptional().get(),equalTo(1));
	}
	@Test
	public void singleOptionalEmpty(){
		assertFalse(SequenceM.of().singleOptional().isPresent());
	}
	@Test
	public void singleOptonal2(){
		assertFalse(SequenceM.of(1,2).singleOptional().isPresent());
	}
	@Test
	public void limitTime(){
		List<Integer> result = SequenceM.of(1,2,3,4,5,6)
										.peek(i->sleep(i*100))
										.limit(1000,TimeUnit.MILLISECONDS)
										.toList();
		
		
		assertThat(result,equalTo(Arrays.asList(1,2,3,4)));
	}
	@Test
	public void limitTimeEmpty(){
		List<Integer> result = SequenceM.<Integer>of()
										.peek(i->sleep(i*100))
										.limit(1000,TimeUnit.MILLISECONDS)
										.toList();
		
		
		assertThat(result,equalTo(Arrays.asList()));
	}
	@Test
	public void skipTime(){
		List<Integer> result = SequenceM.of(1,2,3,4,5,6)
										.peek(i->sleep(i*100))
										.skip(1000,TimeUnit.MILLISECONDS)
										.toList();
		
		
		assertThat(result,equalTo(Arrays.asList(4,5,6)));
	}
	@Test
	public void skipTimeEmpty(){
		List<Integer> result = SequenceM.<Integer>of()
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
		assertThat(SequenceM.of(1,2,3,4,5)
							.skipLast(2)
							.collect(Collectors.toList()),equalTo(Arrays.asList(1,2,3)));
	}
	@Test
	public void testSkipLastEmpty(){
		assertThat(SequenceM.of()
							.skipLast(2)
							.collect(Collectors.toList()),equalTo(Arrays.asList()));
	}
	@Test
	public void testLimitLast(){
		assertThat(SequenceM.of(1,2,3,4,5)
							.limitLast(2)
							.collect(Collectors.toList()),equalTo(Arrays.asList(4,5)));
	}
	@Test
	public void testLimitLastEmpty(){
		assertThat(SequenceM.of()
							.limitLast(2)
							.collect(Collectors.toList()),equalTo(Arrays.asList()));
	}
	@Test
	public void endsWith(){
		assertTrue(SequenceM.of(1,2,3,4,5,6)
				.endsWith(Arrays.asList(5,6)));
	}
	@Test
	public void endsWithFalse(){
		assertFalse(SequenceM.of(1,2,3,4,5,6)
				.endsWith(Arrays.asList(5,6,7)));
	}
	@Test
	public void endsWithToLong(){
		assertFalse(SequenceM.of(1,2,3,4,5,6)
				.endsWith(Arrays.asList(0,1,2,3,4,5,6)));
	}
	@Test
	public void endsWithEmpty(){
		assertTrue(SequenceM.of(1,2,3,4,5,6)
				.endsWith(Arrays.asList()));
	}
	@Test
	public void endsWithWhenEmpty(){
		assertFalse(SequenceM.of()
				.endsWith(Arrays.asList(1,2,3,4,5,6)));
	}
	@Test
	public void endsWithBothEmpty(){
		assertTrue(SequenceM.<Integer>of()
				.endsWith(Arrays.asList()));
	}
	@Test
	public void endsWithStream(){
		assertTrue(SequenceM.of(1,2,3,4,5,6)
				.endsWith(Stream.of(5,6)));
	}
	@Test
	public void endsWithFalseStream(){
		assertFalse(SequenceM.of(1,2,3,4,5,6)
				.endsWith(Stream.of(5,6,7)));
	}
	@Test
	public void endsWithToLongStream(){
		assertFalse(SequenceM.of(1,2,3,4,5,6)
				.endsWith(Stream.of(0,1,2,3,4,5,6)));
	}
	@Test
	public void endsWithEmptyStream(){
		assertTrue(SequenceM.of(1,2,3,4,5,6)
				.endsWith(Stream.of()));
	}
	@Test
	public void endsWithWhenEmptyStream(){
		assertFalse(SequenceM.of()
				.endsWith(Stream.of(1,2,3,4,5,6)));
	}
	@Test
	public void endsWithBothEmptyStream(){
		assertTrue(SequenceM.<Integer>of()
				.endsWith(Stream.of()));
	}
	@Test
	public void anyMTest(){
		List<Integer> list = SequenceM.of(1,2,3,4,5,6)
								.anyM().filter(i->i>3).asSequence().toList();
		
		assertThat(list,equalTo(Arrays.asList(4,5,6)));
	}
	@Test
	public void streamable(){
		Streamable<Integer> repeat = SequenceM.of(1,2,3,4,5,6)
												.map(i->i*2)
												.toStreamable();
		
		assertThat(repeat.sequenceM().toList(),equalTo(Arrays.asList(2,4,6,8,10,12)));
		assertThat(repeat.sequenceM().toList(),equalTo(Arrays.asList(2,4,6,8,10,12)));
	}
	
	@Test
	public void concurrentLazyStreamable(){
		Streamable<Integer> repeat = SequenceM.of(1,2,3,4,5,6)
												.map(i->i*2)
												.toConcurrentLazyStreamable();
		
		assertThat(repeat.sequenceM().toList(),equalTo(Arrays.asList(2,4,6,8,10,12)));
		assertThat(repeat.sequenceM().toList(),equalTo(Arrays.asList(2,4,6,8,10,12)));
	}
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
		assertThat(col.size(),equalTo(5));
	}
	@Test
	public void testLazyCollection(){
		Collection<Integer> col = SequenceM.of(1,2,3,4,5)
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
	
}
