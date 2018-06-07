package cyclops.monads.collections.persistent;


import com.oath.cyclops.anym.AnyMSeq;
import cyclops.reactive.collections.immutable.PersistentQueueX;
import cyclops.reactive.collections.mutable.ListX;
import cyclops.monads.AnyM;
import cyclops.monads.Witness.persistentQueueX;
import cyclops.monads.collections.AbstractAnyMSeqOrderedDependentTest;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class PersistentQueueXTest extends AbstractAnyMSeqOrderedDependentTest<persistentQueueX> {

	@Override
	public <T> AnyMSeq<persistentQueueX,T> of(T... values) {
		return AnyM.fromPersistentQueueX(PersistentQueueX.of(values));
	}

	@Override
	public <T> AnyMSeq<persistentQueueX,T> empty() {
		return AnyM.fromPersistentQueueX(PersistentQueueX.empty());
	}



}

