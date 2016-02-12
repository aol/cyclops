package com.aol.cyclops.types;

import com.aol.cyclops.monad.AnyM;

public interface ToAnyM<T> {

	default AnyM<T> anyM(){
		return AnyM.ofMonad(this);
	}
}
