package com.aol.cyclops.lambda.monads.transformers;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Predicate;

import lombok.Getter;

import org.jooq.lambda.function.Function1;

import com.aol.cyclops.monad.AnyM;


public class CompletableFutureT<A> {
   @Getter
   final AnyM<CompletableFuture<A>> run;

   public CompletableFutureT(final AnyM<CompletableFuture<A>> run){
       this.run = run;
   }
   public CompletableFutureT<A> peek(Consumer<A> peek){
       return of(run.peek(future-> future.thenApply(a->{peek.accept(a); return a;})));
   }
   
   
   public <B> CompletableFutureT<B> map(Function1<A,B> f){
       return new CompletableFutureT<B>(run.map(o-> o.thenApply(f)));
   }
   public <B> CompletableFutureT<B> flatMap(Function1<A,CompletableFutureT<B>> f){
	   return of(run.map(future-> future.thenCompose(a-> f.apply(a).run.unwrap())));
   }
   
   public static <A> CompletableFutureT<A> fromAnyM(AnyM<A> anyM){
	   return of(anyM.map(CompletableFuture::completedFuture));
   }
   
   public static <A> CompletableFutureT<A> of(AnyM<CompletableFuture<A>> monads){
	   return new CompletableFutureT<>(monads);
   }
   
   
   
 
}
