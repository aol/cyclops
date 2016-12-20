package com.aol.cyclops.functions.collections.extensions.standard.anyM;

import com.aol.cyclops.types.anyM.Witness;
import org.junit.Test;

import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.data.collections.extensions.standard.SetX;
import com.aol.cyclops.functions.collections.extensions.AbstractAnyMSeqTest;
import com.aol.cyclops.types.anyM.AnyMSeq;

public class SetXTest extends AbstractAnyMSeqTest<Witness.set>{

	@Override
	public <T> AnyMSeq<Witness.set,T> of(T... values) {
		return AnyM.fromSet(SetX.of(values));
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.functions.collections.extensions.AbstractCollectionXTest#empty()
	 */
	@Override
	public <T> AnyMSeq<Witness.set,T> empty() {
		return AnyM.fromSet(SetX.empty());
	}
	 /* (non-Javadoc)
     * @see com.aol.cyclops.functions.collections.extensions.AbstractAnyMSeqTest#whenGreaterThan2()
     */
    @Override
    @Test
    public void whenGreaterThan2() {
       
    }
}
