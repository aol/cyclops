package com.aol.cyclops.javaslang.streams;

import static com.aol.cyclops.lambda.api.AsAnyM.anyM;
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
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import javaslang.collection.Stream;
import lombok.val;

import org.junit.Test;

import com.aol.cyclops.javaslang.ToStream;
import com.aol.cyclops.sequence.HeadAndTail;
import com.aol.cyclops.sequence.Monoid;
import com.aol.cyclops.sequence.Reducers;
import com.aol.cyclops.sequence.SequenceM;
import com.aol.cyclops.sequence.streamable.AsStreamable;
import com.aol.cyclops.sequence.streamable.Streamable;
public class StreamUtilsTest {
	@Test
	public void headTailReplay(){
	
		Stream<String> helloWorld = Stream.ofAll("hello","world","last");
		HeadAndTail<String> headAndTail = StreamUtils.headAndTail(helloWorld);
		 String head = headAndTail.head();
		 assertThat(head,equalTo("hello"));
		
		SequenceM<String> tail =  headAndTail.tail();
		assertThat(tail.headAndTail().head(),equalTo("world"));
		
	}
	@Test
	public void headTailReplayOptional(){
	
		Stream<String> helloWorld = Stream.ofAll("hello","world","last");
		HeadAndTail<String> headAndTail = StreamUtils.headAndTailOptional(helloWorld).get();
		 String head = headAndTail.head();
		 assertThat(head,equalTo("hello"));
		
		SequenceM<String> tail =  headAndTail.tail();
		assertThat(tail.headAndTail().head(),equalTo("world"));
		
	}
	@Test
	public void headTailReplayOptionalEmpty(){
	
		Stream<String> helloWorld = Stream.ofAll();
		Optional<HeadAndTail<String>> headAndTail = StreamUtils.headAndTailOptional(helloWorld);
		
		 assertTrue(!headAndTail.isPresent());
	}
	@Test
	public void testToLazyCollection(){
		System.out.println(StreamUtils.toLazyCollection(Stream.ofAll(1,2,3,4)).size());
	}
	@Test
	public void testOfType() {

		

		assertThat(StreamUtils.ofType(Stream.ofAll(1, "a", 2, "b", 3, null),Integer.class).toJavaList(),containsInAnyOrder(1, 2, 3));

		assertThat(SequenceM.of(1, "a", 2, "b", 3, null).ofType(Integer.class).toList(),not(containsInAnyOrder("a", "b",null)));

		assertThat(SequenceM.of(1, "a", 2, "b", 3, null)

				.ofType(Serializable.class).toList(),containsInAnyOrder(1, "a", 2, "b", 3));

	}

	@Test
	public void testCastPast() {
		SequenceM.of(1, "a", 2, "b", 3, null).cast(Date.class).map(d -> d.getTime());
	



	}
	@Test
	public void testIntersperse() {
		
		assertThat(SequenceM.of(1,2,3).intersperse(0).toList(),equalTo(Arrays.asList(1,0,2,0,3)));
	



	}
	@Test
	public void testReverse() {
		
		assertThat(StreamUtils.reverse(Stream.ofAll(1,2,3)).toJavaList()
				,equalTo(Arrays.asList(3,2,1)));
	}

	@Test
	public void testReversedStream() {
		
		
		
		StreamUtils.reversedStream(javaslang.collection.List.ofAll(asList(1,2,3)))
				.map(i->i*100)
				.forEach(System.out::println);
		
		
		assertThat(StreamUtils.reversedStream(javaslang.collection.List.ofAll(1,2,3)).toJavaList()
				,equalTo(Arrays.asList(3,2,1)));
		
		
	}

	@Test
	public void testCycleStreamOfU() {
		assertThat(StreamUtils.cycle(Stream.ofAll(1,2,3)).take(6).toJavaList(),equalTo(Arrays.asList(1,2,3,1,2,3)));
	}

	@Test
	public void testCycleStreamableOfU() {
		assertThat(StreamUtils.cycle(AsStreamable.fromStream(ToStream.toSequenceM(Stream.ofAll(1,2,3)))).take(6).toJavaList(),equalTo(Arrays.asList(1,2,3,1,2,3)));
	}

	@Test
	public void testStreamIterableOfU() {
		assertThat(StreamUtils.stream(Arrays.asList(1,2,3)).toJavaList(),equalTo(Arrays.asList(1,2,3)));
	}

	@Test
	public void testStreamIteratorOfU() {
		assertThat(StreamUtils.stream(Arrays.asList(1,2,3).iterator()).toJavaList(),equalTo(Arrays.asList(1,2,3)));
	}

