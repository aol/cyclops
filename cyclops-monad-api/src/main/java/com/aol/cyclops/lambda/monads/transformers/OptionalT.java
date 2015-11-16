package com.aol.cyclops.lambda.monads.transformers;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

import lombok.Getter;

import org.jooq.lambda.function.Function1;

import com.aol.cyclops.monad.AnyM;


public class OptionalT<A> {
   @Getter
   final AnyM<Optional<A>> run;

   public OptionalT(final AnyM<Optional<A>> run){
       this.run = run;
   }
   
   public OptionalT<A> peek(Consumer<A> peek){
       return of(run.peek(opt-> opt.map(a-> { peek.accept(a); return a;})));
   }
   
   public OptionalT<A> filter(Predicate<A> test){
       return of(run.map(opt-> opt.filter(test)));
   }

   public <B> OptionalT<B> map(Function1<A,B> f){
       return new OptionalT<B>(run.map(o-> o.map(f)));
   }
   public <B> OptionalT<B> flatMap(Function1<A,OptionalT<B>> f){
	  
	  return  of( run.flatMap(opt->{
		   if(opt.isPresent())
			   return f.apply(opt.get()).run;
		   return run.unit(Optional.<B>empty());
	   }));
	   
	   
   }
   
   public static <A> OptionalT<A> fromAnyM(AnyM<A> anyM){
	   return of(anyM.map(Optional::ofNullable));
   }
   
   public static <A> OptionalT<A> of(AnyM<Optional<A>> monads){
	   return new OptionalT<>(monads);
   }
   
   public String toString(){
	   return run.toString();
   }
   
 
}
