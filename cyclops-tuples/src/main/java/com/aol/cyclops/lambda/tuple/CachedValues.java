package com.aol.cyclops.lambda.tuple;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.AllArgsConstructor;
import lombok.val;

import com.aol.cyclops.lambda.utils.Mutable;
import com.aol.cyclops.matcher.builders.MatchingInstance;
import com.aol.cyclops.matcher.builders.PatternMatcher;
import com.aol.cyclops.matcher.builders._MembersMatchBuilder;
import com.aol.cyclops.matcher.builders._Simpler_Case;
import com.aol.cyclops.value.StreamableValue;

public interface CachedValues extends Iterable, StreamableValue, Concatenate, LazySwap{

	public List<Object> getCachedValues();
	
	CachedValues withArity(int arity);
	@AllArgsConstructor
	public static class ConvertStep<T extends CachedValues>{
		private final T c;
		public <X> X to(Class<X> to){
			return (X)c.to(to);
		}
	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	default <R,T,I> R  matchValues(Function<_MembersMatchBuilder<I,T>,_MembersMatchBuilder<I,T>> fn){
		
		return (R) new MatchingInstance(new _Simpler_Case( fn.apply( (_MembersMatchBuilder)
					new _Simpler_Case(new PatternMatcher()).withType(this.getClass())).getPatternMatcher()))
						.match(this).get();
	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	default <R,T,I> R  matchValues(Function<_MembersMatchBuilder<I,T>,_MembersMatchBuilder<I,T>> fn, R defaultValue){
		
		return (R) new MatchingInstance(new _Simpler_Case( fn.apply( (_MembersMatchBuilder)
					new _Simpler_Case(new PatternMatcher()).withType(this.getClass())).getPatternMatcher()))
						.match(this).orElse(defaultValue);
	}
	default <T extends CachedValues> ConvertStep<T> convert(){
		return new ConvertStep(this);
	}
	default <X> X to(Class<X> to){
		Constructor<X> cons = (Constructor)Stream.of(to.getConstructors())
							.filter(c -> c.getParameterCount()==arity())
							.findFirst()
							.get();
		try {
			
			return cons.newInstance(getCachedValues().toArray());
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			return new ParamMatcher().create(to, arity(), getCachedValues(),e);
			
		}
		
		
		
	}
	
	default void forEach(Consumer c){
		getCachedValues().forEach(c);
	}
	
	default <T extends CachedValues> T filter(Predicate<PTuple2<Integer,Object>> p){
		Mutable<Integer> index = new Mutable(-1);
		val newList = getCachedValues().stream().map(v-> PowerTuples.tuple(index.set(index.get()+1).get(),v))
						.filter(p).map(PTuple2::v2).collect(Collectors.toList());
		return (T)new TupleImpl(newList,newList.size());
	}
	default <T> List<T> toList(){
		return (List)getCachedValues();
	}
	default <K,V> Map<K,V> toMap(){
		Map result = new HashMap<>();
		Iterator it = getCachedValues().iterator();
		if(arity()%2==0){
			for(int i=0;i+1<arity();i=i+2){
				result.put(getCachedValues().get(i), getCachedValues().get(i+1));
			}
		}
		
		return result;
	}
	public int arity();
	/**
	 * Will attempt to convert each element in the tuple into a flattened Stream
	 * 
	 * @return Flattened Stream
	 */
	default <T extends Stream<?>> T asFlattenedStream(){
		return (T)asStreams().flatMap(s->s);
	}
	/**
	 * Will attempt to convert each element in the tuple into a Stream
	 * 
	 * Collection::stream
	 * CharSequence to stream
	 * File to Stream
	 * URL to Stream
	 * 
	 * @return Stream of Streams
	 */
	default <T extends Stream<?>> Stream<T> asStreams(){
		//each value where stream can't be called, should just be an empty Stream
		return (Stream)getCachedValues().stream()
					.filter(o->o!=null)
					.map(o->DynamicInvoker.invokeStream(o.getClass(), o));
	}
	default Stream<String> asStreamOfStrings(){
		
		return (Stream)getCachedValues().stream()
					.filter(o->o!=null)
					.map(Object::toString);
					
	}
	@Override
	default Iterator iterator(){
		return getCachedValues().iterator();
	}
	
	default Stream stream(){
		return getCachedValues().stream();
	}
	
	default <T extends CachedValues,X> T append(X value){
		List list = new ArrayList(getCachedValues());
		list.add(value);
		return (T)new TupleImpl(list,list.size());
		
	}
	default <T extends CachedValues> T appendAll(CachedValues values){
		List list = new ArrayList(getCachedValues());
		list.addAll(values.getCachedValues());
		return (T)new TupleImpl(list,list.size());
		
	}
	default <T extends CachedValues, X extends CachedValues> T flatMap(Function<X,T> fn){
		return fn.apply((X)this);
	}
	
	default <T extends CachedValues> T map(Function<List,List> fn){
		List list = fn.apply(getCachedValues());
		return (T)new TupleImpl(list,list.size());
	}
}
