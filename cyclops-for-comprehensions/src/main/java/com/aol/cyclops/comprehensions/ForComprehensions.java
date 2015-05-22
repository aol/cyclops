package com.aol.cyclops.comprehensions;

import java.util.function.Function;

import com.aol.cyclops.comprehensions.comprehenders.Comprehenders;
import com.aol.cyclops.comprehensions.converters.MonadicConverters;

/**
 * Static helper methods for for comprehensions
 * This class aims to make using for comprehenions as succint as possible
 * 
 * @author johnmcclean
 *
 */
public class ForComprehensions {

	public static <X,V extends Initialisable<V>> FreeFormForComprehension<X,V>  buildExecutor(State state,Class<X> interfaceStep1,Class<V> variables){
		return new FreeFormForComprehension<>(state,interfaceStep1,variables,null);
	}
	public static FreeFormForComprehension<?,?>  buildExecutor(MonadicConverters converters, Comprehenders comprehenders,StreamUpscaler converter){
		return new FreeFormForComprehension<>(new State(comprehenders,converters,converter));
	}
	public static FreeFormForComprehension<?,?>  buildExecutor(State state){
		return new FreeFormForComprehension<>(state);
	}
	/**
	 * Create  for comprehension over a single Monad or collection
	 * 
	 * @param fn for comprehension
	 * @return Result
	 */
	public static <R> R foreach1(Function<LessTypingForComprehension1.Step1<?,R>,R> fn){
		return (R)new FreeFormForComprehension(LessTypingForComprehension1.Step1.class,LessTypingForComprehension1.getVars()).foreach((Function)fn);
		
	}
	
	/**
	 * Create  for comprehension over two Monads or collections
	 * 
	 * @param fn  for comprehension
	 * @return Result
	 */
	public static <R> R foreach2(Function<LessTypingForComprehension2.Step1<?,R>,R> fn){
		return (R)new FreeFormForComprehension(LessTypingForComprehension2.Step1.class,LessTypingForComprehension2.getVars()).foreach((Function)fn);
	}
	/**
	 * Create  for comprehension over three Monads or collections
	 * 
	 * @param fn for comprehension
	 * @return Result
	 */
	public static <R> R foreach3(Function<LessTypingForComprehension3.Step1<?,R>,R> fn){
		return (R)new FreeFormForComprehension(LessTypingForComprehension3.Step1.class,LessTypingForComprehension3.getVars()).foreach((Function)fn);
	}
	
	/**
	 * Create  for comprehension over four Monads or collections
	 * 
	 * @param fn for comprehension
	 * @return Result
	 */
	public static <R> R foreach4(Function<LessTypingForComprehension4.Step1<?,R>,R> fn){
		return (R)new FreeFormForComprehension(LessTypingForComprehension4.Step1.class,LessTypingForComprehension4.getVars()).foreach((Function)fn);
	}
	
	/**
	 * Create a custom for comprehension virtually unlimited in nesting depths
	 * 
	 * @param fn For comprehension
	 * @return Result
	 */
	public static <R> R foreachX(Function<ComprehensionData<?,R,? extends Initialisable>,R> fn){
		return (R)new FreeFormForComprehension().foreach((Function)fn);
	}
	
	/**
	 * Create a for comprehension using a custom interface 
	 * 
	 * @param c Interface that defines for comprehension - should extend CustomForComprehension
	 * @param fn for comprehension
	 * @return Result
	 */
	public static <X,R> R foreachX(Class<X> c,Function<X,R> fn){
		return (R)new FreeFormForComprehension(c,(Class)null).foreach(fn);
	}
	/**
	 * Step builder for Creating a for comprehension using a custom interface
	 * 
	 * @param c Interface that defines for comprehension - should extend CustomForComprehension
	 * @return next stage in the step builder
	 */
	public static <X,V> MyComprehension<X,V> custom(Class<X> c){
		
	 return   new MyComprehension<>(c,null);
	}
	
	
	
	
}
