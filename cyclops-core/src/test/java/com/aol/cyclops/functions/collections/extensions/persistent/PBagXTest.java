package com.aol.cyclops.functions.collections.extensions.persistent;

import com.aol.cyclops.collections.extensions.CollectionX;
import com.aol.cyclops.collections.extensions.persistent.PBagX;
import com.aol.cyclops.functions.collections.extensions.AbstractCollectionXTest;

public class PBagXTest extends AbstractCollectionXTest{

	@Override
	public <T> CollectionX<T> of(T... values) {
		return PBagX.of(values);
	}

}
