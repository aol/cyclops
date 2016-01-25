package com.aol.cyclops.javaslang.reactivestreams.reactivestream.tests;


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
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import com.aol.cyclops.javaslang.reactivestreams.ReactiveStream;
import com.aol.cyclops.monad.AnyM;
import com.aol.cyclops.sequence.SequenceM;
import com.aol.cyclops.sequence.streamable.Streamable;

public class SequenceMTest {
	
	@Test
	public void subStream(){
		List<Integer> list = ReactiveStream.of(1,2,3,4,5,6)
											.subStream(1,3)
											.toJavaList();
		assertThat(list,equalTo(Arrays.asList(2,3)));
	}
	@Test
    public void emptyPermutations() {
        assertThat(ReactiveStream.of().permutations().map(s->s.toJavaList()).toJavaList(),equalTo(Arrays.asList()));
    }

    @Test
    public void permuations3() {
    	System.out.println(ReactiveStream.of(1, 2, 3).permutations().map(s->s.toList()).toList());
        assertThat(ReactiveStream.of(1, 2, 3).permutations().map(s->s.toList()).toList(),
        		equalTo(ReactiveStream.of(ReactiveStream.of(1, 2, 3),
        		ReactiveStream.of(1, 3, 2), ReactiveStream.of(2, 1, 3), ReactiveStream.of(2, 3, 1), ReactiveStream.of(3, 1, 2), ReactiveStream.of(3, 2, 1)).map(s->s.toList()).toList()));
    }
    
    @Test
    public void emptyAllCombinations() {
        assertThat(ReactiveStream.of().combinations().map(s->s.toJavaList()).toJavaList(),equalTo(Arrays.asList(Arrays.asList())));
    }

    @Test
    public void allCombinations3() {
        assertThat(ReactiveStream.of(1, 2, 3).combinations().map(s->s.toJavaList()).toJavaList(),equalTo(Arrays.asList(Arrays.asList(), Arrays.asList(1), Arrays.asList(2),
        		Arrays.asList(3), Arrays.asList(1, 2), Arrays.asList(1, 3), Arrays.asList(2, 3), Arrays.asList(1, 2, 3))));
    }

  

    @Test
    public void emptyCombinations() {
        assertThat(ReactiveStream.of().combinations(2).toJavaList(),equalTo(Arrays.asList()));
    }

    @Test
    public void combinations2() {
        assertThat(ReactiveStream.of(1, 2, 3).combinations(2).map(s->s.toJavaList()).toJavaList(),
                equalTo(Arrays.asList(Arrays.asList(1, 2), Arrays.asList(1, 3), Arrays.asList(2, 3))));
    }
	@Test
	public void onEmptySwitchEmpty(){
		assertThat(ReactiveStream.of()
							.onEmptySwitch(()->ReactiveStream.of(1,2,3))
							.toJavaList(),
							equalTo(Arrays.asList(1,2,3)));
				
	}
	@Test
	public void onEmptySwitch(){
		assertThat(ReactiveStream.of(4,5,6)
							.onEmptySwitch(()->ReactiveStream.of(1,2,3))
							.toJavaList(),
							equalTo(Arrays.asList(4,5,6)));
				
	}
	
	@Test
	public void elapsedIsPositive(){
		
		
		assertTrue(ReactiveStream.of(1,2,3,4,5).elapsed().seq().noneMatch(t->t._2<0));
	}
	@Test
	public void timeStamp(){
		
		
		assertTrue(ReactiveStream.of(1,2,3,4,5)
							.timestamp()
							.seq().allMatch(t-> t._2 <= System.currentTimeMillis()));
		

	}
	

