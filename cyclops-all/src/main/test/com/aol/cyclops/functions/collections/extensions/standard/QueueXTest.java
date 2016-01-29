package com.aol.cyclops.functions.collections.extensions.standard;

import com.aol.cyclops.collections.extensions.CollectionX;
import com.aol.cyclops.collections.extensions.standard.QueueX;
import com.aol.cyclops.functions.collections.extensions.CollectionXTestsWithNulls;

public class QueueXTest extends CollectionXTestsWithNulls{

	@Override
	public <T> CollectionX<T> of(T... values) {
		return QueueX.of(values);
	}

}
