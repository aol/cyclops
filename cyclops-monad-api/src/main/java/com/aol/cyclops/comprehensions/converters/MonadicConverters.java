package com.aol.cyclops.comprehensions.converters;

import java.util.ServiceLoader;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

import lombok.Getter;
import lombok.val;

import org.pcollections.PStack;

import com.aol.cyclops.lambda.api.MonadicConverter;
import com.aol.cyclops.sequence.Reducers;
import com.aol.cyclops.sequence.SequenceM;
import com.sun.xml.internal.ws.util.StreamUtils;

public class MonadicConverters {
	

	private final  StreamUpscaler upscaler =  stream -> stream;//SequenceM.fromStream(stream);
	
	@Getter
	private final static PStack<MonadicConverter> converters;
	
	
	static {
		val loader  = ServiceLoader.load(MonadicConverter.class);
		converters = Reducers.<MonadicConverter>toPStack().mapReduce(StreamSupport.stream(Spliterators.spliteratorUnknownSize(loader.iterator(), Spliterator.ORDERED),
				false).sorted((a,b) ->  b.priority()-a.priority()));
		
	}
	
	
	public Object convertToMonadicForm(Object o){
		return upscaler.upscaleIfStream(converters.stream().filter(t-> t.accept(o)).map(m -> m.convertToMonadicForm(o)).findFirst().orElse(o));
	}
	
}
