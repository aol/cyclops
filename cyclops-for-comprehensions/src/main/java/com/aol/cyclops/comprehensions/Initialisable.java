package com.aol.cyclops.comprehensions;

public interface Initialisable<T extends Initialisable<T>> {
	public T init(BaseComprehensionData data);
}
