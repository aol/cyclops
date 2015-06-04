package com.aol.cyclops.lambda.monads;

import java.util.function.Function;

import lombok.AllArgsConstructor;
import lombok.experimental.Delegate;
import lombok.experimental.Wither;



@AllArgsConstructor
public class SimplexImpl<X> implements AnyM<X>{
	
	@Wither
	private final Object monad;
	public static <MONAD,T> Monad<MONAD,T>  of(Object of) {
		return new MonadWrapper(of);
		
	}
	public Object unwrap(){
		return monad;
	}
	@Override
    public String toString() {
        return String.format("AnyM(%s)", monad );
    }

	
}
