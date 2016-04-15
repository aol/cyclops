package com.aol.cyclops.control.anym.transformers.seq;

import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.FutureW;
import com.aol.cyclops.control.monads.transformers.FutureWT;
import com.aol.cyclops.control.monads.transformers.seq.FutureWTSeq;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.functions.collections.extensions.AbstractAnyMSeqOrderedDependentTest;
import com.aol.cyclops.types.anyM.AnyMSeq;
public class FutureWTListTest extends AbstractAnyMSeqOrderedDependentTest{

	@Override
	public <T> AnyMSeq<T> of(T... values) {
		return AnyM.fromFutureWTSeq(FutureWT.fromIterable(ListX.of(values).map(FutureW::ofResult)));
	}
	
	/* (non-Javadoc)
	 * @see com.aol.cyclops.functions.collections.extensions.AbstractCollectionXTest#empty()
	 */
	@Override
	public <T> AnyMSeq<T> empty() {
		return AnyM.fromIterable(FutureWTSeq.emptyList());
	}
	
	

}

