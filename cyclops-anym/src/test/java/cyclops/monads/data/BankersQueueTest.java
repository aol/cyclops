package cyclops.monads.data;


import com.oath.anym.AnyMSeq;
import cyclops.reactive.collections.mutable.ListX;
import cyclops.data.BankersQueue;
import cyclops.monads.AnyM;
import cyclops.monads.Witness.bankersQueue;
import cyclops.monads.collections.AbstractAnyMSeqOrderedDependentTest;
import org.junit.Ignore;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class BankersQueueTest extends AbstractAnyMSeqOrderedDependentTest<bankersQueue> {

	@Override
	public <T> AnyMSeq<bankersQueue,T> of(T... values) {
		return AnyM.fromBankersQueue(BankersQueue.of(values));
	}
	/* (non-Javadoc)
	 * @see com.oath.cyclops.function.collections.extensions.AbstractCollectionXTest#zero()
	 */
	@Override
	public <T> AnyMSeq<bankersQueue,T> empty() {
		return AnyM.fromBankersQueue(BankersQueue.empty());
	}
	@Test
    public void when(){

        String res= AnyM.fromList(ListX.of(1,2,3)).visit((x,xs)->
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

