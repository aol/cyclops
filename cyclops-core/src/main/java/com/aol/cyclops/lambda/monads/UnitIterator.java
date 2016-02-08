package com.aol.cyclops.lambda.monads;

import java.util.Iterator;

public interface UnitIterator<U> {
	UnitIterator<U> unitIteratorTyped(Iterator<U> U);
}
