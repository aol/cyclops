package com.aol.cyclops.util.function;

import java.util.function.Predicate;

import org.hamcrest.Matcher;

import com.aol.cyclops.internal.matcher2.ADTPredicateBuilder;


/**
 * 
 * Some useful Predicates
 * 
 * @author johnmcclean
 *
 */
public class Predicates {

	public static <T> Predicate<T> p(Predicate<T> p){
		return p;
	}

	/**
	 * wildcard predicate
	 * 
	 */
	public static final Predicate __ = test ->true;
	
	/**
	 * @see Predicates#__
	 * @return Wildcard predicate, capitlised to disambiguate from Hamcrest.any()
	 * 
	 */
	public static final <Y> Predicate<Y> ANY(){  return __; };
	
	/**
	 * Match against any object that is an instance of supplied type
	 * 
	 * @param c Class type to match against
	 * @return Predicate that mathes against type
	 */
	public static final <Y> Predicate<Y> ANY(Class c){  return a -> a.getClass().isAssignableFrom(c); };
	
	
	/**
	 * Recursively decompose and match against case classes of specified type.
	 * 
	 * <pre>
	 * {@code
	 *  return Matching.<Expression>whenValues().isType( (Add<Const,Mult> a)-> new Const(1))
									.with(__,type(Mult.class).with(__,new Const(0)))
				.whenValues().isType( (Add<Mult,Const> a)-> new Const(0)).with(type(Mult.class).with(__,new Const(0)),__)
				.whenValues().isType( (Add<Add,Const> a)-> new Const(-100)).with(with(__,new Const(2)),__)
				
				
				.apply(e).orElse(new Const(-1));
	 * 
	 * }
	 * </pre>
	 * 
	 * 
	 * @param type Classs type to decompose
	 * @return Predicate builder that can decompose classes of specified type
	 */
	public	static<T> ADTPredicateBuilder<T> type(Class<T> type){
			return new ADTPredicateBuilder<>(type);
	}
	/**
	 * Recursively compose an Object without specifying a type
	 * 
	 * <pre>
	 * {@code 
	 * return Matching.<Expression>whenValues().isType( (Add<Const,Mult> a)-> new Const(1))
									.with(__,type(Mult.class).with(__,new Const(0)))
				.whenValues().isType( (Add<Mult,Const> a)-> new Const(0)).with(type(Mult.class).with(__,new Const(0)),__)
				.whenValues().isType( (Add<Add,Const> a)-> new Const(-100)).with(with(__,new Const(2)),__)
				
				
				.apply(e).orElse(new Const(-1));
	 * 
	 * }
	 * </pre>
	 * 
	 * @param values To match against
	 * @return Predicate builder that can decompose Case class and match against specified values
	 */
	@SafeVarargs
	public	static<V> Predicate<V> has(V... values){
		return new ADTPredicateBuilder<Object>(Object.class).<V>has(values);
	}
	@SafeVarargs
	public	static<V> Predicate<V> hasWhere(Predicate<V>... values){
		return new ADTPredicateBuilder<Object>(Object.class).<V>hasWhere(values);
	}
	@SafeVarargs
	public	static<V> Predicate<V> hasMatch(Matcher<V>... values){
		return new ADTPredicateBuilder<Object>(Object.class).<V>hasMatch(values);
	}
	@SafeVarargs
	public	static<V> Predicate<V> is(V... values){
		return new ADTPredicateBuilder<Object>(Object.class).<V>is(values);
	}
	@SafeVarargs
	public	static<V> Predicate<V> isWhere(Predicate<V>... values){
		return new ADTPredicateBuilder<Object>(Object.class).<V>isWhere(values);
	}
	@SafeVarargs
	public	static<V> Predicate<V> isMatch(Matcher<V>... values){
		return new ADTPredicateBuilder<Object>(Object.class).<V>isMatch(values);
	}
	
	public	static<V> Predicate<V> eq(V value){
		return new ADTPredicateBuilder<Object>(Object.class).eq(value);
	}

	
	
	
}
