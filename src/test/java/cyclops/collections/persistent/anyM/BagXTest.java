package cyclops.collections.persistent.anyM;

import com.aol.cyclops2.types.anyM.AnyMSeq;
import cyclops.collections.AbstractAnyMSeqTest;
import cyclops.collections.immutable.BagX;
import cyclops.collections.immutable.OrderedSetX;
import cyclops.monads.AnyM;
import cyclops.monads.Witness;
import org.jooq.lambda.tuple.Tuple2;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

import static java.util.Comparator.comparing;
import static org.jooq.lambda.tuple.Tuple.tuple;
import static org.junit.Assert.assertEquals;

public class BagXTest extends AbstractAnyMSeqTest<Witness.bagX> {

	@Override
	public <T> AnyMSeq<Witness.bagX,T> of(T... values) {
		return AnyM.fromBagX(BagX.of(values));
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops2.function.collections.extensions.AbstractCollectionXTest#zero()
	 */
	@Override
	public <T> AnyMSeq<Witness.bagX,T> empty() {
		return AnyM.fromBagX(BagX.empty());
	}
	 /* (non-Javadoc)
     * @see com.aol.cyclops2.function.collections.extensions.AbstractAnyMSeqTest#whenGreaterThan2()
     */
    @Override
    @Test
    public void whenGreaterThan2() {
       
    }

    @Test
    public void testRetry(){
        of(1,2,3).retry(i->i+2).printOut();
    }

	@Test @Ignore
	public void testSorted() {



	}
}
