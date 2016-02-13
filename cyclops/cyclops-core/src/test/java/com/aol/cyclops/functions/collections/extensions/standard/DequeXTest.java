package com.aol.cyclops.functions.collections.extensions.standard;

import com.aol.cyclops.data.collections.extensions.CollectionX;
import com.aol.cyclops.data.collections.extensions.standard.DequeX;
import com.aol.cyclops.functions.collections.extensions.AbstractCollectionXTest;

public class DequeXTest extends AbstractCollectionXTest{

	@Override
	public <T> CollectionX<T> of(T... values) {
		return DequeX.of(values);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.functions.collections.extensions.AbstractCollectionXTest#empty()
	 */
	@Override
	public <T> CollectionX<T> empty() {
		return DequeX.empty();
	}
	

}
