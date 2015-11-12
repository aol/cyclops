package com.aol.cyclops.lambda.api;

import com.aol.cyclops.monad.AnyM;
import com.aol.cyclops.monad.AnyMFactory;

public class AnyMFactoryImpl implements AnyMFactory{

	@Override
	public <T> AnyM<T> of(Object o) {
		return AsAnyM.convertToAnyM(o);
	}
	@Override
	public <T> AnyM<T> monad(Object o) {
		return AsAnyM.notTypeSafeAnyM(o);
	}
}
