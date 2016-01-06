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
 * byte var = true;
 * Runnable r = () -> var =false;
 * }</pre>
 * 
 * Won't compile because var is treated as if it is final.
 * This can be 'worked around' by using a wrapping object or array.
 * 
 * e.g.
 * <pre>{@code
 * MutableByte var =  MutableByte.of(true);
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
public class MutableByte implements Supplier<Byte>, Consumer<Byte>,Convertable<Byte>{

	private byte var;
	
	/**
	 * Create a Mutable variable, which can be mutated inside a Closure 
	 * 
	 * e.g.
	 * <pre>{@code
	 *   MutableByte num = MutableByte.of(true);
	 *   
	 *    num.mutate(n->!n))
	 *   
	 *   System.out.println(num.getAsByte());
	 *   //prints false
	 * } </pre>
	 * 
	 * @param var Initial value of Mutable
	 * @return New Mutable instance
	 */
	public static <T> MutableByte of(byte var){
		return new MutableByte(var);
	}
	
	/** 
	 * Construct a MutableByte that gets and sets an external value using the provided Supplier and Consumer
	 * 
	 * e.g.
	 * <pre>
	 * {@code 
	 *    MutableByte mutable = MutableByte.fromExternal(()->!this.value,val->!this.value);
	 * }
	 * </pre>
	 * 
	 * 
	 * @param s Supplier of an external value
	 * @param c Consumer that sets an external value
	 * @return MutableByte that gets / sets an external (mutable) value
	 */
	public static  MutableByte fromExternal(Supplier<Byte> s, Consumer<Byte> c){
		return new MutableByte(){
			public byte getAsByte(){
				return s.get();
			}
			public Byte get(){
				return getAsByte();
			}
			public MutableByte set(byte value){
					c.accept(value);
					return this;
			}
		};
	}
	
	/**
	 * Use the supplied function to perform a lazy map operation when get is called 
	 * <pre>
	 * {@code 
	 *  MutableByte mutable = MutableByte.fromExternal(()->!this.value,val->!this.value);
	 *  Mutable<Byte> withOverride = mutable.mapOutputToObj(b->{ 
	 *                                                        if(override)
	 *                                                             return 1b;
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
	public <R> Mutable<R> mapOutputToObj(Function<Byte,R> fn){
		MutableByte host = this;
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
	 *  MutableByte mutable = MutablByte.fromExternal(()->!this.value,val->!this.value);
	 *  Mutable<Byte> withOverride = mutable.mapInputToObj(b->{ 
	 *                                                        if(override)
	 *                                                             return 1b;
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
	public <T1> Mutable<T1> mapInputToObj(Function<T1,Byte> fn){
		MutableByte host = this;
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
	public byte getAsByte(){
		return var;
	}
	
	/**
	 * @param var New value
	 * @return  this object with mutated value
	 */
	public MutableByte set(byte var){
		this.var = var;
		return this;
	}
	/**
	 * @param varFn New value
	 * @return  this object with mutated value
	 */
	public MutableByte mutate(ByteFunction varFn){
		return set( varFn.apply(this.getAsByte()));
		
	}
	public static interface ByteFunction{
		byte apply(byte var);
	}
	@Override
	public void accept(Byte t) {
		set(t);
		
	}
	@Override
	public Byte get() {
		return getAsByte();
	}
	
}
