package com.aol.cyclops.internal.comprehensions;

import java.util.function.Function;

import com.aol.cyclops.internal.comprehensions.ComprehensionsModule.ComprehensionData;
import com.aol.cyclops.internal.comprehensions.ComprehensionsModule.ContextualExecutor;
import com.aol.cyclops.internal.comprehensions.ComprehensionsModule.ExecutionState;
import com.aol.cyclops.internal.comprehensions.ComprehensionsModule.Foreach;
import com.aol.cyclops.internal.comprehensions.ComprehensionsModule.Initialisable;


public class FreeFormForComprehension<X> {
	

	
	@SuppressWarnings("unchecked")
	<T,R> R foreachNoClass(Function<ComprehensionData<T,R>,R> fn){
		return Foreach.foreach(new ContextualExecutor(new Foreach<R>()){
			@SuppressWarnings("rawtypes")
			public R execute(){
				return (R)fn.apply(new ComprehensionData(new ExecutionState(this)));
			}

			
		});
	}
	
	
	
	@SuppressWarnings("unchecked")
	public <R> R foreach(Function<X,R> fn){
	    return (R)foreachNoClass((Function)fn);
		
	}
	
	
		
	
}
