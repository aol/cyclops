package com.aol.cyclops.matcher;

import java.io.Serializable;
import java.lang.invoke.MethodType;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import javaslang.Function1;
import javaslang.LambdaAccessor;
import lombok.val;

import org.hamcrest.Matcher;

import com.google.common.collect.Maps;


@SuppressWarnings("unchecked")
public class PatternMatcher implements Function1{
	Map<Pair<Predicate,Optional<Extractor>>,Pair<ActionWithReturn,Optional<Extractor>>> cases = Maps.newLinkedHashMap();

	public Object apply(Object t){
		return match(t).get();
	}
	
	public <R> Optional<R> match(Object t){
		
		Object[] result = {null};

		cases.forEach( (match, action) -> {
			if(result[0]==null){
				
				
				Object toUse = match.getSecond().map(ex -> {
							val type = ex.getType();
							return type.parameterType(type.parameterCount() - 1).isAssignableFrom(t.getClass()) ? ex : Function1.identity();
						}
				).orElse(x -> x).apply(t);
				if(match.getFirst().test(toUse))
					result[0] = (R)action.getFirst().apply(action.getSecond().map(ex -> {
						val type = ex.getType();
						return type.parameterType(type.parameterCount() - 1).isAssignableFrom(t.getClass()) ? ex : Function1.identity();

					}).orElse((x) -> x).apply(toUse));
			}
		});
		return Optional.ofNullable((R)result[0]);
	}
	
	public <R,V,T,X> PatternMatcher caseOfType( Extractor<T,R> extractor,Action<V> a){
		return inCaseOfType(extractor,new ActionWithReturnWrapper(a));
		
	}
	public <R,V,T,X> PatternMatcher caseOfValue(R value, Extractor<T,R> extractor,Action<V> a){
		
		return inCaseOfValue(value,extractor,new ActionWithReturnWrapper(a));
	}
	public <V,X> PatternMatcher caseOfValue(V value,Action<V> a){
		
		caseOfThenExtract(it -> Objects.equals(it, value), a, null);
		return this;
	}
	public <V,X> PatternMatcher caseOfType(Action<V> a){
		val type = a.getType();
		val clazz = type.parameterType(type.parameterCount()-1);
		caseOfThenExtract(it -> it.getClass().isAssignableFrom(clazz), a, null);
		return this;
	}
	public <V> PatternMatcher caseOf(Matcher<V> match,Action<V> a){
		inCaseOfThenExtract(it->match.matches(it), new ActionWithReturnWrapper(a), null);
		return this;
	}
	public <V> PatternMatcher caseOf(Predicate<V> match,Action<V> a){
		inCaseOfThenExtract(match, new ActionWithReturnWrapper(a), null);
		return this;
	}
	public <R,V,T> PatternMatcher caseOfThenExtract(Predicate<V> match,Action<V> a, Extractor<T,R> extractor){
		
		cases.put(new Pair(match, Optional.empty()), new Pair<ActionWithReturn, Optional<Extractor>>(new ActionWithReturnWrapper(a), Optional.ofNullable(extractor)));
		return this;
	}
	public <R,V,T> PatternMatcher caseOfThenExtract(Matcher<V> match,Action<V> a, Extractor<T,R> extractor){
		Predicate<V> predicate = it->match.matches(it);
		cases.put(new Pair(predicate, Optional.empty()), new Pair<ActionWithReturn, Optional<Extractor>>(new ActionWithReturnWrapper(a), Optional.ofNullable(extractor)));
		return this;
	}
	public <R,V,T> PatternMatcher caseOf( Extractor<T,R> extractor,Predicate<V> match,Action<V> a){
		
		cases.put(new Pair(match,Optional.of(extractor)),new Pair<ActionWithReturn,Optional<Extractor>>(new ActionWithReturnWrapper(a),Optional.empty()));
		return this;
	}
	public <R,V,T> PatternMatcher caseOf( Extractor<T,R> extractor,Matcher<V> match,Action<V> a){
		Predicate<V> predicate = it->match.matches(it);
		cases.put(new Pair(predicate,Optional.of(extractor)),new Pair<ActionWithReturn,Optional<Extractor>>(new ActionWithReturnWrapper(a),Optional.empty()));
		return this;
	}
	public <V,X> PatternMatcher inCaseOfValue(V value,ActionWithReturn<V,X> a){
		
		inCaseOfThenExtract(it -> Objects.equals(it, value), a, null);
		return this;
	}
	public <V,X> PatternMatcher inCaseOfType(ActionWithReturn<V,X> a){
		val type = a.getType();
		val clazz = type.parameterType(type.parameterCount()-1);
		inCaseOfThenExtract(it -> it.getClass().isAssignableFrom(clazz), a, null);
		return this;
	}
	public <V,X> PatternMatcher inCaseOf(Predicate<V> match,ActionWithReturn<V,X> a){
		inCaseOfThenExtract(match, a, null);
		return this;
	}
	public <R,V,T,X> PatternMatcher inCaseOfThenExtract(Predicate match,ActionWithReturn<R,X> a, Extractor<T,R> extractor){
		
		cases.put(new Pair(match,Optional.empty()),new Pair<ActionWithReturn,Optional<Extractor>>(a,Optional.ofNullable(extractor)));
		return this;
	}
	
	
	public <R,V,T,X> PatternMatcher inCaseOf( Extractor<T,R> extractor,Predicate<V> match,ActionWithReturn<V,X> a){
		
		cases.put(new Pair(match,Optional.of(extractor)),new Pair<ActionWithReturn,Optional<Extractor>>(a,Optional.empty()));
		return this;
	}
	
