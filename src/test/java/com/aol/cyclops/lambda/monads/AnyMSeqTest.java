package com.aol.cyclops.lambda.monads;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;


import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.aol.cyclops.types.anyM.Witness.*;
import org.junit.Test;

import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.types.anyM.AnyMSeq;

import lombok.val;



public class AnyMSeqTest {
    @Test
    public void equals(){
        AnyMSeq<list,ListX<Integer>> test1 = AnyM.fromList(ListX.of(ListX.of(10,1)));
        AnyMSeq<list,ListX<Integer>> test2 = AnyM.fromList(ListX.of(ListX.of(10,1)));
        System.out.println(test1.equals(test2));
        assertThat(test1,equalTo(test2));
    }
    @Test
    public void equalsNull(){
        AnyMSeq<list,ListX<Integer>> test1 = AnyM.fromList(ListX.of(ListX.of(10,1)));
        
        assertThat(test1,not(equalTo(null)));
    }
    @Test
    public void testSequenceAnyMSeq() {
        AnyMSeq<list,Integer> just = AnyM.fromList(ListX.of(10));
        Supplier<AnyMSeq<list,Stream<Integer>>> unitEmpty = ()->AnyM.fromList(ListX.of(Stream.<Integer>empty()));
        Stream<AnyMSeq<list,Integer>> source = ReactiveSeq.of(just,AnyM.fromArray(1));
        AnyMSeq<list,ListX<Integer>> maybes =AnyMSeq.sequence(source, unitEmpty)
                                          .map(s->ReactiveSeq.fromStream(s).toListX());
       
        
       
       
        assertThat(maybes,equalTo(AnyM.fromList(ListX.of(ListX.of(10,1)))));
    }
    @Test
    public void testSequence(){
        
        List<Integer> list = IntStream.range(0, 100).boxed().collect(Collectors.toList());
        List<Stream<Integer>> futures = list
                .stream()
                .map(x ->Stream.of( x))
                .collect(Collectors.toList());
       
        
        AnyM<list,ListX<Integer>> futureList = AnyMSeq.sequence(AnyM.listFromStream(futures));
        
 
        List<Integer> collected = futureList.<Stream<Integer>>unwrap().collect(Collectors.toList());
        assertThat(collected.size(),equalTo( list.size()));
        
        for(Integer next : list){
            assertThat(list.get(next),equalTo( collected.get(next)));
        }
        
    }
   

    @Test
    public void testTraverse(){
        
        List<Integer> list = IntStream.range(0, 100).boxed().collect(Collectors.toList());
        List<Stream<Integer>> futures = list
                .stream()
                .map(x ->Stream.of( x))
                .collect(Collectors.toList());

       
        AnyM<list,ListX<String>> futureList = AnyMSeq.traverse( AnyM.listFromStream(futures), (Integer i) -> "hello" +i);
   
        List<String> collected = futureList.<Stream<String>>unwrap().collect(Collectors.toList());
        assertThat(collected.size(),equalTo( list.size()));
        
        for(Integer next : list){
            assertThat("hello"+list.get(next),equalTo( collected.get(next)));
        }
        
    }


	@Test
	public void testLiftMSimplex(){
		val lifted = AnyMSeq.<stream,Integer,Integer>liftM((Integer a)->a+3);

		AnyM<stream,Integer> result = lifted.apply(AnyM.fromStream(Stream.of(3)));
		
		assertThat(result.<Stream<Integer>>unwrap().findFirst().get(),equalTo(6));
	}
	
	
	
	@Test
	public void testLiftM2Simplex(){
		val lifted = AnyMSeq.<stream,Integer,Integer,Integer>liftM2((Integer a,Integer b)->a+b);
		
		AnyM<stream,Integer> result = lifted.apply(AnyM.fromStream(Stream.of(3)),AnyM.fromStream(Stream.of(4)));
		
		assertThat(result.<Stream<Integer>>unwrap().findFirst().get(),equalTo(7));
	}
	@Test
	public void testLiftM2SimplexNull(){
		val lifted = AnyMSeq.<stream,Integer,Integer,Integer>liftM2((Integer a,Integer b)->a+b);
		
		AnyM<stream,Integer> result = lifted.apply(AnyM.fromStream(Stream.of(3)),AnyM.fromStream(Stream.of()));
		
		assertThat(result.<Stream<Integer>>unwrap().findFirst().isPresent(),equalTo(false));
	}
	
	private Integer add(Integer a, Integer  b){
		return a+b;
	}
	@Test
	public void testLiftM2Mixed(){
		val lifted = AnyMSeq.<stream,Integer,Integer,Integer>liftM2(this::add);
		
		AnyM<stream,Integer> result = lifted.apply(AnyM.fromStream(Stream.of(3)),AnyM.fromStream(Stream.of(4)));
		
		
		assertThat(result.<Stream<Integer>>unwrap().findFirst().get(),equalTo(7));
	}
}
