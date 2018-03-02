package cyclops.monads.collections.mutable;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.stream.Stream;

import com.oath.anym.AnyMSeq;
import com.oath.cyclops.ReactiveConvertableSequence;
import cyclops.reactive.collections.mutable.ListX;
import cyclops.monads.Witness.stream;
import cyclops.monads.collections.AbstractAnyMSeqOrderedDependentTest;
import org.junit.Test;

import cyclops.monads.AnyM;

public class StreamTest extends AbstractAnyMSeqOrderedDependentTest<stream> {

	@Override
	public <T> AnyMSeq<stream,T> of(T... values) {
		return AnyM.fromStream(Stream.of(values));
	}

	@Override
	public <T> AnyMSeq<stream,T> empty() {
		return AnyM.fromStream(Stream.empty());
	}

	int count = 0;
    @Test
    public void testCycleUntil2() {
        count =0;
        System.out.println("Cycle until!");
        count =0;
        ListX<Integer> b= of(1, 2, 3).peek(System.out::println)
                            .cycleUntil(next->count++==6).to(ReactiveConvertableSequence::converter).listX();
        System.out.println("2 " + b);

    }
	@Test
    public void when(){

        String res= of(1,2,3).visit((x,xs)->
                                xs.join(x>2? "hello" : "world"),()->"boo!");

        assertThat(res,equalTo("2world3"));
    }
	@Test
    public void whenGreaterThan2(){
        String res= of(5,2,3).visit((x,xs)->
                                xs.join(x>2? "hello" : "world"),()->"boo!");

        assertThat(res,equalTo("2hello3"));
    }
    @Test
    public void when2(){

        Integer res =   of(1,2,3).visit((x,xs)->x,()->10);
        System.out.println(res);
    }
    @Test
    public void whenNilOrNot(){
        String res1=    of(1,2,3).visit((x,xs)-> x>2? "hello" : "world",()->"EMPTY");
    }
    @Test
    public void whenNilOrNotJoinWithFirstElement(){


        String res= of(1,2,3).visit((x,xs)-> xs.join(x>2? "hello" : "world"),()->"EMPTY");
        assertThat(res,equalTo("2world3"));
    }
	/**
	 *
		Eval e;
		//int cost = ReactiveSeq.of(1,2).when((head,tail)-> head.when(h-> (int)h>5, h-> 0 )
		//		.flatMap(h-> head.when());

		ht.headMaybe().when(some-> Matchable.of(some).matches(
											c->c.hasValues(1,2,3).then(i->"hello world"),
											c->c.hasValues('b','b','c').then(i->"boo!")
									),()->"hello");
									**/


}