	/**hamcrest **/
	public <V,X> PatternMatcher inCaseOf(Matcher<V> match,ActionWithReturn<V,X> a){
		Predicate<V> predicate = it->match.matches(it);
		inCaseOfThenExtract(predicate, a, null);
		return this;
	}
	public <R,V,T,X> PatternMatcher inCaseOfThenExtract(Matcher<V> match,ActionWithReturn<V,X> a, Extractor<T,R> extractor){
		Predicate<V> predicate = it->match.matches(it);
		cases.put(new Pair(predicate,Optional.empty()),
					new Pair<ActionWithReturn,Optional<Extractor>>(a,Optional.ofNullable(extractor)));
		return this;
	}
	
	
	public <R,V,T,X> PatternMatcher inCaseOf( Extractor<T,R> extractor,Matcher<V> match,ActionWithReturn<V,X> a){
		Predicate<V> predicate = it->match.matches(it);
		cases.put(new Pair(predicate,Optional.of(extractor)),new Pair<ActionWithReturn,Optional<Extractor>>(a,Optional.empty()));
		return this;
	}
	
	public <R,V,T,X> PatternMatcher inCaseOfType( Extractor<T,R> extractor,ActionWithReturn<V,X> a){
		val type = a.getType();
		val clazz = type.parameterType(type.parameterCount()-1);
		Predicate predicate = it -> it.getClass().isAssignableFrom(clazz);
		cases.put(new Pair(predicate,Optional.of(extractor)),new Pair<ActionWithReturn,Optional<Extractor>>(a,Optional.empty()));
		return this;
	}
	public <R,V,T,X> PatternMatcher inCaseOfValue(R value, Extractor<T,R> extractor,ActionWithReturn<V,X> a){
		
		Predicate predicate = it -> Objects.equals(it, value);
		cases.put(new Pair(predicate,Optional.of(extractor)),new Pair<ActionWithReturn,Optional<Extractor>>(a,Optional.empty()));
		return this;
	}
	
	public static interface Extractor<T,R> extends Function1<T,R>{
		public R apply(T t);
	}
	
	public static class ActionWithReturnWrapper<T,X> implements ActionWithReturn<T,X>{
		private final Action<T> action;
		ActionWithReturnWrapper(Action<T> action){
			this.action = action;
		}
		public X apply(T t){
			action.act(t);
			return null;
		}
	}
	
	public static interface ActionWithReturn<T,X> extends Function1<T,X> {
		public X apply(T t);
	}
	public static interface Action<T> extends Serializable {
		
		public void act(T t);
		
		default MethodType getType(){
			return LambdaAccessor.getLambdaSignature(this);
		}
	}
	
	
}
