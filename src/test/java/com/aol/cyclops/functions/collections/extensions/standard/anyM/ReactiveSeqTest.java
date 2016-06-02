package com.aol.cyclops.functions.collections.extensions.standard.anyM;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.functions.collections.extensions.AbstractAnyMSeqOrderedDependentTest;
import com.aol.cyclops.types.anyM.AnyMSeq;
public class ReactiveSeqTest extends AbstractAnyMSeqOrderedDependentTest{
    
	@Override
	public <T> AnyMSeq<T> of(T... values) {
		return AnyM.fromIterable(ReactiveSeq.of(values));
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
        
        String res= AnyM.fromIterable(ReactiveSeq.of(1,2,3)).visit((x,xs)->
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
        
        
        String res= of(1,2,3).visit((x,xs)-> x>2? "hello" : "world",()->"EMPTY");
        assertThat(res,equalTo("2world3"));
    }
	
	

}

