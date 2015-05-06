package com.aol.cyclops.comprehensions;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import lombok.AllArgsConstructor;

import org.pcollections.PMap;

import com.aol.cyclops.comprehensions.comprehenders.CompletableFutureComprehender;
import com.aol.cyclops.comprehensions.comprehenders.DoubleStreamComprehender;
import com.aol.cyclops.comprehensions.comprehenders.IntStreamComprehender;
import com.aol.cyclops.comprehensions.comprehenders.LongStreamComprehender;
import com.aol.cyclops.comprehensions.comprehenders.OptionalComprehender;
import com.aol.cyclops.comprehensions.comprehenders.ReflectionComprehender;
import com.aol.cyclops.comprehensions.comprehenders.StreamComprehender;
import com.aol.cyclops.lambda.api.Comprehender;

@AllArgsConstructor
class Yield<T> {
	
	@SuppressWarnings({ "unchecked", "rawtypes", "serial" })
	private static final Map<Class,Comprehender> comprehenders = new HashMap<Class,Comprehender>(){{
		put(Optional.class,new OptionalComprehender());
		put(Stream.class,new StreamComprehender());
		put(IntStream.class,new IntStreamComprehender());
		put(LongStream.class,new LongStreamComprehender());
		put(DoubleStream.class,new DoubleStreamComprehender());
		put(CompletableFuture.class, new CompletableFutureComprehender());
		try{
			Class cases = Class.forName("com.aol.cyclops.matcher.Cases");
			put(cases,(Comprehender)Class.forName("com.aol.cyclops.matcher.comprehenders.CasesComprehender").newInstance());
		}catch(Exception e){
			
		}
		try{
			Class caze = Class.forName("com.aol.cyclops.matcher.Case");
			put(caze,(Comprehender)Class.forName("com.aol.cyclops.matcher.comprehenders.CaseComprehender").newInstance());
		}catch(Exception e){
			
		}
		try{
			Class caze = Class.forName("com.aol.cyclops.enableswitch.Switch");
			put(caze,(Comprehender)Class.forName("com.aol.cyclops.enableswitch.SwitchComprehender").newInstance());
		}catch(Exception e){
			
		}
	}};

	private final  List<Expansion> expansions;
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	T process(ContextualExecutor<?,Map> yieldExecutor, PMap<String,Object> context, 
						Object currentExpansionUnwrapped, String lastExpansionName, int index) {
		
		Tuple2<Comprehender,Object> comprehender = selectComprehender(currentExpansionUnwrapped)
									.orElseGet( ()->selectComprehender(convertToMonadicForm(currentExpansionUnwrapped))
													.orElse( new Tuple2(new ReflectionComprehender(Optional.of(currentExpansionUnwrapped).map(Object::getClass)),currentExpansionUnwrapped)));
			
		
		if (expansions.size() == index) {
			
			return (T)comprehender._1.map( comprehender._2,it->yieldExecutor.executeAndSetContext(context.plus(lastExpansionName,it)));
		
		} else {
			Expansion head = expansions.get(index);
			
			if (head instanceof Filter) {
				
				Object s = comprehender._1.filter(comprehender._2,it->   (boolean)head.getFunction().executeAndSetContext(context.plus(lastExpansionName,it)));
				return process(yieldExecutor, context, s, lastExpansionName,index+1);
			} else {
				
				return (T)comprehender._1.executeflatMap(comprehender._2,it ->{				 	
						PMap newMap  =context.plus(lastExpansionName,it);
						return process((ContextualExecutor)yieldExecutor, newMap, head.getFunction().executeAndSetContext( newMap), head.getName(),index+1);
				 });
			
			}
			
		}
	}

	@AllArgsConstructor
	static class Tuple2<T1,T2>{
		final T1 _1;
		final T2 _2;
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Optional<Tuple2<Comprehender,Object>> selectComprehender(Object structure) {
		return comprehenders.entrySet().stream()
				.filter(e -> e.getKey().isAssignableFrom(structure.getClass()))
				.map(e->e.getValue())
				.map(v->new Tuple2<Comprehender,Object>(v,structure))
				.findFirst();
	}
	@SuppressWarnings("rawtypes")
	private Object convertToMonadicForm(Object f) {
			
			if(f instanceof Collection)
				return ((Collection)f).stream();
			if(f instanceof Map)
				return ((Map)f).entrySet().stream();
			
			return f;
		}
}
