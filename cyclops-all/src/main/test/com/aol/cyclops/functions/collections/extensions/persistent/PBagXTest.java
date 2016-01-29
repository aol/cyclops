package com.aol.cyclops.functions.collections.extensions.persistent;

import com.aol.cyclops.collections.extensions.CollectionX;
import com.aol.cyclops.collections.extensions.persistent.PBagX;
import com.aol.cyclops.functions.collections.extensions.CollectionXTestsWithNulls;

public class PBagXTest extends CollectionXTestsWithNulls{

	@Override
	public <T> CollectionX<T> of(T... values) {
		return PBagX.of(values);
	}

}
