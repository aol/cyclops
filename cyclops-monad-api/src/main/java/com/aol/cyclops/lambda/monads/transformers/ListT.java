package com.aol.cyclops.lambda.monads.transformers;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.Getter;

import org.jooq.lambda.function.Function1;

import com.aol.cyclops.monad.AnyM;
import com.aol.cyclops.sequence.SequenceM;



/**
 * Monad Transformer for Cyclops Lists
 * 
 * @author johnmcclean
 *
 * @param <A>
 */
public class ListT<A> {
   
   final AnyM<List<A>> run;

   private ListT(final AnyM<List<A>> run){
       this.run = run;
   }
   public AnyM<List<A>> unwrap(){
	   return run;
   }
   public ListT<A> peek(Consumer<A> peek){
	   return map(a-> {peek.accept(a); return a;});
     
   }
   public ListT<A> filter(Predicate<A> test){
       return of(run.map(stream-> SequenceM.fromList(stream).filter(test).toList()));
   }
   public <B> ListT<B> map(Function<A,B> f){
       return of(run.map(o-> SequenceM.fromList(o).map(f).toList()));
   }
   public <B> ListT<B> flatMap(Function1<A,ListT<B>> f){
	  
	   return of( run.map(stream-> SequenceM.fromList(stream).flatMap(a-> f.apply(a).run.asSequence()).flatMap(a->a.stream())
			   .toList()));
   }
    
   public static <U, R> Function<ListT<U>, ListT<R>> lift(Function<U, R> fn) {
		return optTu -> optTu.map(input -> fn.apply(input));
   }

	public static <U1, U2, R> BiFunction<ListT<U1>, ListT<U2>, ListT<R>> lift2(BiFunction<U1, U2, R> fn) {
		return (optTu1, optTu2) -> optTu1.flatMap(input1 -> optTu2.map(input2 -> fn.apply(input1, input2)));
	}

   public static <A> ListT<A> fromAnyM(AnyM<A> anyM){
	   return of(anyM.map(Arrays::asList));
   }
   public static <A> ListT<A> of(AnyM<List<A>> monads){
	   return new ListT<>(monads);
   }
   public static <A> ListT<A> fromStream(AnyM<Stream<A>> monads){
	   return of(monads.map(s->s.collect(Collectors.toList())));
   }
   
  
   
   
   
 
}