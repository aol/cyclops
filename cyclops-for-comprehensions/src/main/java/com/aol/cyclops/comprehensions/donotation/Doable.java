package com.aol.cyclops.comprehensions.donotation;

import java.util.function.Function;
import java.util.function.Supplier;

import com.aol.cyclops.comprehensions.donotation.Do.DoComp2;

public interface Doable<T> {
	
	
	default  Object getValue(){
		return this;
	}
	default  DoComp2 doWithThisAnd(Function<T,?> f){
		return Do.with(getValue()).and(f);
	}
	default  DoComp2 doWithThisAndThat(Object o){
		return Do.with(getValue()).with(o);
	}
	default  DoComp2 doWithThisAndThat(Supplier o){
		return Do.with(getValue()).with(o);
	}
}
