package com.aol.cyclops.closures.mutable;

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
 * String var = "hello";
 * Runnable r = () -> var ="world";
 * }</pre>
 * 
 * Won't compile because var is treated as if it is final.
 * This can be 'worked around' by using a wrapping object or array.
 * 
 * e.g.
 * <pre>{@code
 * Mutable<String> var =  Mutable.of("hello");
 * Runnable r = () -> var.set("world");
 * }</pre>
 * 
 * @author johnmcclean
 *
 * @param <T> Type held inside closed var
 */
@AllArgsConstructor
@NoArgsConstructor
@ToString @EqualsAndHashCode
public class Mutable<T> implements Supplier<T>,Consumer<T>, Convertable<T>{

	private T var;
	
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
	 * Construct a Mutable that gets and sets an external value using the provided Supplier and Consumer
	 * 
	 * e.g.
	 * <pre>
	 * {@code 
	 *    Mutable<Integer> mutable = Mutable.from(()->this.value*2,val->this.value=val);
	 * }
	 * </pre>
	 * 
	 * 
	 * @param s Supplier of an external value
	 * @param c Consumer that sets an external value
	 * @return Mutable that gets / sets an external (mutable) value
	 */
	public static <T> Mutable<T> fromExternal(Supplier<T> s, Consumer<T> c){
		return new Mutable<T>(){
			public T get(){
				return s.get();
			}
			public Mutable<T> set(T value){
					c.accept(value);
					return this;
			}
		};
	}
	
	public <R> Mutable<R> mapOutput(Function<T,R> fn){
		Mutable<T> host = this;
		return new Mutable<R>(){
			public R get(){
				return fn.apply(host.get());
			}
			
		};
	}
	public <T1> Mutable<T1> mapInput(Function<T1,T> fn){
		Mutable<T> host = this;
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
		return set(varFn.apply(get()));
	}
	@Override
	public void accept(T t) {
		set(t);
		
	}

	
	
}
