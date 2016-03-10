package com.aol.cyclops.data;

import java.util.OptionalLong;
import java.util.function.Function;
import java.util.function.LongConsumer;
import java.util.function.LongFunction;
import java.util.function.LongSupplier;
import java.util.function.LongUnaryOperator;
import java.util.stream.LongStream;

import com.aol.cyclops.control.Matchable;
import com.aol.cyclops.types.Value;

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
public class MutableLong implements LongSupplier, LongConsumer,Matchable.ValueAndOptionalMatcher<Long>{

	private long var;
	
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
	public static  MutableLong of(long var){
		return new MutableLong(var);
	}
	
	/** 
	 * Construct a MutableLong that gets and sets an external value using the provided Supplier and Consumer
	 * 
	 * e.g.
	 * <pre>
	 * {@code 
	 *    MutableLong mutable = MutableLong.fromExternal(()->!this.value,val->!this.value);
	 * }
	 * </pre>
	 * 
	 * 
	 * @param s Supplier of an external value
	 * @param c Consumer that sets an external value
	 * @return MutableLong that gets / sets an external (mutable) value
	 */
	public static  MutableLong fromExternal(LongSupplier s, LongConsumer c){
		return new MutableLong(){
			public long getAsLong(){
				return s.getAsLong();
			}
			public Long get(){
				return getAsLong();
			}
			public MutableLong set(long value){
					c.accept(value);
					return this;
			}
		};
	}
	
	/**
	 * Use the supplied function to perform a lazy map operation when get is called 
	 * <pre>
	 * {@code 
	 *  MutableLong mutable = MutableLong.fromExternal(()->!this.value,val->!this.value);
	 *  Mutable<Long> withOverride = mutable.mapOutputToObj(b->{ 
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
	public <R> Mutable<R> mapOutputToObj(Function<Long,R> fn){
		MutableLong host = this;
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
	 *  MutableLong mutable = MutableLong.fromExternal(()->!this.value,val->!this.value);
	 *  Mutable<Long> withOverride = mutable.mapInputToObj(b->{ 
	 *                                                        if(override)
	 *                                                             return 10.0;
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
	public <T1> Mutable<T1> mapInputToObj(Function<T1,Long> fn){
		MutableLong host = this;
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
	 *  MutableLong mutable = MutableLong.fromExternal(()->!this.value,val->!this.value);
	 *  MutableLong withOverride = mutable.mapOutput(b->{ 
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
	public  MutableLong mapOutput(LongUnaryOperator fn){
		MutableLong host = this;
		return new MutableLong(){
			public long getAsLong(){
				return fn.applyAsLong(host.getAsLong());
			}
			
		};
	}
	/**
	 * Use the supplied function to perform a lazy map operation when get is called 
	 * <pre>
	 * {@code 
	 *  MutableLong mutable = MutableLong.fromExternal(()->!this.value,val->!this.value);
	 *  MutableLong withOverride = mutable.mapInput(b->{ 
	 *                                                        if(override)
	 *                                                             return 10.0;
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
	public MutableLong mapInput(LongUnaryOperator fn){
		MutableLong host = this;
		return new MutableLong(){
			public MutableLong set(long value){
				host.set(fn.applyAsLong(value));
				return this;
		}
			
		};
	}
	
	/**
	 * @return Current value
	 */
	public long getAsLong(){
		return var;
	}
	
	/**
	 * @param var New value
	 * @return  this object with mutated value
	 */
	public MutableLong set(long var){
		this.var = var;
		return this;
	}
	/**
	 * @param varFn New value
	 * @return  this object with mutated value
	 */
	public MutableLong mutate(LongFunction<Long> varFn){
		this.var = varFn.apply(this.var);
		return this;
	}
	public OptionalLong toOptionalLong(){
		return OptionalLong.of(var);
	}
	
	public LongStream toLongStream(){
		return LongStream.of(var);
	}
	@Override
	public Long get() {
		return getAsLong();
	}
	@Override
	public void accept(long value) {
		set(value);
		
	}
}
