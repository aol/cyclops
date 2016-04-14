package com.aol.cyclops.control.anym.transformers.seq;

import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.monads.transformers.StreamableT;
import com.aol.cyclops.control.monads.transformers.seq.StreamableTSeq;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.functions.collections.extensions.AbstractAnyMSeqOrderedDependentTest;
import com.aol.cyclops.types.anyM.AnyMSeq;
import com.aol.cyclops.util.stream.Streamable;
public class StreamableTListTest extends AbstractAnyMSeqOrderedDependentTest{

	@Override
	public <T> AnyMSeq<T> of(T... values) {
		return AnyM.fromStreamableT(StreamableT.fromIterable(ListX.of(Streamable.of(values))));
	}
	
	/* (non-Javadoc)
	 * @see com.aol.cyclops.functions.collections.extensions.AbstractCollectionXTest#empty()
	 */
	@Override
	public <T> AnyMSeq<T> empty() {
		return AnyM.fromIterable(StreamableTSeq.emptyStreamable());
	}
	
	

}