	@Test
	public void testStreamMapOfKV() {
		
		Map<String,String> map = new HashMap<>();
		map.put("hello","world");
		assertThat(StreamUtils.stream(map).toJavaList(),equalTo(Arrays.asList(new AbstractMap.SimpleEntry("hello","world"))));
	}
	@Test
	public void testStreamMapOfKVJavaslang() {
		javaslang.collection.HashMap<String,String> map = javaslang.collection.HashMap.<String,String>empty().put("hello","world");
		
		assertThat(StreamUtils.stream(map).toJavaList(),equalTo(Arrays.asList(new AbstractMap.SimpleEntry("hello","world"))));
	}
	@Test
    public void testCollectorsStreamable() {
		List result = StreamUtils.collect(Stream.ofAll(1,2,3),
								Streamable.<Collector>of(Collectors.toList(),
								Collectors.summingInt(Integer::intValue),
								Collectors.averagingInt(Integer::intValue))).toJavaList();
		
		assertThat(result.get(0),equalTo(Arrays.asList(1,2,3)));
		assertThat(result.get(1),equalTo(6));
		assertThat(result.get(2),equalTo(2.0));
    }

	@Test
    public void testCollectors2() {
		List result = StreamUtils.collect(Stream.ofAll(1,2,3),
								(Stream)Stream.ofAll(Collectors.toList(),
								Collectors.summingInt(Integer::intValue),
								Collectors.averagingInt(Integer::intValue))).toJavaList();
		
		assertThat(result.get(0),equalTo(Arrays.asList(1,2,3)));
		assertThat(result.get(1),equalTo(6));
		assertThat(result.get(2),equalTo(2.0));
    }
	@Test
    public void testCollectorsIterables() {
		List result = StreamUtils.collect(Stream.ofAll(1,2,3),
								Arrays.asList(Collectors.toList(),
								Collectors.summingInt(Integer::intValue),
								Collectors.averagingInt(Integer::intValue))).toJavaList();
		
		assertThat(result.get(0),equalTo(Arrays.asList(1,2,3)));
		assertThat(result.get(1),equalTo(6));
		assertThat(result.get(2),equalTo(2.0));
    }
	@Test
	public void reducer(){
		Monoid<String> concat = Monoid.of("",(a,b)->a+b);
		Monoid<String> join = Monoid.of("",(a,b)->a+","+b);
		
		
		 assertThat(StreamUtils.reduce(Stream.ofAll("hello", "world", "woo!"),Stream.ofAll(concat,join))
		                 
		                  ,equalTo(javaslang.collection.List.ofAll("helloworldwoo!",",hello,world,woo!")));
	}
	@Test
	public void reducer2(){
		Monoid<Integer> sum = Monoid.of(0,(a,b)->a+b);
		Monoid<Integer> mult = Monoid.of(1,(a,b)->a*b);
		val result = StreamUtils.reduce(Stream.ofAll(1,2,3,4),Arrays.asList(sum,mult));
				
		 
		assertThat(result,equalTo(javaslang.collection.List.ofAll(10,24)));
	}
	@Test
    public void testCollectors() {
		List result = StreamUtils.collect(Stream.ofAll(1,2,3),
				Arrays.asList(Collectors.toList(),Collectors.summingInt(Integer::intValue),
						Collectors.averagingInt(Integer::intValue))).toJavaList();
		
		assertThat(result.get(0),equalTo(Arrays.asList(1,2,3)));
		assertThat(result.get(1),equalTo(6));
		assertThat(result.get(2),equalTo(2.0));
    }
	int count;
	@Test
	public void testCycleWhile(){
		count =0;
		assertThat(StreamUtils.cycleWhile(Stream.ofAll(1,2,2)
											,next -> count++<6 )
											.toJavaList(),equalTo(Arrays.asList(1,2,2,1,2,2)));
	}
	@Test
	public void testCycleUntil(){
		count =0;
		assertThat(StreamUtils.cycleUntil(Stream.ofAll(1,2,2,3)
											,next -> count++>10 )
											.toJavaList(),equalTo(Arrays.asList(1, 2, 2, 3, 1, 2, 2, 3, 1, 2, 2)));
	}
	@Test
	public void testCycle(){
		assertThat(StreamUtils.cycle(3,Streamable.of(1,2,2))
								.toJavaList(),equalTo(Arrays.asList(1,2,2,1,2,2,1,2,2)));
	}
	@Test
	public void testCycleReduce(){
		assertThat(StreamUtils.cycle(Stream.ofAll(1,2,2)
											,Reducers.toCountInt(),3)
											.toJavaList(),
											equalTo(Arrays.asList(3,3,3)));
	}
	
