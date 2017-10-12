package com.aol.cyclops2.streams;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cyclops.companion.Streams;
import org.junit.Test;

import cyclops.function.Monoid;
import cyclops.function.Reducer;
import cyclops.companion.Reducers;
import cyclops.control.anym.AnyM;
import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.Streamable;
import com.aol.cyclops2.react.ThreadPools;
import com.aol.cyclops2.types.stream.HeadAndTail;
import com.aol.cyclops2.types.stream.HotStream;

import lombok.val;
public class StreamUtilsTest {

	@Test
	public void iterate(){

		ReactiveSeq<Integer> s = ReactiveSeq.iterate(1,i->i+1);
		assertThat(s.limit(10).takeRight(1).asFunction().apply(0l),equalTo(10));
		assertThat(s.limit(10).takeRight(1).asFunction().apply(0l),equalTo(10));


	}
    
    @Test
    public void debounceIssue(){
        List<Integer> rs = Streams.debounce(
                Streams.schedule(
                        Stream.of(1,2,3,4,5).peek(x->System.out.println("utilPeek1:"+x))
                        , "* * * * * ?", ThreadPools.getStandardSchedular()
                ).connect(), 10, TimeUnit.SECONDS
        ).peek(x -> System.out.println("utilPeek2:"+x)).collect(Collectors.toList());
        System.out.println("utilResultList:" + rs);
        /**
         * utilPeek1:1
utilPeek2:1
utilPeek1:2
utilPeek1:3
utilPeek1:4
utilPeek1:5
utilResultList:[1]
         */
    }
    
    @Test
    public void reactiveSeq(){
        HotStream<String> hotStream = ReactiveSeq.of("a", "b", "c", "d", "e")
                .peek(x -> System.out.println("peek1:" + x))
                .schedule("* * * * * ?", ThreadPools.getStandardSchedular());
    System.out.println("resultList:" + hotStream.connect().debounce(10, TimeUnit.SECONDS).peek(x->System.out.println("peek2:" + x)).toListX() );
    }
	@Test
	public void headTailReplay(){
	
		Stream<String> helloWorld = Stream.of("hello","world","last");
		HeadAndTail<String> headAndTail = Streams.headAndTail(helloWorld);
		 String head = headAndTail.head();
		 assertThat(head,equalTo("hello"));
		
		ReactiveSeq<String> tail =  headAndTail.tail();
		assertThat(tail.headAndTail().head(),equalTo("world"));
		
	}
	
	@Test
	public void testToLazyCollection(){
		System.out.println(Streams.toLazyCollection(Stream.of(1,2,3,4)).size());
	}
	@Test
	public void testOfType() {

		

		assertThat(Streams.ofType(Stream.of(1, "a", 2, "b", 3, null),Integer.class).collect(Collectors.toList()),containsInAnyOrder(1, 2, 3));

		assertThat(ReactiveSeq.of(1, "a", 2, "b", 3, null).ofType(Integer.class).collect(Collectors.toList()),not(containsInAnyOrder("a", "b",null)));

		assertThat(ReactiveSeq.of(1, "a", 2, "b", 3, null)

				.ofType(Serializable.class).toList(),containsInAnyOrder(1, "a", 2, "b", 3));

	}

	@Test
	public void testCastPast() {
		ReactiveSeq.of(1, "a", 2, "b", 3, null).cast(Date.class).map(d -> d.getTime());
	



	}
	@Test
	public void testIntersperse() {
		
		assertThat(ReactiveSeq.of(1,2,3).intersperse(0).toList(),equalTo(Arrays.asList(1,0,2,0,3)));
	



	}
	@Test
	public void testReverse() {
		
		assertThat(Streams.reverse(Stream.of(1,2,3)).collect(Collectors.toList())
				,equalTo(Arrays.asList(3,2,1)));
	}

