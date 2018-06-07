package cyclops.monads.collections.mutable;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;


import com.oath.cyclops.anym.AnyMSeq;
import com.oath.cyclops.ReactiveConvertableSequence;
import cyclops.monads.Witness;
import cyclops.monads.collections.AbstractAnyMSeqOrderedDependentTest;
import org.junit.Test;

import cyclops.monads.AnyM;
import cyclops.companion.Streamable;

public class StreamableTest extends AbstractAnyMSeqOrderedDependentTest<Witness.streamable> {

	@Override
	public <T> AnyMSeq<Witness.streamable,T> of(T... values) {
		return AnyM.fromStreamable(Streamable.of(values));
	}
	/* (non-Javadoc)
	 * @see com.oath.cyclops.function.collections.extensions.AbstractCollectionXTest#zero()
	 */
	@Override
	public <T> AnyMSeq<Witness.streamable,T> empty() {
		return AnyM.fromStreamable(Streamable.empty());
	}


	@Test
    public void testCycleTimesNotAnyM(){
        assertEquals(asList(1, 2, 1, 2, 1, 2),Streamable.of(1, 2).cycle(3).to(ReactiveConvertableSequence::converter).listX());
    }

}

