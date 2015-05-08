package com.aol.cyclops.comprehensions;

import java.util.function.Supplier;

/**
 * Extend this interface to implement custom For Comprehensions
 * 
 * Format is
 * 
 * to bind a Monad or colleciton use methods in the following format
 * 
 * varName(Monad m);
 * 
 * to access the current value in a filter or yield expression
 * 
 * varName();
 *
 * e.g. 
 * 
 * <pre>
 * static interface Custom extends CustomForComprehension&lt;Stream&lt;Integer&gt;,Custom&gt;{
 *		Integer myVar();
 *		Custom myVar(List&lt;Integer&gt; value);
 *	}
 *</pre>
 *
 * <pre>
 * Stream&lt;Integer&gt; stream = ForComprehensions.foreachX(Custom.class,  
 *									c-&gt; c.myVar(list)
 *										.yield(()-&gt;c.myVar()+3)
 *									);
 * 
 * 
 * </pre>
 * 
 * 
 * 
 * @author johnmcclean
 *
 * @param <R> Return type of the end result
 * @param <T> The type of the Custom For Comprehension (the interface that extends
 *  this one).
 */
public interface CustomForComprehension<R,T extends CustomForComprehension<R,?>>{
	
	public T filter(Supplier<Boolean> s);
	
	public R yield(Supplier s);
	public void run(Runnable r);
}
