package com.aol.cyclops.lambda.monads;

import static com.aol.cyclops.lambda.api.AsGenericMonad.asMonad;
import static com.aol.cyclops.lambda.api.AsGenericMonad.monad;
import static com.aol.cyclops.lambda.api.AsAnyM.anyM;
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
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import lombok.val;

import org.junit.Test;

import com.aol.cyclops.lambda.api.AsGenericMonad;
import com.aol.cyclops.lambda.api.AsAnyM;
import com.aol.cyclops.lambda.api.Monoid;
import com.aol.cyclops.lambda.api.Reducers;
import com.aol.cyclops.lambda.api.Streamable;
import com.aol.cyclops.streams.StreamUtils;


public class AnyMTest {

	 
	
	@Test
	public void test() {
		val list = MonadWrapper.<Stream<Integer>,List<Integer>>of(Stream.of(Arrays.asList(1,3)))
				.flatMap(Collection::stream).unwrap()
				.map(i->i*2)
				.peek(System.out::println)
				.collect(Collectors.toList());
		assertThat(Arrays.asList(2,6),equalTo(list));
	}
	@Test
	public void testMixed() {
		
		List<Integer> list = MonadWrapper.<Stream<Integer>,List<Integer>>of(Stream.of(Arrays.asList(1,3),null))
				.bind(Optional::ofNullable)
				.map(i->i.size())
				.peek(System.out::println)
				.toList();
		assertThat(Arrays.asList(2),equalTo(list));
	}
	int count;
	@Test
	public void testCycleWhile(){
		count =0;
		assertThat(MonadWrapper.<Stream<Integer>,Integer>of(Stream.of(1,2,2))
											.cycleWhile(next -> count++<6)
											.collect(Collectors.toList()),equalTo(Arrays.asList(1,2,2,1,2,2)));
	}
	@Test
	public void testCycle(){
		assertThat(MonadWrapper.<Integer,Stream<Integer>>of(Stream.of(1,2,2))
											.cycle(3).collect(Collectors.toList()),equalTo(Arrays.asList(1,2,2,1,2,2,1,2,2)));
	}
	@Test
	public void testCycleReduce(){
		assertThat(MonadWrapper.<Stream<Integer>,Integer>of(Stream.of(1,2,2))
											.cycle(Reducers.toCountInt(),3)
											.collect(Collectors.toList()),
											equalTo(Arrays.asList(3,3,3)));
	}
	@Test
	public void testCycleMonad(){
		
		assertThat(MonadWrapper.<Stream<Integer>,Integer>of(Stream.of(1,2))
											.cycle(Optional.class,2)
											.collect(Collectors.toList()),
											equalTo(asList(Optional.of(1),Optional.of(2)
												,Optional.of(1),Optional.of(2)	)));
	}
	@Test
	public void testJoin(){
		assertThat(MonadWrapper.<Stream<Integer>,Integer>of(Stream.of(1,2,2)).map(b-> Stream.of(b)).flatten().toList(),equalTo(Arrays.asList(1,2,2)));
	}
	@Test
	public void testJoin2(){
		assertThat(MonadWrapper.<Stream<Integer>,Integer>of(Stream.of(asList(1,2),asList(2))).flatten().toList(),equalTo(Arrays.asList(1,2,2)));
	}
	
	@Test
	public void testToSet(){
		assertThat(MonadWrapper.<Stream<Integer>,Integer>of(Stream.of(1,2,2)).toSet().size(),equalTo(2));
	}
	@Test
	public void testToList(){
		assertThat(MonadWrapper.<Stream<Integer>,Integer>of(Stream.of(1,2,3)).toList(),equalTo(Arrays.asList(1,2,3)));
	}
	@Test
	public void testCollect(){
		assertThat(MonadWrapper.<Stream<Integer>,Integer>of(Stream.of(1,2,3)).collect(Collectors.toList()),equalTo(Arrays.asList(1,2,3)));
	}
	@Test
	public void testToListFlatten(){
		assertThat(MonadWrapper.<Stream<Integer>,Integer>of(Stream.of(1,2,3,null)).bind(Optional::ofNullable).toList(),equalTo(Arrays.asList(1,2,3)));
	}
	@Test
	public void testToListOptional(){
		assertThat(MonadWrapper.<Stream<Integer>,Integer>of(Optional.of(1)).toList(),equalTo(Arrays.asList(1)));
	}
	
