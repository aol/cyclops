package com.aol.cyclops.types.applicative;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.jooq.lambda.tuple.Tuple;
import org.reactivestreams.Publisher;

import com.aol.cyclops.Semigroup;
import com.aol.cyclops.control.For;
import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.types.ConvertableFunctor;
import com.aol.cyclops.types.Functor;
import com.aol.cyclops.types.Unit;
import com.aol.cyclops.types.Value;
import com.aol.cyclops.util.function.Curry;
import com.aol.cyclops.util.function.QuadFunction;
import com.aol.cyclops.util.function.QuintFunction;
import com.aol.cyclops.util.function.TriFunction;

import lombok.AccessLevel;
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
public interface ApplicativeFunctor<T> extends ConvertableFunctor<T>, 
                                               Unit<T>{

	
	public static class Applicatives{
		public static <T,R> ApplyingApplicativeBuilder<T,R,ApplicativeFunctor<R>> applicatives(Unit unit,Functor functor){
			return new ApplyingApplicativeBuilder<T,R,ApplicativeFunctor<R>> (unit,functor);
		}
	}
	
	    
	default <T2,R> ApplicativeFunctor<R> ap(Value<? extends T2> app, BiFunction<? super T,? super T2,? extends R> fn){
	       
	        return (ApplicativeFunctor<R>)map(v->Tuple.tuple(v,Curry.curry2(fn).apply(v)))
	                                  .map(tuple-> app.visit(i->tuple.v2.apply(i),()->tuple.v1 ));
	} 
	default <T2,R> ApplicativeFunctor<R> zip(Iterable<? extends T2> app,BiFunction<? super T,? super T2,? extends R> fn){
	           
            return (ApplicativeFunctor<R>)map(v->Tuple.tuple(v,Curry.curry2(fn).apply(v)))
                                      .map(tuple-> Maybe.fromIterable(app).visit(i->tuple.v2.apply(i),()->tuple.v1 ));
   } 
   default <T2,R> ApplicativeFunctor<R> zip(BiFunction<? super T,? super T2,? extends R> fn,Publisher<? extends T2> app){
            
            return (ApplicativeFunctor<R>)map(v->Tuple.tuple(v,Curry.curry2(fn).apply(v)))
                                      .map(tuple-> Maybe.fromPublisher(app).visit(i->tuple.v2.apply(i),()->tuple.v1 ));
   } 
	    
	    default ApplyFunctions<T> applyFunctions(){
	        return new ApplyFunctions<T>(this);
	    }
	   
	    @AllArgsConstructor(access=AccessLevel.PRIVATE)
	   static class ApplyFunctions<T>{
	       ApplicativeFunctor<T> app;
        	public <R> ApplicativeFunctor<R> ap1(Function<? super T,? extends R> fn){
                    return Applicatives.<T,R>applicatives(app,app).applicative(fn).ap(app);
                    
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
        	public <T2,R> Applicative<T2,R, ?> ap2( BiFunction<? super T,? super T2,? extends R> fn){
        		return  Applicatives.<T,R>applicatives(app,app).applicative2(fn);
        	}
        	public <T2,T3,R> Applicative2<T2,T3,R, ?> ap3( TriFunction<? super T,? super T2,? super T3,? extends R> fn){
        		return  Applicatives.<T,R>applicatives(app,app).applicative3(fn);
        	}
        	public <T2,T3,T4,R> Applicative3<T2,T3,T4,R, ?> ap4( QuadFunction<? super T,? super T2,? super T3,? super T4,? extends R> fn){
        		return  Applicatives.<T,R>applicatives(app,app).applicative4(fn);
        	}
        	public <T2,T3,T4,T5,R> Applicative4<T2,T3,T4,T5,R, ?> ap5( QuintFunction<? super T,? super T2,? super T3,? super T4,? super T5,? extends R> fn){
        		return  Applicatives.<T,R>applicatives(app,app).applicative5(fn);
        	}
	   }
	
}
