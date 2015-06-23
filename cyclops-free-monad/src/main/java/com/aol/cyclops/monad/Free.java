package com.aol.cyclops.monad;

import static com.aol.cyclops.trampoline.Trampoline.done;
import static fj.data.Either.left;
import static fj.data.Either.right;

import java.util.function.Function;
import java.util.stream.Stream;

import lombok.Value;

import com.aol.cyclops.lambda.monads.Functor;
import com.aol.cyclops.matcher.Matchable;
import com.aol.cyclops.matcher.builders.CheckType;
import com.aol.cyclops.trampoline.Trampoline;

import fj.data.Either;


/**
 * experimental attempt at a Free Monad in Java
 * 
 * Inspired by http://www.slideshare.net/kenbot/running-free-with-the-monads
 * and https://github.com/xuwei-k/free-monad-java/blob/master/src/main/java/free/Free.java
 * and of course https://github.com/scalaz/scalaz/blob/series/7.2.x/core/src/main/scala/scalaz/Free.scala
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
	
	<T1> Trampoline<Either<Functor<Free<F,A>>, A>> resume(Functor<T1> f);
	
	


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
		
		public <T1> Trampoline<Either<Functor<Free<F,A>>, A>> resume(Functor<T1> f){
			return done(right(result));
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
	     public <T1> Trampoline<Either<Functor<Free<F,A>>, A>> resume(Functor<T1> f){
				return done(left(next));
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
		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		public <T1> Trampoline<Either<Functor<Free<F, B>>, B>> resume(
				Functor<T1> f) {
			
			
			Either<Either<Functor<Free<F,A>>, A>,Free> res= free.matchType(whenReturnSuspendOrGoSub(f));
		
	
			return res.isLeft() ? (Trampoline)done(res.left().value()) : Trampoline.more(()->res.right().value().resume(f));
		}
		
		@SuppressWarnings({ "rawtypes", "unchecked" })
		private <T1> Function<CheckType<? super Either>,CheckType<? super Either>> whenReturnSuspendOrGoSub(Functor<T1> f){
			
			return  c ->  c.isType((Return<A,F> r) -> right(next.apply(r.result)))
							.isType( (Suspend<A,F> s) -> left((f.map(o -> ((Free) o).flatMap(next)))))
							.isType( (GoSub<A,F,B> y) -> right(y.free.flatMap(o -> y.next.apply(o).flatMap((Function)this.next))));

		}
		
							
	}
	 
}
