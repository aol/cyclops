package com.aol.cyclops.lambda.monads;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;


import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.aol.cyclops.types.anyM.Witness.*;

import com.aol.cyclops.types.anyM.Witness;
import com.aol.cyclops.util.function.MFunc1;
import com.aol.cyclops.util.function.MFunc2;
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

        Stream<AnyMSeq<list,Integer>> source = ReactiveSeq.of(just,AnyM.fromList(Arrays.asList(1)));
        AnyM<list,Stream<Integer>> maybes =AnyM.sequence(source, list.INSTANCE);
       
        
       
       
        assertThat(maybes,equalTo(AnyM.fromList(ListX.of(ListX.of(10,1)))));
    }
    @Test
    public void testSequence(){
        
        List<Integer> list = IntStream.range(0, 100).boxed().collect(Collectors.toList());
        List<Stream<Integer>> futures = list
                .stream()
                .map(x ->Stream.of( x))
                .collect(Collectors.toList());
       
        
        AnyM<stream,ListX<Integer>> futureList = AnyM.sequence(AnyM.listFromStream(futures), stream.INSTANCE);
        
 
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

       
        AnyM<stream,ListX<String>> futureList = AnyM.traverse( AnyM.listFromStream(futures), (Integer i) -> "hello" +i, stream.INSTANCE);
   
        List<String> collected = futureList.<Stream<String>>unwrap().collect(Collectors.toList());
        assertThat(collected.size(),equalTo( list.size()));
        
        for(Integer next : list){
            assertThat("hello"+list.get(next),equalTo( collected.get(next)));
        }
        
    }


	@Test
	public void testLiftMSimplex(){
        MFunc1<stream,Integer,Integer> lifted = AnyM.liftF((Integer a)->a+3);

		AnyM<stream,Integer> result = lifted.apply(AnyM.fromStream(Stream.of(3)));
		
		assertThat(result.<Stream<Integer>>unwrap().findFirst().get(),equalTo(6));
	}
	
	
	
	@Test
	public void testLiftM2Simplex(){
        MFunc2<stream,Integer,Integer,Integer> lifted = AnyM.liftF2((Integer a,Integer b)->a+b);
		
		AnyM<stream,Integer> result = lifted.apply(AnyM.fromStream(Stream.of(3)),AnyM.fromStream(Stream.of(4)));
		
		assertThat(result.<Stream<Integer>>unwrap().findFirst().get(),equalTo(7));
	}
	@Test
	public void testLiftM2SimplexNull(){
        MFunc2<stream,Integer,Integer,Integer> lifted = AnyM.liftF2((Integer a,Integer b)->a+b);
		
		AnyM<stream,Integer> result = lifted.apply(AnyM.fromStream(Stream.of(3)),AnyM.fromStream(Stream.of()));
		
		assertThat(result.<Stream<Integer>>unwrap().findFirst().isPresent(),equalTo(false));
	}
	
	private Integer add(Integer a, Integer  b){
		return a+b;
	}
	@Test
	public void testLiftM2Mixed(){
        MFunc2<stream,Integer,Integer,Integer> lifted = AnyM.liftF2(this::add);
		
		AnyM<stream,Integer> result = lifted.apply(AnyM.fromStream(Stream.of(3)),AnyM.fromStream(Stream.of(4)));
		
		
		assertThat(result.<Stream<Integer>>unwrap().findFirst().get(),equalTo(7));
	}
}
