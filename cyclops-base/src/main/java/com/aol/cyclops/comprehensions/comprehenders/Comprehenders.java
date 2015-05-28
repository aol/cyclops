package com.aol.cyclops.comprehensions.comprehenders;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import org.pcollections.HashTreePMap;
import org.pcollections.PMap;

import com.aol.cyclops.lambda.api.Comprehender;
import com.aol.cyclops.lambda.api.Reducers;
import com.aol.cyclops.streams.StreamUtils;

/**
 * Registered Comprehenders
 * 
 * @author johnmcclean
 *
 */
public class Comprehenders {
	
	private final static PMap<Class,Comprehender> comprehenders;
	static {
		
		ServiceLoader<Comprehender> loader  = ServiceLoader.load(Comprehender.class);
	
	
		comprehenders = Reducers.<Class,Comprehender>toPMap().reduce(StreamUtils.stream(loader.iterator())
													.filter(c -> !(c instanceof InvokeDynamicComprehender))
													.map(comp->HashTreePMap.singleton(comp.getTargetClass(),comp)));
	
		
	}
	
	
	/**
	 * @return Registered Comprehenders
	 */
	public Map<Class,Comprehender> getRegisteredComprehenders(){
		return new HashMap(comprehenders);
	}
	
}
