package cyclops.monads.anym;

import cyclops.companion.Optionals;
import cyclops.monads.Witness;
import cyclops.monads.Witness.*;
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
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;


import com.oath.anym.AnyMSeq;
import cyclops.data.tuple.Tuple;
import cyclops.data.tuple.Tuple2;
import cyclops.monads.Witness;
import cyclops.monads.Witness.*;
import cyclops.monads.WitnessType;
import org.junit.Test;

import cyclops.monads.AnyM;
import cyclops.control.Eval;
import cyclops.control.Maybe;
import cyclops.collections.mutable.ListX;
import cyclops.collections.mutable.QueueX;
import cyclops.collections.mutable.SetX;


import reactor.core.publisher.Flux;



public class AnyMTest {


	@Test
	public void optionalAndStream(){

		AnyM<optional,Integer> opt = AnyM.ofNullable(10);
		AnyM<optional,Integer> optTimes2 = by2(opt);

		AnyM<stream,Integer> stream = AnyM.fromArray(10,20,30);
		AnyM<stream,Integer> streamTimes2  = by2(stream);

		Stream<Integer> rawStream = stream.to(Witness::stream);

		Optional<Integer> rawOptional = opt.to(Witness::optional);

        Optional<Tuple2<Integer, Integer>> o = opt.zip(AnyM.fromOptional(Optionals.fromIterable(ListX.of(2)))).to(Witness::optional);
        assertThat(o.get(),equalTo(Tuple.tuple(10,2)));


	}

	public <W extends WitnessType<W>> AnyM<W,Integer> by2(AnyM<W,Integer> toMultiply){

		return toMultiply.map(i->i*2);

	}


    @Test
    public void testApEval() {

        assertThat(AnyM.fromEval(Eval.now(10)).zip(AnyM.fromEval(Eval.later(()->20)),this::add).unwrap(),equalTo(Eval.now(30)));
    }
    @Test
    public void anyMSetConversion() {
      AnyMSeq<set,Integer> wrapped = AnyM.fromSet(SetX.of(1, 2, 3, 4, 5));

      Eval<Integer> lazyResult = wrapped
              .map(i -> i * 10)
			  .foldLazy(s-> s.reduce(50, (acc, next) -> acc + next));

      assertEquals(200, lazyResult.get().intValue());
    }
    @Test
    public void anyMListConversion() {
      AnyMSeq<list,Integer> wrapped = AnyM.fromList(ListX.of(1, 2, 3, 4, 5));

      Eval<Integer> lazyResult = wrapped
              .map(i -> i * 10)
              .foldLazy(s->s.reduce(50, (acc, next) -> acc + next));

      assertEquals(200, lazyResult.get().intValue());
    }
    @Test
    public void flatMapFirst(){

       List l= AnyM.fromList(ListX.of(1,2,3))
            .flatMap(i->AnyM.fromList(ListX.of(10,i)))
            .unwrap();
      assertThat(l,equalTo(ListX.of(10, 1, 10, 2, 10, 3)));
    }
    @Test
    public void flatMapFirstList(){

       List l= AnyM.fromList(ListX.of(1,2,3))
			       .flatMapI(i->ListX.of(10,i))
                   .unwrap();
       assertThat(l,equalTo(ListX.of(10, 1, 10, 2, 10, 3)));
    }

