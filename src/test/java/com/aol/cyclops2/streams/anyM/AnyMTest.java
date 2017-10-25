package com.aol.cyclops2.streams.anyM;


import static cyclops.monads.AnyM.fromStream;
import static cyclops.monads.AnyM.ofValue;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cyclops.collections.immutable.VectorX;
import cyclops.monads.Witness;
import cyclops.monads.function.AnyMFunction1;
import cyclops.monads.function.AnyMFunction2;
import cyclops.reactive.ReactiveSeq;
import org.junit.Test;

import cyclops.function.Monoid;
import cyclops.companion.Reducers;
import cyclops.monads.AnyM;
import cyclops.control.Maybe;
import com.aol.cyclops2.types.anyM.AnyMSeq;

import lombok.val;


public class AnyMTest {
	/** no longer compiles!
	@Test
	public void multiReturn(){
		AnyMValue<Integer> reactiveStream = AnyM.fromOptional(Optional.of(1))
									.flatMap(i->ReactiveSeq.of(1,2,i).anyM());
		
		reactiveStream.map(i->i+2);
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
	public void collectList(){

		assertThat(AnyM.fromList(Arrays.asList(1,2,2)).collect(Collectors.toSet()).size(),equalTo(2));
	}
	@Test
	public void flatMapWithListComprehender() {
	    List<Integer> list = Arrays.asList(1,2,3);
	    AnyMSeq<Witness.list,Integer> any = AnyM.fromList(list);
	    AnyM<Witness.list,Integer> mapped = any.flatMap(e -> any.unit(e));
	    List<Integer> unwrapped = mapped.unwrap();
	    assertEquals(list, unwrapped);
	}

	@Test
	public void testForEach() {
		   AnyM.fromStream(Stream.of(asList(1,3)))
				  				.flatMap(c->AnyM.fromArray(c))
				  				.stream()
				  				.forEach(System.out::println);
				  				
	}

	/** should no longer compile!
	@Test
	public void testForEachCfFlatMapToStream() {
		   AnyM.fromCompletableFuture(CompletableFuture.completedFuture(asList(1,3)))
		   						.flatMap(c->AnyM.fromStream(c.reactiveStream()))
		   						.reactiveStream()
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
	
	int count;
	@Test
	public void testCycleWhile(){
		count =0;
		assertThat(fromStream(Stream.of(1,2,2)).stream()
											.cycleWhile(next -> count++<6)
											.collect(Collectors.toList()),equalTo(Arrays.asList(1,2,2,1,2,2)));
	}
	@Test
	public void testCycleUntil(){
		count =0;
		assertThat(fromStream(Stream.of(1,2,2)).stream()
											.cycleUntil(next -> count++>6)
											.collect(Collectors.toList()),equalTo(Arrays.asList(1,2,2,1,2,2,1)));
	}
	@Test
	public void testCycleUntilReactiveSeq(){
		count =0;
		assertThat(fromStream(ReactiveSeq.of(1,2,2)).stream()
				.cycleUntil(next -> count++>6)
				.collect(Collectors.toList()),equalTo(Arrays.asList(1,2,2,1,2,2,1)));
	}
	@Test
	public void testCycle(){
		assertThat(fromStream(Stream.of(1,2,2)).stream()
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
		assertThat(fromStream(Stream.of(1,2,2))
							.map(b-> AnyM.fromArray(b))
							.to(AnyM::flatten)
							.stream()
							.toList(),equalTo(Arrays.asList(1,2,2)));
	}

	@Test
	public void testToSet(){
		assertThat(fromStream(Stream.of(1,2,2))
					.stream()
					.toSet().size(),equalTo(2));
	}
	@Test
	public void testToList(){
		assertThat(fromStream(Stream.of(1,2,3))
					.stream()
					.toList(),equalTo(Arrays.asList(1,2,3)));
	}
	@Test
	public void testCollect(){
		assertThat(fromStream(Stream.of(1,2,3))
					.stream()
					.collect(Collectors.toList()),equalTo(Arrays.asList(1,2,3)));
	}
	@Test
	public void testToListFlatten(){
		
	
		assertThat(AnyM.fromStream(Stream.of(1,2,3,null))
					.map(Maybe::ofNullable)
					.filter(Maybe::isPresent)
				    .map(Maybe::toOptional)
					.map(Optional::get)
					.stream()
					.toList(),equalTo(Arrays.asList(1,2,3)));
	
	
	}
	@Test
	public void testToListOptional(){
		assertThat(AnyM.fromOptional(Optional.of(1))
					.stream()
					.toList(),equalTo(Arrays.asList(1)));
	}
	
	@Test
    public void testFold() {
		 
       Supplier<AnyM<Witness.stream,String>> s = () -> fromStream(Stream.of("a","b","c"));

        assertThat("cba",equalTo( s.get().stream().foldRight(Reducers.toString(""))));
        assertThat("abc",equalTo( s.get().stream().reduce(Reducers.toString(""))));
        assertThat( 3,equalTo( s.get().map(i->""+i.length()).stream().foldRightMapToType(Reducers.toCountInt())));
        assertThat( 3,equalTo( s.get().map(i->""+i.length()).stream().mapReduce(Reducers.toCountInt())));
      
    }
	
	

	@Test
	public void traversableTest(){
		 List<List<Integer>> list = AnyM.fromOptional(Optional.of(Arrays.asList(1,2,3,4,5,6)))
								.stream()
								.collect(Collectors.toList());

		 
		 assertThat(list.get(0),hasItems(1,2,3,4,5,6));
	}

	
	@Test
	public void testFlatMap(){
		AnyMSeq<Witness.stream,List<Integer>> m  = AnyM.fromStream(Stream.of(Arrays.asList(1,2,3),Arrays.asList(1,2,3)));
		AnyM<Witness.stream,Integer> intM = m.flatMap( c -> fromStream(c.stream()));
		List<Integer> list = intM.stream().toList();
		assertThat(list,equalTo(Arrays.asList(1, 2, 3, 1, 2, 3)));
	}
	@Test
	public void testBind(){
		AnyM<Witness.stream,List<Integer>> m  = fromStream(Stream.of(Arrays.asList(1,2,3),Arrays.asList(1,2,3)));
		AnyM<Witness.stream,Integer> intM = m.flatMapS(Collection::stream);
		List<Integer> list = intM.stream().toList();
		assertThat(list,equalTo(Arrays.asList(1, 2, 3, 1, 2, 3)));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void zipOptional(){
		Stream<List<Integer>> zipped = fromStream(Stream.of(1,2,3))
										.stream()
										.zip(AnyM.fromOptional(Optional.of(2)),
											(a,b) -> Arrays.asList(a,b)).stream();
		
		
		List<Integer> zip = zipped.collect(Collectors.toList()).get(0);
		assertThat(zip.get(0),equalTo(1));
		assertThat(zip.get(1),equalTo(2));
		
	}
	@Test
	public void zipStream(){
		Stream<List<Integer>> zipped = fromStream(Stream.of(1,2,3))
											.stream()
											.zipS(Stream.of(2,3,4),
													(a,b) -> Arrays.asList(a,b))
													.stream();
		
		
		List<Integer> zip = zipped.collect(Collectors.toList()).get(1);
		assertThat(zip.get(0),equalTo(2));
		assertThat(zip.get(1),equalTo(3));
		
	}
	
	@Test
	public void sliding(){
		List<VectorX<Integer>> list = AnyM.fromStream(Stream.of(1,2,3,4,5,6))
									.stream()
									.sliding(2)
									.collect(Collectors.toList());
		
	
		assertThat(list.get(0),hasItems(1,2));
		assertThat(list.get(1),hasItems(2,3));
	}
	@Test
	public void slidingIncrement(){
		List<VectorX<Integer>> list = AnyM.fromStream(Stream.of(1,2,3,4,5,6))
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
	public void startsWith(){
		assertTrue(fromStream(Stream.of(1,2,3,4))
						.stream()
						.startsWithIterable(Arrays.asList(1,2,3)));
	}
	@Test
	public void startsWithIterator(){
		assertTrue(AnyM.fromStream(Stream.of(1,2,3,4)).stream().startsWith(Arrays.asList(1,2,3).stream()));
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
	public void aggregate(){
		List<Integer> result = AnyM.fromStream(Stream.of(1,2,3,4))
								.aggregate(AnyM.fromArray(5))
								.stream()
							    .flatMap(List::stream)
								.toList();
		
		assertThat(result,equalTo(Arrays.asList(1,2,3,4,5)));
	}
	@Test
	public void aggregate2(){
		List<Integer> result = AnyM.fromOptional(Optional.of(1))
								.aggregate(AnyM.ofNullable(2))
								.stream()
								.toList().get(0);
		
		assertThat(result,equalTo(Arrays.asList(1,2)));
	}



		@Test
	public void testSorted(){
		assertThat(fromStream(Stream.of(4,3,6,7)).stream().sorted().toList(),equalTo(Arrays.asList(3,4,6,7)));
	}
	@Test
	public void testSortedCompartor(){
		assertThat(AnyM.fromStream(Stream.of(4,3,6,7)).stream().sorted((a, b) -> b-a).toList(),equalTo(Arrays.asList(7,6,4,3)));
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
		AnyMFunction1<Witness.optional,Integer,Integer> lifted = AnyM.liftF((Integer a)->a+3);
		
		AnyM<Witness.optional,Integer> result = lifted.apply(AnyM.ofNullable(3));
		
		assertThat(result.<Optional<Integer>>unwrap().get(),equalTo(6));
	}
	
	

	@Test
	public void testLiftM2Simplex(){
		AnyMFunction2<Witness.optional,Integer,Integer,Integer> lifted = AnyM.liftF2((Integer a, Integer b)->a+b);
		
		AnyM<Witness.optional,Integer> result = lifted.apply(AnyM.ofNullable(3),AnyM.ofNullable(4));
		
		assertThat(result.<Optional<Integer>>unwrap().get(),equalTo(7));
	}
	@Test
    public void testLiftM2AnyMValue(){
		AnyMFunction2<Witness.optional,Integer,Integer,Integer> lifted = AnyM.liftF2((Integer a, Integer b)->a+b);
        
        AnyM<Witness.optional,Integer> result = lifted.apply(AnyM.ofNullable(3),AnyM.ofNullable(4));
        
        assertThat(result.<Optional<Integer>>unwrap().get(),equalTo(7));
    }
	@Test
	public void testLiftM2SimplexNull(){
		AnyMFunction2<Witness.optional,Integer,Integer,Integer> lifted = AnyM.liftF2((Integer a, Integer b)->a+b);
		
		AnyM<Witness.optional,Integer> result = lifted.apply(AnyM.ofNullable(3),AnyM.ofNullable(null));
		
		assertThat(result.<Optional<Integer>>unwrap().isPresent(),equalTo(false));
	}
	
	private Integer add(Integer a, Integer  b){
		return a+b;
	}
	@Test
	public void testLiftM2Mixed(){
		AnyMFunction2<Witness.optional,Integer,Integer,Integer> lifted = AnyM.liftF2(this::add);
		
		AnyM<Witness.optional,Integer> result = lifted.apply(AnyM.ofNullable(3),AnyM.ofNullable(4));
		
		
		assertThat(result.<Optional<List<Integer>>>unwrap().get(),equalTo(7));
	}
}
