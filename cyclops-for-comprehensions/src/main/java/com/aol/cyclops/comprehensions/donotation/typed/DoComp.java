package com.aol.cyclops.comprehensions.donotation.typed;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import lombok.AllArgsConstructor;

import org.pcollections.PStack;

import com.aol.cyclops.comprehensions.ComprehensionData;
import com.aol.cyclops.comprehensions.ForComprehensions;
import com.aol.cyclops.lambda.api.Unwrapable;
import com.aol.cyclops.lambda.utils.Mutable;

@AllArgsConstructor
public abstract class DoComp {
	
	PStack<Entry> assigned;
	
	protected PStack<Entry> addToAssigned(Function f){
		return assigned.plus(assigned.size(),createEntry(f));
	}
	protected Entry createEntry(Function f){
		return new Entry("$$monad"+assigned.size(),new Assignment(f));
	}
	
	protected <T> T yieldInternal(Function f){
		return (T)ForComprehensions.foreachX(c->build(c,f));
	}
	
	 @SuppressWarnings({"rawtypes","unchecked"})
	private Object handleNext(Entry e,ComprehensionData c,List<String> assigned){
		 List<String>  newList = new ArrayList(assigned); 
		if(e.getValue() instanceof Guard){
			
			final Function f = ((Guard)e.getValue()).getF();
			c.filter( ()-> {
						
						return unwrapNestedFunction(c, f, newList);
							
							}  );
			
		}
		else if(e.getValue() instanceof Assignment){
			
			final Function f = ((Assignment)e.getValue()).getF();
			c.$(e.getKey(), ()-> {
							
								return unwrapNestedFunction(c, f, newList);
							
							}  );
			
		}
		else
			c.$(e.getKey(),handleUnwrappable(e.getValue()));
		
		return null;
	}
	 private Object handleUnwrappable(Object o){
		 if(o instanceof Unwrapable)
				return ((Unwrapable)o).unwrap();
			return o;
	 }
	private Object build(
			ComprehensionData c, Function f) {
		Mutable<List<String>> vars = new Mutable<>(new ArrayList());
		assigned.stream().forEach(e-> addToVar(e,vars,handleNext(e,c,vars.get())));
		Mutable<Object> var = new Mutable<>(f);
		
		return c.yield(()-> { 
			return unwrapNestedFunction(c, f, vars.get());
			
	}  );
		
	}
	private Object unwrapNestedFunction(ComprehensionData c, Function f,
			List<String> vars) {
		Function next = f;
		Object result = null;
		for(String e : vars){
			
				result = next.apply(c.$(e ));
			if(result instanceof Function){
				next = ((Function)result);
			}
			
		}
		if(result instanceof Unwrapable)
			return ((Unwrapable)result).unwrap();
		return result;
	}

	private Object addToVar(Entry e,Mutable<List<String>> vars, Object handleNext) {
		if(!(e.getValue() instanceof Guard)){	
			vars.get().add(e.getKey());
		}
		return handleNext;
	}

}