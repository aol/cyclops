package com.aol.cyclops.comprehensions;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import lombok.AllArgsConstructor;

import org.pcollections.PMap;

import com.aol.cyclops.comprehensions.comprehenders.IntStreamComprehender;
import com.aol.cyclops.comprehensions.comprehenders.LongStreamComprehender;
import com.aol.cyclops.comprehensions.comprehenders.OptionalComprehender;
import com.aol.cyclops.comprehensions.comprehenders.ReflectionComprehender;
import com.aol.cyclops.comprehensions.comprehenders.StreamComprehender;

@AllArgsConstructor
public class Yield<T> {
	
	private static final Map<Class,Comprehender> comprehenders = new HashMap(){{
		put(Optional.class,new OptionalComprehender());
		put(Stream.class,new StreamComprehender());
		put(IntStream.class,new IntStreamComprehender());
		put(LongStream.class,new LongStreamComprehender());
	}};

	private final  List<Expansion> expansions;
	
	T process(ContextualExecutor<?,Map> yieldExecutor, PMap<String,Object> context, 
						Object currentExpansionUnwrapped, String lastExpansionName, int index) {
		
		if (expansions.size() == index) {
			
			return (T)selectComprehender(currentExpansionUnwrapped).map( currentExpansionUnwrapped,it->yieldExecutor.executeAndSetContext(context.plus(lastExpansionName,it)));
		
		} else {
			Expansion head = expansions.get(index);
			
			if (head instanceof Filter) {
				
				System.out.println("Context : " + context);
				
				Object s = selectComprehender(currentExpansionUnwrapped).filter(currentExpansionUnwrapped,it->   (boolean)head.getFunction().executeAndSetContext(context.plus(lastExpansionName,it)));
				return process(yieldExecutor, context, s, lastExpansionName,index+1);
			} else {
				
				return (T)selectComprehender(currentExpansionUnwrapped).flatMap(currentExpansionUnwrapped,it ->{				 	
						PMap newMap  =context.plus(lastExpansionName,it);
						return process((ContextualExecutor)yieldExecutor, newMap, head.getFunction().executeAndSetContext( newMap), head.getName(),index+1);
				 });
			
			}
			
		}
	}

	private Comprehender selectComprehender(Object structure) {
		return comprehenders.entrySet().stream()
				.filter(e -> e.getKey().isAssignableFrom(structure.getClass()))
				.findFirst()
				.orElse(new AbstractMap.SimpleEntry(null, new ReflectionComprehender()))
				.getValue();
	}
}