	@Test
    public void testFold() {
		 
       Supplier<Monad<Stream<String>,String>> s = () -> AsGenericMonad.asMonad(Stream.of("a","b","c"));

        assertThat("cba",equalTo( s.get().foldRight(Reducers.toString(""))));
        assertThat("abc",equalTo( s.get().foldLeft(Reducers.toString(""))));
        assertThat( 3,equalTo( s.get().map(i->""+i.length()).foldRightMapToType(Reducers.toCountInt())));
        assertThat( 3,equalTo( s.get().map(i->""+i.length()).foldLeftMapToType(Reducers.toCountInt())));
      
    }
	
	@Test
	public void testLift(){
		
		
		List<String> result = AsGenericMonad.<Stream<String>,String>asMonad(Stream.of("input.file"))
								.map(getClass().getClassLoader()::getResource)
								.peek(System.out::println)
								.map(URL::getFile)
								.<Stream<String>,String>liftAndBind(File::new)
								.toList();
		
		assertThat(result,equalTo(Arrays.asList("hello","world")));
	}
	
	
	
	@Test
	public void testSequenceNative(){
		
        List<Integer> list = IntStream.range(0, 100).boxed().collect(Collectors.toList());
        List<CompletableFuture<Integer>> futures = list
                .stream()
                .map(x -> CompletableFuture.supplyAsync(() -> x))
                .collect(Collectors.toList());

        
        CompletableFuture<List<Integer>> futureList = Monads.sequenceNative(CompletableFuture.class, futures);
   
        List<Integer> collected = futureList.join();
        assertThat(collected.size(),equalTo( list.size()));
        
        for(Integer next : list){
        	assertThat(list.get(next),equalTo( collected.get(next)));
        }
        
	}
	
	@Test
	public void testTraverseNative(){
		
        List<Integer> list = IntStream.range(0, 100).boxed().collect(Collectors.toList());
        List<CompletableFuture<Integer>> futures = list
                .stream()
                .map(x -> CompletableFuture.supplyAsync(() -> x))
                .collect(Collectors.toList());

       
        CompletableFuture<List<String>> futureList = Monads.traverseNative(CompletableFuture.class, futures, (Integer i) -> "hello" +i);
   
        List<String> collected = futureList.join();
        assertThat(collected.size(),equalTo( list.size()));
        
        for(Integer next : list){
        	assertThat("hello"+list.get(next),equalTo( collected.get(next)));
        }
        
	}
	@Test
	public void testSequence(){
		
        List<Integer> list = IntStream.range(0, 100).boxed().collect(Collectors.toList());
        List<CompletableFuture<Integer>> futures = list
                .stream()
                .map(x -> CompletableFuture.supplyAsync(() -> x))
                .collect(Collectors.toList());
       
        
        AnyM<List<Integer>> futureList = Monads.sequence(CompletableFuture.class,futures).anyM();
        
 
        List<Integer> collected = futureList.<CompletableFuture<List<Integer>>>unwrap().join();
        assertThat(collected.size(),equalTo( list.size()));
        
        for(Integer next : list){
        	assertThat(list.get(next),equalTo( collected.get(next)));
        }
        
	}
	@Test
	public void testSequenceList(){
		
        
       
        
        AnyM<List<Integer>> futureList = Monads.sequence(Stream.class,Arrays.asList(Arrays.asList(1,2),Arrays.asList(3,4))).anyM();
        
 
        assertThat(futureList.toList(),equalTo(Arrays.asList(1,2,3,4)));
        
	}
	@Test
	public void testSequenceStream(){
	
        
       
        
        AnyM<List<Stream<Integer>>> result = Monads.sequence(Stream.class,Arrays.asList(Stream.of(1,2),Stream.of(3,4))).anyM();
        
 
       
        assertThat(result
        		      .toList(),
        				equalTo(Arrays.asList(1,2,3,4)));
        
	}
	@Test
	public void testSequenceOptional(){
		
        
       
        
        AnyM<List<Integer>> futureList = Monads.sequence(Optional.class,
        						Arrays.asList(Optional.of(7),Optional.of(8),Optional.of(9))).anyM();
        
 
        assertThat(futureList.toList(),equalTo(Arrays.asList(7,8,9)));
        
	}
	