	@Test
	public void singleTest(){
		assertThat(ReactiveStream.of(1).single(),equalTo(1));
	}
	@Test(expected=UnsupportedOperationException.class)
	public void singleEmpty(){
		ReactiveStream.of().single();
	}
	@Test(expected=UnsupportedOperationException.class)
	public void single2(){
		ReactiveStream.of(1,2).single();
	}
	@Test
	public void singleOptionalTest(){
		assertThat(ReactiveStream.of(1).singleOption().get(),equalTo(1));
	}
	@Test
	public void singleOptionalEmpty(){
		assertFalse(ReactiveStream.of().singleOption().isDefined());
	}
	@Test
	public void singleOptonal2(){
		assertFalse(ReactiveStream.of(1,2).singleOption().isDefined());
	}
	@Test
	public void limitTime(){
		List<Integer> result = ReactiveStream.of(1,2,3,4,5,6)
										.peek(i->sleep(i*100))
										.take(1000,TimeUnit.MILLISECONDS)
										.toJavaList();
		
		
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
		List<Integer> result = ReactiveStream.of(1,2,3,4,5,6)
										.peek(i->sleep(i*100))
										.drop(1000,TimeUnit.MILLISECONDS)
										.toJavaList();
		
		
		assertThat(result,equalTo(Arrays.asList(4,5,6)));
	}
	@Test
	public void skipTimeEmpty(){
		List<Integer> result = ReactiveStream.<Integer>of()
										.peek(i->sleep(i*100))
										.drop(1000,TimeUnit.MILLISECONDS)
										.toJavaList();
		
		
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
		assertThat(ReactiveStream.of(1,2,3,4,5)
							.dropRight(2)
							.collect(Collectors.toList()),equalTo(Arrays.asList(1,2,3)));
	}
	@Test
	public void testSkipLastEmpty(){
		assertThat(ReactiveStream.of()
							.dropRight(2)
							.collect(Collectors.toList()),equalTo(Arrays.asList()));
	}
	@Test
	public void testLimitLast(){
		assertThat(ReactiveStream.of(1,2,3,4,5)
							.takeRight(2)
							.collect(Collectors.toList()),equalTo(Arrays.asList(4,5)));
	}
	@Test
	public void testLimitLastEmpty(){
		assertThat(ReactiveStream.of()
							.takeRight(2)
							.collect(Collectors.toList()),equalTo(Arrays.asList()));
	}
	@Test
	public void endsWith(){
		assertTrue(ReactiveStream.of(1,2,3,4,5,6)
				.endsWith(ReactiveStream.of(5,6)));
	}
	@Test
	public void endsWithFalse(){
		assertFalse(ReactiveStream.of(1,2,3,4,5,6)
				.endsWith(javaslang.collection.List.of(5,6,7)));
	}
	@Test
	public void endsWithToLong(){
		assertFalse(ReactiveStream.of(1,2,3,4,5,6)
				.endsWith(ReactiveStream.of(0,1,2,3,4,5,6)));
	}
	@Test
	public void endsWithEmpty(){
		assertTrue(ReactiveStream.of(1,2,3,4,5,6)
				.endsWith(ReactiveStream.empty()));
	}
	@Test
	public void endsWithWhenEmpty(){
		assertFalse(ReactiveStream.of()
				.endsWith(ReactiveStream.of(1,2,3,4,5,6)));
	}
	@Test
	public void endsWithBothEmpty(){
		assertTrue(SequenceM.<Integer>of()
				.endsWith(ReactiveStream.empty()));
	}
	@Test
	public void endsWithStream(){
		assertTrue(ReactiveStream.of(1,2,3,4,5,6)
				.endsWith(ReactiveStream.of(5,6)));
	}
	@Test
	public void endsWithFalseStream(){
		assertFalse(ReactiveStream.of(1,2,3,4,5,6)
				.endsWith(ReactiveStream.of(5,6,7)));
	}
	@Test
	public void endsWithToLongStream(){
		assertFalse(ReactiveStream.of(1,2,3,4,5,6)
				.endsWith(ReactiveStream.of(0,1,2,3,4,5,6)));
	}
	@Test
	public void endsWithEmptyStream(){
		assertTrue(ReactiveStream.of(1,2,3,4,5,6)
				.endsWith(ReactiveStream.of()));
	}
	@Test
	public void endsWithWhenEmptyStream(){
		assertFalse(ReactiveStream.of()
				.endsWith(ReactiveStream.of(1,2,3,4,5,6)));
	}
	@Test
	public void endsWithBothEmptyStream(){
		assertTrue(ReactiveStream.<Integer>of()
				.endsWith(ReactiveStream.of()));
	}
	@Test
	public void anyMTest(){
		List<Integer> list = ReactiveStream.of(1,2,3,4,5,6)
								.anyM().filter(i->i>3).asSequence().toList();
		
		assertThat(list,equalTo(Arrays.asList(4,5,6)));
	}
	@Test
	public void streamable(){
		Streamable<Integer> repeat = ReactiveStream.of(1,2,3,4,5,6)
												.map(i->i*2)
												.streamable();
		
		assertThat(repeat.sequenceM().toList(),equalTo(Arrays.asList(2,4,6,8,10,12)));
		assertThat(repeat.sequenceM().toList(),equalTo(Arrays.asList(2,4,6,8,10,12)));
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
	public void testOfType() {

		ReactiveStream.range(0, 100_000_000)
					.ofType(Integer.class)
					.forEach(System.out::println);

		assertThat(ReactiveStream.of(1, "a", 2, "b", 3, null).ofType(Integer.class).toList(),containsInAnyOrder(1, 2, 3));

		assertThat(ReactiveStream.of(1, "a", 2, "b", 3, null).ofType(Integer.class).toList(),not(containsInAnyOrder("a", "b",null)));

		assertThat(ReactiveStream.of(1, "a", 2, "b", 3, null)

				.ofType(Serializable.class).toList(),containsInAnyOrder(1, "a", 2, "b", 3));

	}

	@Test(expected =ClassCastException.class)
	public void testCastPast() {
		ReactiveStream.of(1, "a", 2, "b", 3, null)
						.cast(Date.class)
						.map(d -> d.getTime())
						.forEach(System.out::println);
	



	}
	
	
	@Test
	public void testIntersperse() {
		
		assertThat(ReactiveStream.of(1,2,3).intersperse(0).toJavaList(),equalTo(Arrays.asList(1,0,2,0,3)));
	



	}
	@Test(expected=ClassCastException.class)
	public void cast(){
		ReactiveStream.of(1,2,3).cast(String.class).collect(Collectors.toList());
	}
	@Test
	public void xMatch(){
		assertTrue(ReactiveStream.of(1,2,3,5,6,7).xMatch(3, i-> i>4 ));
	}
	
	
	
}
