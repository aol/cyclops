package com.aol.cyclops.streams.anyM;


import static com.aol.cyclops.control.AnyM.ofSeq;
import static com.aol.cyclops.control.AnyM.ofValue;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import com.aol.cyclops.Monoid;
import com.aol.cyclops.Reducer;
import com.aol.cyclops.Reducers;
import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.types.anyM.AnyMSeq;
import com.aol.cyclops.types.anyM.AnyMValue;
import com.aol.cyclops.util.stream.Streamable;

import lombok.val;


public class AnyMTest {
	/** no longer compiles!
	@Test
	public void multiReturn(){
		AnyMValue<Integer> stream = AnyM.fromOptional(Optional.of(1))
									.flatMap(i->ReactiveSeq.of(1,2,i).anyM());
		
		stream.map(i->i+2);
	}
	**/
    @Test
    public void listTest(){
        List<String> l = AnyM.fromList(Arrays.asList(1,2,3))
                .map(i->"hello"+i)
                .unwrap();
        assertThat(l,equalTo(Arrays.asList("hello1","hello2","hello3")));
    }
	@Test
	public void multiReturnBind(){

	  
	   
	   
		AnyM<List<Integer>> stream = AnyM.fromOptional(Optional.of(1))
										 .bind(i->Stream.of(1,2,i));
		
		stream.map(i->i.size());
	}
	@Test
	public void collectList(){
		assertThat(AnyM.fromList(Arrays.asList(1,2,2)).collect(Collectors.toSet()).size(),equalTo(2));
	}
	@Test
	public void flatMapWithListComprehender() {
	    List<Integer> list = Arrays.asList(1,2,3);
	    AnyMSeq<Integer> any = AnyM.fromList(list); 
	    AnyM<Integer> mapped = any.flatMap(e -> any.unit(e));
	    List<Integer> unwrapped = mapped.unwrap();
	    assertEquals(list, unwrapped);
	}
	@Test
	public void testLisOfConvertable(){
		AnyM<Integer> list = AnyM.ofConvertableSeq(Arrays.asList(1,2,3));
		assertThat(list.unwrap(),instanceOf(List.class));
	}
	@Test
	public void testForEach() {
		   AnyM.fromStream(Stream.of(asList(1,3)))
				  				.flatMap(c->ofSeq(c.stream()))
				  				.stream()
				  				.forEach(System.out::println);
				  				
	}
	@Test
	public void testForEachCf() {
		   ofValue(CompletableFuture.completedFuture(asList(1,3)))
		   						.stream()
				  				.forEach(System.out::println);
				  				
	}
	/** should no longer compile!
	@Test
	public void testForEachCfFlatMapToStream() {
		   AnyM.fromCompletableFuture(CompletableFuture.completedFuture(asList(1,3)))
		   						.flatMap(c->AnyM.fromStream(c.stream()))
		   						.stream()
				  				.forEach(System.out::println);
				  				
	}
	 **/
	
	
	@Test
	public void test() {
	
		  List<Integer> list = AnyM.fromStream(Stream.of(asList(1,3)))
				  				.flatMap(c->AnyM.fromStream(c.stream()))
				  				.stream()
				  				.map(i->i*2)
				  				.peek(System.out::println)
				  				.collect(Collectors.toList());
		assertThat(Arrays.asList(2,6),equalTo(list));
	}
	@Test
	public void testMixed() {
		
		List<Integer> list = AnyM.fromStream(Stream.of(Arrays.asList(1,3),null))
									.flatMap(d->AnyM.fromOptional(Optional.ofNullable(d)))
									.map(i->i.size())
									.peek(System.out::println)
									.stream()
									.toList();
		assertThat(Arrays.asList(2),equalTo(list));
	}
	int count;
	@Test
	public void testCycleWhile(){
		count =0;
		assertThat(ofSeq(Stream.of(1,2,2)).stream()
											.cycleWhile(next -> count++<6)
											.collect(Collectors.toList()),equalTo(Arrays.asList(1,2,2,1,2,2)));
	}
	@Test
	public void testCycleUntil(){
		count =0;
		assertThat(ofSeq(Stream.of(1,2,2)).stream()
											.cycleUntil(next -> count++>6)
											.collect(Collectors.toList()),equalTo(Arrays.asList(1,2,2,1,2,2,1)));
	}
	@Test
	public void testCycle(){
		assertThat(ofSeq(Stream.of(1,2,2)).stream()
											.cycle(3).collect(Collectors.toList()),equalTo(Arrays.asList(1,2,2,1,2,2,1,2,2)));
	}
	@Test
	public void testCycleReduce(){
		assertThat(AnyM.fromStream(Stream.of(1,2,2)).stream()
											.cycle(Reducers.toCountInt(),3)
											.collect(Collectors.toList()),
											equalTo(Arrays.asList(3,3,3)));
	}
	
