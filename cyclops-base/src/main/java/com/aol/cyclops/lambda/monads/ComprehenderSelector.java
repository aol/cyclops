package com.aol.cyclops.lambda.monads;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.aol.cyclops.comprehensions.comprehenders.Comprehenders;
import com.aol.cyclops.comprehensions.comprehenders.InvokeDynamicComprehender;
import com.aol.cyclops.lambda.api.Comprehender;

public class ComprehenderSelector {

	private final Comprehenders comprehenders = new Comprehenders();
	@SuppressWarnings("rawtypes")
	private final ConcurrentMap<Class,Comprehender> cache= new ConcurrentHashMap<>();
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Comprehender selectComprehender(Class structure) {
		
		return cache.computeIfAbsent(structure, st->comprehenders.getRegisteredComprehenders().stream()
																 .filter(e -> e.getKey().isAssignableFrom(structure))
																 .map(e->e.getValue())
																 .findFirst()
																 .orElse(new InvokeDynamicComprehender(Optional.of(structure))));
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Comprehender selectComprehender(Object structure) {

		return cache.computeIfAbsent(structure.getClass(), st-> comprehenders.getRegisteredComprehenders().stream()
																	.filter(e -> e.getKey().isAssignableFrom(structure.getClass()))
																	.map(e->e.getValue())
																	.findFirst()
																	.orElse(new InvokeDynamicComprehender(Optional.ofNullable(structure)
																	.map(Object::getClass))));
		
	}
	
}
