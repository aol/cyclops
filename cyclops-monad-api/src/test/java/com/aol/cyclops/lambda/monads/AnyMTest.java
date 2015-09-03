package com.aol.cyclops.lambda.monads;

import static com.aol.cyclops.lambda.api.AsAnyM.anyM;
import static com.aol.cyclops.lambda.api.AsAnyMList.collectionToAnyMList;
import static com.aol.cyclops.lambda.api.AsAnyMList.completableFutureToAnyMList;
import static com.aol.cyclops.lambda.api.AsAnyMList.optionalToAnyMList;
import static com.aol.cyclops.lambda.api.AsAnyMList.streamToAnyMList;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
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
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import lombok.val;

import org.junit.Test;

import com.aol.cyclops.monad.AnyM;
import com.aol.cyclops.sequence.Monoid;
import com.aol.cyclops.sequence.Reducers;
import com.aol.cyclops.sequence.streamable.Streamable;


public class AnyMTest {
	@Test
	public void testForEach() {
		   anyM(Stream.of(asList(1,3)))
				  				.flatMap(c->anyM(c.stream()))
				  				.forEach(System.out::println);
				  				
	}
	@Test
	public void testForEachCf() {
		   anyM(CompletableFuture.completedFuture(asList(1,3)))
				  
				  				.forEach(System.out::println);
				  				
	}
	@Test
	public void testForEachCfFlatMapToStream() {
		   anyM(CompletableFuture.completedFuture(asList(1,3)))
		   						.flatMap(c->anyM(c.stream()))
		   						.toSequence()
				  				.forEach(System.out::println);
				  				
	}
	 
	
	@Test
	public void test() {
	
		  List<Integer> list = anyM(Stream.of(asList(1,3)))
				  				.flatMap(c->anyM(c.stream()))
				  				.asSequence()
				  				.map(i->i*2)
				  				.peek(System.out::println)
				  				.collect(Collectors.toList());
		assertThat(Arrays.asList(2,6),equalTo(list));
	}
	@Test
	public void testMixed() {
		
		List<Integer> list = anyM(Stream.of(Arrays.asList(1,3),null))
									.flatMap(d->anyM(Optional.ofNullable(d)))
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
		assertThat(anyM(Stream.of(1,2,2)).asSequence()
											.cycleWhile(next -> count++<6)
											.collect(Collectors.toList()),equalTo(Arrays.asList(1,2,2,1,2,2)));
	}
	@Test
	public void testCycleUntil(){
		count =0;
		assertThat(anyM(Stream.of(1,2,2)).asSequence()
											.cycleUntil(next -> count++>6)
											.collect(Collectors.toList()),equalTo(Arrays.asList(1,2,2,1,2,2,1)));
	}
	@Test
	public void testCycle(){
		assertThat(anyM(Stream.of(1,2,2)).asSequence()
											.cycle(3).collect(Collectors.toList()),equalTo(Arrays.asList(1,2,2,1,2,2,1,2,2)));
	}
	@Test
	public void testCycleReduce(){
		assertThat(anyM(Stream.of(1,2,2)).asSequence()
											.cycle(Reducers.toCountInt(),3)
											.collect(Collectors.toList()),
											equalTo(Arrays.asList(3,3,3)));
	}
	@Test
	public void testCycleMonad(){
		
		assertThat(anyM(Stream.of(1,2)).asSequence()
											.cycle(Optional.class,2)
											.collect(Collectors.toList()),
											equalTo(asList(Optional.of(1),Optional.of(2)
												,Optional.of(1),Optional.of(2)	)));
	}
	@Test
	public void testJoin(){
		assertThat(anyM(Stream.of(1,2,2))
							.map(b-> Stream.of(b))
							.flatten()
							.asSequence()
							.toList(),equalTo(Arrays.asList(1,2,2)));
	}
	@Test
	public void testJoin2(){
		assertThat(anyM(Stream.of(asList(1,2),asList(2)))
						.flatten()
						.asSequence()
						.toList(),equalTo(Arrays.asList(1,2,2)));
	}
	
