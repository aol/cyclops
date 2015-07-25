package com.aol.cyclops.lambda.monads;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.aol.cyclops.comprehensions.comprehenders.Comprehenders;
import com.aol.cyclops.comprehensions.comprehenders.InvokeDynamicComprehender;
import com.aol.cyclops.lambda.api.Comprehender;

public class ComprehenderSelector {

	private final Comprehenders comprehenders = new Comprehenders();
	@SuppressWarnings("rawtypes")
	private final Map<Class,Comprehender> cache= new HashMap<>();
	
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
