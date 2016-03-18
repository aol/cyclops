package com.aol.cyclops.internal.comprehensions;

import java.util.function.Function;

import com.aol.cyclops.internal.comprehensions.ComprehensionsModule.ComprehensionData;
import com.aol.cyclops.internal.comprehensions.ComprehensionsModule.Initialisable;

/**
 * Static helper methods for for comprehensions
 * This class aims to make using for comprehenions as succint as possible
 * 
 * @author johnmcclean
 *
 */
public class ForComprehensions {

	
	
	/**
	 * Create a custom for comprehension virtually unlimited in nesting depths
	 * 
	 * <pre>
	 * {@code 
	 * List<Integer> list= Arrays.asList(1,2,3);
		Stream<Integer> stream = foreachX(c -> c.$("hello",list)
										.filter(()->c.<Integer>$("hello")<10)
										.yield(()-> c.<Integer>$("hello")+2));
		
		assertThat(Arrays.asList(3,4,5),equalTo(stream.collect(Collectors.toList())));
	 * }
	 * </pre>
	 * 
	 * @param fn For comprehension
	 * @return Result
	 */
	public static <R> R foreachX(Function<ComprehensionData<?,R>,R> fn){
		return (R)new FreeFormForComprehension().foreach((Function)fn);
	}
	
	
	
	
	
}
