package com.aol.cyclops.lambda.monads.transformers;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import lombok.Getter;

import org.jooq.lambda.function.Function1;

import com.aol.cyclops.monad.AnyM;
import com.aol.cyclops.sequence.streamable.Streamable;


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
	   return of(run.map(stream-> stream.flatMap(a-> f.apply(a).run.asSequence())
			   							.<B>flatMap(a->a)));
   }
   
   private static <T> T print(T t){
	   System.out.println("!");
	   if(!(t instanceof Stream))
		   System.out.println(t);
	   else{
		   Stream s = (Stream)t;
		   s.forEach(StreamT::print);
	   }
		   
	   return t;
   }
   public static <U, R> Function<StreamT<U>, StreamT<R>> lift(Function<U, R> fn) {
		return optTu -> optTu.map(input -> fn.apply(input));
	}
/**
 * only possible for Streamable or List
	public static <U1, U2, R> BiFunction<StreamT<U1>, StreamT<U2>, StreamT<R>> lift2(BiFunction<U1, U2, R> fn) {
		return (optTu1, optTu2) -> optTu1.flatMap(input1 -> optTu2.map(input2 -> fn.apply(input1, input2)));
	}
**/
   public static <A> StreamT<A> fromAnyM(AnyM<A> anyM){
	   return of(anyM.map(Stream::of));
   }
   
   public static <A> StreamT<A> of(AnyM<Stream<A>> monads){
	   return new StreamT<>(monads);
   }
   
   
   
 
}
