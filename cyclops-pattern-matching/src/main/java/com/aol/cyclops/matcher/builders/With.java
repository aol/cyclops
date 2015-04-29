package com.aol.cyclops.matcher.builders;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import lombok.AllArgsConstructor;

import org.hamcrest.Matcher;
import org.jooq.lambda.Seq;

import com.aol.cyclops.matcher.Extractors;
import com.aol.cyclops.matcher.SeqUtils;

@AllArgsConstructor
public class With<T>{
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