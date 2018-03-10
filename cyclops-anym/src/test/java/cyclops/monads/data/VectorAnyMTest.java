package cyclops.monads.data;

import com.oath.cyclops.anym.AnyMSeq;
import cyclops.data.Vector;
import cyclops.monads.AnyM;
import cyclops.monads.Witness.vector;
import cyclops.monads.collections.AbstractAnyMSeqOrderedDependentTest;
import org.junit.Ignore;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class VectorAnyMTest extends AbstractAnyMSeqOrderedDependentTest<vector> {

	@Override
	public <T> AnyMSeq<vector,T> of(T... values) {
		return AnyM.fromVector(Vector.of(values));
	}
	/* (non-Javadoc)
	 * @see com.oath.cyclops.function.collections.extensions.AbstractCollectionXTest#zero()
	 */
	@Override
	public <T> AnyMSeq<vector,T> empty() {
		return AnyM.fromVector(Vector.empty());
	}
	@Test
    public void when(){

        String res=of(1,2,3).visit((x,xs)->
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

    @Test @Ignore //only works for lazy data types
    public void testRecover1(){

     }
    @Test  @Ignore //only works for lazy data types
    public void testRecover2(){
    }

}

