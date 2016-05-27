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
import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.types.anyM.AnyMSeq;
import com.aol.cyclops.types.anyM.AnyMValue;

import lombok.val;



public class AnyMValueTest {

    @Test
    public void testSequenceAnyM() {
        AnyMValue<Integer> just = AnyM.ofNullable(10);
        Supplier<AnyM<Stream<Integer>>> unitEmpty = ()->AnyM.fromMaybe(Maybe.just(Stream.<Integer>empty()));
        Stream<AnyM<Integer>> source = ReactiveSeq.of(just,AnyM.ofNullable(1));
        AnyM<ListX<Integer>> maybes =AnyM.sequence(source, unitEmpty)
                                          .map(s->ReactiveSeq.fromStream(s).toListX());
        assertThat(maybes,equalTo(AnyM.ofNullable(ListX.of(10,1))));
    }
    @Test
    public void testSequenceAnyMValue() {
        AnyMValue<Integer> just = AnyM.ofNullable(10);
        Supplier<AnyMValue<Stream<Integer>>> unitEmpty = ()->AnyM.fromMaybe(Maybe.just(Stream.<Integer>empty()));
        Stream<AnyMValue<Integer>> source = ReactiveSeq.of(just,AnyM.ofNullable(1));
        AnyM<ListX<Integer>> maybes =AnyMValue.sequence(source, unitEmpty)
                                          .map(s->ReactiveSeq.fromStream(s).toListX());
        assertThat(maybes,equalTo(AnyM.ofNullable(ListX.of(10,1))));
    }
	@Test
	public void testSequence(){
		
        List<Integer> list = IntStream.range(0, 100).boxed().collect(Collectors.toList());
        List<CompletableFuture<Integer>> futures = list
                .stream()
                .map(x -> CompletableFuture.supplyAsync(() -> x))
                .collect(Collectors.toList());
       
        
        AnyM<ListX<Integer>> futureList = AnyMValue.sequence(AnyM.listFromCompletableFuture(futures));
        
 
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

       
        AnyM<ListX<String>> futureList = AnyMValue.traverse( AnyM.listFromCompletableFuture(futures), (Integer i) -> "hello" +i);
   
        List<String> collected = futureList.<CompletableFuture<List<String>>>unwrap().join();
        assertThat(collected.size(),equalTo( list.size()));
        
        for(Integer next : list){
        	assertThat("hello"+list.get(next),equalTo( collected.get(next)));
        }
        
	}
	

	

	@Test
	public void testLiftMSimplex(){
		val lifted = AnyMValue.liftM((Integer a)->a+3);
		
		AnyM<Integer> result = lifted.apply(AnyM.fromOptional(Optional.of(3)));
		
		assertThat(result.<Optional<Integer>>unwrap().get(),equalTo(6));
	}
	
	
	
	@Test
	public void testLiftM2Simplex(){
		val lifted = AnyMValue.liftM2((Integer a,Integer b)->a+b);
		
		AnyM<Integer> result = lifted.apply(AnyM.fromOptional(Optional.of(3)),AnyM.fromOptional(Optional.of(4)));
		
		assertThat(result.<Optional<Integer>>unwrap().get(),equalTo(7));
	}
	@Test
	public void testLiftM2SimplexNull(){
		val lifted = AnyMValue.liftM2((Integer a,Integer b)->a+b);
		
		AnyM<Integer> result = lifted.apply(AnyM.fromOptional(Optional.of(3)),AnyM.fromOptional(Optional.ofNullable(null)));
		
		assertThat(result.<Optional<Integer>>unwrap().isPresent(),equalTo(false));
	}
	
	private Integer add(Integer a, Integer  b){
		return a+b;
	}
	@Test
	public void testLiftM2Mixed(){
		val lifted = AnyMValue.liftM2(this::add); 
		
		AnyM<Integer> result = lifted.apply(AnyM.fromOptional(Optional.of(3)),AnyM.fromMaybe(Maybe.of(4)));
		
		
		assertThat(result.<Optional<Integer>>unwrap().get(),equalTo(7));
	}
}
