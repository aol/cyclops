package com.aol.cyclops.comprehensions.converters;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

import org.pcollections.ConsPStack;
import org.pcollections.PStack;

public class MonadicConverters {
	
	@AllArgsConstructor
	public static enum Companion{ 
		instance(new MonadicConverters());
		
		@Getter
		private final MonadicConverters converters;
		
		public MonadicConverters withMoreConverters(List<MonadicConverter> convList){
			return new MonadicConverters(convList,defaultList);
		}
	}
	
	public static final PStack<MonadicConverter> defaultList = ConsPStack.<MonadicConverter>singleton(new CollectionToStreamConverter()).plus(new DecomposableToStreamConverter());
	
	
	private final PStack<MonadicConverter> converters;
	
	private MonadicConverters(){
		converters = ConsPStack.<MonadicConverter>singleton(new CollectionToStreamConverter()).plus(new DecomposableToStreamConverter());
	}
	public MonadicConverters(List<MonadicConverter> converters,List<MonadicConverter> converters2){
		this.converters = ConsPStack.from(converters).plusAll(converters2);
	}
	public MonadicConverters(List<MonadicConverter> converters){
		this.converters = ConsPStack.from(converters);
	}
	
	public Object convertToMonadicForm(Object o){
		return converters.stream().filter(t-> t.accept(o)).map(m -> m.convertToMonadicForm(o)).findFirst().orElse(o);
	}
	
}
