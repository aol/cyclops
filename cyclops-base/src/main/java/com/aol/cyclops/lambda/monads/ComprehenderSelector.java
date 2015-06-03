package com.aol.cyclops.lambda.monads;

import java.util.Optional;

import lombok.AllArgsConstructor;


import com.aol.cyclops.comprehensions.comprehenders.Comprehenders;
import com.aol.cyclops.comprehensions.comprehenders.InvokeDynamicComprehender;
import com.aol.cyclops.lambda.api.Comprehender;

public class ComprehenderSelector {

	private final Comprehenders comprehenders = new Comprehenders();
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Comprehender selectComprehender(Class structure) {
		
		return comprehenders.getRegisteredComprehenders().stream()
				.filter(e -> e.getKey().isAssignableFrom(structure))
				.map(e->e.getValue())
				.findFirst()
				.orElse(new InvokeDynamicComprehender(Optional.of(structure)));
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Comprehender selectComprehender(Object structure) {
//		comprehenders.getRegisteredComprehenders().entrySet().forEach(System.out::println);
		return comprehenders.getRegisteredComprehenders().stream()
				.filter(e -> e.getKey().isAssignableFrom(structure.getClass()))
				.map(e->e.getValue())
				.findFirst()
				.map(i-> { System.out.println("comp is " + i + " o  is " + structure); return i;})
				.orElse(new InvokeDynamicComprehender(Optional.ofNullable(structure)
				.map(Object::getClass)));
	}
}