	@Test
	public void testToSet(){
		assertThat(anyM(Stream.of(1,2,2))
					.asSequence()
					.toSet().size(),equalTo(2));
	}
	@Test
	public void testToList(){
		assertThat(anyM(Stream.of(1,2,3))
					.asSequence()
					.toList(),equalTo(Arrays.asList(1,2,3)));
	}
	@Test
	public void testCollect(){
		assertThat(anyM(Stream.of(1,2,3))
					.asSequence()
					.collect(Collectors.toList()),equalTo(Arrays.asList(1,2,3)));
	}
	@Test
	public void testToListFlatten(){
		
	
		assertThat(anyM(Stream.of(1,2,3,null))
					.flatMapOptional(Optional::ofNullable)
					.asSequence()
					.toList(),equalTo(Arrays.asList(1,2,3)));
	
	
	}
	@Test
	public void testToListOptional(){
		assertThat(anyM(Optional.of(1))
					.asSequence()
					.toList(),equalTo(Arrays.asList(1)));
	}
	
	@Test
    public void testFold() {
		 
       Supplier<AnyM<String>> s = () -> anyM(Stream.of("a","b","c"));

        assertThat("cba",equalTo( s.get().asSequence().foldRight(Reducers.toString(""))));
        assertThat("abc",equalTo( s.get().asSequence().foldLeft(Reducers.toString(""))));
        assertThat( 3,equalTo( s.get().map(i->""+i.length()).asSequence().foldRightMapToType(Reducers.toCountInt())));
        assertThat( 3,equalTo( s.get().map(i->""+i.length()).asSequence().foldLeftMapToType(Reducers.toCountInt())));
      
    }
	
	@Test
	public void testLift(){
		
		
		List<String> result = anyM(Stream.of("input.file"))
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
       
        
        AnyM<Stream<Integer>> futureList = AnyMonads.sequence(completableFutureToAnyMList(futures));
        
 
        List<Integer> collected = futureList.<CompletableFuture<List<Integer>>>unwrap().join();
        assertThat(collected.size(),equalTo( list.size()));
        
        for(Integer next : list){
        	assertThat(list.get(next),equalTo( collected.get(next)));
        }
        
	}
	@Test
	public void testSequenceList(){
		
        
       
        
		AnyM<Stream<Integer>> futureList = AnyMonads.sequence(collectionToAnyMList(asList(asList(1,2),asList(3,4) ) ) );
        
      
        assertThat(futureList.toSequence(c->  c)
        						.toList(),equalTo(Arrays.asList(1,2,3,4)));
        
	}
	@Test
	public void testSequenceStream(){
	
        
       
        
        AnyM<Stream<Integer>> result = AnyMonads.sequence(streamToAnyMList(asList(Stream.of(1,2),Stream.of(3,4))));
        
 
       
        assertThat(result.toSequence()
        		      .toList(),
        				equalTo(Arrays.asList(1,2,3,4)));
        
	}
	@Test
	public void testSequenceOptional(){
		
        
       
        
        AnyM<Stream<Integer>> futureList = AnyMonads.sequence(optionalToAnyMList(asList(Optional.of(7),Optional.of(8),Optional.of(9))));
        
 
        assertThat(futureList.toSequence().toList(),equalTo(Arrays.asList(7,8,9)));
        
	}
	@Test
	public void traversableTestToList(){
		 List<Integer> list = anyM(Optional.of(Arrays.asList(1,2,3,4,5,6)))
								.toSequence(i->i.stream())
								.toList();

		 
		 assertThat(list,hasItems(1,2,3,4,5,6));
	}
	@Test
	public void traversableTest(){
		 List<Integer> list = anyM(Optional.of(Arrays.asList(1,2,3,4,5,6)))
								.toSequence(i->i.stream())
								.collect(Collectors.toList());

		 
		 assertThat(list,hasItems(1,2,3,4,5,6));
	}
	@Test
	public void traversableTestStream(){
		 List<Integer> list = anyM(Stream.of(Arrays.asList(1,2,3,4,5,6)))
								.toSequence(i->i.stream())
								.collect(Collectors.toList());

		 
		 assertThat(list,hasItems(1,2,3,4,5,6));
	}
	@Test
	public void traversableTestStreamNested(){
		List<Integer> list = anyM(Stream.of(Stream.of(1,2,3,4,5,6)))
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

       
        AnyM<List<String>> futureList = AnyMonads.traverse( completableFutureToAnyMList(futures), (Integer i) -> "hello" +i);
   
        List<String> collected = futureList.<CompletableFuture<List<String>>>unwrap().join();
        assertThat(collected.size(),equalTo( list.size()));
        
        for(Integer next : list){
        	assertThat("hello"+list.get(next),equalTo( collected.get(next)));
        }
        
	}
	
