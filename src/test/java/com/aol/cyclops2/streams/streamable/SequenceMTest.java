package com.aol.cyclops2.streams.streamable;


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
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cyclops.companion.Streams;
import cyclops.control.Option;
import org.junit.Test;

import cyclops.monads.AnyM;
import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.Streamable;

public class SequenceMTest {
	
	@Test
    public void emptyAllPermutations() {
        assertThat(Streamable.of().permutations().map(s->s.toList()).toList(),equalTo(Arrays.asList()));
    }

    @Test
    public void allPermutations3() {
    	System.out.println(Streamable.of(1, 2, 3).permutations().map(s->s.toList()).toList());
        assertThat(Streamable.of(1, 2, 3).permutations().map(s->s.toList()).toList(),
        		equalTo(Streamable.of(Streamable.of(1, 2, 3),
        		Streamable.of(1, 3, 2), Streamable.of(2, 1, 3), Streamable.of(2, 3, 1), Streamable.of(3, 1, 2), Streamable.of(3, 2, 1)).map(s->s.toList()).toList()));
    }
    

    

  

    @Test
    public void emptyCombinations() {
        assertThat(Streamable.of().combinations(2).toList(),equalTo(Arrays.asList()));
    }

    @Test
    public void combinations2() {
        assertThat(Streamable.of(1, 2, 3).combinations(2).map(s->s.toList()).toList(),
                equalTo(Arrays.asList(Arrays.asList(1, 2), Arrays.asList(1, 3), Arrays.asList(2, 3))));
    }
	@Test
	public void onEmpty(){
		assertThat(Streamable.of()
						.onEmpty(1)
						.toList(),
				equalTo(Arrays.asList(1)));

	}
	@Test
	public void onEmptySwitchEmpty(){
		assertThat(Streamable.of()
							.onEmptySwitch(()->Streamable.of(1,2,3))
							.toList(),
							equalTo(Arrays.asList(1,2,3)));
				
	}
	@Test
	public void onEmptySwitch(){
		assertThat(Streamable.of(4,5,6)
							.onEmptySwitch(()->Streamable.of(1,2,3))
							.toList(),
							equalTo(Arrays.asList(4,5,6)));
				
	}
	
	@Test
	public void elapsedIsPositive(){
		
		
		assertTrue(Streamable.of(1,2,3,4,5).elapsed().noneMatch(t->t._2()<0));
	}
	@Test
	public void timeStamp(){
		
		
		assertTrue(Streamable.of(1,2,3,4,5)
							.timestamp()
							.allMatch(t-> t._2() <= System.currentTimeMillis()));
		

	}
	@Test
	public void elementAt0(){
		assertThat(Streamable.of(1).elementAt(0).toOptional().get(),equalTo(1));
	}
	@Test
	public void elementAtMultple(){
		assertThat(Streamable.of(1,2,3,4,5).elementAt(2),equalTo(Option.some(3)));
	}
	
