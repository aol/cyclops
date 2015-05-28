package com.aol.cyclops.comprehensions;

import java.util.Map;

import org.pcollections.ConsPStack;
import org.pcollections.HashTreePMap;
import org.pcollections.PStack;

import com.aol.cyclops.comprehensions.converters.MonadicConverters;

class Foreach<T> {

	private PStack<Expansion> generators = ConsPStack.empty();

	
	
	public T yield(ExecutionState state) {
		Expansion head = generators.get(0);
		return new Yield<T>(generators)
				.process(state.contextualExecutor, HashTreePMap.empty(), head
				.getFunction().executeAndSetContext(HashTreePMap.empty()), head
				.getName(), 1);
	}

	void addExpansion(Expansion g) {
		generators = generators.plus(generators.size(),g);
	}

	public static<T> T foreach(ContextualExecutor<T,Foreach<T>> comprehension) {

		return comprehension.executeAndSetContext(new Foreach<>());
	}

}
