package com.aol.cyclops.lambda.monads.transformers;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import lombok.Getter;

import org.jooq.lambda.function.Function1;

import com.aol.cyclops.monad.AnyM;


public class StreamT<A> {
   @Getter
   final AnyM<Stream<A>> run;

   public StreamT(final AnyM<Stream<A>> run){
       this.run = run;
   }
   public StreamT<A> peek(Consumer<A> peek){
       return of(run.peek(stream-> stream.peek(peek)));
   }
   public StreamT<A> filter(Predicate<A> test){
       return of(run.map(stream-> stream.filter(test)));
   }
   public <B> StreamT<B> map(Function1<A,B> f){
       return new StreamT<B>(run.map(o-> o.map(f)));
   }
   public <B> StreamT<B> flatMap(Function1<A,StreamT<B>> f){
	   return of(run.map(stream-> stream.flatMap(a-> f.apply(a).run.unwrap())));
   }
   
   public static <A> StreamT<A> fromAnyM(AnyM<A> anyM){
	   return of(anyM.map(Stream::of));
   }
   
   public static <A> StreamT<A> of(AnyM<Stream<A>> monads){
	   return new StreamT<>(monads);
   }
   
   
   
 
}
