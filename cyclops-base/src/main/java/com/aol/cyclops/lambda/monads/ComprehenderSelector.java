package com.aol.cyclops.lambda.monads;

import java.util.Optional;

import lombok.AllArgsConstructor;


import com.aol.cyclops.comprehensions.comprehenders.Comprehenders;
import com.aol.cyclops.comprehensions.comprehenders.InvokeDynamicComprehender;
import com.aol.cyclops.lambda.api.Comprehender;

public class ComprehenderSelector {

	private final Comprehenders comprehenders = new Comprehenders();
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Comprehender selectComprehender(Object structure) {
		
		return comprehenders.getRegisteredComprehenders().entrySet().stream()
				.filter(e -> e.getKey().isAssignableFrom(structure.getClass()))
				.map(e->e.getValue())
				.findFirst()
				.orElse(new InvokeDynamicComprehender(Optional.ofNullable(structure)
				.map(Object::getClass)));
	}
}
