package com.aol.cyclops.lambda.monads.transformers;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import lombok.Getter;

import org.jooq.lambda.function.Function1;

import com.aol.cyclops.monad.AnyM;
import com.aol.cyclops.sequence.streamable.Streamable;


/**
 * Monad Transformer for Cyclops Streamables
 * 
 * @author johnmcclean
 *
 * @param <A>
 */
public class StreamableT<A> {
   
   final AnyM<Streamable<A>> run;

   private StreamableT(final AnyM<Streamable<A>> run){
       this.run = run;
   }
   public AnyM<Streamable<A>> unwrap(){
	   return run;
   }
   public StreamableT<A> peek(Consumer<A> peek){
	   return map(a-> {peek.accept(a); return a;});
     
   }
   public StreamableT<A> filter(Predicate<A> test){
       return of(run.map(stream-> stream.filter(test)));
   }
   public <B> StreamableT<B> map(Function<A,B> f){
       return new StreamableT<B>(run.map(o-> o.map(f)));
   }
   public <B> StreamableT<B> flatMap(Function1<A,StreamableT<B>> f){
	   return of(run.map(stream-> stream.flatMap(a-> Streamable.fromStream(f.apply(a).run.asSequence()))
			   							.<B>flatMap(a->a)));
   }
    
   public static <U, R> Function<StreamableT<U>, StreamableT<R>> lift(Function<U, R> fn) {
		return optTu -> optTu.map(input -> fn.apply(input));
   }

	public static <U1, U2, R> BiFunction<StreamableT<U1>, StreamableT<U2>, StreamableT<R>> lift2(BiFunction<U1, U2, R> fn) {
		return (optTu1, optTu2) -> optTu1.flatMap(input1 -> optTu2.map(input2 -> fn.apply(input1, input2)));
	}

   public static <A> StreamableT<A> fromAnyM(AnyM<A> anyM){
	   return of(anyM.map(Streamable::of));
   }
   public static <A> StreamableT<A> of(AnyM<Streamable<A>> monads){
	   return new StreamableT<>(monads);
   }
   
   public static <A> StreamableT<A> fromStream(AnyM<Stream<A>> monads){
	   return new StreamableT<>(monads.map(Streamable::fromStream));
   }
   
   
   
 
}