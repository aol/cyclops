package cyclops.monads.data;


import com.oath.cyclops.anym.AnyMSeq;
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

	@Override
	public <T> AnyMSeq<bankersQueue,T> empty() {
		return AnyM.fromBankersQueue(BankersQueue.empty());
	}

    @Test @Ignore //only works for lazy data types
    public void testRecover1(){

    }
    @Test  @Ignore //only works for lazy data types
    public void testRecover2(){
    }

}

