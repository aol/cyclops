package cyclops.monads.anym;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;


import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;


import com.oath.anym.AnyMSeq;
import com.oath.cyclops.ReactiveConvertableSequence;
import cyclops.monads.Witness;
import cyclops.monads.Witness.list;
import cyclops.monads.Witness.stream;
import cyclops.monads.function.AnyMFunction1;
import cyclops.monads.function.AnyMFunction2;
import org.junit.Test;

import cyclops.monads.AnyM;
import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.collections.mutable.ListX;



public class AnyMSeqTest {
    @Test
    public void equals(){
        AnyMSeq<list,ListX<Integer>> test1 = AnyM.fromList(ListX.of(ListX.of(10,1)));
        AnyMSeq<list,ListX<Integer>> test2 = AnyM.fromList(ListX.of(ListX.of(10,1)));
        System.out.println(test1.equals(test2));
        assertThat(test1,equalTo(test2));
        test1.appendAll(ListX.of(1,2,3)).printOut();
        test1.cycle(200).sliding(3).deleteBetween(5,10).combinations();
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




        assertThat(maybes.map(s->s.collect(Collectors.toList())),equalTo(AnyM.fromList(ListX.of(ListX.of(10,1)))));
    }
    @Test
    public void testSequence(){

        List<Integer> list = IntStream.range(0, 100).boxed().collect(Collectors.toList());
        List<Stream<Integer>> futures = list
                .stream()
                .map(x ->Stream.of( x))
                .collect(Collectors.toList());



        AnyM<stream,ListX<Integer>> futureList = AnyM.sequence(AnyM.listFromStream(futures), stream.INSTANCE);


        System.out.println(" future list " + futureList);

        ListX<ListX<Integer>> collected = futureList.to(Witness::toReactiveSeq).to(ReactiveConvertableSequence::converter).listX();


        System.out.println(collected);
        assertThat(collected.get(0).size(),equalTo( list.size()));



    }


    @Test
    public void testTraverse(){

        List<Integer> list = IntStream.range(0, 100).boxed().collect(Collectors.toList());
        List<Stream<Integer>> futures = list
                .stream()
                .map(x ->Stream.of( x))
                .collect(Collectors.toList());


        AnyM<stream,ListX<String>> futureList = AnyM.traverse( AnyM.listFromStream(futures), (Integer i) -> "hello" +i, stream.INSTANCE);

        ListX<ListX<String>> collected = futureList.to(Witness::toReactiveSeq).to(ReactiveConvertableSequence::converter).listX();

        assertThat(collected.get(0).size(),equalTo( list.size()));


    }


	@Test
	public void testLiftMSimplex(){
        AnyMFunction1<stream,Integer,Integer> lifted = AnyM.liftF((Integer a)->a+3);

		AnyM<stream,Integer> result = lifted.apply(AnyM.fromStream(Stream.of(3)));

		assertThat(result.<Stream<Integer>>unwrap().findFirst().get(),equalTo(6));
	}



	@Test
	public void testLiftM2Simplex(){

        AnyMFunction2<stream,Integer,Integer,Integer> lifted = AnyM.liftF2((Integer a, Integer b)->a+b);

		AnyM<stream,Integer> result = lifted.apply(AnyM.fromStream(Stream.of(3)),AnyM.fromStream(Stream.of(4)));

		assertThat(result.<Stream<Integer>>unwrap().findFirst().get(),equalTo(7));
	}
	@Test
	public void testLiftM2SimplexNull(){
        AnyMFunction2<stream,Integer,Integer,Integer> lifted = AnyM.liftF2((Integer a, Integer b)->a+b);

		AnyM<stream,Integer> result = lifted.apply(AnyM.fromStream(Stream.of(3)),AnyM.fromStream(Stream.of()));

		assertThat(result.<Stream<Integer>>unwrap().findFirst().isPresent(),equalTo(false));
	}

	private Integer add(Integer a, Integer  b){
		return a+b;
	}
	@Test
	public void testLiftM2Mixed(){
        AnyMFunction2<stream,Integer,Integer,Integer> lifted = AnyM.liftF2(this::add);

		AnyM<stream,Integer> result = lifted.apply(AnyM.fromStream(Stream.of(3)),AnyM.fromStream(Stream.of(4)));


		assertThat(result.<Stream<Integer>>unwrap().findFirst().get(),equalTo(7));
	}
}
