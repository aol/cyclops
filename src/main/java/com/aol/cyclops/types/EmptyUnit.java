package com.aol.cyclops.types;

public interface EmptyUnit<T> extends Unit<T> {
	public <T> Unit<T> emptyUnit();
}
