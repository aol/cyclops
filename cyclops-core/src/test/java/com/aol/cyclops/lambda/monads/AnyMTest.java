package com.aol.cyclops.lambda.monads;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import org.junit.Test;

import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.monad.AnyM;
import com.aol.cyclops.sequence.SequenceM;

import lombok.val;



public class AnyMTest {
	@Test
	public void createAnyMFromListOrOptional(){
		List<Integer> list = Arrays.asList(1,2,3);
		assertThat(AnyM.ofMonad(list).unwrap(),instanceOf(List.class));
		Optional<Integer> opt = Optional.of(1);
		assertThat(AnyM.ofMonad(opt).unwrap(),instanceOf(Optional.class));
	}
	@Test
	public void createAnyMFromListOrOptionalAsAnyM(){
		List<Integer> list = Arrays.asList(1,2,3);
		assertThat(AnyM.ofMonad(list).unwrap(),instanceOf(List.class));
		Optional<Integer> opt = Optional.of(1);
		assertThat(AnyM.ofMonad(opt).unwrap(),instanceOf(Optional.class));
	}
	@Test
	public void flatMapWithListComprehender() {
	    List<Integer> list = Arrays.asList(1,2,3);
	    AnyM<Integer> any = AnyM.fromList(list); 
	    AnyM<Integer> mapped = any.flatMap(e -> any.unit(e));
	    List<Integer> unwrapped = mapped.unwrap();
	    assertEquals(list, unwrapped);
	}
	@Test
	public void fromStreamLong(){
		AnyM<Long> stream = AnyM.fromLongStream(LongStream.of(1));
		assertThat(stream.unwrap(),instanceOf(Stream.class));
	}
	@Test
	public void fromStreamDouble(){
		AnyM<Double> stream = AnyM.fromDoubleStream(DoubleStream.of(1));
		assertThat(stream.unwrap(),instanceOf(Stream.class));
	}
	@Test
	public void fromStreamInt(){
		AnyM<Integer> stream = AnyM.fromIntStream(IntStream.of(1));
		assertThat(stream.unwrap(),instanceOf(Stream.class));
	}
	@Test
	public void fromStream(){
		AnyM<Integer> stream = AnyM.fromStream(Stream.of(1));
		assertThat(stream.unwrap(),instanceOf(Stream.class));
	}
	@Test
	public void fromOptionalLong(){
		AnyM<Long> opt = AnyM.fromOptionalLong(OptionalLong.of(1));
		assertThat(opt.unwrap(),instanceOf(Optional.class));
	}
	@Test
	public void fromOptionalDouble(){
		AnyM<Double> opt = AnyM.fromOptionalDouble(OptionalDouble.of(1));
		assertThat(opt.unwrap(),instanceOf(Optional.class));
	}
	@Test
	public void fromOptionalInt(){
		AnyM<Integer> opt = AnyM.fromOptionalInt(OptionalInt.of(1));
		assertThat(opt.unwrap(),instanceOf(Optional.class));
	}
	@Test
	public void fromOptional(){
		AnyM<Integer> opt = AnyM.fromOptional(Optional.of(1));
		assertThat(opt.unwrap(),instanceOf(Optional.class));
	}
	@Test
	public void fromCompletableFuture(){
		AnyM<Integer> future = AnyM.fromCompletableFuture(CompletableFuture.supplyAsync(()->1));
		assertThat(future.unwrap(),instanceOf(CompletableFuture.class));
	}
	
	@Test
	public void ofNullable(){
		AnyM<Integer> opt = AnyM.ofNullable(null);
		assertThat(opt.unwrap(),instanceOf(Optional.class));
	}
	@Test
	public void ofMonad(){
		AnyM<Integer> opt = AnyM.ofMonad(Optional.of(1));
		assertThat(opt.unwrap(),instanceOf(Optional.class));
	}
	@Test
	public void ofConvertable(){
		AnyM<Integer> future = AnyM.ofConvertable((Supplier<Integer>)()->1);
		assertThat(future.unwrap(),instanceOf(CompletableFuture.class));
	}
	@Test
	public void testLisOfMonad(){
		AnyM<Integer> list = AnyM.ofMonad(Arrays.asList(1,2,3));
		assertThat(list.unwrap(),instanceOf(List.class));
	}
	
