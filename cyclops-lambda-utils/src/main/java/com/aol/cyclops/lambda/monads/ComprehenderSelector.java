package com.aol.cyclops.lambda.monads;

import java.util.Optional;

import lombok.AllArgsConstructor;


import com.aol.cyclops.comprehensions.comprehenders.Comprehenders;
import com.aol.cyclops.comprehensions.comprehenders.ReflectionComprehender;
import com.aol.cyclops.lambda.api.Comprehender;

public class ComprehenderSelector {

	
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Comprehender selectComprehender(Comprehenders comprehenders,Object structure) {
		
		return comprehenders.getRegisteredComprehenders().entrySet().stream()
				.filter(e -> e.getKey().isAssignableFrom(structure.getClass()))
				.map(e->e.getValue())
				.findFirst()
				.orElse(new ReflectionComprehender(Optional.ofNullable(structure)
				.map(Object::getClass)));
	}
}