	@Test
	public void testReversedStream() {
		
		
		
		Streams.reversedStream(asList(1,2,3))
				.map(i->i*100)
				.forEach(System.out::println);
		
		
		assertThat(Streams.reversedStream(Arrays.asList(1,2,3)).collect(Collectors.toList())
				,equalTo(Arrays.asList(3,2,1)));
		
		
	}

	@Test
	public void testCycleStreamOfU() {
		assertThat(Streams.cycle(Stream.of(1,2,3)).limit(6).collect(Collectors.toList()),equalTo(Arrays.asList(1,2,3,1,2,3)));
	}

	@Test
	public void testCycleStreamableOfU() {
		assertThat(Streams.cycle(Streamable.fromStream(Stream.of(1,2,3))).limit(6).collect(Collectors.toList()),equalTo(Arrays.asList(1,2,3,1,2,3)));
	}

	@Test
	public void testStreamIterableOfU() {
		assertThat(Streams.stream(Arrays.asList(1,2,3)).collect(Collectors.toList()),equalTo(Arrays.asList(1,2,3)));
	}

	@Test
	public void testStreamIteratorOfU() {
		assertThat(Streams.stream(Arrays.asList(1,2,3).iterator()).collect(Collectors.toList()),equalTo(Arrays.asList(1,2,3)));
	}

	@Test
	public void testStreamMapOfKV() {
		Map<String,String> map = new HashMap<>();
		map.put("hello","world");
		assertThat(Streams.stream(map).collect(Collectors.toList()),equalTo(Arrays.asList(new AbstractMap.SimpleEntry("hello","world"))));
	}
	
	@Test
	public void reducer(){
		Monoid<String> concat = Monoid.of("",(a,b)->a+b);
		Monoid<String> join = Monoid.of("",(a,b)->a+","+b);
		
		
		 assertThat(Streams.reduce(Stream.of("hello", "world", "woo!"),Stream.of(concat,join))
		                 
		                  ,equalTo(Arrays.asList("helloworldwoo!",",hello,world,woo!")));
	}
	@Test
	public void reducer2(){
		Reducer<Integer> sum = Reducer.of(0,a->b->a+b,i->(int)i);
		Reducer<Integer> mult = Reducer.of(1,a->b->a*b,i->(int)i);
		val result = Streams.reduce(Stream.of(1,2,3,4),Arrays.asList(sum,mult));
				
		 
		assertThat(result,equalTo(Arrays.asList(10,24)));
	}
	
	int count;
	@Test
	public void testCycleWhile(){
		count =0;
		assertThat(Streams.cycleWhile(Stream.of(1,2,2)
											,next -> count++<6 )
											.collect(Collectors.toList()),equalTo(Arrays.asList(1,2,2,1,2,2)));
	}
	@Test
	public void testCycleUntil(){
		count =0;
		assertThat(Streams.cycleUntil(Stream.of(1,2,2,3)
											,next -> count++>10 )
											.collect(Collectors.toList()),equalTo(Arrays.asList(1, 2, 2, 3, 1, 2, 2, 3, 1, 2, 2)));
	}
	@Test
	public void testCycle(){
		assertThat(Streams.cycle(3,Streamable.of(1,2,2))
								.collect(Collectors.toList()),equalTo(Arrays.asList(1,2,2,1,2,2,1,2,2)));
	}
	@Test
	public void testCycleReduce(){
		assertThat(Streams.cycle(Stream.of(1,2,2)
											,Reducers.toCountInt(),3)
											.collect(Collectors.toList()),
											equalTo(Arrays.asList(3,3,3)));
	}
	
	@Test
	public void testSkipUntil(){
		
		assertThat(Streams.skipUntil(Stream.of(4,3,6,7), i->i==6).collect(Collectors.toList()),
				equalTo(Arrays.asList(6,7)));
		
		
	}
	@Test
	public void testSkipWhile(){
		assertThat(Streams.skipWhile(Stream.of(4,3,6,7).sorted(), i->i<6).collect(Collectors.toList()),
				equalTo(Arrays.asList(6,7)));
	}
	
