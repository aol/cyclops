package com.aol.cyclops.validation;

import java.util.Optional;


public interface ValidationResult<T,E> {
	
	public Optional<? super T> success();
	public Optional<? super E> failure();
	
}

