package com.aol.cyclops.comprehensions.donotation;

import java.util.function.Function;
import java.util.function.Supplier;

import com.aol.cyclops.comprehensions.donotation.UntypedDo.DoComp2;

public interface Doable<T> {
	
	
	default  Object getValue(){
		return this;
	}
	default  DoComp2 doWithThisAnd(Function<T,?> f){
		return UntypedDo.add(getValue()).with(f);
	}
	default  DoComp2 doWithThisAndThat(Object o){
		return UntypedDo.add(getValue()).add(o);
	}
	default  DoComp2 doWithThisAndThat(Supplier o){
		return UntypedDo.add(getValue()).add(o);
	}
}
