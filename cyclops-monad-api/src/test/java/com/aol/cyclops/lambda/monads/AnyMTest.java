package com.aol.cyclops.lambda.monads;

import static com.aol.cyclops.lambda.api.AsAnyM.anyM;
import static com.aol.cyclops.lambda.api.AsAnyMList.completableFutureToAnyMList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.*;

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

import lombok.val;

import org.junit.Test;

import com.aol.cyclops.lambda.api.AsAnyM;
import com.aol.cyclops.monad.AnyM;



public class AnyMTest {
	@Test
	public void flatMapWithListComprehender() {
	    List<Integer> list = Arrays.asList(1,2,3);
	    AnyM<Integer> any = AsAnyM.anyMList(list); 
	    AnyM<Integer> mapped = any.flatMap(e -> any.unit(e));
	    List<Integer> unwrapped = mapped.unwrap();
	    assertEquals(list, unwrapped);
	}
	@Test
	public void fromStreamLong(){
		AnyM<Long> stream = AnyM.fromLongStream(LongStream.of(1));
		assertThat(stream.unwrap(),instanceOf(LongStream.class));
	}
	@Test
	public void fromStreamDouble(){
		AnyM<Double> stream = AnyM.fromDoubleStream(DoubleStream.of(1));
		assertThat(stream.unwrap(),instanceOf(DoubleStream.class));
	}
	@Test
	public void fromStreamInt(){
		AnyM<Integer> stream = AnyM.fromIntStream(IntStream.of(1));
		assertThat(stream.unwrap(),instanceOf(IntStream.class));
	}
	@Test
	public void fromStream(){
		AnyM<Integer> stream = AnyM.fromStream(Stream.of(1));
		assertThat(stream.unwrap(),instanceOf(Stream.class));
	}
	@Test
	public void fromOptionalLong(){
		AnyM<Long> opt = AnyM.fromOptional(OptionalLong.of(1));
		assertThat(opt.unwrap(),instanceOf(Optional.class));
	}
	@Test
	public void fromOptionalDouble(){
		AnyM<Double> opt = AnyM.fromOptional(OptionalDouble.of(1));
		assertThat(opt.unwrap(),instanceOf(Optional.class));
	}
	@Test
	public void fromOptionalInt(){
		AnyM<Integer> opt = AnyM.fromOptional(OptionalInt.of(1));
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
	public void testList(){
		AnyM<Integer> list = AsAnyM.anyMList(Arrays.asList(1,2,3));
		assertThat(list.unwrap(),instanceOf(List.class));
	}
	@Test
	public void testListMap(){
		AnyM<Integer> list = AsAnyM.anyMList(Arrays.asList(1,2,3));
		assertThat(list.map(i->i+2).unwrap(),equalTo(Arrays.asList(3,4,5)));
	}
	
	@Test
	public void testListFlatMap(){
		AnyM<Integer> list = AsAnyM.anyMList(Arrays.asList(1,2,3));
		assertThat(list.flatMapCollection(i->Arrays.asList(i+2,i-2)).unwrap(),equalTo(Arrays.asList(3,-1,4,0,5,1)));
	}
	@Test
	public void testListFilter(){
		AnyM<Integer> list = AsAnyM.anyMList(Arrays.asList(1,2,3));
		assertThat(list.filter(i->i<3).unwrap(),equalTo(Arrays.asList(1,2)));
	}
	@Test
	public void testSet(){
		AnyM<Integer> set = AsAnyM.anyMSet(new HashSet<>(Arrays.asList(1,2,3)));
		assertThat(set.unwrap(),instanceOf(Set.class));
	}
	@Test
	public void testSetMap(){
		AnyM<Integer> set = AsAnyM.anyMSet(new HashSet<>(Arrays.asList(1,2,3)));
		assertThat(set.map(i->i+2).unwrap(),equalTo(new HashSet<>(Arrays.asList(3,4,5))));
		
	}
	
	@Test
	public void testSetFlatMap(){
		AnyM<Integer> set = AsAnyM.anyMSet(new HashSet<>(Arrays.asList(1,2,3)));
		assertThat(set.flatMapCollection(i->Arrays.asList(i+2,i-2)).unwrap(),equalTo(new HashSet<>(Arrays.asList(3,-1,4,0,5,1))));
		
	}
	@Test
	public void testSetFilter(){
		AnyM<Integer> set = AsAnyM.anyMSet(new HashSet<>(Arrays.asList(1,2,3)));
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
       
        
        AnyM<Stream<Integer>> futureList = AnyMonads.sequence(completableFutureToAnyMList(futures));
        
 
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

       
        AnyM<List<String>> futureList = AnyMonads.traverse( completableFutureToAnyMList(futures), (Integer i) -> "hello" +i);
   
        List<String> collected = futureList.<CompletableFuture<List<String>>>unwrap().join();
        assertThat(collected.size(),equalTo( list.size()));
        
        for(Integer next : list){
        	assertThat("hello"+list.get(next),equalTo( collected.get(next)));
        }
        
	}
	

	@Test
	public void testReplicateM(){
		
		 AnyM<List<Integer>> applied =anyM(Optional.of(2)).replicateM(5);
		 assertThat(applied.unwrap(),equalTo(Optional.of(Arrays.asList(2,2,2,2,2))));
	
	}

	@Test
	public void testLiftMSimplex(){
		val lifted = AnyMonads.liftM((Integer a)->a+3);
		
		AnyM<Integer> result = lifted.apply(anyM(Optional.of(3)));
		
		assertThat(result.<Optional<Integer>>unwrap().get(),equalTo(6));
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
