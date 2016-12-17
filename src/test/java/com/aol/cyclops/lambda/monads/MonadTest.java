package com.aol.cyclops.lambda.monads;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.Test;

import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.data.collections.extensions.standard.ListX;

import lombok.val;


public class MonadTest {

	 Optional<Integer> value = Optional.of(42);
	 Monad<Integer> monadicValue = new MonadWrapper<>(value);
	 Function<Optional<Integer>,Monad<Integer>> monadOf = input ->new MonadWrapper<>(input);
	 Function<Optional<Integer>,Monad<Integer>> f = input -> new MonadWrapper<>(Optional.of(input.get()*5));
	 Function<Optional<Integer>,Monad<Integer>> g = input -> new MonadWrapper<>(Optional.of(input.get()*50));

	

	
	
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
	public void testTraverse(){
		
        List<Integer> list = IntStream.range(0, 100).boxed().collect(Collectors.toList());
        List<CompletableFuture<Integer>> futures = list
                .stream()
                .map(x -> CompletableFuture.supplyAsync(() -> x))
                .collect(Collectors.toList());

       
        AnyM<ListX<String>> futureList = AnyM.traverse(AnyM.listFromCompletableFuture(futures), (Integer i) -> "hello" +i);
   
        List<String> collected = futureList.<CompletableFuture<List<String>>>unwrap().join();
        assertThat(collected.size(),equalTo( list.size()));
        
        for(Integer next : list){
        	assertThat("hello"+list.get(next),equalTo( collected.get(next)));
        }
        
	}
	
	
	
	@Test
	public void testReplicateM(){
		 AnyM<List<Integer>> applied =new MonadWrapper<>(Optional.of(2)).replicateM(5).anyMValue();
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
