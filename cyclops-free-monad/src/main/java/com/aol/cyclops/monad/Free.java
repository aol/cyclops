package com.aol.cyclops.monad;

import java.util.function.Function;
import java.util.stream.Stream;

import lombok.Value;
import lombok.val;

import com.aol.cyclops.lambda.monads.Functor;
import com.aol.cyclops.lambda.monads.LiftableFunctor;

//experimental attempt at Free Monad
public interface Free<F extends Functor<?>,A> {

	public A result();
	
	
	default <B> Free<F,B> map(Function<A,B> fn){
		
		return flatMap(x ->  ret(fn.apply(x)));
	}
	
	public <B> Free<F,B> flatMap(Function<A,Free<F,B>> fn);
	
	public static <A,X extends LiftableFunctor<A,Free<?,A>,?>> Free<Functor<?>,A> liftF(LiftableFunctor<A,Free,X> f){
		return (Free)Free.suspend((Functor<Free<Functor, A>>) f.of(Free.ret(f)));
	}
	public static <A,F extends Functor<?>> Suspend<A,F> suspend(Functor<Free<F,A>> suspend){
		return new Suspend(suspend);
	}
	public static <A> Return ret(A ret){
		return new Return(ret);
	}
	@Value
	static class Return<A,F extends Functor<?>> implements Free<F,A> {
		
		A a;

		public A result(){
			return a;
		}
		public <B> Free<F,B> map(Function<A,B> fn){
			
			return flatMap(x -> ret(fn.apply(x)));
		}
		
	      public <B> Free<F,B> flatMap(Function<A,Free<F,B>> fn) {
	    	  val res = fn.apply(a);
	    	  return res;

	      }
	    
	}

	@Value
	static class Suspend<A,F extends Functor<?>> implements Free<F,A> {
		Functor<Free<F,A>> next;
	    
	    public A result(){
	    	Object o = next.unwrap();
	    	return (A)o;
	    }
	    
	    public <B> Free<F,B> map(Function<A,B> fn){
			return flatMap(x ->  ret(fn.apply(x)));
		}
	  
	     public <B> Free<F,B> flatMap(Function<A,Free<F,B>> fn) {
	    	
	    	 Free<F,B> res=  suspend(next.map( free -> free.flatMap(fn) ));	
	    	 
	    	 return res;

	      }
   
	}
	 
}
