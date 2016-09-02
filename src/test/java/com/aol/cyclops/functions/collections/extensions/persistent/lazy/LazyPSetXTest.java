package com.aol.cyclops.functions.collections.extensions.persistent.lazy;

import com.aol.cyclops.data.collections.extensions.CollectionX;
import com.aol.cyclops.data.collections.extensions.persistent.PSetX;
import com.aol.cyclops.functions.collections.extensions.AbstractLazyTest;

public class LazyPSetXTest extends AbstractLazyTest{

	@Override
	public <T> CollectionX<T> of(T... values) {
		return PSetX.of(values);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.functions.collections.extensions.AbstractCollectionXTest#empty()
	 */
	@Override
	public <T> CollectionX<T> empty() {
		return PSetX.empty();
	}
}