	@Test
	public void testSkipUntil(){
		
		assertThat(StreamUtils.skipUntil(Stream.ofAll(4,3,6,7),i->i==6).toJavaList(),
				equalTo(Arrays.asList(6,7)));
		
		
	}
	@Test
	public void testSkipWhile(){
		assertThat(StreamUtils.skipWhile(Stream.ofAll(4,3,6,7).sort(),i->i<6).toJavaList(),
				equalTo(Arrays.asList(6,7)));
	}
	
	@Test
	public void testLimitWhile(){
		assertThat(StreamUtils.limitWhile(Stream.ofAll(4,3,6,7).sort(),i->i<6).toJavaList(),
				equalTo(Arrays.asList(3,4)));
	}
	@Test
	public void testLimitUntil(){
		assertThat(StreamUtils.limitUntil(Stream.ofAll(4,3,6,7),i->i==6).toJavaList(),
				equalTo(Arrays.asList(4,3)));
	}
	
	@Test
	public void zipOptional(){
		Stream<List<Integer>> zipped = StreamUtils.zipAnyM(Stream.ofAll(1,2,3)
										,anyM(Optional.of(2)), 
											(a,b) -> Arrays.asList(a,b));
		
		
		List<Integer> zip = zipped.toJavaList().get(0);
		assertThat(zip.get(0),equalTo(1));
		assertThat(zip.get(1),equalTo(2));
		
	}
	@Test
	public void zipOptionalSequence(){
		Stream<List<Integer>> zipped = StreamUtils.zipAnyM(Stream.ofAll(1,2,3)
										,anyM(Optional.of(2)), 
											(a,b) -> Arrays.asList(a,b));
		
		
		List<Integer> zip = zipped.toJavaList().get(0);
		assertThat(zip.get(0),equalTo(1));
		assertThat(zip.get(1),equalTo(2));
		
	}
	@Test
	public void zipStream(){
		Stream<List<Integer>> zipped = StreamUtils.zipStream(Stream.ofAll(1,2,3)
												,java.util.stream.Stream.of(2,3,4), 
													(a,b) -> Arrays.asList(a,b));
		
		
		List<Integer> zip = zipped.toJavaList().get(1);
		assertThat(zip.get(0),equalTo(2));
		assertThat(zip.get(1),equalTo(3));
		
	}
	@Test
	public void zipSequence(){
		Stream<List<Integer>> zipped = StreamUtils.zipSequence(Stream.ofAll(1,2,3)
												,Stream.ofAll(2,3,4), 
													(a,b) -> Arrays.asList(a,b));
		
		
		List<Integer> zip = zipped.toJavaList().get(1);
		assertThat(zip.get(0),equalTo(2));
		assertThat(zip.get(1),equalTo(3));
		
	}
	@Test
	public void sliding(){
		List<List<Integer>> list = StreamUtils.sliding(Stream.ofAll(1,2,3,4,5,6)
												,2)
												.map(l->l.toJavaList())
									.toJavaList();
		
	
		assertThat(list.get(0),hasItems(1,2));
		assertThat(list.get(1),hasItems(2,3));
	}
	@Test
	public void grouped(){
		
		List<List<Integer>> list = StreamUtils.batchBySize(Stream.ofAll(1,2,3,4,5,6)
														,3)
														.map(l->l.toJavaList())
													.toJavaList();
		
		
		assertThat(list.get(0),hasItems(1,2,3));
		assertThat(list.get(1),hasItems(4,5,6));
	
	}
	
	
	@Test
	public void startsWith(){
		assertTrue(StreamUtils.startsWith(Stream.ofAll(1,2,3,4)
									,Arrays.asList(1,2,3)));
	}
	@Test
	public void startsWithIterator(){
		assertTrue(StreamUtils.startsWith(Stream.ofAll(1,2,3,4),Arrays.asList(1,2,3).iterator()));
	}
	@Test
    public void scanLeft() {
        assertEquals(
            asList("", "a", "ab", "abc"),
            StreamUtils.scanLeft(Stream.ofAll("a", "b", "c")
            		,Reducers.toString(""))
            		.toJavaList());

        
    }
	
	
	@Test
	public void xMatch(){
		assertTrue(StreamUtils.xMatch(Stream.ofAll(1,2,3,5,6,7),3, i->i>4));
	}
	@Test
	public void testIntersperse2() {
		
		assertThat(StreamUtils.intersperse(Stream.ofAll(1,2,3),0).toJavaList(),
				equalTo(Arrays.asList(1,0,2,0,3)));
	

	}
	@Test(expected=ClassCastException.class)
	public void cast(){
		StreamUtils.cast(Stream.ofAll(1,2,3),String.class).toJavaList();
	}
}
