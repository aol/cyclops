package com.aol.cyclops.comprehensions;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import lombok.AllArgsConstructor;

import org.pcollections.PMap;

import com.aol.cyclops.comprehensions.comprehenders.Comprehenders;
import com.aol.cyclops.comprehensions.comprehenders.InvokeDynamicComprehender;
import com.aol.cyclops.comprehensions.comprehenders.MaterializedList;
import com.aol.cyclops.comprehensions.converters.MonadicConverters;
import com.aol.cyclops.lambda.api.Comprehender;
import com.aol.cyclops.lambda.api.MonadicConverter;

@AllArgsConstructor
class Yield<T> {
	
	
	private final  List<Expansion> expansions;
	private final MonadicConverters converters = new MonadicConverters();
	
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	T process(ContextualExecutor<?,Map> yieldExecutor, PMap<String,Object> context, 
						Object currentExpansionUnwrapped, String lastExpansionName, int index) {
		
		Tuple2<Comprehender,Object> comprehender = selectComprehender(currentExpansionUnwrapped)
									.orElseGet( ()->selectComprehender(converters.convertToMonadicForm(currentExpansionUnwrapped))
													.orElse( new Tuple2(new InvokeDynamicComprehender(Optional.ofNullable(currentExpansionUnwrapped).map(Object::getClass)),currentExpansionUnwrapped)));
			
		
		if (expansions.size() == index) {
			
			return (T)comprehender._1.map( comprehender._2,it->yieldExecutor.executeAndSetContext(context.plus(lastExpansionName,it)));
		
		} else {
			Expansion head = expansions.get(index);
			
			if (head instanceof Filter) {
				
				Object s = comprehender._1.filter(comprehender._2,it->   (boolean)head.getFunction().executeAndSetContext(context.plus(lastExpansionName,it)));
				return process(yieldExecutor, context, s, lastExpansionName,index+1);
			} else {
				
				T result =  (T)comprehender._1.executeflatMap(comprehender._2,it ->{				 	
						PMap newMap  =context.plus(lastExpansionName,it);
						return process((ContextualExecutor)yieldExecutor, newMap, head.getFunction().executeAndSetContext( newMap), head.getName(),index+1);
				 });
				 return  (T)comprehender._1.map(result,this::takeFirst);
			
			}
			
		}
	}
	private <T> T takeFirst(Object o){
		if(o instanceof MaterializedList){
			return (T)((List)o).get(0);
		}
		return (T)o;
	}

	@AllArgsConstructor
	static class Tuple2<T1,T2>{
		final T1 _1;
		final T2 _2;
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Optional<Tuple2<Comprehender,Object>> selectComprehender(Object structure) {
		if(structure==null)
			return Optional.empty();
		return new Comprehenders().getRegisteredComprehenders().stream()
				.filter(e -> e.getKey().isAssignableFrom(structure.getClass()))
				.map(e->e.getValue())
				.map(v->new Tuple2<Comprehender,Object>(v,structure))
				.findFirst();
	}
	
}
