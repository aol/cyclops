package cyclops.monads.collections.mutable;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.oath.cyclops.anym.AnyMSeq;
import cyclops.monads.Witness.list;
import cyclops.monads.collections.AbstractAnyMSeqOrderedDependentTest;
import org.junit.Test;

import cyclops.monads.AnyM;
import cyclops.reactive.collections.mutable.ListX;

public class ListXTest extends AbstractAnyMSeqOrderedDependentTest<list> {

	@Override
	public <T> AnyMSeq<list,T> of(T... values) {
		return AnyM.fromList(ListX.of(values));
	}

	@Override
	public <T> AnyMSeq<list,T> empty() {
		return AnyM.fromList(ListX.empty());
	}


}

