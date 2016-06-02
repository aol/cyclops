package com.aol.cyclops.control.anym.transformers.seq;

import org.junit.Test;

import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.monads.transformers.SetT;
import com.aol.cyclops.control.monads.transformers.seq.SetTSeq;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.data.collections.extensions.standard.SetX;
import com.aol.cyclops.functions.collections.extensions.AbstractAnyMSeqTest;
import com.aol.cyclops.types.anyM.AnyMSeq;
public class SetTListTest extends AbstractAnyMSeqTest{

	@Override
	public <T> AnyMSeq<T> of(T... values) {
		return AnyM.fromSetT(SetT.fromIterable(ListX.of(SetX.of(values))));
	}
	
	/* (non-Javadoc)
	 * @see com.aol.cyclops.functions.collections.extensions.AbstractCollectionXTest#empty()
	 */
	@Override
	public <T> AnyMSeq<T> empty() {
		return AnyM.fromIterable(SetTSeq.emptySet());
	}

    /* (non-Javadoc)
     * @see com.aol.cyclops.functions.collections.extensions.AbstractAnyMSeqTest#whenGreaterThan2()
     */
    @Override
    @Test
    public void whenGreaterThan2() {
       
    }
	
	
	

}

