package com.aol.cyclops.internal.comprehensions;

import java.util.function.Function;

import com.aol.cyclops.internal.comprehensions.ComprehensionsModule.ComprehensionData;
import com.aol.cyclops.internal.comprehensions.ComprehensionsModule.Initialisable;
import com.aol.cyclops.internal.comprehensions.ComprehensionsModule.MyComprehension;

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
	public static <R> R foreachX(Function<ComprehensionData<?,R,? extends Initialisable>,R> fn){
		return (R)new FreeFormForComprehension().foreach((Function)fn);
	}
	
	/**
	 * Create a for comprehension using a custom interface 
	 * 
	 * <pre>
	 * {@code
	 * 	List<Integer> list= Arrays.asList(1,2,3);
	 	Stream<Integer> stream = foreachX(Custom.class,  
									c-> c.myVar(list)
										.yield(()->c.myVar()+3)
									);
		
		assertThat(Arrays.asList(4,5,6),equalTo(stream.collect(Collectors.toList())));
		
		static interface Custom extends CustomForComprehension<Stream<Integer>,Custom>{
			Integer myVar();
			Custom myVar(List<Integer> value);
		 }
	 * }</pre>
	 * 
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
	 * <pre>
	 * {@code
	 *  MyComprehension<Custom2,Custom2> comp2 = ForComprehensions.custom(Custom2.class);
	 *   comp.foreach(c -> c.i(Arrays.asList(20,30))
								.j(Arrays.asList(1,2,3))
								.yield(() -> c.i() +c.j()));
	 * }
	 * 
	 * </pre>
	 * 
	 * @param c Interface that defines for comprehension - should extend CustomForComprehension
	 * @return next stage in the step builder
	 */
	public static <X,V> MyComprehension<X,V> custom(Class<X> c){
		
	 return   new MyComprehension<>(c,null);
	}
	
	
	
	
}