	@Test
	public void testJoin(){
		assertThat(ofSeq(Stream.of(1,2,2))
							.map(b-> Stream.of(b))
							.flatten()
							.stream()
							.toList(),equalTo(Arrays.asList(1,2,2)));
	}
	@Test
	public void testJoin2(){
		assertThat(ofSeq(Stream.of(asList(1,2),asList(2)))
						.flatten()
						.stream()
						.toList(),equalTo(Arrays.asList(1,2,2)));
	}
	
	@Test
	public void testToSet(){
		assertThat(ofSeq(Stream.of(1,2,2))
					.stream()
					.toSet().size(),equalTo(2));
	}
	@Test
	public void testToList(){
		assertThat(ofSeq(Stream.of(1,2,3))
					.stream()
					.toList(),equalTo(Arrays.asList(1,2,3)));
	}
	@Test
	public void testCollect(){
		assertThat(ofSeq(Stream.of(1,2,3))
					.stream()
					.collect(Collectors.toList()),equalTo(Arrays.asList(1,2,3)));
	}
	@Test
	public void testToListFlatten(){
		
	
		assertThat(AnyM.fromStream(Stream.of(1,2,3,null))
					.map(Maybe::ofNullable)
					.flatMap(Maybe::anyM)
					.stream()
					.toList(),equalTo(Arrays.asList(1,2,3)));
	
	
	}
	@Test
	public void testToListOptional(){
		assertThat(ofValue(Optional.of(1))
					.stream()
					.toList(),equalTo(Arrays.asList(1)));
	}
	
	@Test
    public void testFold() {
		 
       Supplier<AnyM<String>> s = () -> ofSeq(Stream.of("a","b","c"));

        assertThat("cba",equalTo( s.get().stream().foldRight(Reducers.toString(""))));
        assertThat("abc",equalTo( s.get().stream().reduce(Reducers.toString(""))));
        assertThat( 3,equalTo( s.get().map(i->""+i.length()).stream().foldRightMapToType(Reducers.toCountInt())));
        assertThat( 3,equalTo( s.get().map(i->""+i.length()).stream().mapReduce(Reducers.toCountInt())));
      
    }
	
	
	@Test
	public void traversableTestToList(){
		 List<Integer> list = AnyM.fromOptional(Optional.of(Arrays.asList(1,2,3,4,5,6)))
								.toSequence(i->i.stream())
								.toList();

		 
		 assertThat(list,hasItems(1,2,3,4,5,6));
	}
	@Test
	public void traversableTest(){
		 List<Integer> list = AnyM.fromOptional(Optional.of(Arrays.asList(1,2,3,4,5,6)))
								.toSequence(i->i.stream())
								.collect(Collectors.toList());

		 
		 assertThat(list,hasItems(1,2,3,4,5,6));
	}
	@Test
	public void traversableTestStream(){
		 List<Integer> list = AnyM.fromStream(Stream.of(Arrays.asList(1,2,3,4,5,6)))
								.toSequence(i->i.stream())
								.collect(Collectors.toList());

		 
		 assertThat(list,hasItems(1,2,3,4,5,6));
	}
	@Test
	public void traversableTestStreamNested(){
		List<Integer> list = AnyM.fromStream(Stream.of(Stream.of(1,2,3,4,5,6)))
								.toSequence(i->i)
								.collect(Collectors.toList());

		 
		 assertThat(list,hasItems(1,2,3,4,5,6));
	}
	
