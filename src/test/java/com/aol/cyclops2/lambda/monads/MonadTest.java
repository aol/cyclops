package com.aol.cyclops2.lambda.monads;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static cyclops.control.anym.Witness.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import cyclops.control.anym.Witness;
import cyclops.control.anym.function.AnyMFunction1;
import cyclops.control.anym.function.AnyMFunction2;
import org.junit.Test;

import cyclops.control.anym.AnyM;
import cyclops.collectionx.mutable.ListX;


public class MonadTest {

	 Optional<Integer> value = Optional.of(42);


	@Test
	public void testSequence(){
		
        List<Integer> list = IntStream.range(0, 100).boxed().collect(Collectors.toList());
        List<CompletableFuture<Integer>> futures = list
                .stream()
                .map(x -> CompletableFuture.supplyAsync(() -> x))
                .collect(Collectors.toList());
       
        
        AnyM<completableFuture,ListX<Integer>> futureList = AnyM.sequence(AnyM.listFromCompletableFuture(futures), Witness.completableFuture.INSTANCE);
        
 
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

       
        AnyM<completableFuture,ListX<String>> futureList = AnyM.traverse(AnyM.listFromCompletableFuture(futures), (Integer i) -> "hello" +i, completableFuture.INSTANCE);
   
        List<String> collected = futureList.<CompletableFuture<List<String>>>unwrap().join();
        assertThat(collected.size(),equalTo( list.size()));
        
        for(Integer next : list){
        	assertThat("hello"+list.get(next),equalTo( collected.get(next)));
        }
        
	}
	
	
	

	@Test
	public void testLiftMSimplex(){
		AnyMFunction1<completableFuture,Integer,Integer> lifted = AnyM.liftF((Integer a)->a+3);
		
		AnyM<completableFuture,Integer> result = lifted.apply(AnyM.fromCompletableFuture(CompletableFuture.completedFuture(3)));
		
		assertThat(result.<CompletableFuture<Integer>>unwrap().join(),equalTo(6));
	}
	
	
	
	@Test
	public void testLiftM2Simplex(){
		AnyMFunction2<completableFuture,Integer,Integer,Integer> lifted = AnyM.liftF2((Integer a, Integer b)->a+b);
		
		AnyM<completableFuture,Integer> result = lifted.apply(AnyM.fromCompletableFuture(CompletableFuture.completedFuture(3)),
																AnyM.fromCompletableFuture(CompletableFuture.completedFuture(4)));
		
		assertThat(result.<CompletableFuture<Integer>>unwrap().join(),equalTo(7));
	}
	@Test
	public void testLiftM2SimplexNull(){
		AnyMFunction2<optional,Integer,Integer,Integer> lifted = AnyM.liftF2((Integer a, Integer b)->a+b);
		
		AnyM<optional,Integer> result = lifted.apply(AnyM.ofNullable(3),
														AnyM.ofNullable(null));
		
		assertThat(result.<Optional<Integer>>unwrap().isPresent(),equalTo(false));
	}
	
	private Integer add(Integer a, Integer  b){
		return a+b;
	}
	@Test
	public void testLiftM2Stream(){
		AnyMFunction2<stream,Integer,Integer,Integer> lifted = AnyM.liftF2(this::add);
		
		AnyM<stream,Integer> result = lifted.apply(AnyM.fromArray(3),AnyM.fromArray(4,6,7));
		
		
		assertThat(result.<Stream<List<Integer>>>unwrap().collect(Collectors.toList()),equalTo(Arrays.asList(7,9,10)));
	}
}
