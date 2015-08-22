package com.aol.simple.react.stream.traits.operators;

import java.util.function.Function;
import java.util.function.Supplier;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;



@AllArgsConstructor
@NoArgsConstructor
@ToString @EqualsAndHashCode
class Mutable<T> implements Supplier<T>{

	private volatile T var;
	
	/**
	 * Create a Mutable variable, which can be mutated inside a Closure 
	 * 
	 * e.g.
	 * <pre>{@code
	 *   Mutable<Integer> num = Mutable.of(20);
	 *   
	 *   Stream.of(1,2,3,4).map(i->i*10).peek(i-> num.mutate(n->n+i)).foreach(System.out::println);
	 *   
	 *   System.out.println(num.get());
	 *   //prints 120
	 * } </pre>
	 * 
	 * @param var Initial value of Mutable
	 * @return New Mutable instance
	 */
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
	/**
	 * @param varFn New value
	 * @return  this object with mutated value
	 */
	public Mutable<T> mutate(Function<T,T> varFn){
		this.var = varFn.apply(this.var);
		return this;
	}
	
}

