package com.aol.cyclops2.functions.collections.extensions.standard.anyM;

import cyclops.monads.Witness;
import org.junit.Test;

import cyclops.monads.AnyM;
import cyclops.collections.mutable.SetX;
import com.aol.cyclops2.functions.collections.extensions.AbstractAnyMSeqTest;
import com.aol.cyclops2.types.anyM.AnyMSeq;

public class SetXTest extends AbstractAnyMSeqTest<Witness.set>{

	@Override
	public <T> AnyMSeq<Witness.set,T> of(T... values) {
		return AnyM.fromSet(SetX.of(values));
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops2.function.collections.extensions.AbstractCollectionXTest#zero()
	 */
	@Override
	public <T> AnyMSeq<Witness.set,T> empty() {
		return AnyM.fromSet(SetX.empty());
	}
	 /* (non-Javadoc)
     * @see com.aol.cyclops2.function.collections.extensions.AbstractAnyMSeqTest#whenGreaterThan2()
     */
    @Override
    @Test
    public void whenGreaterThan2() {
       
    }

    @Test
    public void testRetry(){
        of(1,2,3).retry(i->i+2).printOut();
    }
}
