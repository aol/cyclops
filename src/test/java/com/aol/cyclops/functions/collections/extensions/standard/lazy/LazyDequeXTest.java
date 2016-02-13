package com.aol.cyclops.functions.collections.extensions.standard.lazy;

import com.aol.cyclops.data.collections.extensions.CollectionX;
import com.aol.cyclops.data.collections.extensions.standard.DequeX;
import com.aol.cyclops.functions.collections.extensions.AbstractCollectionXTest;
import com.aol.cyclops.functions.collections.extensions.AbstractLazyTest;

public class LazyDequeXTest extends AbstractLazyTest{

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