    @Test
    public void flatMapFirstFlux(){

       List l= AnyM.fromList(ListX.of(1,2,3))
                   .flatMapP(i->Flux.just(10,i)).unwrap();
       assertThat(l,equalTo(ListX.of(10, 1, 10, 2, 10, 3)));
    }
    @Test
    public void flatMapValueFirstList(){

       Maybe l= AnyM.fromMaybe(Maybe.of(1))
            .flatMapI(i->ListX.of(10,i))
            .toMaybe();
       assertThat(l,equalTo(Maybe.of(10)));
    }
    @Test
    public void flatMapValueFirstSet(){

       Maybe l= AnyM.fromMaybe(Maybe.of(1))
            		.flatMapI(i->SetX.of(10,i)).toMaybe();

       assertThat(l.toOptional().get(),instanceOf(Integer.class));
    }
    @Test
    public void flatMapValueFirstQueue(){

        Maybe l= AnyM.fromMaybe(Maybe.of(1))
            .flatMapI(i->QueueX.of(10,i)).toMaybe();

        assertThat(l,equalTo(Maybe.of(10)));
    }
    @Test
    public void flatMapValueFirstFlux(){

        Maybe l= AnyM.fromMaybe(Maybe.of(1))
            .flatMapP(i->Flux.just(10,i))
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
	public void createAnyMFromListOrOptionalAsAnyM(){
		List<Integer> list = Arrays.asList(1,2,3);
		assertThat(AnyM.fromList(list).unwrap(),instanceOf(List.class));
		Optional<Integer> opt = Optional.of(1);
		assertThat(AnyM.fromOptional(opt).unwrap(),instanceOf(Optional.class));
	}
	@Test
	public void flatMapWithListComprehender() {
	    List<Integer> list = Arrays.asList(1,2,3);

	    AnyMSeq<list,Integer> any = AnyM.fromList(list);
	    AnyM<list,Integer> mapped = any.flatMap(e -> any.unit(e));
	    List<Integer> unwrapped = mapped.unwrap();
	    assertEquals(list, unwrapped);
	}
	@Test
	public void fromStreamLong(){
		AnyM<stream,Long> stream = AnyM.fromLongStream(LongStream.of(1));
		assertThat(stream.unwrap(),instanceOf(Stream.class));
	}
	@Test
	public void fromStreamDouble(){
		AnyM<stream,Double> stream = AnyM.fromDoubleStream(DoubleStream.of(1));
		assertThat(stream.unwrap(),instanceOf(Stream.class));
	}
	@Test
	public void fromStreamInt(){
		AnyM<stream,Integer> stream = AnyM.fromIntStream(IntStream.of(1));
		assertThat(stream.unwrap(),instanceOf(Stream.class));
	}
	@Test
	public void fromStream(){
		AnyM<stream,Integer> stream = AnyM.fromStream(Stream.of(1));
		assertThat(stream.unwrap(),instanceOf(Stream.class));
	}
	@Test
	public void fromOptionalLong(){
		AnyM<optional,Long> opt = AnyM.fromOptionalLong(OptionalLong.of(1));
		assertThat(opt.unwrap(),instanceOf(Optional.class));
	}
	@Test
	public void fromOptionalDouble(){
		AnyM<optional,Double> opt = AnyM.fromOptionalDouble(OptionalDouble.of(1));
		assertThat(opt.unwrap(),instanceOf(Optional.class));
	}
	@Test
	public void fromOptionalInt(){
		AnyM<optional,Integer> opt = AnyM.fromOptionalInt(OptionalInt.of(1));
		assertThat(opt.unwrap(),instanceOf(Optional.class));
	}
	@Test
	public void fromOptional(){
		AnyM<optional,Integer> opt = AnyM.fromOptional(Optional.of(1));
		assertThat(opt.unwrap(),instanceOf(Optional.class));
	}
	@Test
	public void fromCompletableFuture(){
		AnyM<completableFuture,Integer> future = AnyM.fromCompletableFuture(CompletableFuture.supplyAsync(()->1));
		assertThat(future.unwrap(),instanceOf(CompletableFuture.class));
	}

	@Test
	public void ofNullable(){
		AnyM<optional,Integer> opt = AnyM.ofNullable(null);
		assertThat(opt.unwrap(),instanceOf(Optional.class));
	}



	@Test
	public void testListFromList(){
		AnyM<list,Integer> list = AnyM.fromList(Arrays.asList(1,2,3));
		assertThat(list.unwrap(),instanceOf(List.class));
	}

	@Test
	public void testListMap(){
		AnyM<list,Integer> list = AnyM.fromList(Arrays.asList(1,2,3));
		assertThat(list.map(i->i+2).unwrap(),equalTo(Arrays.asList(3,4,5)));
	}


	@Test
	public void testListFilter(){
		AnyM<list,Integer> list = AnyM.fromList(Arrays.asList(1,2,3));
		assertThat(list.filter(i->i<3).unwrap(),equalTo(Arrays.asList(1,2)));
	}
	@Test
	public void testSet(){

		AnyM<set,Integer> set = AnyM.fromSet(new HashSet<>(Arrays.asList(1,2,3)));
		assertThat(set.unwrap(),instanceOf(Set.class));
	}
	@Test
	public void testSetMap(){
		AnyM<set,Integer> set = AnyM.fromSet(new HashSet<>(Arrays.asList(1,2,3)));
		assertThat(set.map(i->i+2).unwrap(),equalTo((Set<Integer>)new HashSet<>(Arrays.asList(3,4,5))));

	}


	@Test
	public void testSetFilter(){
		AnyM<set,Integer> set = AnyM.fromSet(new HashSet<>(Arrays.asList(1,2,3)));
		System.out.println(set.filter(i->i<3).unwrap().getClass());
		assertThat(set.filter(i->i<3).unwrap(),equalTo((Set<Integer>)new HashSet<>(Arrays.asList(1,2))));
	}



	@Test
	public void unitOptional() {
		AnyM<optional,Integer> empty = AnyM.fromOptional(Optional.empty());
		AnyM<optional,Integer> unit = empty.unit(1);
		Optional<Integer> unwrapped = unit.unwrap();
		assertEquals(Integer.valueOf(1), unwrapped.get());
	}




	private Integer add(Integer a, Integer  b){
		return a+b;
	}

}
