package com.aol.cyclops.monad;

import static fj.data.Either.left;
import static fj.data.Either.right;

import java.util.function.Function;

import lombok.Value;

import com.aol.cyclops.lambda.monads.Functor;
import com.aol.cyclops.matcher.Matchable;

import fj.data.Either;


/**
 * experimental attempt at a Free Monad in Java
 * 
 * Inspired by http://www.slideshare.net/kenbot/running-free-with-the-monads
 * 
 * Uses generic Functor interface
 * Builds an abstract tree of computations
 * 
 * @author johnmcclean
 *
 * @param <F> Functor type
 * @param <A> Return type
 */
public interface Free<F extends Functor<?>,A> extends Matchable {

	/**
	 * @return Unwraps the last Functor
	 */
	public A unwrap();
	
	
	/**
	 * 
	 * @param fn
	 * @return
	 */
	default <B> Free<F,B> map(Function<A,B> fn){
		
		return flatMap(x ->  ret(fn.apply(x)));
	}
	
	public <B> Free<F,B> flatMap(Function<A,Free<F,B>> fn);
	
	default <T1> Either<Free<F, A>, A> resume(Functor<T1> f){
		if(this instanceof GoSub){
			return ((GoSub)this).handleGoSub(f);
		}
		return this.match( newCase->
			 newCase.isType( (Return r) -> right(r.result) )
			.newCase().isType( (Suspend s) -> left(s.next) )		
				);
		
	}
	
	


	public static <A,F extends Functor<?>> Free<F,A> liftF(A f){
		return (Free)Free.suspend(new FunctorWrapper((Free.ret(f))));
	}
	/**
	 * Create a suspended execution state
	 * 
	 * @param suspend Functor to mapped when Suspended state is resumed
	 * @return Suspended state
	 */
	public static <A,F extends Functor<?>> Suspend<A,F> suspend(Functor<Free<F,A>> suspend){
		return new Suspend<>(suspend);
	}
	/**
	 * Create a completed execution state with some value
	 * 
	 * @param ret Value
	 * @return Completed execution state
	 */
	public static <A,F extends Functor<?>> Return<A,F> ret(A ret){
		return new Return<>(ret);
	}
	@Value
	static class Return<A,F extends Functor<?>> implements Free<F,A> {
		
		A result;

		public A unwrap(){
			return result;
		}
		public <B> Free<F,B> map(Function<A,B> fn){
			
			return flatMap(x -> ret(fn.apply(x)));
		}
		
	      public <B> Free<F,B> flatMap(Function<A,Free<F,B>> fn) {
	    	  return new GoSub(this,fn);
	 

	      }
	    
	}

	@Value
	static class Suspend<A,F extends Functor<?>> implements Free<F,A> {
		Functor<Free<F,A>> next;
	    
	    public A unwrap(){
	    	Object o = next.unwrap();
	    	return (A)o;
	    }
	    
	    public <B> Free<F,B> map(Function<A,B> fn){
			return flatMap(x ->  ret(fn.apply(x)));
		}
	  
	     public <B> Free<F,B> flatMap(Function<A,Free<F,B>> fn) {
	    	return new GoSub(this,fn);
	    	
	      }
   
	}
	@Value
	static class GoSub<A,F extends Functor<?>,B>  implements Free<F,B>{
		Free<F,A> free;
		Function<A,Free<F,B>> next;
		@Override
		public B unwrap() {
			return null;
		}
		@Override
		public <B1> Free<F, B1> flatMap(Function<B, Free<F, B1>> newFn) {
			return new GoSub<>(free,a-> new GoSub<>(next.apply(a),newFn));
		}
		public  Either<Free<F, A>, A> handleGoSub(Functor<Free> f){
			return free.match(  newCase ->
					newCase.isType((Return<A,F> r) -> next.apply(r.result).resume(f))
					.newCase().isType( (Suspend<A,F> s) -> left((f.map(o -> 
							o.flatMap(next)))))
					.newCase().isType( (GoSub<A,F,B> y) ->(Free)y.free.flatMap(o ->
			           y.next.apply(o).flatMap((Function)this.next)).resume(f))
			
				);
							
		}
							
	}
	 
}
