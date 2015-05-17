package com.aol.cyclops.comprehensions;

import lombok.AllArgsConstructor;
import lombok.Setter;

import com.aol.cyclops.comprehensions.comprehenders.Comprehenders;
import com.aol.cyclops.comprehensions.converters.MonadicConverters;

@AllArgsConstructor
final class State {
	
	public static volatile StreamConverter globalStreamConverter = stream -> stream;
	public final Comprehenders comprehenders;
	public final MonadicConverters converters;
	public final StreamConverter streamConverter ;
	
	public State(){
		this.comprehenders = Comprehenders.Companion.instance.getComprehenders();
		this.converters = MonadicConverters.Companion.instance.getConverters();
		this.streamConverter = globalStreamConverter;
	}
	
	public static <T,X> void setGlobalStreamConverter(StreamConverter<T,X> converter){
		globalStreamConverter = converter;
	}
}
