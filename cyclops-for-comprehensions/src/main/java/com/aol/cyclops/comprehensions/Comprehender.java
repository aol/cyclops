package com.aol.cyclops.comprehensions;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Interface for defining how Comprehensions should work for a type
 * Cyclops For Comprehensions will supply either a JDK 8 Predicate or Function
 * for filter / map / flatMap
 * The comprehender should wrap these in a suitable type and make the call to the
 * underlying Monadic Type (T) the Comprehender implementation supports.
 * 
 * E.g. To support mapping for the Functional Java Option type wrap the supplied JDK 8 Function in a Functional Java
 * fj.F type, call the make call to option.map( ) and retun the result.
 * 
 * <pre>
 *  OptionComprehender&lt;Option&gt; {
 *    
 *     public Object map(Option o, Function fn){
 *        return o.map( a-&gt; fn.apply(a));
 *     }
 *     
 * }
 * </pre>
 * 
 * @author johnmcclean
 *
 * @param <T> Monadic Type being wrapped
 */
public interface Comprehender<T> {

	/**
	 * Wrapper around filter
	 * 
	 * @param t Monadic type being wrapped
	 * @param p JDK Predicate to wrap
	 * @return Result of call to t.filter ( i -> p.test(i));
	 */
	public Object filter(T t, Predicate p);
	
	/**
	 * Wrapper around map
	 * 
	 * @param t Monadic type being wrapped
	 * @param fn JDK Function to wrap
	 * @return Result of call to t.map( i -> fn.apply(i));
	 */
	public Object map(T t, Function fn);
	
	/**
	 * Wrapper around flatMap
	 * 
	 * @param t Monadic type being wrapped
	 * @param fn JDK Function to wrap
	 * @return Result of call to t.flatMap( i -> fn.apply(i));
	 */
	public T flatMap(T t, Function fn);
	
}