	@Test
	public void testTraverse(){
		
        List<Integer> list = IntStream.range(0, 100).boxed().collect(Collectors.toList());
        List<CompletableFuture<Integer>> futures = list
                .stream()
                .map(x -> CompletableFuture.supplyAsync(() -> x))
                .collect(Collectors.toList());

       
        AnyM<List<String>> futureList = Monads.traverse(CompletableFuture.class, futures, (Integer i) -> "hello" +i).anyM();
   
        List<String> collected = futureList.<CompletableFuture<List<String>>>unwrap().join();
        assertThat(collected.size(),equalTo( list.size()));
        
        for(Integer next : list){
        	assertThat("hello"+list.get(next),equalTo( collected.get(next)));
        }
        
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void zipOptional(){
		Stream<List<Integer>> zipped = asMonad(Stream.of(1,2,3)).zip(asMonad(Optional.of(2)), 
													(a,b) -> Arrays.asList(a,b)).toStream();
		
		
		List<Integer> zip = zipped.collect(Collectors.toList()).get(0);
		assertThat(zip.get(0),equalTo(1));
		assertThat(zip.get(1),equalTo(2));
		
	}
	@Test
	public void zipStream(){
		Stream<List<Integer>> zipped = monad(Stream.of(1,2,3)).zip(Stream.of(2,3,4), 
													(a,b) -> Arrays.asList(a,b))
													.stream();
		
		
		List<Integer> zip = zipped.collect(Collectors.toList()).get(1);
		assertThat(zip.get(0),equalTo(2));
		assertThat(zip.get(1),equalTo(3));
		
	}
	
	@Test
	public void sliding(){
		List<List<Integer>> list = monad(Stream.of(1,2,3,4,5,6)).sliding(2).collect(Collectors.toList());
		
		
		assertThat(list.get(0),hasItems(1,2));
		assertThat(list.get(1),hasItems(2,3));
	}
	@Test
	public void grouped(){
		List<List<Integer>> list = monad(Stream.of(1,2,3,4,5,6)).grouped(3).collect(Collectors.toList());
		
		
		assertThat(list.get(0),hasItems(1,2,3));
		assertThat(list.get(1),hasItems(4,5,6));
	}
	@Test
	public void groupedOptional(){
		List<List<Integer>> list = monad(Optional.of(Arrays.asList(1,2,3,4,5,6)))
											.<Stream<Integer>,Integer>streamedMonad()
											.grouped(3).collect(Collectors.toList());
		
		
		assertThat(list.get(0),hasItems(1,2,3));
		assertThat(list.get(1),hasItems(4,5,6));
	}
	
	@Test
	public void startsWith(){
		assertTrue(monad(Stream.of(1,2,3,4)).startsWith(Arrays.asList(1,2,3)));
	}
	@Test
	public void startsWithIterator(){
		assertTrue(monad(Stream.of(1,2,3,4)).startsWith(Arrays.asList(1,2,3).iterator()));
	}
	@Test
	public void distinctOptional(){
		List<Integer> list = monad(Optional.of(Arrays.asList(1,2,2,2,5,6)))
											.<Stream<Integer>,Integer>streamedMonad()
											.distinct().collect(Collectors.toList());
		
		
		assertThat(list.size(),equalTo(4));
	}
	@Test
    public void scanLeft() {
        assertEquals(
            asList("", "a", "ab", "abc"),
            monad(Stream.of("a", "b", "c")).scanLeft(Reducers.toString("")).toList());

        
    }
	@Test
    public void testCollectors() {
		List result = monad(Stream.of(1,2,3)).collect(Stream.of(Collectors.toList(),Collectors.summingInt(Integer::intValue),Collectors.averagingInt(Integer::intValue)));
		
		assertThat(result.get(0),equalTo(Arrays.asList(1,2,3)));
		assertThat(result.get(1),equalTo(6));
		assertThat(result.get(2),equalTo(2.0));
    }
	
	@Test
	public void reducer1(){
		Monoid<Integer> sum = Monoid.of(0,(a,b)->a+b);
		Monoid<Integer> mult = Monoid.of(1,(a,b)->a*b);
		val result = monad(Stream.of(1,2,3,4))
						.reduce(Arrays.asList(sum,mult).stream() );
				
		 
		assertThat(result,equalTo(Arrays.asList(10,24)));
	}
	@Test
	public void reducer2(){
		Monoid<Integer> sum = Monoid.of(0,(a,b)->a+b);
		Monoid<Integer> mult = Monoid.of(1,(a,b)->a*b);
		val result = monad(Optional.of(Stream.of(1,2,3,4)))
						.<Stream<Integer>,Integer>streamedMonad()
						.reduce(Arrays.asList(sum,mult) );
				
		 
		assertThat(result,equalTo(Arrays.asList(10,24)));
	}
	@Test
	public void reducer3(){
		Monoid<Integer> sum = Monoid.of(0,(a,b)->a+b);
		Monoid<Integer> mult = Monoid.of(1,(a,b)->a*b);
		val result = monad(Optional.of(Stream.of()))
						.<Stream<Integer>,Integer>streamedMonad()
						.reduce(Arrays.asList(sum,mult) );
				
		 
		assertThat(result,equalTo(Arrays.asList(0,1)));
	}
	@Test
	public void reducer4(){
		Monoid<Integer> sum = Monoid.of(0,(a,b)->a+b);
		Monoid<Integer> mult = Monoid.of(1,(a,b)->a*b);
		val result = monad(Optional.empty())
						.<Stream<Integer>,Integer>streamedMonad()
						.reduce(Arrays.asList(sum,mult) );
				
		 
		assertThat(result,equalTo(Arrays.asList(0,1)));
	}
	
	@Test
	public void aggregate(){
		List<Integer> result = monad(Stream.of(1,2,3,4)).<Integer>aggregate(monad(Optional.of(5))).toList();
		
		assertThat(result,equalTo(Arrays.asList(1,2,3,4,5)));
	}
	@Test
	public void aggregate2(){
		List<Integer> result = monad(Optional.of(Arrays.asList(1,2,3,4)))
								.<Integer>aggregate(monad(CompletableFuture.completedFuture(5)))
								.toList();
		
		assertThat(result,equalTo(Arrays.asList(1,2,3,4,5)));
	}
	@Test
	public void aggregate3(){
		List<Integer> result = monad(Optional.of(Arrays.asList(1,2,3,4)))
								.<Integer>aggregate(monad(CompletableFuture.supplyAsync(()->Arrays.asList(5,6))))
								.toList();
		
		assertThat(result,equalTo(Arrays.asList(1,2,3,4,5,6)));
	}
	
	@Test
	public void testApplyM(){
	 AnyM<Integer> applied =monad(Stream.of(1,2,3)).applyM(monad(Streamable.of( (Integer a)->a+1 ,(Integer a) -> a*2))).anyM();
	
	 assertThat(applied.toList(),equalTo(Arrays.asList(2, 2, 3, 4, 4, 6)));
	 
	}
	@Test
	public void testApplyMOptional(){
	 AnyM<Integer> applied =monad(Optional.of(2)).applyM(monad(Optional.of( (Integer a)->a+1)) ).anyM();
	
	 assertThat(applied.toList(),equalTo(Arrays.asList(3)));
	 
	}
	@Test
	public void testApplyMOptionalEmpty(){
	 AnyM<Integer> applied =monad(Optional.of(2)).applyM(monad(Optional.empty())).<Integer>anyM();
	
	 assertThat(applied.toList(),equalTo(Arrays.asList()));
	 
	}
	@Test
	public void testApplyMEmptyOptional(){
		AnyM<Integer> empty= 	monad(Optional.empty()).anyM();
		AnyM<Integer> applied =	empty.applyM(anyM(Optional.of((Integer a)->a+1)) ).anyM();
	
		assertThat(applied.toList(),equalTo(Arrays.asList()));
	 
	}

	@Test
	public void testSimpleFilter(){
	 AnyM<Stream<Integer>> applied =monad(Stream.of(1,2,3)).simpleFilter(monad(Streamable.of( (Integer a)->a>5 ,(Integer a) -> a<3))).anyM();
	
	// System.out.println(applied.toList());
	 assertThat(applied.map(s->s.collect(Collectors.toList())).toList(),equalTo(Arrays.asList(Arrays.asList(1), Arrays.asList(2),Arrays.asList())));
	
	}
	@Test
	public void testSimpleFilterOptional(){
	 AnyM<Optional<Integer>> applied =monad(Optional.of(2)).simpleFilter(monad(Streamable.of( (Integer a)->a>5 ,(Integer a) -> a<3))).anyM();
	
	 assertThat(applied.toList(),equalTo(Arrays.asList(2)));
	
	}
	
	@Test
	public void testReplicateM(){
		 AnyM<List<Integer>> applied =monad(Optional.of(2)).replicateM(5).anyM();
		 assertThat(applied.unwrap(),equalTo(Optional.of(Arrays.asList(2,2,2,2,2))));
	}
	@Test
	public void testReplicateMStream(){
		 AnyM<Integer> applied =monad(Stream.of(2,3,4)).replicateM(5).anyM();
		 assertThat(applied.toList(),equalTo(Arrays.asList(2,3,4,2,3,4,2,3,4,2,3,4,2,3,4)));
	}
	
	@Test
	public void testSorted(){
		assertThat(monad(Stream.of(4,3,6,7)).sorted().toList(),equalTo(Arrays.asList(3,4,6,7)));
	}
	@Test
	public void testSortedCompartor(){
		assertThat(monad(Stream.of(4,3,6,7)).sorted((a,b) -> b-a).toList(),equalTo(Arrays.asList(7,6,4,3)));
	}
	@Test
	public void testSkip(){
		assertThat(monad(Stream.of(4,3,6,7)).skip(2).toList(),equalTo(Arrays.asList(6,7)));
	}
	@Test
	public void testSkipUntil(){
		assertThat(monad(Stream.of(4,3,6,7)).skipUntil(i->i==6).toList(),equalTo(Arrays.asList(6,7)));
	}
	@Test
	public void testSkipWhile(){
		assertThat(monad(Stream.of(4,3,6,7)).sorted().skipWhile(i->i<6).toList(),equalTo(Arrays.asList(6,7)));
	}
	@Test
	public void testLimit(){
		assertThat(monad(Stream.of(4,3,6,7)).limit(2).toList(),equalTo(Arrays.asList(4,3)));
	}
	@Test
	public void testLimitUntil(){
		assertThat(monad(Stream.of(4,3,6,7)).limitUntil(i->i==6).toList(),equalTo(Arrays.asList(4,3)));
	}
	@Test
	public void testLimitWhile(){
		assertThat(monad(Stream.of(4,3,6,7)).sorted().limitWhile(i->i<6).toList(),equalTo(Arrays.asList(3,4)));
	}
	@Test
	public void testLiftM(){
		Function<Monad<Optional<Integer>,Integer>,Monad<Optional<Integer>,Integer>> lifted = Monads.liftMonad((Integer a)->a+3);
		
		Monad<Optional<Integer>, Integer> result = lifted.apply(monad(Optional.of(3)));
		
		assertThat(result.unwrap().get(),equalTo(6));
	}
	@Test
	public void testLiftMSimplex(){
		val lifted = Monads.liftM((Integer a)->a+3);
		
		AnyM<Integer> result = lifted.apply(anyM(Optional.of(3)));
		
		assertThat(result.<Optional<Integer>>unwrap().get(),equalTo(6));
	}
	@Test
	public void testLiftMNative(){
		Function<Optional<Integer>,Optional<Integer>> lifted = Monads.liftMNative((Integer a)->a+3);
		
		Optional<Integer> result = lifted.apply(Optional.of(3));
		
		assertThat(result.get(),equalTo(6));
	}
	
	@Test
	public void testReduceM(){
		Monoid<Optional<Integer>> optionalAdd = Monoid.of(Optional.of(0), (a,b)-> Optional.of(a.get()+b.get()));
		
		assertThat(monad(Stream.of(2,8,3,1)).reduceM(optionalAdd).unwrap(),equalTo(Optional.of(14)));
	}
	
	@Test
	public void testLiftM2(){
		BiFunction<Monad<Optional<Integer>,Integer>,Monad<Optional<Integer>,Integer>,Monad<Optional<Integer>,Integer>> lifted =
								Monads.liftMonad2((Integer a, Integer b)->a+3+b);
		
		Monad<Optional<Integer>, Integer> result = lifted.apply(monad(Optional.of(3)),monad(Optional.of(3)));
		
		assertThat(result.unwrap().get(),equalTo(9));
	}
	@Test
	public void testLiftM2Simplex(){
		val lifted = Monads.liftM2((Integer a,Integer b)->a+b);
		
		AnyM<Integer> result = lifted.apply(anyM(Optional.of(3)),anyM(Optional.of(4)));
		
		assertThat(result.<Optional<Integer>>unwrap().get(),equalTo(7));
	}
	@Test
	public void testLiftM2SimplexNull(){
		val lifted = Monads.liftM2((Integer a,Integer b)->a+b);
		
		AnyM<Integer> result = lifted.apply(anyM(Optional.of(3)),anyM(Optional.ofNullable(null)));
		
		assertThat(result.<Optional<Integer>>unwrap().isPresent(),equalTo(false));
	}
	@Test
	public void testLiftM2Native(){
		BiFunction<Optional<Integer>,Optional<Integer>,Optional<Integer>> lifted = Monads.liftMNative2((Integer a, Integer b)->a*b);
		
		Optional<Integer> result = lifted.apply(Optional.of(3),Optional.of(6));
		
		assertThat(result.get(),equalTo(18));
	}
	private Integer add(Integer a, Integer  b){
		return a+b;
	}
	@Test
	public void testLiftM2Mixed(){
		val lifted = Monads.liftM2(this::add); 
		
		AnyM<Integer> result = lifted.apply(anyM(Optional.of(3)),anyM(Stream.of(4,6,7)));
		
		
		assertThat(result.<Optional<List<Integer>>>unwrap().get(),equalTo(Arrays.asList(7,9,10)));
	}
}
