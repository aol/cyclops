package com.aol.cyclops.lambda.monads;

import lombok.Value;
import lombok.experimental.Wither;

import com.aol.cyclops.lambda.api.Decomposable;

@Value
public class MonadWrapper<T,MONAD> implements Monad<T,MONAD>, Decomposable{
	@Wither
	private final Object monad;
	public static <T,MONAD> Monad<T,MONAD>  of(Object of) {
		return new MonadWrapper(of);
		
	}
	public MONAD unwrap(){
		return (MONAD)monad;
	}
	
	
}
