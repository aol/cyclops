package com.aol.cyclops.functions.collections.extensions.persistent;

import com.aol.cyclops.collections.extensions.CollectionX;
import com.aol.cyclops.collections.extensions.persistent.PQueueX;
import com.aol.cyclops.collections.extensions.standard.ListX;
import com.aol.cyclops.functions.collections.extensions.AbstractCollectionXTest;

public class PQueueXTest extends AbstractCollectionXTest{

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
