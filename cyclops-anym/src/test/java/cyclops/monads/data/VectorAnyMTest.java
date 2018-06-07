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

    @Test @Ignore //only works for lazy data types
    public void testRecover1(){

     }
    @Test  @Ignore //only works for lazy data types
    public void testRecover2(){
    }

}