	@Test
	public void testFlatMap(){
		AnyM<List<Integer>> m  = anyM(Stream.of(Arrays.asList(1,2,3),Arrays.asList(1,2,3)));
		AnyM<Integer> intM = m.flatMap( c -> anyM(c.stream()));
		List<Integer> list = intM.asSequence().toList();
		assertThat(list,equalTo(Arrays.asList(1, 2, 3, 1, 2, 3)));
	}
	@Test
	public void testBind(){
		AnyM<List<Integer>> m  = anyM(Stream.of(Arrays.asList(1,2,3),Arrays.asList(1,2,3)));
		AnyM<Integer> intM = m.bind(Collection::stream);
		List<Integer> list = intM.asSequence().toList();
		assertThat(list,equalTo(Arrays.asList(1, 2, 3, 1, 2, 3)));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void zipOptional(){
		Stream<List<Integer>> zipped = anyM(Stream.of(1,2,3))
										.asSequence()
										.zipAnyM(anyM(Optional.of(2)), 
											(a,b) -> Arrays.asList(a,b)).toStream();
		
		
		List<Integer> zip = zipped.collect(Collectors.toList()).get(0);
		assertThat(zip.get(0),equalTo(1));
		assertThat(zip.get(1),equalTo(2));
		
	}/**
	@Test
	public void zipOptionalSequence(){
		Stream<List<Integer>> zipped = anyM(Stream.of(1,2,3))
										.asSequence()
										.zip(anyM(Optional.of(2))
											 .asSequence(), 
											(a,b) -> Arrays.asList(a,b)).toStream();
		
		
		List<Integer> zip = zipped.collect(Collectors.toList()).get(0);
		assertThat(zip.get(0),equalTo(1));
		assertThat(zip.get(1),equalTo(2));
		
	}**/
	@Test
	public void zipStream(){
		Stream<List<Integer>> zipped = anyM(Stream.of(1,2,3))
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
		List<List<Integer>> list = anyM(Stream.of(1,2,3,4,5,6))
									.asSequence()
									.sliding(2)
									.collect(Collectors.toList());
		
	
		assertThat(list.get(0),hasItems(1,2));
		assertThat(list.get(1),hasItems(2,3));
	}
	@Test
	public void slidingIncrement(){
		List<List<Integer>> list = anyM(Stream.of(1,2,3,4,5,6))
									.asSequence()
									.sliding(3,2)
									.collect(Collectors.toList());
		
	
		assertThat(list.get(0),hasItems(1,2,3));
		assertThat(list.get(1),hasItems(3,4,5));
	}
	@Test
	public void grouped(){
		
		List<List<Integer>> list = anyM(Stream.of(1,2,3,4,5,6))
									.asSequence()
									.grouped(3)
									.collect(Collectors.toList());
		
		
		assertThat(list.get(0),hasItems(1,2,3));
		assertThat(list.get(1),hasItems(4,5,6));
	
	}
	@Test
	public void groupedOptional(){
		
		List<List<Integer>> list = anyM(Optional.of(Arrays.asList(1,2,3,4,5,6)))
											.<Integer>toSequence()
											.grouped(3)
											.collect(Collectors.toList());
		
		
		assertThat(list.get(0),hasItems(1,2,3));
		assertThat(list.get(1),hasItems(4,5,6));
		
	}
	
	@Test
	public void startsWith(){
		assertTrue(anyM(Stream.of(1,2,3,4))
						.asSequence()
						.startsWith(Arrays.asList(1,2,3)));
	}
	@Test
	public void startsWithIterator(){
		assertTrue(anyM(Stream.of(1,2,3,4)).asSequence().startsWith(Arrays.asList(1,2,3).iterator()));
	}
	@Test
	public void distinctOptional(){
		List<Integer> list = anyM(Optional.of(Arrays.asList(1,2,2,2,5,6)))
											.<Integer>toSequence()
											.distinct()
											.collect(Collectors.toList());
		
		
		assertThat(list.size(),equalTo(4));
	}
	@Test
    public void scanLeft() {
        assertEquals(
            asList("", "a", "ab", "abc"),
            anyM(Stream.of("a", "b", "c"))
            		.asSequence()
            		.scanLeft(Reducers.toString(""))
            		.toList());

        
    }
	@Test
    public void testCollectors() {
		List result = anyM(Stream.of(1,2,3))
							.asSequence()
							.collectStream(Stream.of(Collectors.toList(),Collectors.summingInt(Integer::intValue),Collectors.averagingInt(Integer::intValue)));
		
		assertThat(result.get(0),equalTo(Arrays.asList(1,2,3)));
		assertThat(result.get(1),equalTo(6));
		assertThat(result.get(2),equalTo(2.0));
    }
	
	@Test
	public void reducer1(){
		Monoid<Integer> sum = Monoid.of(0,(a,b)->a+b);
		Monoid<Integer> mult = Monoid.of(1,(a,b)->a*b);
		val result = anyM(Stream.of(1,2,3,4))
						.asSequence()
						.reduce(Arrays.asList(sum,mult).stream() );
				
		 
		assertThat(result,equalTo(Arrays.asList(10,24)));
	}
	@Test
	public void reducer2(){
		Monoid<Integer> sum = Monoid.of(0,(a,b)->a+b);
		Monoid<Integer> mult = Monoid.of(1,(a,b)->a*b);
		val result = anyM(Optional.of(Stream.of(1,2,3,4)))
						.<Integer>toSequence()
						.reduce(Arrays.asList(sum,mult) );
				
		 
		assertThat(result,equalTo(Arrays.asList(10,24)));
	}
	@Test
	public void reducer3(){
		Monoid<Integer> sum = Monoid.of(0,(a,b)->a+b);
		Monoid<Integer> mult = Monoid.of(1,(a,b)->a*b);
		val result = anyM(Optional.of(Stream.of()))
						.<Integer>toSequence()
						.reduce(Arrays.asList(sum,mult) );
				
		 
		assertThat(result,equalTo(Arrays.asList(0,1)));
	}
	@Test
	public void reducer4(){
		Monoid<Integer> sum = Monoid.of(0,(a,b)->a+b);
		Monoid<Integer> mult = Monoid.of(1,(a,b)->a*b);
		val result = anyM(Optional.empty())
						.<Integer>toSequence()
						.reduce(Arrays.asList(sum,mult) );
				
		 
		assertThat(result,equalTo(Arrays.asList(0,1)));
	}
	
	@Test
	public void aggregate(){
		List<Integer> result = anyM(Stream.of(1,2,3,4))
								.aggregate(anyM(Optional.of(5)))
								.asSequence()
								.toList();
		
		assertThat(result,equalTo(Arrays.asList(1,2,3,4,5)));
	}
	@Test
	public void aggregate2(){
		List<Integer> result = anyM(Optional.of(Arrays.asList(1,2,3,4)))
								.<Integer>aggregateUntyped(anyM(CompletableFuture.completedFuture(5)))
								.<Integer>toSequence()
								.toList();
		
		assertThat(result,equalTo(Arrays.asList(1,2,3,4,5)));
	}
	@Test
	public void aggregate3(){
		List<Integer> result = anyM(Optional.of(Arrays.asList(1,2,3,4)))
								.<Integer>aggregateUntyped(anyM(CompletableFuture.supplyAsync(()->Arrays.asList(5,6))))
								.<Integer>toSequence()
								.toList();
		
		assertThat(result,equalTo(Arrays.asList(1,2,3,4,5,6)));
	}
	
	@Test
	public void testApplyM(){
	 AnyM<Integer> applied =	anyM(Stream.of(1,2,3))
			 							.applyM(anyM(Streamable.of( (Integer a)->a+1 ,(Integer a) -> a*2)));
	
	 assertThat(applied.toSequence().toList(),equalTo(Arrays.asList(2, 2, 3, 4, 4, 6)));
	 
	}
	
	
	@Test
	public void testApplyMOptional(){
	 AnyM<Integer> applied =anyM(Optional.of(2)).applyM(anyM(Optional.of( (Integer a)->a+1)) );
	
	 assertThat(applied.toSequence().toList(),equalTo(Arrays.asList(3)));
	 
	}
	@Test
	public void testApplyMOptionalEmpty(){
	 AnyM<Integer> applied =anyM(Optional.of(2)).applyM(anyM(Optional.empty()));
	
	 assertThat(applied.toSequence().toList(),equalTo(Arrays.asList()));
	 
	}
	@Test
	public void testApplyMEmptyOptional(){
		AnyM<Integer> empty= 	anyM(Optional.empty());
		AnyM<Integer> applied =	empty.applyM(anyM(Optional.of((Integer a)->a+1)) );
	
		assertThat(applied.toSequence().toList(),equalTo(Arrays.asList()));
	 
	}
	@Test
	public void testSimpleFilter(){
	 AnyM<AnyM<Integer>> applied =anyM(Stream.of(1,2,3))
			 							.simpleFilter(anyM(Streamable.of( (Integer a)->a>5 ,(Integer a) -> a<3)));
	
	
	 assertThat(applied.map(s->s.asSequence().collect(Collectors.toList())).asSequence().toList(),equalTo(Arrays.asList(Arrays.asList(1), Arrays.asList(2),Arrays.asList())));
	
	}
	@Test
	public void testSimpleFilterStream(){
	 AnyM<Stream<Integer>> applied =anyM(Stream.of(1,2,3))
			 							.simpleFilter(Streamable.of( (Integer a)->a>5 ,(Integer a) -> a<3));
	
	
	 assertThat(applied.map(s->s.collect(Collectors.toList())).asSequence().toList(),equalTo(Arrays.asList(Arrays.asList(1), Arrays.asList(2),Arrays.asList())));
	
	}
	@Test
	public void testSimpleFilterOptional(){
	 AnyM<AnyM<Integer>> applied =anyM(Optional.of(2))
			 							.simpleFilter(anyM(Streamable.of( (Integer a)->a>5 ,(Integer a) -> a<3)));
	
	 assertThat(applied.toSequence().flatten().toList(),equalTo(Arrays.asList(2)));
	
	}
	
	@Test
	public void testReplicateM(){
		
		 AnyM<List<Integer>> applied =anyM(Optional.of(2)).replicateM(5);
		 assertThat(applied.unwrap(),equalTo(Optional.of(Arrays.asList(2,2,2,2,2))));
	
	}
	@Test
	public void testReplicateMStream(){
	
		AnyM<List<Integer>> replicated =anyM(Stream.of(2,3,4)).replicateM(5);
		
		assertThat(replicated.toSequence().toList(),
						equalTo(Arrays.asList(2,3,4 ,2,3,4 ,2,3,4 ,2,3,4 ,2,3,4)));
	
	}
	
	@Test
	public void testSorted(){
		assertThat(anyM(Stream.of(4,3,6,7)).asSequence().sorted().toList(),equalTo(Arrays.asList(3,4,6,7)));
	}
	@Test
	public void testSortedCompartor(){
		assertThat(anyM(Stream.of(4,3,6,7)).asSequence().sorted((a,b) -> b-a).toList(),equalTo(Arrays.asList(7,6,4,3)));
	}
	@Test
	public void testSkip(){
		assertThat(anyM(Stream.of(4,3,6,7)).asSequence().skip(2).toList(),equalTo(Arrays.asList(6,7)));
	}
	@Test
	public void testSkipUntil(){
		assertThat(anyM(Stream.of(4,3,6,7)).asSequence().skipUntil(i->i==6).toList(),equalTo(Arrays.asList(6,7)));
	}
	@Test
	public void testSkipWhile(){
		assertThat(anyM(Stream.of(4,3,6,7)).asSequence().sorted().skipWhile(i->i<6).toList(),equalTo(Arrays.asList(6,7)));
	}
	@Test
	public void testLimit(){
		assertThat(anyM(Stream.of(4,3,6,7)).asSequence().limit(2).toList(),equalTo(Arrays.asList(4,3)));
	}
	@Test
	public void testLimitUntil(){
		assertThat(anyM(Stream.of(4,3,6,7)).asSequence().limitUntil(i->i==6).toList(),equalTo(Arrays.asList(4,3)));
	}
	@Test
	public void testLimitWhile(){
		assertThat(anyM(Stream.of(4,3,6,7)).asSequence().sorted().limitWhile(i->i<6).toList(),equalTo(Arrays.asList(3,4)));
	}
	
	@Test
	public void testLiftMSimplex(){
		val lifted = AnyMonads.liftM((Integer a)->a+3);
		
		AnyM<Integer> result = lifted.apply(anyM(Optional.of(3)));
		
		assertThat(result.<Optional<Integer>>unwrap().get(),equalTo(6));
	}
	
	
	@Test
	public void testReduceM(){
		Monoid<Optional<Integer>> optionalAdd = Monoid.of(Optional.of(0), (a,b)-> Optional.of(a.get()+b.get()));
		
		assertThat(anyM(Stream.of(2,8,3,1)).reduceMOptional(optionalAdd).unwrap(),equalTo(Optional.of(14)));
	}
	
	@Test
	public void testLiftM2Simplex(){
		val lifted = AnyMonads.liftM2((Integer a,Integer b)->a+b);
		
		AnyM<Integer> result = lifted.apply(anyM(Optional.of(3)),anyM(Optional.of(4)));
		
		assertThat(result.<Optional<Integer>>unwrap().get(),equalTo(7));
	}
	@Test
	public void testLiftM2SimplexNull(){
		val lifted = AnyMonads.liftM2((Integer a,Integer b)->a+b);
		
		AnyM<Integer> result = lifted.apply(anyM(Optional.of(3)),anyM(Optional.ofNullable(null)));
		
		assertThat(result.<Optional<Integer>>unwrap().isPresent(),equalTo(false));
	}
	
	private Integer add(Integer a, Integer  b){
		return a+b;
	}
	@Test
	public void testLiftM2Mixed(){
		val lifted = AnyMonads.liftM2(this::add); 
		
		AnyM<Integer> result = lifted.apply(anyM(Optional.of(3)),anyM(Stream.of(4,6,7)));
		
		
		assertThat(result.<Optional<List<Integer>>>unwrap().get(),equalTo(Arrays.asList(7,9,10)));
	}
}
