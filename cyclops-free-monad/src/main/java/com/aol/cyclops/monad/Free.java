package com.aol.cyclops.monad;

import java.util.function.Function;

import lombok.Value;

import com.aol.cyclops.lambda.monads.Functor;
import com.aol.cyclops.lambda.monads.LiftableFunctor;
//experimental attempt at Free Monad
public interface Free<F extends Functor<?>,A> {

	default <B> Free<F,B> map(Function<A,B> fn){
		return flatMap(x -> {  System.out.println(x); return ret(fn.apply(x));});
	}
	
	public <B> Free<F,B> flatMap(Function<A,Free<F,B>> fn);
	/**
	public static <A,X extends LiftableFunctor<A,Free<?,A>,?>> Free<Functor<?>,A> liftF(LiftableFunctor<A,Free,X> f){
		return Free.suspend((Functor<Free<Functor, A>>) f.of(Free.ret(f)));
	}**/
	public static <A,F extends Functor<?>> Suspend<A,F> suspend(Functor<Free<F,A>> suspend){
		return new Suspend(suspend);
	}
	public static <A> Return ret(A ret){
		return new Return(ret);
	}
	@Value
	static class Return<A,F extends Functor<?>> implements Free<F,A> {
		
		A a;

		
		
	      public <B> Free<F,B> flatMap(Function<A,Free<F,B>> fn) {

	    	  return fn.apply(a);

	      }
	    
	}

	@Value
	static class Suspend<A,F extends Functor<?>> implements Free<F,A> {


	    Functor<Free<F,A>> next;
	    
	   
	    
	     public <B> Free<F,B> flatMap(Function<A,Free<F,B>> fn) {
	    //	 return new Gosub<>(this,fn);
	    	return  suspend(next.map( free -> free.flatMap(fn) ));	

	      }
	}
	 @Value
	 static final class Gosub<F extends Functor, A, B>  implements Free<F, B>{
		    private final Free<F, A> a;
		    private final Function<A, Free<F, B>> f;
		    /**
			@Override
			public  Free<Functor, B> flatMap(Function<B, Free<Functor, B>> fn) {
				// TODO Auto-generated method stub
				return null;
			}**/

		    
		    @Override
		    public <C> Free<F, C> flatMap(final Function<B, Free<F, C>> g) {
		    	return new Gosub<>(a, aa -> new Gosub<>(f.apply(aa), g));
		    }
		  }

}
