package cyclops.monads.collections.mutable;


import com.oath.cyclops.anym.AnyMSeq;
import cyclops.reactive.collections.mutable.QueueX;
import cyclops.monads.AnyM;
import cyclops.monads.Witness.queue;
import cyclops.monads.collections.AbstractAnyMSeqOrderedDependentTest;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class QueueXTest extends AbstractAnyMSeqOrderedDependentTest<queue> {

	@Override
	public <T> AnyMSeq<queue,T> of(T... values) {
		return AnyM.fromQueue(QueueX.of(values));
	}

	@Override
	public <T> AnyMSeq<queue,T> empty() {
		return AnyM.fromQueue(QueueX.empty());
	}

}

