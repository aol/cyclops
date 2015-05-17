package com.aol.cyclops.lambda.utils;

import java.util.function.Supplier;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Class that represents a Closed Variable
 * In Java 8 because of the effectively final rule references to captured
 * variables can't be changed.
 * e.g.
 * <pre>
 * String var = "hello";
 * Runnable r = () -> var ="world";
 * </pre>
 * 
 * Won't compile because var is treated as if it is final.
 * This can be 'worked around' by using a wrapping object or array.
 * 
 * e.g.
 * <pre>
 * ClosedVar&lt;String&gt; var = new ClosedVar("hello");
 * Runnable r = () -> var.set("world");
 * </pre>
 * 
 * @author johnmcclean
 *
 * @param <T> Type held inside closed var
 */
@AllArgsConstructor
@NoArgsConstructor
@ToString @EqualsAndHashCode
public class Mutable<T> implements Supplier<T>{

	private volatile T var;
	
	public static <T> Mutable<T> of(T var){
		return new Mutable<T>(var);
	}
	/**
	 * @return Current value
	 */
	public T get(){
		return var;
	}
	
	/**
	 * @param var New value
	 * @return  this object with mutated value
	 */
	public Mutable<T> set(T var){
		this.var = var;
		return this;
	}
	
}
