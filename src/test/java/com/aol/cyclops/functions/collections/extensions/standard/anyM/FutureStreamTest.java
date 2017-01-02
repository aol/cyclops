package com.aol.cyclops.functions.collections.extensions.standard.anyM;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

import java.util.Date;

import cyclops.monads.Witness;
import cyclops.stream.FutureStream;
import org.junit.Test;
import java.util.stream.Stream;

import cyclops.monads.AnyM;
import cyclops.collections.ListX;
import com.aol.cyclops.functions.collections.extensions.AbstractAnyMSeqOrderedDependentTest;
import com.aol.cyclops.types.anyM.AnyMSeq;

public class FutureStreamTest extends AbstractAnyMSeqOrderedDependentTest<Witness.reactiveSeq>{
   
	@Override
	public <T> AnyMSeq<Witness.reactiveSeq,T> of(T... values) {
		return AnyM.fromStream(FutureStream.of(values).async());
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.function.collections.extensions.AbstractCollectionXTest#empty()
	 */
	@Override
	public <T> AnyMSeq<Witness.reactiveSeq,T> empty() {
		return AnyM.fromStream(FutureStream.empty());
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
    public void cast(){
        assertThat(of(1,2,3).cast(String.class).toListX(),equalTo(ListX.of()));
    }
	@Test
    public void testCastPast() {
        of(1, "a", 2, "b", 3).cast(Date.class).map(d -> d.getTime())
                .toList();
    }
    
    @Test
    public void testParallelFlatMap() {
        assertThat(FutureStream.lazyFutureStream(Stream.generate(() -> 1).limit(1000)).parallel()
                .map(a -> Thread.currentThread().getName()).toSet().size(), greaterThan(1));
    }

}

