package com.aol.cyclops.control;

import java.util.function.Function;

import com.aol.cyclops.types.Functor;

public interface Reader<T,R> extends Function<T,R>, 
                                     Functor<R>{

	default <R1> Reader<T, R1> map(Function<? super R, ? extends R1> f2){
	    return FluentFunctions.of(this.andThen( f2));
	}

	default <R1> Reader<T, R1> flatMap(Function<? super R, ? extends Reader<T, R1>> f){
	    return FluentFunctions.of(a -> f.apply(this.apply(a)).apply(a));
	}
	
	default AnyM<R> anyM(){
	    return AnyM.ofValue(this);
	}

	
}
