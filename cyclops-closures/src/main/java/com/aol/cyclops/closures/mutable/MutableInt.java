package com.aol.cyclops.closures.mutable;

import java.util.OptionalInt;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;
import java.util.function.IntUnaryOperator;
import java.util.stream.IntStream;

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
public class MutableInt implements IntSupplier, IntConsumer, Convertable<Integer>{

	private int var;
	
	/**
	 * Create a Mutable variable, which can be mutated inside a Closure 
	 * 
	 * e.g.
	 * <pre>{@code
	 *   MutableInt num = MutableInt.of(20);
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
	public static  MutableInt of(int var){
		return new MutableInt(var);
	}
	
	/** 
	 * Construct a MutableInt that gets and sets an external value using the provided Supplier and Consumer
	 * 
	 * e.g.
	 * <pre>
	 * {@code 
	 *    MutableInt mutable = MutableInt.fromExternal(()->!this.value,val->!this.value);
	 * }
	 * </pre>
	 * 
	 * 
	 * @param s Supplier of an external value
	 * @param c Consumer that sets an external value
	 * @return MutableInt that gets / sets an external (mutable) value
	 */
	public static  MutableInt fromExternal(IntSupplier s, IntConsumer c){
		return new MutableInt(){
			public int getAsInt(){
				return s.getAsInt();
			}
			public Integer get(){
				return getAsInt();
			}
			public MutableInt set(int value){
					c.accept(value);
					return this;
			}
		};
	}
	
	/**
	 * Use the supplied function to perform a lazy map operation when get is called 
	 * <pre>
	 * {@code 
	 *  MutableInt mutable = MutableInt.fromExternal(()->!this.value,val->!this.value);
	 *  Mutable<Int> withOverride = mutable.mapOutputToObj(b->{ 
	 *                                                        if(override)
	 *                                                             return 10.0;
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
	public <R> Mutable<R> mapOutputToObj(Function<Integer,R> fn){
		MutableInt host = this;
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
	 *  MutableInt mutable = MutableInt.fromExternal(()->!this.value,val->!this.value);
	 *  Mutable<Int> withOverride = mutable.mapInputToObj(b->{ 
	 *                                                        if(override)
	 *                                                             return 10;
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
	public <T1> Mutable<T1> mapInputToObj(Function<T1,Integer> fn){
		MutableInt host = this;
		return new Mutable<T1>(){
			public Mutable<T1> set(T1 value){
				host.set(fn.apply(value));
				return this;
		}
			
		};
	}
	/**
	 * Use the supplied function to perform a lazy map operation when get is called 
	 * <pre>
	 * {@code 
	 *  MutableInt mutable = MutableInt.fromExternal(()->!this.value,val->!this.value);
	 *  MutableInt withOverride = mutable.mapOutput(b->{ 
	 *                                                        if(override)
	 *                                                             return 10;
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
	public  MutableInt mapOutput(IntUnaryOperator fn){
		MutableInt host = this;
		return new MutableInt(){
			public int getAsInt(){
				return fn.applyAsInt(host.getAsInt());
			}
			
		};
	}
	/**
	 * Use the supplied function to perform a lazy map operation when get is called 
	 * <pre>
	 * {@code 
	 *  MutableInt mutable = MutableInt.fromExternal(()->!this.value,val->!this.value);
	 *  MutableInt withOverride = mutable.mapInput(b->{ 
	 *                                                        if(override)
	 *                                                             return 10;
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
	public MutableInt mapInput(IntUnaryOperator fn){
		MutableInt host = this;
		return new MutableInt(){
			public MutableInt set(int value){
				host.set(fn.applyAsInt(value));
				return this;
		}
			
		};
	}
	/**
	 * @return Current value
	 */
	public int getAsInt(){
		return var;
	}
	
	/**
	 * @param var New value
	 * @return  this object with mutated value
	 */
	public MutableInt set(int var){
		this.var = var;
		return this;
	}
	/**
	 * @param varFn New value
	 * @return  this object with mutated value
	 */
	public MutableInt mutate(IntFunction<Integer> varFn){
		this.var = varFn.apply(this.var);
		return this;
	}
	public OptionalInt toOptionalInt(){
		return OptionalInt.of(var);
	}
	
	public IntStream toIntStream(){
		return IntStream.of(var);
	}
	@Override
	public Integer get() {
		return getAsInt();
	}
	@Override
	public void accept(int value) {
		set(value);
		
	}
}
