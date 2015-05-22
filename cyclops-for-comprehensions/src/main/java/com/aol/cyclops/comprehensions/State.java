package com.aol.cyclops.comprehensions;

import lombok.AllArgsConstructor;

import org.jooq.lambda.Seq;

import com.aol.cyclops.comprehensions.comprehenders.Comprehenders;
import com.aol.cyclops.comprehensions.converters.MonadicConverters;



@AllArgsConstructor
final class State {
	
	public static final StreamUpscaler globalStreamConverter = getConverter();
	public final Comprehenders comprehenders;
	public final MonadicConverters converters;
	public final StreamUpscaler streamConverter ;
	
	public State(){
		this.comprehenders = Comprehenders.Companion.instance.getComprehenders();
		this.converters = MonadicConverters.Companion.instance.getConverters();
		this.streamConverter = globalStreamConverter;
	}

	private static StreamUpscaler getConverter() {
		try{
			Class.forName("org.jooq.lambda.Seq");
			return stream -> Seq.seq(stream);
		}catch(ClassNotFoundException e){
			return  stream -> stream;
		}
		
	}
	
}
