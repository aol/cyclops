package com.aol.cyclops.functions.collections.extensions.standard.anyM;

import cyclops.monads.Witness;
import org.junit.Test;

import cyclops.monads.AnyM;
import cyclops.collections.SetX;
import com.aol.cyclops.functions.collections.extensions.AbstractAnyMSeqTest;
import com.aol.cyclops.types.anyM.AnyMSeq;

public class SetXTest extends AbstractAnyMSeqTest<Witness.set>{

	@Override
	public <T> AnyMSeq<Witness.set,T> of(T... values) {
		return AnyM.fromSet(SetX.of(values));
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.function.collections.extensions.AbstractCollectionXTest#empty()
	 */
	@Override
	public <T> AnyMSeq<Witness.set,T> empty() {
		return AnyM.fromSet(SetX.empty());
	}
	 /* (non-Javadoc)
     * @see com.aol.cyclops.function.collections.extensions.AbstractAnyMSeqTest#whenGreaterThan2()
     */
    @Override
    @Test
    public void whenGreaterThan2() {
       
    }
}
