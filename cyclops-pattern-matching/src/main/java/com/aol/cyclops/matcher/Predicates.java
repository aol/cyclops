package com.aol.cyclops.matcher;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import lombok.AllArgsConstructor;

import org.hamcrest.Matcher;
import org.jooq.lambda.Seq;


public class Predicates {

	public static <T> Predicate<T> p(Predicate<T> p){
		return p;
	}

	public static final Predicate __ = test ->true;
	public static final <Y> Predicate<Y> ANY(){  return __; };
	public static final <Y> Predicate<Y> ANY(Class c){  return a -> a.getClass().isAssignableFrom(c); };
	
	
	public	static<T> With<T> type(Class<T> type){
			return new With(type);
	}
	public	static<V> Predicate with(V... values){
		return new With<Object>(Object.class).<V>with(values);
}
	@AllArgsConstructor
	public static class With<T>{
			private final Class<T> type;
			
			
			Predicate toPredicate(){
				return t ->  Optional.of(t).map(v->v.getClass().isAssignableFrom(type)).orElse(false);
			}
			public<V> Predicate with(V... values){
				Seq<Predicate> predicates = Seq.of(values).map(nextValue->convertToPredicate(nextValue));
				return t -> toPredicate().test(t) && SeqUtils.seq(Extractors.decompose().apply(t))
								.zip(predicates).map(tuple -> tuple.v2.test(tuple.v1))
								.allMatch(v->v==true);
			}
			private Predicate convertToPredicate(Object o){
				if(o instanceof Predicate)
					return (Predicate)o;
				if(o instanceof Matcher)
					return test -> ((Matcher)o).matches(test);
					
				return test -> Objects.equals(test,o);
			}

		
	}
}
