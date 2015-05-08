package com.aol.cyclops.comprehensions;

import java.util.Map;

import lombok.AllArgsConstructor;

import com.aol.cyclops.comprehensions.comprehenders.Comprehenders;
import com.aol.cyclops.comprehensions.converters.MonadicConverters;

@AllArgsConstructor
final class State {

	public final Comprehenders comprehenders;
	public final MonadicConverters converters;
	
	public State(){
		this.comprehenders = Comprehenders.Companion.instance.getComprehenders();
		this.converters = MonadicConverters.Companion.instance.getConverters();
	}
}
