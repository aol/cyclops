package com.aol.cyclops.control.anym.transformers;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertThat;

import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Test;

import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.monads.transformers.ListT;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.functions.collections.extensions.AbstractAnyMSeqOrderedDependentTest;
import com.aol.cyclops.types.anyM.AnyMSeq;
import com.aol.cyclops.types.stream.reactive.SeqSubscriber;
public class ListTOptionalTest extends AbstractAnyMSeqOrderedDependentTest{

	@Override
	public <T> AnyMSeq<T> of(T... values) {
		return AnyM.fromListT(ListT.fromOptional(Optional.of(ListX.of(values))));
	}
	@Test
    public void stream(){
       
        assertThat(of(1,2,3).stream().collect(Collectors.toList()),hasItems(1,2,3));
    }
	
	@Test
	public void publisher(){
	    SeqSubscriber<Integer> sub = SeqSubscriber.subscriber();
	    of(1,2,3).subscribe(sub);
	    sub.forEach(System.out::println);
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
		
		String res=	of(1,2,3).visit((x,xs)-> xs.join(x>2? "hello" : "world"),
		                                                      ()->"boo!");
					
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
	

}

