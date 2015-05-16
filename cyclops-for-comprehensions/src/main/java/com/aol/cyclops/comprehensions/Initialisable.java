package com.aol.cyclops.comprehensions;

public interface Initialisable<T extends Initialisable> {
	public T init(BaseComprehensionData data);
}
