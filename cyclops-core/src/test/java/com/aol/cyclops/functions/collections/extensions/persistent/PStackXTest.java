package com.aol.cyclops.functions.collections.extensions.persistent;

import com.aol.cyclops.collections.extensions.CollectionX;
import com.aol.cyclops.collections.extensions.persistent.PStackX;
import com.aol.cyclops.functions.collections.extensions.CollectionXTestsWithNulls;

public class PStackXTest extends CollectionXTestsWithNulls{

	@Override
	public <T> CollectionX<T> of(T... values) {
	
		return PStackX.of(values);
	}

}