	@Test
	public void testFlatMap(){
		AnyMSeq<List<Integer>> m  = AnyM.fromStream(Stream.of(Arrays.asList(1,2,3),Arrays.asList(1,2,3)));
		AnyM<Integer> intM = m.flatMap( c -> ofSeq(c.stream()));
		List<Integer> list = intM.stream().toList();
		assertThat(list,equalTo(Arrays.asList(1, 2, 3, 1, 2, 3)));
	}
	@Test
	public void testBind(){
		AnyM<List<Integer>> m  = ofSeq(Stream.of(Arrays.asList(1,2,3),Arrays.asList(1,2,3)));
		AnyM<Integer> intM = m.bind(Collection::stream);
		List<Integer> list = intM.stream().toList();
		assertThat(list,equalTo(Arrays.asList(1, 2, 3, 1, 2, 3)));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void zipOptional(){
		Stream<List<Integer>> zipped = ofSeq(Stream.of(1,2,3))
										.stream()
										.zipAnyM(ofValue(Optional.of(2)), 
											(a,b) -> Arrays.asList(a,b)).toStream();
		
		
		List<Integer> zip = zipped.collect(Collectors.toList()).get(0);
		assertThat(zip.get(0),equalTo(1));
		assertThat(zip.get(1),equalTo(2));
		
	}
	@Test
	public void zipStream(){
		Stream<List<Integer>> zipped = ofSeq(Stream.of(1,2,3))
											.stream()
											.zipStream(Stream.of(2,3,4), 
													(a,b) -> Arrays.asList(a,b))
													.toStream();
		
		
		List<Integer> zip = zipped.collect(Collectors.toList()).get(1);
		assertThat(zip.get(0),equalTo(2));
		assertThat(zip.get(1),equalTo(3));
		
	}
	
	@Test
	public void sliding(){
		List<List<Integer>> list = AnyM.fromStream(Stream.of(1,2,3,4,5,6))
									.stream()
									.sliding(2)
									.collect(Collectors.toList());
		
	
		assertThat(list.get(0),hasItems(1,2));
		assertThat(list.get(1),hasItems(2,3));
	}
	@Test
	public void slidingIncrement(){
		List<List<Integer>> list = AnyM.fromStream(Stream.of(1,2,3,4,5,6))
									.stream()
									.sliding(3,2)
									.collect(Collectors.toList());
		
	
		assertThat(list.get(0),hasItems(1,2,3));
		assertThat(list.get(1),hasItems(3,4,5));
	}
	@Test
	public void grouped(){
		
		List<List<Integer>> list = AnyM.fromStream(Stream.of(1,2,3,4,5,6))
									.stream()
									.grouped(3)
									.collect(Collectors.toList());
		
		
		assertThat(list.get(0),hasItems(1,2,3));
		assertThat(list.get(1),hasItems(4,5,6));
	
	}
	@Test
	public void groupedOptional(){
		
		List<List<Integer>> list = AnyM.fromOptional(Optional.of(Arrays.asList(1,2,3,4,5,6)))
											.toSequence(i->i.stream())
											.grouped(3)
											.collect(Collectors.toList());
		
		
		assertThat(list.get(0),hasItems(1,2,3));
		assertThat(list.get(1),hasItems(4,5,6));
		
	}
	
	@Test
	public void startsWith(){
		assertTrue(ofValue(Stream.of(1,2,3,4))
						.stream()
						.startsWithIterable(Arrays.asList(1,2,3)));
	}
	@Test
	public void startsWithIterator(){
		assertTrue(AnyM.fromStream(Stream.of(1,2,3,4)).stream().startsWith(Arrays.asList(1,2,3).stream()));
	}
	@Test
	public void distinctOptional(){
		List<Integer> list = AnyM.fromOptional(Optional.of(Arrays.asList(1,2,2,2,5,6)))
											.<Integer>toSequence(i->i.stream())
											.distinct()
											.collect(Collectors.toList());
		
		
		assertThat(list.size(),equalTo(4));
	}
	@Test
    public void scanLeft() {
        assertEquals(
            asList("", "a", "ab", "abc"),
            AnyM.fromStream(Stream.of("a", "b", "c"))
            		.stream()
            		.scanLeft(Reducers.toString(""))
            		.toList());

        
    }
	
	
	@Test
	public void reducer1(){
		Monoid<Integer> sum = Monoid.of(0,(a,b)->a+b);
		Monoid<Integer> mult = Monoid.of(1,(a,b)->a*b);
		val result = AnyM.fromStream(Stream.of(1,2,3,4))
						.stream()
						.reduce(Arrays.asList(sum,mult).stream() );
				
		 
		assertThat(result,equalTo(Arrays.asList(10,24)));
	}
	@Test
	public void reducer2(){
		Reducer<Integer> sum = Reducer.of(0,a->b->a+b,i->(int)i);
		Reducer<Integer> mult = Reducer.of(1,a->b->a*b,i->(int)i);
		val result = AnyM.fromOptional(Optional.of(Stream.of(1,2,3,4)))
						.<Integer>toSequence(i->i)
						.reduce(Arrays.asList(sum,mult) );
				
		 
		assertThat(result,equalTo(Arrays.asList(10,24)));
	}
	@Test
	public void reducer3(){
		Reducer<Integer> sum = Reducer.of(0,a->b->a+b,i->(int)i);
		Reducer<Integer> mult = Reducer.of(1,a->b->a*b,i->(int)i);
		val result = AnyM.fromOptional(Optional.of(Stream.of()))
		                .<Integer>toSequence()
						.reduce(Arrays.asList(sum,mult) );
				
		 
		assertThat(result,equalTo(Arrays.asList(0,1)));
	}
	@Test
	public void reducer4(){
		Reducer<Integer> sum = Reducer.of(0,a->b->a+b,i->(int)i);
		Reducer<Integer> mult = Reducer.of(1,a->b->a*b,i->(int)i);
		val result = AnyM.<Stream<Integer>>fromOptional(Optional.empty())
		                    .<Integer>toSequence(i->i)
						.reduce(Arrays.asList(sum,mult) );
				
		 
		assertThat(result,equalTo(Arrays.asList(0,1)));
	}
	
	@Test
	public void aggregate(){
		List<Integer> result = AnyM.fromStream(Stream.of(1,2,3,4))
								.aggregate(ofValue(Optional.of(5)))
								.stream()
								.<Integer>flatten()
								.toList();
		
		assertThat(result,equalTo(Arrays.asList(1,2,3,4,5)));
	}
	@Test
	public void aggregate2(){
		List<Integer> result = AnyM.fromOptional(Optional.of(Arrays.asList(1,2,3,4)))
								.aggregate((AnyM)AnyM.fromCompletableFuture(CompletableFuture.completedFuture(5)))
								.<Integer>toSequence()
								.toList();
		
		assertThat(result,equalTo(Arrays.asList(1,2,3,4,5)));
	}
	@Test
	public void aggregate3(){
		List<Integer> result = AnyM.fromOptional(Optional.of(Arrays.asList(1,2,3,4)))
								.<Integer>aggregate(ofValue(CompletableFuture.supplyAsync(()->Arrays.asList(5,6))))
								.<Integer>toSequence()
								.toList();
		
		assertThat(result,equalTo(Arrays.asList(1,2,3,4,5,6)));
	}
	
	@Test
	public void testApplyM(){
	AnyM<Function<Integer,Integer>> anyM = AnyM.fromStreamable(Streamable.of( (Integer a)->a+1 ,(Integer a) -> a*2));
	
	 AnyM<Integer> applied =	AnyM.fromStream(Stream.of(1,2,3))
			
			 							.applyM(AnyM.fromStreamable(Streamable.of( (Integer a)->a+1 ,(Integer a) -> a*2)));
	
	 assertThat(applied.stream().toList(),equalTo(Arrays.asList(2, 2, 3, 4, 4, 6)));
	 
	}
	
	
	@Test
	public void testApplyMOptional(){
	 AnyM<Integer> applied =AnyM.fromOptional(Optional.of(2))
			 						.applyM(AnyM.fromOptional(Optional.of( (Integer a)->a+1) ));
	
	 assertThat(applied.stream().toList(),equalTo(Arrays.asList(3)));
	 
	}
	@Test
	public void testApplyMOptionalEmpty(){
	 AnyM<Integer> applied =AnyM.fromOptional(Optional.of(2)).applyM(AnyM.fromOptional(Optional.empty()));
	
	 assertThat(applied.stream().toList(),equalTo(Arrays.asList()));
	 
	}
	@Test
	public void testApplyMEmptyOptional(){
		AnyMValue<Integer> empty= 	AnyM.fromOptional(Optional.empty());
		AnyM<Integer> applied =	empty.applyM(AnyM.fromOptional(Optional.of((Integer a)->a+1)) );
	
		assertThat(applied.stream().toList(),equalTo(Arrays.asList()));
	 
	}
	
	
	
	@Test
	public void testReplicateM(){
		
		 AnyM<List<Integer>> applied =AnyM.fromOptional(Optional.of(2)).replicateM(5);
		 assertThat(applied.unwrap(),equalTo(Optional.of(Arrays.asList(2,2,2,2,2))));
	
	}
	@Test
	public void testReplicateMStream(){
	
		AnyMSeq<Integer> replicated =AnyM.fromStream(Stream.of(2,3,4)).replicateM(5);
		
		assertThat(replicated.stream().toList(),
						equalTo(Arrays.asList(2,3,4 ,2,3,4 ,2,3,4 ,2,3,4 ,2,3,4)));
	
	}
	
	@Test
	public void testSorted(){
		assertThat(ofSeq(Stream.of(4,3,6,7)).stream().sorted().toList(),equalTo(Arrays.asList(3,4,6,7)));
	}
	@Test
	public void testSortedCompartor(){
		assertThat(AnyM.fromStream(Stream.of(4,3,6,7)).stream().sorted((a,b) -> b-a).toList(),equalTo(Arrays.asList(7,6,4,3)));
	}
	@Test
	public void testSkip(){
		assertThat(AnyM.fromStream(Stream.of(4,3,6,7)).stream().skip(2).toList(),equalTo(Arrays.asList(6,7)));
	}
	@Test
	public void testSkipUntil(){
		assertThat(AnyM.fromStream(Stream.of(4,3,6,7)).stream().skipUntil(i->i==6).toList(),equalTo(Arrays.asList(6,7)));
	}
	@Test
	public void testSkipWhile(){
		assertThat(AnyM.fromStream(Stream.of(4,3,6,7)).stream().sorted().skipWhile(i->i<6).toList(),equalTo(Arrays.asList(6,7)));
	}
	@Test
	public void testLimit(){
		assertThat(AnyM.fromStream(Stream.of(4,3,6,7)).stream().limit(2).toList(),equalTo(Arrays.asList(4,3)));
	}
	@Test
	public void testLimitUntil(){
		assertThat(AnyM.fromStream(Stream.of(4,3,6,7)).stream().limitUntil(i->i==6).toList(),equalTo(Arrays.asList(4,3)));
	}
	@Test
	public void testLimitWhile(){
		assertThat(AnyM.fromStream(Stream.of(4,3,6,7)).stream().sorted().limitWhile(i->i<6).toList(),equalTo(Arrays.asList(3,4)));
	}
	
	@Test
	public void testLiftMSimplex(){
		val lifted = AnyM.liftM((Integer a)->a+3);
		
		AnyM<Integer> result = lifted.apply(ofValue(Optional.of(3)));
		
		assertThat(result.<Optional<Integer>>unwrap().get(),equalTo(6));
	}
	
	
	@Test
	public void testReduceM(){
		Monoid<AnyMValue<Integer>> optionalAdd = Monoid.of(ofValue(Optional.of(0)), (a,b)-> AnyM.<Integer>ofValue(Optional.of(a.get()+b.get())));
		
		val res = AnyM.fromStream(Stream.of(2,8,3,1)).reduceMValue(optionalAdd);
		assertThat(AnyM.fromStream(Stream.of(2,8,3,1)).reduceMValue(optionalAdd).unwrap(),equalTo(Optional.of(14)));
	}
	@Test
    public void testReduceSeqM(){
        Monoid<AnyMSeq<Integer>> streamAdd = Monoid.of(ofSeq(ReactiveSeq.of(0)), (a,b)-> AnyM.<Integer>ofSeq(ReactiveSeq.of(a.single()+b.single())));
        
      
        assertThat(AnyM.fromStream(Stream.of(2,8,3,1)).reduceMSeq(streamAdd).toList(),equalTo(Arrays.asList(14)));
    }
	
	@Test
	public void testLiftM2Simplex(){
		val lifted = AnyM.liftM2((Integer a,Integer b)->a+b);
		
		AnyM<Integer> result = lifted.apply(ofValue(Optional.of(3)),ofValue(Optional.of(4)));
		
		assertThat(result.<Optional<Integer>>unwrap().get(),equalTo(7));
	}
	@Test
    public void testLiftM2AnyMValue(){
        val lifted = AnyMValue.liftM2((Integer a,Integer b)->a+b);
        
        AnyM<Integer> result = lifted.apply(ofValue(Optional.of(3)),ofValue(Optional.of(4)));
        
        assertThat(result.<Optional<Integer>>unwrap().get(),equalTo(7));
    }
	@Test
	public void testLiftM2SimplexNull(){
		val lifted = AnyM.liftM2((Integer a,Integer b)->a+b);
		
		AnyM<Integer> result = lifted.apply(ofValue(Optional.of(3)),ofValue(Optional.ofNullable(null)));
		
		assertThat(result.<Optional<Integer>>unwrap().isPresent(),equalTo(false));
	}
	
	private Integer add(Integer a, Integer  b){
		return a+b;
	}
	@Test
	public void testLiftM2Mixed(){
		val lifted = AnyM.liftM2(this::add); 
		
		AnyM<Integer> result = lifted.apply(ofValue(Optional.of(3)),ofValue(Stream.of(4,6,7)));
		
		
		assertThat(result.<Optional<List<Integer>>>unwrap().get(),equalTo(Arrays.asList(7,9,10)));
	}
}
