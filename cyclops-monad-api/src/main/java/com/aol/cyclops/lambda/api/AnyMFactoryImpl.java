package com.aol.cyclops.lambda.api;

import com.aol.cyclops.comprehensions.converters.MonadicConverters;
import com.aol.cyclops.lambda.monads.MonadWrapper;
import com.aol.cyclops.monad.AnyM;
import com.aol.cyclops.monad.AnyMFactory;
import com.aol.cyclops.monad.AnyMFunctions;
import com.aol.cyclops.monad.AnyMonads;

public class AnyMFactoryImpl implements AnyMFactory{

	/* 
	 * This will convert the supplied Object if possible into a supported Monad type (or more efficient type)
	 * (non-Javadoc)
	 * @see com.aol.cyclops.monad.AnyMFactory#of(java.lang.Object)
	 */
	@Override
	public <T> AnyM<T> of(Object o) {
		return new MonadWrapper<>(new MonadicConverters().convertToMonadicForm(o)).anyM();
	}
	/* This will accept the supplied monad as is
	 * 
	 * (non-Javadoc)
	 * @see com.aol.cyclops.monad.AnyMFactory#monad(java.lang.Object)
	 */
	@Override
	public <T> AnyM<T> monad(Object o) {
		return new MonadWrapper<>(o).anyM();
	}
	@Override
	public AnyMFunctions anyMonads() {
		return new AnyMonads();
	}
}
