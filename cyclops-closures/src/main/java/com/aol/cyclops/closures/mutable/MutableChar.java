package com.aol.cyclops.closures.mutable;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

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
 * char var = true;
 * Runnable r = () -> var =false;
 * }</pre>
 * 
 * Won't compile because var is treated as if it is final.
 * This can be 'worked around' by using a wrapping object or array.
 * 
 * e.g.
 * <pre>{@code
 * MutableChar var =  MutableChar.of(true);
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
public class MutableChar implements Supplier<Character>, Consumer<Character>,Convertable<Character>{

	private char var;
	
	/**
	 * Create a Mutable variable, which can be mutated inside a Closure 
	 * 
	 * e.g.
	 * <pre>{@code
	 *   MutableChar char = MutableChar.of('c');
	 *   
	 *    char.mutate(n->'d'))
	 *   
	 *   System.out.println(num.getAsChar());
	 *   //prints d
	 * } </pre>
	 * 
	 * @param var Initial value of Mutable
	 * @return New Mutable instance
	 */
	public static <T> MutableChar of(char var){
		return new MutableChar(var);
	}
	
	
	
	/**
	 * Use the supplied function to perform a lazy map operation when get is called 
	 * <pre>
	 * {@code 
	 *  MutableBoolean mutable = MutableBoolean.fromExternal(()->!this.value,val->!this.value);
	 *  Mutable<Boolean> withOverride = mutable.mapOutput(b->{ 
	 *                                                        if(override)
	 *                                                             return true;
	 *                                                         return b;
	 *                                                         });
	 *          
	 * }
	 * </pre>
	 * 
	 * 
	 * @param fn Map function to be applied to the result when get is called
	 * @return Mutable that lazily applies the provided function when get is called to the return value
	 */
	public <R> Mutable<R> mapOutputToObj(Function<Character,R> fn){
		MutableChar host = this;
		return new Mutable<R>(){
			public R get(){
				return fn.apply(host.get());
			}
			
		};
	}
	/**
	 * Use the supplied function to perform a lazy map operation when get is called 
	 * <pre>
	 * {@code 
	 *  MutableBoolean mutable = MutableBoolean.fromExternal(()->!this.value,val->!this.value);
	 *  Mutable<Boolean> withOverride = mutable.mapInput(b->{ 
	 *                                                        if(override)
	 *                                                             return true;
	 *                                                         return b;
	 *                                                         });
	 *          
	 * }
	 * </pre>
	 * 
	 * 
	 * @param fn Map function to be applied to the input when set is called
	 * @return Mutable that lazily applies the provided function when set is called to the input value
	 */
	public <T1> Mutable<T1> mapInputToObj(Function<T1,Character> fn){
		MutableChar host = this;
		return new Mutable<T1>(){
			public Mutable<T1> set(T1 value){
				host.set(fn.apply(value));
				return this;
		}
			
		};
	}
	/**
	 * @return Current value
	 */
	public char getAsChar(){
		return var;
	}
	
	/**
	 * @param var New value
	 * @return  this object with mutated value
	 */
	public MutableChar set(char var){
		this.var = var;
		return this;
	}
	/**
	 * @param varFn New value
	 * @return  this object with mutated value
	 */
	public MutableChar mutate(CharFunction varFn){
		return set( varFn.apply(get()));
		
	}
	public static interface CharFunction{
		char apply(char var);
	}
	@Override
	public void accept(Character t) {
		set(t);
		
	}
	@Override
	public Character get() {
		return getAsChar();
	}
	
}
