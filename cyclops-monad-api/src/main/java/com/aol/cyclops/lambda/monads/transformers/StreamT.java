package com.aol.cyclops.lambda.monads.transformers;


import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.jooq.lambda.function.Function1;

import com.aol.cyclops.monad.AnyM;



public class StreamT<A> {
  
   final AnyM<Stream<A>> run;

   private StreamT(final AnyM<Stream<A>> run){
       this.run = run;
   }
   public AnyM<Stream<A>> unwrap(){
	   return run;
   }
   public StreamT<A> peek(Consumer<A> peek){
	   return map(a-> {peek.accept(a); return a;});
   }
   public StreamT<A> filter(Predicate<A> test){
       return of(run.map(stream-> stream.filter(test)));
   }
   public <B> StreamT<B> map(Function<A,B> f){
       return new StreamT<B>(run.map(o-> o.map(f)));
   }
   public <B> StreamT<B> flatMap(Function<A,StreamT<B>> f){
	   return of(run.map(stream-> stream.flatMap(a-> f.apply(a).run.asSequence())
			   							.<B>flatMap(a->a)));
   }
   
   
   public static <U, R> Function<StreamT<U>, StreamT<R>> lift(Function<U, R> fn) {
		return optTu -> optTu.map(input -> fn.apply(input));
	}

   public static <A> StreamT<A> fromAnyM(AnyM<A> anyM){
	   return of(anyM.map(Stream::of));
   }
   
   public static <A> StreamT<A> of(AnyM<Stream<A>> monads){
	   return new StreamT<>(monads);
   }
   
   
   
 
}