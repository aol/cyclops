package com.aol.cyclops.comprehensions.anyM;

import java.util.function.Function;

import com.aol.cyclops.comprehensions.donotation.typed.Do;
import com.aol.cyclops.monad.AnyM;

public class AnyMForComprehensions<U> {

	public <R1, R> AnyM<R> forEachAnyM2(AnyM<U> anyM, Function<U, ? extends AnyM<R1>> monad, Function<U, Function<R1, R>> yieldingFunction) {
		return Do.add(anyM).withAnyM(u -> monad.apply(u)).yield(yieldingFunction).unwrap();

	}

	
	public <R1,R> AnyM<R> forEachAnyM2(AnyM<U> anyM, Function<U,? extends AnyM<R1>> monad, 
												Function<U, Function<R1, Boolean>> filterFunction,
													Function<U,Function<R1,R>> yieldingFunction ){
		 return Do.add(anyM)
				  .withAnyM(u->monad.apply(u))
				  .filter(filterFunction)
				  .yield(yieldingFunction).unwrap();
			
	}

}
