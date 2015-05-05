package com.aol.cyclops.comprehensions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
abstract class ContextualExecutor<T,C> {

	
	private  volatile C context;
	
	public T executeAndSetContext(C context){
			this.context = context;
			return execute();
		
	}
	public abstract T execute();
}
