package com.aol.cyclops.functions.collections.extensions.standard.anyM;

import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.data.collections.extensions.standard.SetX;
import com.aol.cyclops.functions.collections.extensions.AbstractAnyMSeqTest;
import com.aol.cyclops.types.anyM.AnyMSeq;

public class SetXTest extends AbstractAnyMSeqTest{

	@Override
	public <T> AnyMSeq<T> of(T... values) {
		return AnyM.fromIterable(SetX.of(values));
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.functions.collections.extensions.AbstractCollectionXTest#empty()
	 */
	@Override
	public <T> AnyMSeq<T> empty() {
		return AnyM.fromIterable(SetX.empty());
	}
}
