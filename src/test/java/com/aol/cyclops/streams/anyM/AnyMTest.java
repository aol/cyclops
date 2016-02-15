package com.aol.cyclops.streams.anyM;


import static com.aol.cyclops.control.AnyM.*;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.Test;

import com.aol.cyclops.Monoid;
import com.aol.cyclops.Reducer;
import com.aol.cyclops.Reducers;
import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
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
		AnyM<Integer> list = AnyM.ofConvertable(Arrays.asList(1,2,3));
		assertThat(list.unwrap(),instanceOf(List.class));
	}
	@Test
	public void testForEach() {
		   AnyM.fromStream(Stream.of(asList(1,3)))
				  				.flatMap(c->ofMonad(c.stream()))
				  				.toSequence()
				  				.forEach(System.out::println);
				  				
	}
	@Test
	public void testForEachCf() {
		   ofMonad(CompletableFuture.completedFuture(asList(1,3)))
		   						.toSequence()
				  				.forEach(System.out::println);
				  				
	}
	/** should no longer compile!
	@Test
	public void testForEachCfFlatMapToStream() {
		   AnyM.fromCompletableFuture(CompletableFuture.completedFuture(asList(1,3)))
		   						.flatMap(c->AnyM.fromStream(c.stream()))
		   						.toSequence()
				  				.forEach(System.out::println);
				  				
	}
	 **/
	
	
	@Test
	public void test() {
	
		  List<Integer> list = AnyM.fromStream(Stream.of(asList(1,3)))
				  				.flatMap(c->AnyM.fromStream(c.stream()))
				  				.asSequence()
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
									.asSequence()
									.toList();
		assertThat(Arrays.asList(2),equalTo(list));
	}
	int count;
	@Test
	public void testCycleWhile(){
		count =0;
		assertThat(ofMonad(Stream.of(1,2,2)).asSequence()
											.cycleWhile(next -> count++<6)
											.collect(Collectors.toList()),equalTo(Arrays.asList(1,2,2,1,2,2)));
	}
	@Test
	public void testCycleUntil(){
		count =0;
		assertThat(ofMonad(Stream.of(1,2,2)).asSequence()
											.cycleUntil(next -> count++>6)
											.collect(Collectors.toList()),equalTo(Arrays.asList(1,2,2,1,2,2,1)));
	}
	@Test
	public void testCycle(){
		assertThat(ofMonad(Stream.of(1,2,2)).asSequence()
											.cycle(3).collect(Collectors.toList()),equalTo(Arrays.asList(1,2,2,1,2,2,1,2,2)));
	}
	@Test
	public void testCycleReduce(){
		assertThat(AnyM.fromStream(Stream.of(1,2,2)).asSequence()
											.cycle(Reducers.toCountInt(),3)
											.collect(Collectors.toList()),
											equalTo(Arrays.asList(3,3,3)));
	}
	
	@Test
	public void testJoin(){
		assertThat(ofMonad(Stream.of(1,2,2))
							.map(b-> Stream.of(b))
							.flatten()
							.asSequence()
							.toList(),equalTo(Arrays.asList(1,2,2)));
	}
	@Test
	public void testJoin2(){
		assertThat(ofMonad(Stream.of(asList(1,2),asList(2)))
						.flatten()
						.asSequence()
						.toList(),equalTo(Arrays.asList(1,2,2)));
	}
	
	@Test
	public void testToSet(){
		assertThat(ofMonad(Stream.of(1,2,2))
					.asSequence()
					.toSet().size(),equalTo(2));
	}
	@Test
	public void testToList(){
		assertThat(ofMonad(Stream.of(1,2,3))
					.asSequence()
					.toList(),equalTo(Arrays.asList(1,2,3)));
	}
	@Test
	public void testCollect(){
		assertThat(ofMonad(Stream.of(1,2,3))
					.asSequence()
					.collect(Collectors.toList()),equalTo(Arrays.asList(1,2,3)));
	}
	@Test
	public void testToListFlatten(){
		
	
		assertThat(AnyM.fromStream(Stream.of(1,2,3,null))
					.map(Maybe::ofNullable)
					.flatMap(Maybe::anyM)
					.asSequence()
					.toList(),equalTo(Arrays.asList(1,2,3)));
	
	
	}
	@Test
	public void testToListOptional(){
		assertThat(ofMonad(Optional.of(1))
					.asSequence()
					.toList(),equalTo(Arrays.asList(1)));
	}
	
	@Test
    public void testFold() {
		 
       Supplier<AnyM<String>> s = () -> ofMonad(Stream.of("a","b","c"));

        assertThat("cba",equalTo( s.get().asSequence().foldRight(Reducers.toString(""))));
        assertThat("abc",equalTo( s.get().asSequence().reduce(Reducers.toString(""))));
        assertThat( 3,equalTo( s.get().map(i->""+i.length()).asSequence().foldRightMapToType(Reducers.toCountInt())));
        assertThat( 3,equalTo( s.get().map(i->""+i.length()).asSequence().mapReduce(Reducers.toCountInt())));
      
    }
	
	@Test
	public void testLift(){
		
		
		List<String> result =AnyM.fromStream(Stream.of("input.file"))
								.map(getClass().getClassLoader()::getResource)
								.peek(System.out::println)
								.map(URL::getFile)
								.<String>liftAndBind(File::new)
								.asSequence()
								.toList();
		
		assertThat(result,equalTo(Arrays.asList("hello","world")));
	}
	
	
	
	@Test
	public void testSequence(){
		
        List<Integer> list = IntStream.range(0, 100).boxed().collect(Collectors.toList());
        List<CompletableFuture<Integer>> futures = list
                .stream()
                .map(x -> CompletableFuture.supplyAsync(() -> x))
                .collect(Collectors.toList());
       
        
        AnyM<ListX<Integer>> futureList = AnyM.sequence(listFromCompletableFuture(futures));
        
 
        List<Integer> collected = futureList.<CompletableFuture<List<Integer>>>unwrap().join();
        assertThat(collected.size(),equalTo( list.size()));
        
        for(Integer next : list){
        	assertThat(list.get(next),equalTo( collected.get(next)));
        }
        
	}
	@Test
	public void testSequenceList(){
		
        
       
        
		AnyM<ListX<Integer>> futureList = AnyM.sequence(AnyM.listFromCollection(asList(asList(1,2),asList(3,4) ) ) );
        
      
        assertThat(futureList.toSequence()
        						.toList(),equalTo(Arrays.asList(1,2,3,4)));
        
	}
	@Test
	public void testSequenceStream(){
	
        
       
        
        AnyM<ListX<Integer>> result = AnyM.sequence(listFromStream(asList(Stream.of(1,2),Stream.of(3,4))));
        
 
       
        assertThat(result.toSequence()
        		      .toList(),
        				equalTo(Arrays.asList(1,2,3,4)));
        
	}
	@Test
	public void testSequenceOptional(){
		
        
       
        
        AnyM<ListX<Integer>> futureList = AnyM.sequence(listFromOptional(asList(Optional.of(7),Optional.of(8),Optional.of(9))));
        
 
        assertThat(futureList.toSequence().toList(),equalTo(Arrays.asList(7,8,9)));
        
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
	public void testTraverse(){
		
        List<Integer> list = IntStream.range(0, 100).boxed().collect(Collectors.toList());
        List<CompletableFuture<Integer>> futures = list
                .stream()
                .map(x -> CompletableFuture.supplyAsync(() -> x))
                .collect(Collectors.toList());

       
        AnyM<ListX<String>> futureList = AnyM.traverse( listFromCompletableFuture(futures), (Integer i) -> "hello" +i);
   
        List<String> collected = futureList.<CompletableFuture<List<String>>>unwrap().join();
        assertThat(collected.size(),equalTo( list.size()));
        
        for(Integer next : list){
        	assertThat("hello"+list.get(next),equalTo( collected.get(next)));
        }
        
	}
	
	@Test
	public void testFlatMap(){
		AnyMSeq<List<Integer>> m  = AnyM.fromStream(Stream.of(Arrays.asList(1,2,3),Arrays.asList(1,2,3)));
		AnyM<Integer> intM = m.flatMap( c -> ofMonad(c.stream()));
		List<Integer> list = intM.asSequence().toList();
		assertThat(list,equalTo(Arrays.asList(1, 2, 3, 1, 2, 3)));
	}
	@Test
	public void testBind(){
		AnyM<List<Integer>> m  = ofMonad(Stream.of(Arrays.asList(1,2,3),Arrays.asList(1,2,3)));
		AnyM<Integer> intM = m.bind(Collection::stream);
		List<Integer> list = intM.asSequence().toList();
		assertThat(list,equalTo(Arrays.asList(1, 2, 3, 1, 2, 3)));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void zipOptional(){
		Stream<List<Integer>> zipped = ofMonad(Stream.of(1,2,3))
										.asSequence()
										.zipAnyM(ofMonad(Optional.of(2)), 
											(a,b) -> Arrays.asList(a,b)).toStream();
		
		
		List<Integer> zip = zipped.collect(Collectors.toList()).get(0);
		assertThat(zip.get(0),equalTo(1));
		assertThat(zip.get(1),equalTo(2));
		
	}/**
	@Test
	public void zipOptionalSequence(){
		Stream<List<Integer>> zipped = ofMonad(Stream.of(1,2,3))
										.asSequence()
										.zip(ofMonad(Optional.of(2))
											 .asSequence(), 
											(a,b) -> Arrays.asList(a,b)).toStream();
		
		
		List<Integer> zip = zipped.collect(Collectors.toList()).get(0);
		assertThat(zip.get(0),equalTo(1));
		assertThat(zip.get(1),equalTo(2));
		
	}**/
	@Test
	public void zipStream(){
		Stream<List<Integer>> zipped = ofMonad(Stream.of(1,2,3))
											.asSequence()
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
									.asSequence()
									.sliding(2)
									.collect(Collectors.toList());
		
	
		assertThat(list.get(0),hasItems(1,2));
		assertThat(list.get(1),hasItems(2,3));
	}
	@Test
	public void slidingIncrement(){
		List<List<Integer>> list = AnyM.fromStream(Stream.of(1,2,3,4,5,6))
									.asSequence()
									.sliding(3,2)
									.collect(Collectors.toList());
		
	
		assertThat(list.get(0),hasItems(1,2,3));
		assertThat(list.get(1),hasItems(3,4,5));
	}
	@Test
	public void grouped(){
		
		List<List<Integer>> list = AnyM.fromStream(Stream.of(1,2,3,4,5,6))
									.asSequence()
									.grouped(3)
									.collect(Collectors.toList());
		
		
		assertThat(list.get(0),hasItems(1,2,3));
		assertThat(list.get(1),hasItems(4,5,6));
	
	}
	@Test
	public void groupedOptional(){
		
		List<List<Integer>> list = ofMonad(Optional.of(Arrays.asList(1,2,3,4,5,6)))
											.<Integer>toSequence()
											.grouped(3)
											.collect(Collectors.toList());
		
		
		assertThat(list.get(0),hasItems(1,2,3));
		assertThat(list.get(1),hasItems(4,5,6));
		
	}
	
	@Test
	public void startsWith(){
		assertTrue(ofMonad(Stream.of(1,2,3,4))
						.asSequence()
						.startsWith(Arrays.asList(1,2,3)));
	}
	@Test
	public void startsWithIterator(){
		assertTrue(AnyM.fromStream(Stream.of(1,2,3,4)).asSequence().startsWith(Arrays.asList(1,2,3).iterator()));
	}
	@Test
	public void distinctOptional(){
		List<Integer> list = ofMonad(Optional.of(Arrays.asList(1,2,2,2,5,6)))
											.<Integer>toSequence()
											.distinct()
											.collect(Collectors.toList());
		
		
		assertThat(list.size(),equalTo(4));
	}
	@Test
    public void scanLeft() {
        assertEquals(
            asList("", "a", "ab", "abc"),
            AnyM.fromStream(Stream.of("a", "b", "c"))
            		.asSequence()
            		.scanLeft(Reducers.toString(""))
            		.toList());

        
    }
	
	
	@Test
	public void reducer1(){
		Monoid<Integer> sum = Monoid.of(0,(a,b)->a+b);
		Monoid<Integer> mult = Monoid.of(1,(a,b)->a*b);
		val result = AnyM.fromStream(Stream.of(1,2,3,4))
						.asSequence()
						.reduce(Arrays.asList(sum,mult).stream() );
				
		 
		assertThat(result,equalTo(Arrays.asList(10,24)));
	}
	@Test
	public void reducer2(){
		Reducer<Integer> sum = Reducer.of(0,a->b->a+b,i->(int)i);
		Reducer<Integer> mult = Reducer.of(1,a->b->a*b,i->(int)i);
		val result = ofMonad(Optional.of(Stream.of(1,2,3,4)))
						.<Integer>toSequence()
						.reduce(Arrays.asList(sum,mult) );
				
		 
		assertThat(result,equalTo(Arrays.asList(10,24)));
	}
	@Test
	public void reducer3(){
		Reducer<Integer> sum = Reducer.of(0,a->b->a+b,i->(int)i);
		Reducer<Integer> mult = Reducer.of(1,a->b->a*b,i->(int)i);
		val result = ofMonad(Optional.of(Stream.of()))
						.<Integer>toSequence()
						.reduce(Arrays.asList(sum,mult) );
				
		 
		assertThat(result,equalTo(Arrays.asList(0,1)));
	}
	@Test
	public void reducer4(){
		Reducer<Integer> sum = Reducer.of(0,a->b->a+b,i->(int)i);
		Reducer<Integer> mult = Reducer.of(1,a->b->a*b,i->(int)i);
		val result = ofMonad(Optional.empty())
						.<Integer>toSequence()
						.reduce(Arrays.asList(sum,mult) );
				
		 
		assertThat(result,equalTo(Arrays.asList(0,1)));
	}
	
	@Test
	public void aggregate(){
		List<Integer> result = AnyM.fromStream(Stream.of(1,2,3,4))
								.aggregate(ofMonad(Optional.of(5)))
								.asSequence()
								.<Integer>flatten()
								.toList();
		
		assertThat(result,equalTo(Arrays.asList(1,2,3,4,5)));
	}
	@Test
	public void aggregate2(){
		List<Integer> result = ofMonad(Optional.of(Arrays.asList(1,2,3,4)))
								.<Integer>aggregate(ofMonad(CompletableFuture.completedFuture(5)))
								.<Integer>toSequence()
								.toList();
		
		assertThat(result,equalTo(Arrays.asList(1,2,3,4,5)));
	}
	@Test
	public void aggregate3(){
		List<Integer> result = ofMonad(Optional.of(Arrays.asList(1,2,3,4)))
								.<Integer>aggregate(ofMonad(CompletableFuture.supplyAsync(()->Arrays.asList(5,6))))
								.<Integer>toSequence()
								.toList();
		
		assertThat(result,equalTo(Arrays.asList(1,2,3,4,5,6)));
	}
	
	@Test
	public void testApplyM(){
	AnyM<Function<Integer,Integer>> anyM = AnyM.fromStreamable(Streamable.of( (Integer a)->a+1 ,(Integer a) -> a*2));
	
	 AnyM<Integer> applied =	AnyM.fromStream(Stream.of(1,2,3))
			
			 							.applyM(AnyM.fromStreamable(Streamable.of( (Integer a)->a+1 ,(Integer a) -> a*2)));
	
	 assertThat(applied.toSequence().toList(),equalTo(Arrays.asList(2, 2, 3, 4, 4, 6)));
	 
	}
	
	
	@Test
	public void testApplyMOptional(){
	 AnyM<Integer> applied =AnyM.fromOptional(Optional.of(2))
			 						.applyM(AnyM.fromOptional(Optional.of( (Integer a)->a+1) ));
	
	 assertThat(applied.toSequence().toList(),equalTo(Arrays.asList(3)));
	 
	}
	@Test
	public void testApplyMOptionalEmpty(){
	 AnyM<Integer> applied =AnyM.fromOptional(Optional.of(2)).applyM(AnyM.fromOptional(Optional.empty()));
	
	 assertThat(applied.toSequence().toList(),equalTo(Arrays.asList()));
	 
	}
	@Test
	public void testApplyMEmptyOptional(){
		AnyMValue<Integer> empty= 	AnyM.fromOptional(Optional.empty());
		AnyM<Integer> applied =	empty.applyM(AnyM.fromOptional(Optional.of((Integer a)->a+1)) );
	
		assertThat(applied.toSequence().toList(),equalTo(Arrays.asList()));
	 
	}
	
	
	
	@Test
	public void testReplicateM(){
		
		 AnyM<List<Integer>> applied =AnyM.fromOptional(Optional.of(2)).replicateM(5);
		 assertThat(applied.unwrap(),equalTo(Optional.of(Arrays.asList(2,2,2,2,2))));
	
	}
	@Test
	public void testReplicateMStream(){
	
		AnyM<List<Integer>> replicated =AnyM.fromStream(Stream.of(2,3,4)).replicateM(5);
		
		assertThat(replicated.toSequence().toList(),
						equalTo(Arrays.asList(2,3,4 ,2,3,4 ,2,3,4 ,2,3,4 ,2,3,4)));
	
	}
	
	@Test
	public void testSorted(){
		assertThat(ofMonad(Stream.of(4,3,6,7)).asSequence().sorted().toList(),equalTo(Arrays.asList(3,4,6,7)));
	}
	@Test
	public void testSortedCompartor(){
		assertThat(AnyM.fromStream(Stream.of(4,3,6,7)).asSequence().sorted((a,b) -> b-a).toList(),equalTo(Arrays.asList(7,6,4,3)));
	}
	@Test
	public void testSkip(){
		assertThat(AnyM.fromStream(Stream.of(4,3,6,7)).asSequence().skip(2).toList(),equalTo(Arrays.asList(6,7)));
	}
	@Test
	public void testSkipUntil(){
		assertThat(AnyM.fromStream(Stream.of(4,3,6,7)).asSequence().skipUntil(i->i==6).toList(),equalTo(Arrays.asList(6,7)));
	}
	@Test
	public void testSkipWhile(){
		assertThat(AnyM.fromStream(Stream.of(4,3,6,7)).asSequence().sorted().skipWhile(i->i<6).toList(),equalTo(Arrays.asList(6,7)));
	}
	@Test
	public void testLimit(){
		assertThat(AnyM.fromStream(Stream.of(4,3,6,7)).asSequence().limit(2).toList(),equalTo(Arrays.asList(4,3)));
	}
	@Test
	public void testLimitUntil(){
		assertThat(AnyM.fromStream(Stream.of(4,3,6,7)).asSequence().limitUntil(i->i==6).toList(),equalTo(Arrays.asList(4,3)));
	}
	@Test
	public void testLimitWhile(){
		assertThat(AnyM.fromStream(Stream.of(4,3,6,7)).asSequence().sorted().limitWhile(i->i<6).toList(),equalTo(Arrays.asList(3,4)));
	}
	
	@Test
	public void testLiftMSimplex(){
		val lifted = AnyM.liftM((Integer a)->a+3);
		
		AnyM<Integer> result = lifted.apply(ofMonad(Optional.of(3)));
		
		assertThat(result.<Optional<Integer>>unwrap().get(),equalTo(6));
	}
	
	
	@Test
	public void testReduceM(){
		Monoid<Optional<Integer>> optionalAdd = Monoid.of(Optional.of(0), (a,b)-> Optional.of(a.get()+b.get()));
		
		
		assertThat(AnyM.fromStream(Stream.of(2,8,3,1)).reduceMOptional(optionalAdd).unwrap(),equalTo(Optional.of(14)));
	}
	
	@Test
	public void testLiftM2Simplex(){
		val lifted = AnyM.liftM2((Integer a,Integer b)->a+b);
		
		AnyM<Integer> result = lifted.apply(ofMonad(Optional.of(3)),ofMonad(Optional.of(4)));
		
		assertThat(result.<Optional<Integer>>unwrap().get(),equalTo(7));
	}
	@Test
	public void testLiftM2SimplexNull(){
		val lifted = AnyM.liftM2((Integer a,Integer b)->a+b);
		
		AnyM<Integer> result = lifted.apply(ofMonad(Optional.of(3)),ofMonad(Optional.ofNullable(null)));
		
		assertThat(result.<Optional<Integer>>unwrap().isPresent(),equalTo(false));
	}
	
	private Integer add(Integer a, Integer  b){
		return a+b;
	}
	@Test
	public void testLiftM2Mixed(){
		val lifted = AnyM.liftM2(this::add); 
		
		AnyM<Integer> result = lifted.apply(ofMonad(Optional.of(3)),ofMonad(Stream.of(4,6,7)));
		
		
		assertThat(result.<Optional<List<Integer>>>unwrap().get(),equalTo(Arrays.asList(7,9,10)));
	}
}
