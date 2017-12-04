package cyclops.monads.collections.mutable;

import com.oath.anym.AnyMSeq;
import cyclops.monads.Witness;
import cyclops.monads.Witness.*;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import com.oath.cyclops.react.ThreadPools;
import cyclops.async.LazyReact;


import cyclops.monads.collections.AbstractAnyMSeqOrderedDependentTest;
import cyclops.reactive.FutureStream;
import org.junit.Test;

import java.util.stream.Stream;

import cyclops.monads.AnyM;
import cyclops.collections.mutable.ListX;


public class FutureStreamTest extends AbstractAnyMSeqOrderedDependentTest<futureStream> {
    int count = 0;

    public <T> FutureStream<T> ft(T... values) {
        return new LazyReact(ThreadPools.getCommonFreeThread()).async().of(values);
    }


    @Test
	public void materialize(){
		ListX<Integer> d= of(1, 2, 3).cycleUntil(next->count++==6).toListX();
		System.out.println("D " + d);
		count =0;
		assertEquals(asList(1, 2,3, 1, 2,3),of(1, 2, 3).cycleUntil(next->count++==6).toListX());
	}
	@Override
	public <T> AnyMSeq<futureStream,T> of(T... values) {
		return AnyM.fromFutureStream(new LazyReact(ThreadPools.getCommonFreeThread()).async().of(values));
	}
	/* (non-Javadoc)
	 * @see com.oath.cyclops.function.collections.extensions.AbstractCollectionXTest#zero()
	 */
	@Override
	public <T> AnyMSeq<futureStream,T> empty() {
		return AnyM.fromFutureStream(new LazyReact().of());
	}
	@Test
	public void when(){

		String res=	of(1,2,3).visit((x,xs)->
								xs.join(x>2? "hello" : "world"),()->"boo!");

		assertThat(res,equalTo("2world3"));
	}
	@Test
	public void whenGreaterThan2(){
		String res=	of(5,2,3).visit((x,xs)->
								xs.join(x>2? "hello" : "world"),()->"boo!");

		assertThat(res,equalTo("2hello3"));
	}
	@Test
	public void when2(){

		Integer res =	of(1,2,3).visit((x,xs)->x,()->10);
		System.out.println(res);
	}
	@Test
	public void whenNilOrNot(){
		String res1=	of(1,2,3).visit((x,xs)-> x>2? "hello" : "world",()->"EMPTY");
	}

    @Test
    public void whenNilOrNotJoinWithFirstElement() {

        String res = of(1, 2, 3).visit((x, xs) -> xs.join(x > 2 ? "hello" : "world"), () -> "EMPTY");
        assertThat(res, equalTo("2world3"));
    }


    @Test
    public void testParallelFlatMap() {
        assertThat(new LazyReact(ThreadPools.getCommonFreeThread()).fromStream(Stream.generate(() -> 1).limit(1000)).parallel()
                .map(a -> Thread.currentThread().getName()).toSet().size(), greaterThan(1));
    }

}

