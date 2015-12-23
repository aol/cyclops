package com.aol.cyclops.lambda.monads.transformers;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import lombok.Getter;

import org.jooq.lambda.function.Function1;

import com.aol.cyclops.monad.AnyM;


public class CompletableFutureT<A> {
   
   final AnyM<CompletableFuture<A>> run;

   public AnyM<CompletableFuture<A>> unwrap(){
	   return run;
   }
   private CompletableFutureT(final AnyM<CompletableFuture<A>> run){
       this.run = run;
   }
   public CompletableFutureT<A> peek(Consumer<A> peek){
	  
       return of(run.peek(future-> future.thenApply(a->{peek.accept(a); return a;})));
   }
   
   
   public <B> CompletableFutureT<B> map(Function<A,B> f){
       return new CompletableFutureT<B>(run.map(o-> o.thenApply(f)));
   }
   public <B> CompletableFutureT<B> flatMap(Function1<A,CompletableFutureT<B>> f){
	   return of(run.map(future-> future.thenCompose(a-> f.apply(a).run.asSequence().toList().get(0))));
   }
   
   public static <U, R> Function<CompletableFutureT<U>, CompletableFutureT<R>> lift(Function<U, R> fn) {
		return optTu -> optTu.map(input -> fn.apply(input));
	}

	public static <U1, U2, R> BiFunction<CompletableFutureT<U1>, CompletableFutureT<U2>, CompletableFutureT<R>> lift2(BiFunction<U1, U2, R> fn) {
		return (optTu1, optTu2) -> optTu1.flatMap(input1 -> optTu2.map(input2 -> fn.apply(input1, input2)));
	}

   public static <A> CompletableFutureT<A> fromAnyM(AnyM<A> anyM){
	   return of(anyM.map(CompletableFuture::completedFuture));
   }
   
   public static <A> CompletableFutureT<A> of(AnyM<CompletableFuture<A>> monads){
	   return new CompletableFutureT<>(monads);
   }
   
   
   
 
}