package com.aol.cyclops.closures.mutable;

import java.util.OptionalDouble;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleFunction;
import java.util.function.DoubleSupplier;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;
import java.util.stream.DoubleStream;

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
public class MutableDouble implements DoubleSupplier, DoubleConsumer,Value<Double>{

	private double var;
	
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
	public static <T> MutableDouble of(double var){
		return new MutableDouble(var);
	}
	/** 
	 * Construct a MutableDouble that gets and sets an external value using the provided Supplier and Consumer
	 * 
	 * e.g.
	 * <pre>
	 * {@code 
	 *    MutableDouble mutable = MutableDouble.fromExternal(()->!this.value,val->!this.value);
	 * }
	 * </pre>
	 * 
	 * 
	 * @param s Supplier of an external value
	 * @param c Consumer that sets an external value
	 * @return MutableDouble that gets / sets an external (mutable) value
	 */
	public static  MutableDouble fromExternal(DoubleSupplier s, DoubleConsumer c){
		return new MutableDouble(){
			public double getAsDouble(){
				return s.getAsDouble();
			}
			public Double get(){
				return getAsDouble();
			}
			public MutableDouble set(double value){
					c.accept(value);
					return this;
			}
		};
	}
	
	/**
	 * Use the supplied function to perform a lazy map operation when get is called 
	 * <pre>
	 * {@code 
	 *  MutableDouble mutable = MutableDouble.fromExternal(()->!this.value,val->!this.value);
	 *  Mutable<Double> withOverride = mutable.mapOutputToObj(b->{ 
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
	public <R> Mutable<R> mapOutputToObj(Function<Double,R> fn){
		MutableDouble host = this;
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
	 *  MutableDouble mutable = MutableDouble.fromExternal(()->!this.value,val->!this.value);
	 *  Mutable<Double> withOverride = mutable.mapInputToObj(b->{ 
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
	public <T1> Mutable<T1> mapInputToObj(Function<T1,Double> fn){
		MutableDouble host = this;
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
	 *  MutableDouble mutable = MutableDouble.fromExternal(()->!this.value,val->!this.value);
	 *  MutableDouble withOverride = mutable.mapOutput(b->{ 
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
	public  MutableDouble mapOutput(DoubleUnaryOperator fn){
		MutableDouble host = this;
		return new MutableDouble(){
			public double getAsDouble(){
				return fn.applyAsDouble(host.getAsDouble());
			}
			
		};
	}
	/**
	 * Use the supplied function to perform a lazy map operation when get is called 
	 * <pre>
	 * {@code 
	 *  MutableDouble mutable = MutableDouble.fromExternal(()->!this.value,val->!this.value);
	 *  MutableDouble withOverride = mutable.mapInput(b->{ 
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
	public MutableDouble mapInput(DoubleUnaryOperator fn){
		MutableDouble host = this;
		return new MutableDouble(){
			public MutableDouble set(double value){
				host.set(fn.applyAsDouble(value));
				return this;
		}
			
		};
	}
	/**
	 * @return Current value
	 */
	public double getAsDouble(){
		return var;
	}
	
	/**
	 * @param var New value
	 * @return  this object with mutated value
	 */
	public MutableDouble set(double var){
		this.var = var;
		return this;
	}
	/**
	 * @param varFn New value
	 * @return  this object with mutated value
	 */
	public MutableDouble mutate(DoubleFunction<Double> varFn){
		return set(varFn.apply(get()));
		
	}
	public OptionalDouble toOptionalDouble(){
		return OptionalDouble.of(var);
	}
	
	public DoubleStream toDoubleStream(){
		return DoubleStream.of(var);
	}
	@Override
	public Double get() {
		return getAsDouble();
	}
	@Override
	public void accept(double value) {
		set(value);
		
	}
	
}
