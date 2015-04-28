package com.aol.cyclops.matcher;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Predicate;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;
@AllArgsConstructor(access=AccessLevel.PRIVATE)
public class Case<T,R> implements Function<T,Optional<R>>{
	
	private final Tuple2<Predicate<T>,Function<T,R>> pattern;
	private final boolean isEmpty;
	
	Predicate<T> getPredicate(){
		return pattern.v1;
	}
	Function<T,R> getAction(){
		return pattern.v2;
	}
	
	public Case<T,R> negate(){
		return map(t2-> Tuple.tuple(t2.v1.negate(),t2.v2));
	}
	
	public Case<T,R> negate(Function<T,R> action){
		return map(t2-> Tuple.tuple(t2.v1.negate(),action));
	}
	
	public  Case<T,R> filter(Predicate<Tuple2<Predicate<T>,Function<T,R>>> predicate){
		return predicate.test(pattern) ? this : empty();
	}
	public  Case<T,R> mapPredicate(Function<Predicate<T>,Predicate<T>> mapper){
		return this.map(t2-> Tuple.tuple(mapper.apply(t2.v1),t2.v2));
	}
	public  <R1> Case<T,R1> mapFunction(Function<Function<T,R>,Function<T,R1>> mapper){
		return this.<T,R1>map(t2-> Tuple.tuple(t2.v1,mapper.apply(t2.v2)));
	}
	public <T1,R1> Case<T1,R1> map(Function<Tuple2<Predicate<T>,Function<T,R>>,Tuple2<Predicate<T1>,Function<T1,R1>>> mapper){
		return Case.of(mapper.apply(pattern));	
	}
	public <T1,R1> Case<T1,R1> flatMap(Function<Tuple2<Predicate<T>,Function<T,R>>,Case<T1,R1>> mapper){
		return mapper.apply(pattern);	
	}
	
	
	public  Case<T,T> andThen(Case<R,T> after){
		return after.compose(this);
	}
	
	public <T1> Case<T1,R> compose(Case<T1,T> before){
		
		final Object[] array = {null};
		return Case.<T1,R>of(t-> {
			final boolean passed;
			if(before.pattern.v1.test(t)){
				passed=true;
				array[0]=before.pattern.v2.apply(t);
			}else
				passed= false;
			return  passed && pattern.v1.test((T)array[0]);
		},input -> pattern.v2.apply((T)array[0]) );
		
	}
	public <T1> Case<T1,R> composeOr(Case<T1,T> before){
		
		
		return Case.<T1,R>of(t->   before.pattern.v1.test(t) || pattern.v1.test(before.pattern.v2.apply(t)),
				input -> pattern.v2.apply(before.pattern.v2.apply(input)));
		
	}
	public <T1> Case<T1,R> composeFunction(Function<T1,T> before){
		return this.compose(Case.<T1,T>of(t->true,before));
	}
	public  Case<T,T> andThenFunction(Function<R,T>  after){
		return this.andThen(Case.<R,T>of(r->true,after));
	}
	public Case<T,R> or(Predicate<T> or){
		return composeOr(Case.of(or,Function.identity()));
		
	}
	public<T1> Case<T1,R> or(Predicate<T1> or, Function<T1,T> fn){
		return composeOr(Case.of(or,fn));
		
	}
	public Case<T,R> and(Predicate<T> and){
		return compose(Case.of(and,Function.identity()));
	}
	public <T1> Case<T,R> and(Predicate<T> and, Function<T1,T> fn){
		return compose(Case.of(and,Function.identity()));
	}
	public boolean isNotEmpty(){
		return !this.isEmpty;
	}
	public Optional<R> match(T value){
		if(pattern.v1.test(value))
			return Optional.of(pattern.v2.apply(value));
		return Optional.empty();
	}
	public CompletableFuture<Optional<R>> matchAsync(Executor executor, T value){
		return CompletableFuture.supplyAsync(()->match(value),executor);
	}
	public static <T,R> Case<T,R> of(Predicate<T> predicate,Function<T,R> action){
		return new Case<T,R>(Tuple.tuple(predicate,action),false);
	}
	public static <T,R> Case<T,R> of(Tuple2<Predicate<T>,Function<T,R>> pattern){
		return new Case<>(pattern,false);
	}
	
	private static final Case empty = new Case(Tuple.<Predicate,Function>tuple(t->false,input->input),true);
	public final static <T,R> Case<T,R> empty(){
		return empty;
	}
	@Override
	public Optional<R> apply(T t) {
		return match(t);
	}
	
}