	@Test
	public void testListFromList(){
		AnyM<Integer> list = AnyM.fromList(Arrays.asList(1,2,3));
		assertThat(list.unwrap(),instanceOf(List.class));
	}
	@Test
	public void testList(){
		AnyM<Integer> list = AnyM.fromIterable(Arrays.asList(1,2,3));
		assertThat(list.unwrap(),instanceOf(List.class));
	}
	@Test
	public void testListMap(){
		AnyM<Integer> list = AnyM.fromIterable(Arrays.asList(1,2,3));
		assertThat(list.map(i->i+2).unwrap(),equalTo(Arrays.asList(3,4,5)));
	}
	
	
	@Test
	public void testListFilter(){
		AnyM<Integer> list = AnyM.fromIterable(Arrays.asList(1,2,3));
		assertThat(list.filter(i->i<3).unwrap(),equalTo(Arrays.asList(1,2)));
	}
	@Test
	public void testSet(){
		AnyM<Integer> set = AnyM.fromIterable(new HashSet<>(Arrays.asList(1,2,3)));
		assertThat(set.unwrap(),instanceOf(Set.class));
	}
	@Test
	public void testSetMap(){
		AnyM<Integer> set = AnyM.fromIterable(new HashSet<>(Arrays.asList(1,2,3)));
		assertThat(set.map(i->i+2).unwrap(),equalTo(new HashSet<>(Arrays.asList(3,4,5))));
		
	}
	
	
	@Test
	public void testSetFilter(){
		AnyM<Integer> set = AnyM.fromIterable(new HashSet<>(Arrays.asList(1,2,3)));
		System.out.println(set.filter(i->i<3).unwrap().getClass());
		assertThat(set.filter(i->i<3).unwrap(),equalTo(new HashSet<>(Arrays.asList(1,2))));
	}
	
	@Test
	public void testSequence(){
		
        List<Integer> list = IntStream.range(0, 100).boxed().collect(Collectors.toList());
        List<CompletableFuture<Integer>> futures = list
                .stream()
                .map(x -> CompletableFuture.supplyAsync(() -> x))
                .collect(Collectors.toList());
       
        
        AnyM<ListX<Integer>> futureList = AnyM.sequence(AnyM.listFromCompletableFuture(futures));
        
 
        List<Integer> collected = futureList.<CompletableFuture<List<Integer>>>unwrap().join();
        assertThat(collected.size(),equalTo( list.size()));
        
        for(Integer next : list){
        	assertThat(list.get(next),equalTo( collected.get(next)));
        }
        
	}
	@Test
	public void testSequenceStream(){
		
		 List<Integer> list = IntStream.range(0, 100).boxed().collect(Collectors.toList());
	        List<CompletableFuture<Integer>> futures = list
	                .stream()
	                .map(x -> CompletableFuture.supplyAsync(() -> x))
	                .collect(Collectors.toList());
       
        
        AnyM<SequenceM<Integer>> futureList = AnyM.sequence(AnyM.listFromCompletableFuture(futures).stream());
        
 
        List<Integer> collected = futureList.<CompletableFuture<List<Integer>>>unwrap().join();
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

       
        AnyM<ListX<String>> futureList = AnyM.traverse( AnyM.listFromCompletableFuture(futures), (Integer i) -> "hello" +i);
   
        List<String> collected = futureList.<CompletableFuture<List<String>>>unwrap().join();
        assertThat(collected.size(),equalTo( list.size()));
        
        for(Integer next : list){
        	assertThat("hello"+list.get(next),equalTo( collected.get(next)));
        }
        
	}
	

	@Test
	public void testReplicateM(){
		
		 AnyM<List<Integer>> applied =AnyM.fromOptional(Optional.of(2)).replicateM(5);
		 assertThat(applied.unwrap(),equalTo(Optional.of(Arrays.asList(2,2,2,2,2))));
	
	}

	@Test
	public void testLiftMSimplex(){
		val lifted = AnyM.liftM((Integer a)->a+3);
		
		AnyM<Integer> result = lifted.apply(AnyM.fromOptional(Optional.of(3)));
		
		assertThat(result.<Optional<Integer>>unwrap().get(),equalTo(6));
	}
	
	
	
	@Test
	public void testLiftM2Simplex(){
		val lifted = AnyM.liftM2((Integer a,Integer b)->a+b);
		
		AnyM<Integer> result = lifted.apply(AnyM.fromOptional(Optional.of(3)),AnyM.fromOptional(Optional.of(4)));
		
		assertThat(result.<Optional<Integer>>unwrap().get(),equalTo(7));
	}
	@Test
	public void testLiftM2SimplexNull(){
		val lifted = AnyM.liftM2((Integer a,Integer b)->a+b);
		
		AnyM<Integer> result = lifted.apply(AnyM.fromOptional(Optional.of(3)),AnyM.fromOptional(Optional.ofNullable(null)));
		
		assertThat(result.<Optional<Integer>>unwrap().isPresent(),equalTo(false));
	}
	
	private Integer add(Integer a, Integer  b){
		return a+b;
	}
	@Test
	public void testLiftM2Mixed(){
		val lifted = AnyM.liftM2(this::add); 
		
		AnyM<Integer> result = lifted.apply(AnyM.fromOptional(Optional.of(3)),AnyM.fromStream(Stream.of(4,6,7)));
		
		
		assertThat(result.<Optional<List<Integer>>>unwrap().get(),equalTo(Arrays.asList(7,9,10)));
	}
}
