package com.aol.cyclops.control.anym.transformers.seq;

import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.monads.transformers.StreamT;
import com.aol.cyclops.control.monads.transformers.seq.StreamTSeq;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.functions.collections.extensions.AbstractAnyMSeqOrderedDependentTest;
import com.aol.cyclops.types.anyM.AnyMSeq;
public class StreamTListTest extends AbstractAnyMSeqOrderedDependentTest{

	@Override
	public <T> AnyMSeq<T> of(T... values) {
		return AnyM.fromStreamT(StreamT.fromIterable(ListX.of(ReactiveSeq.of(values))));
	}
	
	/* (non-Javadoc)
	 * @see com.aol.cyclops.functions.collections.extensions.AbstractCollectionXTest#empty()
	 */
	@Override
	public <T> AnyMSeq<T> empty() {
		return AnyM.fromIterable(StreamTSeq.emptyStream());
	}
	
	

}

