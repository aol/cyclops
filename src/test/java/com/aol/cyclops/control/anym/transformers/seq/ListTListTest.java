package com.aol.cyclops.control.anym.transformers.seq;

import java.util.Arrays;

import org.junit.Test;

import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.monads.transformers.ListT;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.functions.collections.extensions.AbstractAnyMSeqOrderedDependentTest;
import com.aol.cyclops.types.anyM.AnyMSeq;
public class ListTListTest extends AbstractAnyMSeqOrderedDependentTest{

	@Override
	public <T> AnyMSeq<T> of(T... values) {
		return AnyM.fromListT(ListT.fromIterable(ListX.of(ListX.of(values))));
	}
	
	/* (non-Javadoc)
	 * @see com.aol.cyclops.functions.collections.extensions.AbstractCollectionXTest#empty()
	 */
	@Override
	public <T> AnyMSeq<T> empty() {
		return AnyM.fromIterable(ListT.emptyList());
	}
	
	
	@Test
	public void listT(){
	    //check this issue doesn't affect cyclops-react ListT : https://github.com/scalaz/scalaz/pull/166
	    ListT.fromIterable(Arrays.asList(ListX.range(0,2000))).map(i->i*2).printOut();
	}
}

