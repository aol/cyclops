package com.aol.cyclops.comprehensions;

import static com.aol.cyclops.comprehensions.ForComprehensions.foreachX;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import lombok.Getter;


/**
 * Class that collects data for free form for comprehensions
 * 
 * @author johnmcclean
 *
 * @param <T> Variable type
 * @param <R> Return type
 * @param <V> Aggregate Variable type holder
 */
public class ComprehensionData<T,R,V extends Initialisable>{
	private final BaseComprehensionData data;
	@Getter
	private final V vars;
	
	private final Proxier proxier = new Proxier();
	
	ComprehensionData(ExecutionState state,Optional<Class<V>> varsClass) {
		super();
		data = new BaseComprehensionData(state);
	
		this.vars = (V)new Varsonly();
		this.vars.init(data);
	}
	
	ComprehensionData(V vars,ExecutionState state) {
		super();
		data = new BaseComprehensionData(state);
		
		this.vars = (V)((Initialisable)vars);
		this.vars.init(data);
		
	}
	
	
	/**
	 * Add a guard to the for comprehension
	 * 
	 * <pre>{@code
	 *  	foreachX(c -> c.$("hello",list)
						   .filter(()->c.<Integer>$("hello")<10)
							.yield(()-> c.<Integer>$("hello")+2));
		  }</pre>
		  
	 * @param s Supplier that returns true for elements that should stay in the
	 * 					comprehension
	 * @return this
	 */
	public ComprehensionData<T,R,V> filter(Supplier<Boolean> s){
		data.guardInternal(s);
		return this;
		
	}
	
	ComprehensionData<T,R,V> filterFunction(Function<V,Boolean> s){
		data.guardInternal(()->s.apply(vars));
		return this;
		
	}
	/**
	 * Define the yeild section of a for comprehension and kick of processing
	 *  for a comprehension
	 *  
	 *  <pre>{@code
	 *  	foreachX(c -> c.$("hello",list)
						   .filter(()->c.<Integer>$("hello")<10)
							.yield(()-> c.<Integer>$("hello")+2));
		  }</pre>
	 *  
	 * @param s Yield section
	 * @return result of for comprehension
	 */
	public <R> R yield(Supplier s){
		return data.yieldInternal(s);
		
	}
	<R> R yieldFunction(Function<V,?> s){
		return data.yieldInternal(()->s.apply(vars));
		
	}
	
	/**
	 * Extract a bound variable
	 * <pre>{@code
	 *  	foreachX(c -> c.$("hello",list)
						   .filter(()->c.<Integer>$("hello")<10)
							.yield(()-> c.<Integer>$("hello")+2));
		  }</pre>
		  
	 * 
	 * @param name Variable name
	 * @return variable value
	 */
	public <T> T $(String name){
		return data.$Internal(name);
	
	}
	
	
	
	/**
	 * Bind a variable in this for comprehension
	 * 
	 * <pre>{@code
	 *  	foreachX(c -> c.$("hello",list)
						   .filter(()->c.<Integer>$("hello")<10)
							.yield(()-> c.<Integer>$("hello")+2));
		  }</pre>
	 * 
	 * @param name of variable to bind
	 * @param f value
	 * @return this
	 */
	public  <T> ComprehensionData<T,R,V> $(String name,Object f){
		data.$Internal(name, f);
		
		return (ComprehensionData)this;
	}
	/**
	 * Lazily bind a variable in this for comprehension
	 * 
	 * <pre>{@code
	 *  	foreachX(c -> c.$("hello",list)
						   .filter(()->c.<Integer>$("hello")<10)
							.yield(()-> c.<Integer>$("hello")+2));
		  }</pre>
	 * 
	 * @param name name of variable to bind
	 * @param f value
	 * @return this
	 */
	public  <T> ComprehensionData<T,R,V> $(String name,Supplier f){
		data.$Internal(name, f);
		
		return (ComprehensionData)this;
	}
	
	
	
	
}
