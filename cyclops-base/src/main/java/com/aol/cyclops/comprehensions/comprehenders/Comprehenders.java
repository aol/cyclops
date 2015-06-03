package com.aol.cyclops.comprehensions.comprehenders;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.stream.Stream;

import org.pcollections.HashTreePMap;
import org.pcollections.PMap;
import org.pcollections.PStack;

import com.aol.cyclops.lambda.api.Comprehender;
import com.aol.cyclops.lambda.api.MonadicConverter;
import com.aol.cyclops.lambda.api.Reducers;
import com.aol.cyclops.streams.StreamUtils;

/**
 * Registered Comprehenders
 * 
 * @author johnmcclean
 *
 */
public class Comprehenders {
	
	private final static PStack<Map.Entry<Class,Comprehender>> comprehenders;
	static {	
		ServiceLoader<Comprehender> loader  = ServiceLoader.load(Comprehender.class);
		

		comprehenders = Reducers.<Map.Entry<Class,Comprehender>>toPStack().mapReduce((Stream)StreamUtils.stream(loader.iterator())
													.sorted((a,b) ->  b.priority()-a.priority())
													.filter(c -> !(c instanceof InvokeDynamicComprehender))
													.map(comp->new AbstractMap.SimpleEntry(comp.getTargetClass(),comp)));
	
	}
	
	
	/**
	 * @return Registered Comprehenders
	 */
	public PStack<Map.Entry<Class,Comprehender>> getRegisteredComprehenders(){
		return comprehenders;
	}
	
}
