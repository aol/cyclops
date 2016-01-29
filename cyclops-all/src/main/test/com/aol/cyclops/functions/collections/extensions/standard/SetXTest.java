package com.aol.cyclops.functions.collections.extensions.standard;

import com.aol.cyclops.collections.extensions.CollectionX;
import com.aol.cyclops.collections.extensions.standard.SetX;
import com.aol.cyclops.functions.collections.extensions.AbstractCollectionXTest;

public class SetXTest extends AbstractCollectionXTest{

	@Override
	public <T> CollectionX<T> of(T... values) {
		return SetX.of(values);
	}

}
