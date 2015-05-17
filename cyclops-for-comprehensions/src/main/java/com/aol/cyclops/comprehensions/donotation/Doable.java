package com.aol.cyclops.comprehensions.donotation;

import java.util.Iterator;
import java.util.function.Function;

import com.aol.cyclops.comprehensions.donotation.Do.DoComp2;

public interface Doable<T> {//extends Iterable<T> {
	
	
	default  T getValue(){
		return (T)this;
	}
	default  DoComp2 doWithThisAnd(Function<T,?> f){
		return Do.with(getValue()).and(f);
	}
}
