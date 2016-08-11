package com.aol.cyclops.functions.collections.extensions.persistent.lazy;

import com.aol.cyclops.data.collections.extensions.CollectionX;
import com.aol.cyclops.data.collections.extensions.persistent.PQueueX;
import com.aol.cyclops.functions.collections.extensions.AbstractLazyTest;

public class LazyPQueueXTest extends AbstractLazyTest{

	@Override
	public <T> CollectionX<T> of(T... values) {
		return PQueueX.of(values);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.functions.collections.extensions.AbstractCollectionXTest#empty()
	 */
	@Override
	public <T> CollectionX<T> empty() {
		return PQueueX.empty();
	}
}
