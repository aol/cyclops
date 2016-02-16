package com.aol.cyclops.internal.matcher2;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import org.hamcrest.Matcher;
import org.jooq.lambda.tuple.Tuple;

import com.aol.cyclops.control.ReactiveSeq;

import lombok.AllArgsConstructor;

/**
 * Predicate Builder for Algebraic Data Types
 * Can be used to recursively match on ADTs
 * 
 * @author johnmcclean
 *
 * @param <T>
 */
@AllArgsConstructor
public class ADTPredicateBuilder<T>{
		private final Class<T> type;
		
		public static class InternalADTPredicateBuilder<T>{
			private final ADTPredicateBuilder<T> builder;
			
			public InternalADTPredicateBuilder(Class<T> type){
				builder = new ADTPredicateBuilder<T>(type);
			}
			@SafeVarargs
			final public<V> Predicate<V> hasWhere(Predicate<V>... values){
				ReactiveSeq<Predicate> predicates = ReactiveSeq.of(values).map(nextValue->convertToPredicate(nextValue));
				
				return t -> builder.toPredicate().test(t) 
						  	&& SeqUtils.seq(Extractors.decomposeCoerced().apply(t))
								.zip(predicates,(a,b)->Tuple.tuple(a, b)).map(tuple -> tuple.v2.test(tuple.v1))
								.allMatch(v->v==true);
			}
			@SafeVarargs
			final public<V> Predicate<V> hasMatch(Matcher<V>... values){
				ReactiveSeq<Predicate> predicates = ReactiveSeq.of(values)
																.map(nextValue->convertToPredicate(nextValue));
				
				return t -> builder.toPredicate().test(t) 
						  	&& SeqUtils.seq(Extractors.decomposeCoerced().apply(t))
								.zip(predicates,(a,b)->Tuple.tuple(a, b)).map(tuple -> tuple.v2.test(tuple.v1))
								.allMatch(v->v==true);
			}
			@SafeVarargs
			final public<V> Predicate<V> isWhere(Predicate<V>... values){
				Predicate p = test->SeqUtils.EMPTY==test;
				ReactiveSeq<Predicate> predicates = ReactiveSeq.of(values)
																.map(nextValue->convertToPredicate(nextValue))
																.concat(p);;
				
				return t -> builder.toPredicate().test(t) 
						
						  	&& SeqUtils.seq(Extractors.decomposeCoerced().apply(t))
								.zip(predicates,(a,b)->Tuple.tuple(a, b)).map(tuple -> tuple.v2.test(tuple.v1))
								.allMatch(v->v==true);
			}
			@SafeVarargs
			final public<V> Predicate<V> isMatch(Matcher<V>... values){
				Predicate p = test->SeqUtils.EMPTY==test;
				ReactiveSeq<Predicate> predicates = ReactiveSeq.of(values)
														.map(nextValue->convertToPredicate(nextValue))
														.concat(p);;
				
				return t -> builder.toPredicate().test(t) 
						  	&& SeqUtils.seq(Extractors.decomposeCoerced().apply(t))
								.zip(predicates,(a,b)->Tuple.tuple(a, b)).map(tuple -> tuple.v2.test(tuple.v1))
								.allMatch(v->v==true);
			}
		}
		
		Predicate toPredicate(){
			
			return t ->  Optional.of(t).map(v->type.isAssignableFrom(v.getClass())).orElse(false);
		}
		
		final public<V> Predicate<V> anyValues(){
			return hasGuard();
		}
		/**
		 * Generate a predicate that determines the provided values hold.
		 * Values can be comparison value, JDK 8 Predicate, or Hamcrest Matcher  
		 * for each Element to match on.
		 *
		 * isType will attempt to match on the type of the supplied Case class. If it matches the Case class will be 'decomposed' via it's unapply method
		 * and the Case will then attempt to match on each of the elements that make up the Case class. If the Case class implements Decomposable, that interface and it's
		 * unapply method will be used. Otherwise in Extractors it is possible to register Decomposition Funcitons that will unapply Case classes from other sources (e.g.
		 * javaslang, jADT or even Scala). If no Decomposition Function has been registered, reflection will be used to call an unapply method on the Case class if it exists.
		 * 
		 * @see com.aol.cyclops.internal.matcher2.Extractors#decompose
		 * @see com.aol.cyclops.internal.matcher2.Extractors#registerDecompositionFunction
		 * 
		 * @param values Matching rules for each element in the decomposed / unapplied user input
		 * @return A single Predicate encompassing supplied rules
		 */
		@SafeVarargs
		final public<V> Predicate<V> hasGuard(V... values){
			ReactiveSeq<Predicate> predicates = ReactiveSeq.of(values).map(nextValue->convertToPredicate(nextValue));
			
			return t -> toPredicate().test(t) 
					  	&& SeqUtils.seq(Extractors.decomposeCoerced().apply(t))
							.zip(predicates,(a,b)->Tuple.tuple(a, b)).map(tuple -> tuple.v2.test(tuple.v1))
							.allMatch(v->v==true);
		}
		
		
		@SafeVarargs
		final  public<V> Predicate<V> isGuard(V... values){
			Predicate p = test->SeqUtils.EMPTY==test;
			ReactiveSeq<Predicate> predicates = ReactiveSeq.of(values)
														   .map(nextValue->convertToPredicate(nextValue))
														   .concat(p);
														   
														   
			
			
			return t -> toPredicate().test(t) 
					  	&& SeqUtils.seq(Extractors.decomposeCoerced().apply(t))
							.zip(predicates,(a,b)->Tuple.tuple(a, b)).map(tuple -> tuple.v2.test(tuple.v1))
							.allMatch(v->v==true);
		}
		
		
		
		final  public<V> Predicate<V> eq(V value){
			Predicate p = test->SeqUtils.EMPTY==test;
			ReactiveSeq<Predicate> predicates = ReactiveSeq.of(value)
														   .map(nextValue->convertToPredicate(nextValue))
														   .concat(p);
														   
														   
			
			
			return t -> toPredicate().test(t) 
					  	&& SeqUtils.seq(Extractors.decomposeCoerced().apply(t))
							.zip(predicates,(a,b)->Tuple.tuple(a, b)).map(tuple -> tuple.v2.test(tuple.v1))
							.allMatch(v->v==true);
		}
		
		
		public static <T>  Predicate<T> convertToPredicateTyped(Object o){
			return convertToPredicate(o);
		}
			
		 static  Predicate convertToPredicate(Object o){
			if(o instanceof Predicate)
				return (Predicate)o;
			if(o instanceof Matcher)
				return test -> ((Matcher)o).matches(test);
				
			return test -> Objects.equals(test,o);
		}

	
}