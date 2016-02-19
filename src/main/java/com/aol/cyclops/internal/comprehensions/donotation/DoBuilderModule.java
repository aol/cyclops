package com.aol.cyclops.internal.comprehensions.donotation;

import java.util.function.Function;
import java.util.function.Predicate;

import lombok.Value;

public interface DoBuilderModule {
	@Value
	public static class Assignment{

		Function f;
	}
	@Value
	public static  class Entry{
		String key;
		Object value;
	}
	@Value
	public class Guard{
		
		Function f;
		public Guard(Function f){
            this.f=f;
		}
        
		public Guard(Predicate p){
		    f= a->p.test(a);
		}
	}

}
