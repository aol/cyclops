package cyclops.collectionx.standard.anyM;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import cyclops.collectionx.AbstractAnyMSeqOrderedDependentTest;
import cyclops.control.anym.Witness;
import org.junit.Test;

import cyclops.control.anym.AnyM;
import cyclops.reactive.ReactiveSeq;
import com.aol.cyclops2.types.anyM.AnyMSeq;
public class ReactiveSeqTest extends AbstractAnyMSeqOrderedDependentTest<Witness.reactiveSeq> {
    
	@Override
	public <T> AnyMSeq<Witness.reactiveSeq,T> of(T... values) {
		return AnyM.fromStream(ReactiveSeq.of(values));
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops2.function.collections.extensions.AbstractCollectionXTest#zero()
	 */
	@Override
	public <T> AnyMSeq<Witness.reactiveSeq,T> empty() {
		return AnyM.fromStream(ReactiveSeq.empty());
	}
	@Test
    public void when(){
        
        String res= AnyM.fromStream(ReactiveSeq.of(1,2,3)).visit((x,xs)->
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
	
	

}

