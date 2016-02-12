package com.aol.cyclops.functions.collections.extensions.persistent.lazy;

import com.aol.cyclops.data.collections.extensions.CollectionX;
import com.aol.cyclops.data.collections.extensions.persistent.PBagX;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.functions.collections.extensions.AbstractCollectionXTest;
import com.aol.cyclops.functions.collections.extensions.AbstractLazyTest;

public class LazyPBagXTest extends AbstractLazyTest{

	@Override
	public <T> CollectionX<T> of(T... values) {
		return PBagX.of(values);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.functions.collections.extensions.AbstractCollectionXTest#empty()
	 */
	@Override
	public <T> CollectionX<T> empty() {
		return PBagX.empty();
	}

}
