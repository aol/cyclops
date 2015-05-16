package com.aol.cyclops.comprehensions.donotation;

import java.util.function.Function;

import com.aol.cyclops.comprehensions.donotation.Do.DoComp2;

public interface Doable extends Iterable {

	default <T> DoComp2 doWithThisAnd(Function<T,?> f){
		return Do.with(this).and(f);
	}
}
