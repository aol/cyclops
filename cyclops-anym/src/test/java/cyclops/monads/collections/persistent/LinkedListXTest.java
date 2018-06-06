package cyclops.monads.collections.persistent;

import com.oath.cyclops.anym.AnyMSeq;
import cyclops.reactive.collections.immutable.LinkedListX;
import cyclops.reactive.collections.mutable.ListX;
import cyclops.monads.AnyM;
import cyclops.monads.Witness.linkedListX;
import cyclops.monads.collections.AbstractAnyMSeqOrderedDependentTest;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class LinkedListXTest extends AbstractAnyMSeqOrderedDependentTest<linkedListX> {

	@Override
	public <T> AnyMSeq<linkedListX,T> of(T... values) {
		return AnyM.fromLinkedListX(LinkedListX.of(values));
	}

	@Override
	public <T> AnyMSeq<linkedListX,T> empty() {
		return AnyM.fromLinkedListX(LinkedListX.empty());
	}



}

