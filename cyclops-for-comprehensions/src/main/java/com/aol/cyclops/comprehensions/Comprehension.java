package com.aol.cyclops.comprehensions;

import java.util.Map;

import org.pcollections.ConsPStack;
import org.pcollections.HashTreePMap;
import org.pcollections.PStack;

public class Comprehension<T> {

	private PStack<Expansion> generators = ConsPStack.empty();

	public T yield(ContextualExecutor<?, Map> c) {
		Expansion head = generators.get(0);
		return new Yield<T>(generators).process(c, HashTreePMap.empty(), head
				.getFunction().executeAndSetContext(HashTreePMap.empty()), head
				.getName(), 1);
	}

	void addExpansion(Expansion g) {
		generators = generators.plus(g);
	}

	static Object foreach(ContextualExecutor comprehension) {

		return comprehension.executeAndSetContext(new Comprehension());
	}

}
