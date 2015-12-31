package com.aol.cyclops.closures.mutable;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import com.aol.cyclops.closures.Convertable;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Class that represents a Closed Variable
 * In Java 8 because of the effectively final rule references to captured
 * variables can't be changed.
 * e.g.
 *<pre>{@code 
 * boolean var = true;
 * Runnable r = () -> var =false;
 * }</pre>
 * 
 * Won't compile because var is treated as if it is final.
 * This can be 'worked around' by using a wrapping object or array.
 * 
 * e.g.
 * <pre>{@code
 * MutableBoolean var =  MutableBoolean.of(true);
 * Runnable r = () -> var.set(false);
 * }</pre>
 * 
 * @author johnmcclean
 *
 * @param <T> Type held inside closed var
 */
@AllArgsConstructor
@NoArgsConstructor
@ToString @EqualsAndHashCode
public class MutableBoolean implements BooleanSupplier, Consumer<Boolean>, Convertable<Boolean>{

	private boolean var;
	
	/**
	 * Create a Mutable variable, which can be mutated inside a Closure 
	 * 
	 * e.g.
	 * <pre>{@code
	 *   MutableBoolean num = MutableBoolean.of(true);
	 *   
	 *    num.mutate(n->!n))
	 *   
	 *   System.out.println(num.getAsBoolean());
	 *   //prints false
	 * } </pre>
	 * 
	 * @param var Initial value of Mutable
	 * @return New Mutable instance
	 */
	public static <T> MutableBoolean of(boolean var){
		return new MutableBoolean(var);
	}
	/**
	 * @return Current value
	 */
	public boolean getAsBoolean(){
		return var;
	}
	
	/**
	 * @param var New value
	 * @return  this object with mutated value
	 */
	public MutableBoolean set(boolean var){
		this.var = var;
		return this;
	}
	/**
	 * @param varFn New value
	 * @return  this object with mutated value
	 */
	public MutableBoolean mutate(BooleanFunction varFn){
		this.var = varFn.apply(this.var);
		return this;
	}
	public static interface BooleanFunction{
		boolean apply(boolean var);
	}
	@Override
	public void accept(Boolean t) {
		set(t);
		
	}
	@Override
	public Boolean get() {
		return getAsBoolean();
	}
	
}
