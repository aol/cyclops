package com.aol.cyclops.functions.collections.extensions.standard.anyM;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.function.Supplier;

import org.junit.Test;

import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.functions.collections.extensions.AbstractAnyMSeqOrderedDependentTest;
import com.aol.cyclops.types.anyM.AnyMSeq;
import com.aol.cyclops.types.futurestream.LazyFutureStream;
public class LazyFutureStreamTest extends AbstractAnyMSeqOrderedDependentTest{
   
	@Override
	public <T> AnyMSeq<T> of(T... values) {
		return AnyM.fromIterable(LazyFutureStream.of(values).async());
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.functions.collections.extensions.AbstractCollectionXTest#empty()
	 */
	@Override
	public <T> AnyMSeq<T> empty() {
		return AnyM.fromIterable(ListX.empty());
	}
	@Test
	public void when(){
		
		String res=	AnyM.fromIterable(ListX.of(1,2,3)).visit((x,xs)->
								xs.join(x.visit(some-> (int)some>2? "hello" : "world",()->"boo!"))
					);
		assertThat(res,equalTo("2world3"));
	}
	@Test
	public void whenGreaterThan2(){
		String res=	AnyM.fromIterable(ListX.of(5,2,3)).visit((x,xs)->
								xs.join(x.visit(some-> (int)some>2? "hello" : "world",()->"boo!"))
					);
		assertThat(res,equalTo("2hello3"));
	}
	@Test
	public void when2(){
		
		Integer res =	AnyM.fromIterable(ListX.of(1,2,3)).visit((x,xs)->{
						
								System.out.println(x.isPresent());
								System.out.println(x.get());
								return x.get();
								});
		System.out.println(res);
	}
	@Test
	public void whenNilOrNot(){
		String res1=	AnyM.fromIterable(ListX.of(1,2,3)).visit((x,xs)-> x.visit(some-> (int)some>2? "hello" : "world",()->"EMPTY"));
	}
	@Test
	public void whenNilOrNotJoinWithFirstElement(){
		
		
		String res=	AnyM.fromIterable(ListX.of(1,2,3)).visit((x,xs)-> x.visit(some-> xs.join((int)some>2? "hello" : "world"),()->"EMPTY"));
		assertThat(res,equalTo("2world3"));
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

}

