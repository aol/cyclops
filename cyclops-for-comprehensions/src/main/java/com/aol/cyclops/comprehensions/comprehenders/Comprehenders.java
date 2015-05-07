package com.aol.cyclops.comprehensions.comprehenders;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import com.aol.cyclops.lambda.api.Comprehender;

/**
 * Registered Comprehenders
 * 
 * @author johnmcclean
 *
 */
public class Comprehenders {
	
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
		try{
			Class caze = Class.forName("com.aol.cyclops.trycatch.Try");
			put(caze,(Comprehender)Class.forName("com.aol.cyclops.trycatch.TryComprehender").newInstance());
		}catch(Exception e){
			
		}
		
	}};
	
	/**
	 * Careful - global mutable state, with the possiblity of changing behaviour for existing comprehenders
	 * 
	 * @param c Class to add 
	 * @param comp Comprehender for class
	 */
	public static void addComprehender(Class c, Comprehender comp){
		comprehenders.put(c,comp);
	}

	/**
	 * @return Registered Comprehenders
	 */
	public static Map<Class,Comprehender> getRegisteredComprehenders(){
		return new HashMap(comprehenders);
	}
	
}
