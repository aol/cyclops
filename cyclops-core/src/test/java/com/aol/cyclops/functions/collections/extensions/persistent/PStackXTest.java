package com.aol.cyclops.functions.collections.extensions.persistent;

import com.aol.cyclops.collections.extensions.CollectionX;
import com.aol.cyclops.collections.extensions.persistent.PStackX;
import com.aol.cyclops.functions.collections.extensions.CollectionXTestsWithNulls;

public class PStackXTest extends CollectionXTestsWithNulls{

	@Override
	public <T> CollectionX<T> of(T... values) {
		PStackX<T> list = PStackX.empty();
		for(T next : values){
			list = list.plus(list.size(),next);
		}
		System.out.println("List " + list);
		return list.efficientOpsOff();
		
	}

}