	@Test
	public void elementAtMultiple1(){
		assertThat(Streamable.of(1).elementAt(1),equalTo(Option.none()));
	}
	@Test
	public void elementAtEmpty(){
		assertThat(Streamable.of().elementAt(0),equalTo(Option.none()));
	}
	@Test
	public void get0(){
		assertTrue(Streamable.of(1).elementAt(0).isPresent());
	}
	@Test
	public void getMultple(){
		assertThat(Streamable.of(1,2,3,4,5).elementAt(2).toOptional().get(),equalTo(3));
	}
	@Test
	public void getAt1(){
		assertFalse(Streamable.of(1).elementAt(1).isPresent());
	}
	@Test
	public void getEmpty(){
		assertFalse(Streamable.of().elementAt(0).isPresent());
	}
	@Test
	public void singleTest(){
		assertThat(Streamable.of(1).singleOrElse(-1),equalTo(1));
	}
	@Test
	public void singleEmpty(){
		assertThat(Streamable.of().singleOrElse(null),equalTo(null));
	}
	@Test
	public void single2(){
		assertThat(Streamable.of(1,2).singleOrElse(null),equalTo(null));
	}
	@Test
	public void limitTime(){
		List<Integer> result = Streamable.of(1,2,3,4,5,6)
										.peek(i->sleep(i*100))
										.limit(1000,TimeUnit.MILLISECONDS)
										.toList();
		
		
		assertThat(result,equalTo(Arrays.asList(1,2,3)));
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
		List<Integer> result = Streamable.of(1,2,3,4,5,6)
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
		assertThat(Streamable.of(1,2,3,4,5)
							.skipLast(2)
							.collect(Collectors.toList()),equalTo(Arrays.asList(1,2,3)));
	}
	@Test
	public void testSkipLastEmpty(){
		assertThat(Streamable.of()
							.skipLast(2)
							.collect(Collectors.toList()),equalTo(Arrays.asList()));
	}
	@Test
	public void testLimitLast(){
		assertThat(Streamable.of(1,2,3,4,5)
							.limitLast(2)
							.collect(Collectors.toList()),equalTo(Arrays.asList(4,5)));
	}
	@Test
	public void testLimitLastEmpty(){
		assertThat(Streamable.of()
							.limitLast(2)
							.collect(Collectors.toList()),equalTo(Arrays.asList()));
	}
	@Test
	public void endsWith(){
		assertTrue(Streamable.of(1,2,3,4,5,6)
				.endsWithIterable(Arrays.asList(5,6)));
	}
	@Test
	public void endsWithFalse(){
		assertFalse(Streamable.of(1,2,3,4,5,6)
				.endsWithIterable(Arrays.asList(5,6,7)));
	}
	@Test
	public void endsWithToLong(){
		assertFalse(Streamable.of(1,2,3,4,5,6)
				.endsWithIterable(Arrays.asList(0,1,2,3,4,5,6)));
	}
	@Test
	public void endsWithEmpty(){
		assertTrue(Streamable.of(1,2,3,4,5,6)
				.endsWithIterable(Arrays.asList()));
	}
	@Test
	public void endsWithWhenEmpty(){
		assertFalse(Streamable.of()
				.endsWithIterable(Arrays.asList(1,2,3,4,5,6)));
	}
	@Test
	public void endsWithBothEmpty(){
		assertTrue(ReactiveSeq.<Integer>of()
				.endsWithIterable(Arrays.asList()));
	}
	@Test
	public void endsWithStream(){
		assertTrue(Streamable.of(1,2,3,4,5,6)
				.endsWith(Streamable.of(5,6)));
	}
	@Test
	public void endsWithFalseStream(){
		assertFalse(Streamable.of(1,2,3,4,5,6)
				.endsWith(Streamable.of(5,6,7)));
	}
	@Test
	public void endsWithToLongStream(){
		assertFalse(Streamable.of(1,2,3,4,5,6)
				.endsWith(Streamable.of(0,1,2,3,4,5,6)));
	}
	@Test
	public void endsWithEmptyStream(){
		assertTrue(Streamable.of(1,2,3,4,5,6)
				.endsWith(Streamable.of()));
	}
	@Test
	public void endsWithWhenEmptyStream(){
		assertFalse(Streamable.of()
				.endsWith(Streamable.of(1,2,3,4,5,6)));
	}
	@Test
	public void endsWithBothEmptyStream(){
		assertTrue(ReactiveSeq.<Integer>of()
				.endsWith(Stream.of()));
	}
	@Test
	public void anyMTest(){
		List<Integer> list = Streamable.of(1,2,3,4,5,6)
								.anyM().filter(i->i>3).stream().toList();
		
		assertThat(list,equalTo(Arrays.asList(4,5,6)));
	}
	
	@Test
	public void splitBy(){
		assertThat( Streamable.of(1, 2, 3, 4, 5, 6).splitBy(i->i<4)._1().toList(),equalTo(Arrays.asList(1,2,3)));
		assertThat( Streamable.of(1, 2, 3, 4, 5, 6).splitBy(i->i<4)._2().toList(),equalTo(Arrays.asList(4,5,6)));
	}
	@Test
	public void testLazy(){
		Collection<Integer> col = Streamable.of(1,2,3,4,5)
											.peek(System.out::println).to()
											.lazyCollection();
		System.out.println("takeOne!");
		col.forEach(System.out::println);
		assertThat(col.size(),equalTo(5));
	}
	@Test
	public void testLazyCollection(){
		Collection<Integer> col = Streamable.of(1,2,3,4,5)
											.peek(System.out::println).to()
											.lazyCollectionSynchronized();
		System.out.println("takeOne!");
		col.forEach(System.out::println);
		assertThat(col.size(),equalTo(5));
	}
	int peek = 0;
	@Test
	public void testPeek() {
		peek = 0 ;
		   AnyM.fromStream(Stream.of(asList(1,3)))
				  				.flatMap(c->AnyM.fromStream(c.stream()))
				  				.stream()
				  				.map(i->i*2)
				  				.peek(i-> peek=i)
				  				.collect(Collectors.toList());
		assertThat(peek,equalTo(6));
	}
	@Test
	public void testMap() {
		  List<Integer> list = AnyM.fromStream(Stream.of(asList(1,3)))
				  				.flatMap(c->AnyM.fromStream(c.stream()))
				  				.stream()
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
		Stream<String> tail = Streams.stream(it);
		tail.forEach(System.out::println);
	}
	@Test
	public void testOfType() {

		

		assertThat(Streamable.of(1, "a", 2, "b", 3, null).ofType(Integer.class).toList(),containsInAnyOrder(1, 2, 3));

		assertThat(Streamable.of(1, "a", 2, "b", 3, null).ofType(Integer.class).toList(),not(containsInAnyOrder("a", "b",null)));

		assertThat(Streamable.of(1, "a", 2, "b", 3, null)

				.ofType(Serializable.class).toList(),containsInAnyOrder(1, "a", 2, "b", 3));

	}


	@Test
	public void testIntersperse() {
		
		assertThat(Streamable.of(1,2,3).intersperse(0).toList(),equalTo(Arrays.asList(1,0,2,0,3)));
	



	}

	@Test
	public void xMatch(){
		assertTrue(Streamable.of(1,2,3,5,6,7).xMatch(3, i-> i>4 ));
	}
	
	
}