	@Test
	public void testLimitWhile(){
		assertThat(Streams.limitWhile(Stream.of(4,3,6,7).sorted(), i->i<6).collect(Collectors.toList()),
				equalTo(Arrays.asList(3,4)));
	}
	@Test
	public void testLimitUntil(){
		assertThat(Streams.limitUntil(Stream.of(4,3,6,7), i->i==6).collect(Collectors.toList()),
				equalTo(Arrays.asList(4,3)));
	}
	
	@Test
	public void zipOptional(){
		Stream<List<Integer>> zipped = Streams.zipAnyM(Stream.of(1,2,3)
										,AnyM.fromArray(2),
											(a,b) -> Arrays.asList(a,b));
		
		
		List<Integer> zip = zipped.collect(Collectors.toList()).get(0);
		assertThat(zip.get(0),equalTo(1));
		assertThat(zip.get(1),equalTo(2));
		
	}
	@Test
	public void zipOptionalSequence(){
		Stream<List<Integer>> zipped = Streams.zipAnyM(Stream.of(1,2,3)
										,AnyM.fromArray(2),
											(a,b) -> Arrays.asList(a,b));
		
		
		List<Integer> zip = zipped.collect(Collectors.toList()).get(0);
		assertThat(zip.get(0),equalTo(1));
		assertThat(zip.get(1),equalTo(2));
		
	}
	@Test
	public void zipStream(){
		Stream<List<Integer>> zipped = Streams.zipStream(Stream.of(1,2,3)
												,Stream.of(2,3,4), 
													(a,b) -> Arrays.asList(a,b));
		
		
		List<Integer> zip = zipped.collect(Collectors.toList()).get(1);
		assertThat(zip.get(0),equalTo(2));
		assertThat(zip.get(1),equalTo(3));
		
	}
	@Test
	public void zipSequence(){
		Stream<List<Integer>> zipped = Streams.zipSequence(Stream.of(1,2,3)
												,ReactiveSeq.of(2,3,4), 
													(a,b) -> Arrays.asList(a,b));
		
		
		List<Integer> zip = zipped.collect(Collectors.toList()).get(1);
		assertThat(zip.get(0),equalTo(2));
		assertThat(zip.get(1),equalTo(3));
		
	}
	@Test
	public void sliding(){
		List<List<Integer>> list = Streams.sliding(Stream.of(1,2,3,4,5,6)
												,2)
									.collect(Collectors.toList());
		
	
		assertThat(list.get(0),hasItems(1,2));
		assertThat(list.get(1),hasItems(2,3));
	}
	@Test
	public void grouped(){
		
		List<List<Integer>> list = Streams.grouped(Stream.of(1,2,3,4,5,6)
														,3)
													.collect(Collectors.toList());
		
		
		assertThat(list.get(0),hasItems(1,2,3));
		assertThat(list.get(1),hasItems(4,5,6));
	
	}
	
	
	@Test
	public void startsWith(){
		assertTrue(Streams.startsWith(Stream.of(1,2,3,4)
									,Arrays.asList(1,2,3)));
	}
	@Test
	public void startsWithIterator(){
		assertTrue(Streams.startsWith(Stream.of(1,2,3,4),Arrays.asList(1,2,3).iterator()));
	}
	@Test
    public void scanLeft() {
        assertEquals(
            asList("", "a", "ab", "abc"),
            Streams.scanLeft(Stream.of("a", "b", "c")
            		,Reducers.toString(""))
            		.collect(Collectors.toList()));

        
    }
	
	
	@Test
	public void xMatch(){
		assertTrue(Streams.xMatch(Stream.of(1,2,3,5,6,7),3, i->i>4));
	}
	@Test
	public void testIntersperse2() {
		
		assertThat(Streams.intersperse(Stream.of(1,2,3),0).collect(Collectors.toList()),
				equalTo(Arrays.asList(1,0,2,0,3)));
	

	}
	@Test(expected=ClassCastException.class)
	public void cast(){
		Streams.cast(Stream.of(1,2,3),String.class).collect(Collectors.toList());
	}
}
