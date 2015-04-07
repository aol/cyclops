package com.aol.cyclops.api;

public interface OptionConverter <T,R>{

	public R option(T t);
}
