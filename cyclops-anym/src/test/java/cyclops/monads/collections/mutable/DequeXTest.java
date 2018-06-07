package cyclops.monads.collections.mutable;

import com.oath.cyclops.anym.AnyMSeq;
import cyclops.monads.Witness;
import cyclops.monads.Witness.*;
import cyclops.reactive.collections.mutable.DequeX;
import cyclops.monads.AnyM;
import cyclops.monads.collections.AbstractAnyMSeqOrderedDependentTest;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class DequeXTest extends AbstractAnyMSeqOrderedDependentTest<deque> {

	@Override
	public <T> AnyMSeq<deque,T> of(T... values) {
		return AnyM.fromDeque(DequeX.of(values));
	}
	/* (non-Javadoc)
	 * @see com.oath.cyclops.function.collections.extensions.AbstractCollectionXTest#zero()
	 */
	@Override
	public <T> AnyMSeq<Witness.deque,T> empty() {
		return AnyM.fromDeque(DequeX.empty());
	}

}

