package com.aol.cyclops2.lambda.monads;

import cyclops.control.anym.Witness.*;
import static com.aol.cyclops2.Matchers.equivalent;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

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
import cyclops.reactive.ReactiveSeq;
import cyclops.collectionx.mutable.ListX;
import com.aol.cyclops2.types.anyM.AnyMValue;


public class AnyMValueTest {

    @Test
    public void testSequenceAnyM() {
        AnyMValue<optional,Integer> just = AnyM.ofNullable(10);

        Stream<AnyM<optional,Integer>> source = ReactiveSeq.of(just,AnyM.ofNullable(1));
        AnyM<optional,Stream<Integer>> maybes =AnyM.sequence(source, optional.INSTANCE);
        assertThat(maybes.map(s->s.collect(Collectors.toList())),equalTo(AnyM.ofNullable(ListX.of(10,1))));
    }
   
    @Test
    public void testSequenceAnyMValue() {
        AnyMValue<optional,Integer> just = AnyM.ofNullable(10);

        Stream<AnyM<optional,Integer>> source = ReactiveSeq.of(just,AnyM.ofNullable(1));
        AnyM<optional,ListX<Integer>> maybes =AnyM.sequence(source, optional.INSTANCE)
                                          .map(s->ReactiveSeq.fromStream(s).toListX());
        assertThat(maybes,equivalent(AnyM.ofNullable(ListX.of(10,1))));
    }
	@Test
	public void testSequence(){
		
        List<Integer> list = IntStream.range(0, 100).boxed().collect(Collectors.toList());
        List<CompletableFuture<Integer>> futures = list
                .stream()
                .map(x -> CompletableFuture.supplyAsync(() -> x))
                .collect(Collectors.toList());
       
        
        AnyM<completableFuture,ListX<Integer>> futureList = AnyM.sequence(AnyM.listFromCompletableFuture(futures),Witness.completableFuture.INSTANCE);
        
 
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

       
        AnyM<completableFuture,ListX<String>> futureList = AnyM.traverse( AnyM.listFromCompletableFuture(futures), (Integer i) -> "hello" +i, completableFuture.INSTANCE);
   
        List<String> collected = futureList.<CompletableFuture<List<String>>>unwrap().join();
        assertThat(collected.size(),equalTo( list.size()));
        
        for(Integer next : list){
        	assertThat("hello"+list.get(next),equalTo( collected.get(next)));
        }
        
	}
	

	

	@Test
	public void testLiftMSimplex(){
		AnyMFunction1<optional,Integer,Integer> lifted = AnyM.liftF((Integer a)->a+3);
		
		AnyM<Witness.optional,Integer> result = lifted.apply(AnyM.fromOptional(Optional.of(3)));
		
		assertThat(result.<Optional<Integer>>unwrap().get(),equalTo(6));
	}
	
	
	
	@Test
	public void testLiftM2Simplex(){
		AnyMFunction2<optional,Integer,Integer,Integer> lifted = AnyM.liftF2((Integer a, Integer b)->a+b);
		
		AnyM<Witness.optional,Integer> result = lifted.apply(AnyM.fromOptional(Optional.of(3)),AnyM.fromOptional(Optional.of(4)));
		
		assertThat(result.<Optional<Integer>>unwrap().get(),equalTo(7));
	}
	@Test
	public void testLiftM2SimplexNull(){
		AnyMFunction2<optional,Integer,Integer,Integer> lifted = AnyM.liftF2((Integer a, Integer b)->a+b);
		
		AnyM<Witness.optional,Integer> result = lifted.apply(AnyM.fromOptional(Optional.of(3)),AnyM.fromOptional(Optional.ofNullable(null)));
		
		assertThat(result.<Optional<Integer>>unwrap().isPresent(),equalTo(false));
	}
	
	private Integer add(Integer a, Integer  b){
		return a+b;
	}
	@Test
	public void testLiftM2Mixed(){
		AnyMFunction2<optional,Integer,Integer,Integer> lifted = AnyM.liftF2(this::add);
		
		AnyM<Witness.optional,Integer> result = lifted.apply(AnyM.fromOptional(Optional.of(3)),AnyM.ofNullable(4));
		
		
		assertThat(result.<Optional<Integer>>unwrap().get(),equalTo(7));
	}
	
}
