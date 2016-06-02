package com.aol.cyclops.functions.collections.extensions.standard.lazy;

import org.junit.Test;

import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.data.collections.extensions.CollectionX;
import com.aol.cyclops.data.collections.extensions.persistent.PStackX;
import com.aol.cyclops.data.collections.extensions.standard.DequeX;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.functions.collections.extensions.AbstractLazyTest;
import com.aol.cyclops.functions.collections.extensions.CollectionXTestsWithNulls;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
public class LazyListXTest extends AbstractLazyTest{

	@Override
	public <T> CollectionX<T> of(T... values) {
		return ListX.of(values);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.functions.collections.extensions.AbstractCollectionXTest#empty()
	 */
	@Override
	public <T> CollectionX<T> empty() {
		return ListX.empty();
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
		String res1=	ListX.of(1,2,3).visit((x,xs)-> x>2? "hello" : "world",()->"EMPTY");
	}

    @Test
    public void whenNilOrNotJoinWithFirstElement() {

        String res = of(1, 2, 3).visit((x, xs) -> xs.join(x > 2 ? "hello" : "world"), () -> "EMPTY");
        assertThat(res, equalTo("2world3"));
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

