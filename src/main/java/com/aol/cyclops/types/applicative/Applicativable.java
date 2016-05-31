package com.aol.cyclops.types.applicative;

import java.util.function.BiFunction;
import java.util.function.Function;

import com.aol.cyclops.Semigroup;
import com.aol.cyclops.Semigroups;
import com.aol.cyclops.control.Xor;
import com.aol.cyclops.types.ConvertableFunctor;
import com.aol.cyclops.types.Functor;
import com.aol.cyclops.types.Unit;
import com.aol.cyclops.types.Value;
import com.aol.cyclops.util.function.QuadFunction;
import com.aol.cyclops.util.function.QuintFunction;
import com.aol.cyclops.util.function.TriFunction;

import lombok.AllArgsConstructor;
import lombok.experimental.Wither;

/**
 * @author johnmcclean
 *
 * Interface for applicative-like behavior. Allows the application of functions within a wrapped context, support both 
 * abscence / error short-circuiting and error accumulation
 * 
 * <pre> {@code ap(BiFunction<T,T,T>)}</pre> and <pre>{@code ap(Semigroup<T>}</pre> for accumulation despite absence
 * use ap1..5 for absence short-circuiting
 *
 * @param <T>
 */
public interface Applicativable<T> extends ConvertableFunctor<T>, Unit<T>{

	
	public static class Applicatives{
		public static <T,R> ApplyingApplicativeBuilder<T,R,Applicativable<R>> applicatives(Unit unit,Functor functor){
			return new ApplyingApplicativeBuilder<T,R,Applicativable<R>> (unit,functor);
		}
	}
	
	

    @AllArgsConstructor
    public static class SemigroupApplyer<T> {
        BiFunction<T, T, T> combiner;
        @Wither
        ConvertableFunctor<T> functor;

        public SemigroupApplyer<T> ap(ConvertableFunctor<T> fn) {
            
            return functor.visit(p-> fn.visit(p2-> withFunctor(functor.map(v1 -> combiner.apply(v1, fn.get()))),
                                                     ()-> this),
                                    ()->withFunctor(fn));
           
        }

        public Value<T> convertable() {
            return functor;
        }
    }
	    
	    
	    /**
	     * Apply the provided function to combine multiple different Applicatives, wrapping the same type.
	     * 
	  
	     * We can combine Applicative types together without unwrapping the values.
	     * 
	     * <pre>
	     * {@code
	     *   Xor<String,String> fail1 = Xor.secondary("failed1");
            
            fail1.swap().ap(Semigroups.stringConcat)
                        .ap(Xor.secondary("failed2").swap())
                        .ap(Xor.<String,String>primary("success").swap())
                                    .
                                    
            // [failed1failed2]
	     *  }
	     *  </pre>
	     * 
	     * @param fn
	     * @return
	     */
	    default  SemigroupApplyer<T> ap(BiFunction<T,T,T> fn){
	        return  new SemigroupApplyer<T>(fn, this);
	    }
	    default  SemigroupApplyer<T> ap(Semigroup<T> fn){
	        return  new SemigroupApplyer<>(fn.combiner(), this);
	    }
	    
	    default <R> Applicativable<R> ap1(Function<? super T,? extends R> fn){
	        return Applicatives.<T,R>applicatives(this,this).applicative(fn).ap(this);
	        
	    }    
	    
	/**
	 * Apply the provided function to two different Applicatives. e.g. given a method add
	 * 
	 * <pre>
	 * {@code 
	 * 	public int add(Integer a,Integer b){
	 * 			return a+b;
	 * 	}
	 * 
	 * }
	 * </pre>
	 * We can add two Applicative types together without unwrapping the values
	 * 
	 * <pre>
	 * {@code 
	 *  Maybe.of(10).ap2(this::add).ap(Maybe.of(20))
	 *  
	 *  //Maybe[30];
	 *  }
	 *  </pre>
	 * 
	 * @param fn
	 * @return
	 */
	default <T2,R> Applicative<T2,R, ?> ap2( BiFunction<? super T,? super T2,? extends R> fn){
		return  Applicatives.<T,R>applicatives(this,this).applicative2(fn);
	}
	default <T2,T3,R> Applicative2<T2,T3,R, ?> ap3( TriFunction<? super T,? super T2,? super T3,? extends R> fn){
		return  Applicatives.<T,R>applicatives(this,this).applicative3(fn);
	}
	default <T2,T3,T4,R> Applicative3<T2,T3,T4,R, ?> ap4( QuadFunction<? super T,? super T2,? super T3,? super T4,? extends R> fn){
		return  Applicatives.<T,R>applicatives(this,this).applicative4(fn);
	}
	default <T2,T3,T4,T5,R> Applicative4<T2,T3,T4,T5,R, ?> ap5( QuintFunction<? super T,? super T2,? super T3,? super T4,? super T5,? extends R> fn){
		return  Applicatives.<T,R>applicatives(this,this).applicative5(fn);
	}
	
	
}
