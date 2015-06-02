package com.aol.cyclops.lambda.monads;

import static com.aol.cyclops.lambda.api.AsGenericMonad.asMonad;
import static com.aol.cyclops.lambda.api.AsGenericMonad.monad;

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

import com.aol.cyclops.lambda.api.AsGenericMonad;
import com.aol.cyclops.lambda.api.Monoid;
import com.aol.cyclops.lambda.api.Reducers;


public class MonadTest {

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

        
        CompletableFuture<List<Integer>> futureList = Monad.sequenceNative(CompletableFuture.class, futures);
   
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

       
        CompletableFuture<List<String>> futureList = Monad.traverseNative(CompletableFuture.class, futures, (Integer i) -> "hello" +i);
   
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
       
        
        Simplex<List<Integer>> futureList = Monad.sequence(CompletableFuture.class,futures).simplex();
        
 
        List<Integer> collected = futureList.<CompletableFuture<List<Integer>>>monad().join();
        assertThat(collected.size(),equalTo( list.size()));
        
        for(Integer next : list){
        	assertThat(list.get(next),equalTo( collected.get(next)));
        }
        
	}
	
	@Test
	public void testTraverse(){
		
        List<Integer> list = IntStream.range(0, 100).boxed().collect(Collectors.toList());
        List<CompletableFuture<Integer>> futures = list
                .stream()
                .map(x -> CompletableFuture.supplyAsync(() -> x))
                .collect(Collectors.toList());

       
        Simplex<List<String>> futureList = Monad.traverse(CompletableFuture.class, futures, (Integer i) -> "hello" +i).simplex();
   
        List<String> collected = futureList.<CompletableFuture<List<String>>>monad().join();
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

}
