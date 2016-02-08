package com.aol.cyclops.closures.mutable;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.aol.cyclops.value.Value;

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
 * float var = true;
 * Runnable r = () -> var =false;
 * }</pre>
 * 
 * Won't compile because var is treated as if it is final.
 * This can be 'worked around' by using a wrapping object or array.
 * 
 * e.g.
 * <pre>{@code
 * MutableFloat var =  MutableFloat.of(true);
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
public class MutableFloat implements Supplier<Float>, Consumer<Float>,Value<Float>{

	private float var;
	
	/**
	 * Create a Mutable variable, which can be mutated inside a Closure 
	 * 
	 * e.g.
	 * <pre>{@code
	 *   MutableFloat num = MutableFloat.of(true);
	 *   
	 *    num.mutate(n->!n))
	 *   
	 *   System.out.println(num.getAsFloat());
	 *   //prints false
	 * } </pre>
	 * 
	 * @param var Initial value of Mutable
	 * @return New Mutable instance
	 */
	public static <T> MutableFloat of(float var){
		return new MutableFloat(var);
	}
	/** 
	 * Construct a MutableFloat that gets and sets an external value using the provided Supplier and Consumer
	 * 
	 * e.g.
	 * <pre>
	 * {@code 
	 *    MutableFloat mutable = MutableFloat.fromExternal(()->!this.value,val->!this.value);
	 * }
	 * </pre>
	 * 
	 * 
	 * @param s Supplier of an external value
	 * @param c Consumer that sets an external value
	 * @return MutableFloat that gets / sets an external (mutable) value
	 */
	public static  MutableFloat fromExternal(Supplier<Float> s, Consumer<Float> c){
		return new MutableFloat(){
			public float getAsFloat(){
				return s.get();
			}
			public Float get(){
				return getAsFloat();
			}
			public MutableFloat set(float value){
					c.accept(value);
					return this;
			}
		};
	}
	
	
	/**
	 * Use the supplied function to perform a lazy map operation when get is called 
	 * <pre>
	 * {@code 
	 *  MutableFloat mutable = MutableFlaot.fromExternal(()->!this.value,val->!this.value);
	 *  Mutable<Float> withOverride = mutable.mapOutputToObj(b->{ 
	 *                                                        if(override)
	 *                                                             return 1f;
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
	public <R> Mutable<R> mapOutputToObj(Function<Float,R> fn){
		MutableFloat host = this;
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
	 *  MutableFloat mutable = MutableBoolean.fromExternal(()->!this.value,val->!this.value);
	 *  Mutable<Float> withOverride = mutable.mapInputToObj(b->{ 
	 *                                                        if(override)
	 *                                                             return 2f;
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
	public <T1> Mutable<T1> mapInputToObj(Function<T1,Float> fn){
		MutableFloat host = this;
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
	public float getAsFloat(){
		return var;
	}
	
	/**
	 * @param var New value
	 * @return  this object with mutated value
	 */
	public MutableFloat set(float var){
		this.var = var;
		return this;
	}
	/**
	 * @param varFn New value
	 * @return  this object with mutated value
	 */
	public MutableFloat mutate(FloatFunction varFn){
		this.var = varFn.apply(this.var);
		return this;
	}
	public static interface FloatFunction{
		float apply(float var);
	}
	@Override
	public Float get() {
		return var;
	}
	@Override
	public void accept(Float t) {
		set(t);
		
	}
	
}
