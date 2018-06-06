package cyclops.monads.data;

import com.oath.cyclops.anym.AnyMSeq;
import cyclops.data.Seq;
import cyclops.monads.AnyM;
import cyclops.monads.Witness.seq;
import cyclops.monads.collections.AbstractAnyMSeqOrderedDependentTest;
import org.junit.Ignore;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class SeqAnyMTest extends AbstractAnyMSeqOrderedDependentTest<seq> {

	@Override
	public <T> AnyMSeq<seq,T> of(T... values) {
		return AnyM.fromSeq(Seq.of(values));
	}

	@Override
	public <T> AnyMSeq<seq,T> empty() {
		return AnyM.fromSeq(Seq.empty());
	}

    @Test @Override @Ignore //only works for lazy data types
    public void testRecover1(){

     }
    @Test  @Ignore //only works for lazy data types
    public void testRecover2(){
    }

}

