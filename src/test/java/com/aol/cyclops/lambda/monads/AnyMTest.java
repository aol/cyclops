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
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import org.junit.Test;

import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.Eval;
import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.data.collections.extensions.standard.QueueX;
import com.aol.cyclops.data.collections.extensions.standard.SetX;
import com.aol.cyclops.types.anyM.AnyMSeq;

import lombok.val;
import reactor.core.publisher.Flux;



public class AnyMTest {
    @Test
    public void testApEval() {
        assertThat(AnyM.fromEval(Eval.now(10)).ap(Eval.later(()->20),this::add).unwrap(),equalTo(Eval.now(30)));
    }
    @Test
    public void anyMSetConversion() {
      AnyMSeq<Integer> wrapped = AnyM.fromSet(SetX.of(1, 2, 3, 4, 5));

      Eval<Integer> lazyResult = wrapped
              .map(i -> i * 10)
              .lazyOperations() 
              .reduce(50, (acc, next) -> acc + next);

      assertEquals(200, lazyResult.get().intValue());
    }
    @Test
    public void anyMListConversion() {
      AnyMSeq<Integer> wrapped = AnyM.fromList(ListX.of(1, 2, 3, 4, 5));

      Eval<Integer> lazyResult = wrapped
              .map(i -> i * 10)
              .lazyOperations() 
              .reduce(50, (acc, next) -> acc + next);

      assertEquals(200, lazyResult.get().intValue());
    }
    @Test
    public void flatMapFirst(){
     
       List l= AnyM.fromList(ListX.of(1,2,3))
            .flatMapFirst(i->AnyM.fromList(ListX.of(10,i)))
            .unwrap();
      assertThat(l,equalTo(ListX.of(10, 1, 10, 2, 10, 3)));
    }
    @Test
    public void flatMapFirstList(){
     
       List l= AnyM.fromList(ListX.of(1,2,3))
            .flatMapFirst(i->ListX.of(10,i))
            .unwrap();
       assertThat(l,equalTo(ListX.of(10, 1, 10, 2, 10, 3)));
    }
    @Test
    public void flatMapFirstQueue(){
     
       List l= AnyM.fromList(ListX.of(1,2,3))
            .flatMapFirst(i->QueueX.of(10,i))
            .unwrap();
       assertThat(l,equalTo(ListX.of(10, 1, 10, 2, 10, 3)));
    }
    @Test
    public void flatMapFirstFlux(){
     
       List l= AnyM.fromList(ListX.of(1,2,3))
            .flatMapFirstPublisher(i->Flux.just(10,i))
            .unwrap();
       assertThat(l,equalTo(ListX.of(10, 1, 10, 2, 10, 3)));
    }
    @Test
    public void flatMapValueFirstList(){
     
       Maybe l= AnyM.fromMaybe(Maybe.of(1))
            .flatMapFirst(i->ListX.of(10,i))
            .unwrap();
       assertThat(l,equalTo(Maybe.of(10)));
    }
    @Test
    public void flatMapValueFirstQueue(){
     
        Maybe l= AnyM.fromMaybe(Maybe.of(1))
            .flatMapFirst(i->QueueX.of(10,i))
            .unwrap();
        assertThat(l,equalTo(Maybe.of(10)));
    }
    @Test
    public void flatMapValueFirstFlux(){
     
        Maybe l= AnyM.fromMaybe(Maybe.of(1))
            .flatMapFirstPublisher(i->Flux.just(10,i))
            .unwrap();
        assertThat(l,equalTo(Maybe.of(10)));
    }
    @Test
    public void flatMap(){
       AnyM.fromStream(Stream.of(1,2,3))
            .flatMap(i->AnyM.fromStream(Stream.of(10,i)))
            .forEach(System.out::println);
    }
	@Test
	public void createAnyMFromListOrOptional(){
		List<Integer> list = Arrays.asList(1,2,3);
		assertThat(AnyM.ofSeq(list).unwrap(),instanceOf(List.class));
		Optional<Integer> opt = Optional.of(1);
		assertThat(AnyM.ofSeq(opt).unwrap(),instanceOf(Optional.class));
	}
	@Test
	public void createAnyMFromListOrOptionalAsAnyM(){
		List<Integer> list = Arrays.asList(1,2,3);
		assertThat(AnyM.ofSeq(list).unwrap(),instanceOf(List.class));
		Optional<Integer> opt = Optional.of(1);
		assertThat(AnyM.ofSeq(opt).unwrap(),instanceOf(Optional.class));
	}
	@Test
	public void flatMapWithListComprehender() {
	    List<Integer> list = Arrays.asList(1,2,3);
	    AnyMSeq<Integer> any = AnyM.fromList(list); 
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
		AnyM<Integer> opt = AnyM.ofSeq(Optional.of(1));
		assertThat(opt.unwrap(),instanceOf(Optional.class));
	}
	@Test
	public void ofConvertable(){
		AnyM<Integer> future = AnyM.ofConvertableValue((Supplier<Integer>)()->1);
		assertThat(future.unwrap(),instanceOf(CompletableFuture.class));
	}
	@Test
	public void testLisOfMonad(){
		AnyM<Integer> list = AnyM.ofSeq(Arrays.asList(1,2,3));
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
		assertThat(set.map(i->i+2).unwrap(),equalTo((Set<Integer>)new HashSet<>(Arrays.asList(3,4,5))));
		
	}
	
	
	@Test
	public void testSetFilter(){
		AnyM<Integer> set = AnyM.fromIterable(new HashSet<>(Arrays.asList(1,2,3)));
		System.out.println(set.filter(i->i<3).unwrap().getClass());
		assertThat(set.filter(i->i<3).unwrap(),equalTo((Set<Integer>)new HashSet<>(Arrays.asList(1,2))));
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
