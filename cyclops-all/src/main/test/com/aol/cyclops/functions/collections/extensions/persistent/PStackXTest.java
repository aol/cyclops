package com.aol.cyclops.functions.collections.extensions.persistent;

import com.aol.cyclops.collections.extensions.CollectionX;
import com.aol.cyclops.collections.extensions.standard.ListX;
import com.aol.cyclops.functions.collections.extensions.AbstractOrderDependentCollectionXTest;
import com.aol.cyclops.functions.collections.extensions.CollectionXTestsWithNulls;

public class PStackXTest extends CollectionXTestsWithNulls{

	@Override
	public <T> CollectionX<T> of(T... values) {
		return ListX.of(values);
	}

}
