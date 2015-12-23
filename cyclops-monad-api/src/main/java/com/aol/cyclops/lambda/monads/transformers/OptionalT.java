package com.aol.cyclops.lambda.monads.transformers;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import lombok.Getter;

import org.jooq.lambda.function.Function1;

import com.aol.cyclops.monad.AnyM;


/**
 * Monad transformer for JDK Optional
 * 
 * @author johnmcclean
 *
 * @param <A>
 */
public class OptionalT<A> {
   
   private final AnyM<Optional<A>> run;
   
   
   private OptionalT(final AnyM<Optional<A>> run){
       this.run = run;
   }
   
   public AnyM<Optional<A>> unwrap(){
	   return run;
   }

   
   public OptionalT<A> peek(Consumer<A> peek){
       return of(run.peek(opt-> opt.map(a-> { peek.accept(a); return a;})));
   }
   
   public OptionalT<A> filter(Predicate<A> test){
       return of(run.map(opt-> opt.filter(test)));
   }

   public <B> OptionalT<B> map(Function<A,B> f){
       return new OptionalT<B>(run.map(o-> o.map(f)));
   }
   public <B> OptionalT<B> flatMap(Function1<A,OptionalT<B>> f){
	  
	  return  of( run.flatMap(opt->{
		   if(opt.isPresent())
			   return f.apply(opt.get()).run;
		   return run.unit(Optional.<B>empty());
	   }));
	   
	   
   }

	public static <U, R> Function<OptionalT<U>, OptionalT<R>> lift(Function<U, R> fn) {
		return optTu -> optTu.map(input -> fn.apply(input));
	}

	public static <U1, U2, R> BiFunction<OptionalT<U1>, OptionalT<U2>, OptionalT<R>> lift2(BiFunction<U1, U2, R> fn) {
		return (optTu1, optTu2) -> optTu1.flatMap(input1 -> optTu2.map(input2 -> fn.apply(input1, input2)));
	}

	public static <A> OptionalT<A> fromAnyM(AnyM<A> anyM) {
		return of(anyM.map(Optional::ofNullable));
	}
   
   public static <A> OptionalT<A> of(AnyM<Optional<A>> monads){
	   return new OptionalT<>(monads);
   }
   
   public String toString(){
	   return run.toString();
   }
   
 
}
