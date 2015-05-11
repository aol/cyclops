package com.aol.cyclops.lambda.tuple;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.AllArgsConstructor;
import lombok.val;

import com.aol.cyclops.lambda.api.Decomposable;
import com.aol.cyclops.lambda.utils.ClosedVar;
import com.aol.cyclops.lambda.utils.ExceptionSoftener;

public interface CachedValues extends Iterable, Decomposable{

	public List<Object> getCachedValues();
	
	
	@AllArgsConstructor
	public static class ConvertStep<T extends CachedValues>{
		private final T c;
		public <X> X to(Class<X> to){
			return (X)c.to(to);
		}
	}
	default <T extends CachedValues> ConvertStep<T> convert(){
		return new ConvertStep(this);
	}
	default <X> X to(Class<X> to){
		Constructor<X> cons = (Constructor)Stream.of(to.getConstructors())
							.filter(c -> c.getParameterCount()==2)
							.findFirst()
							.get();
		try {
			
			return cons.newInstance(getCachedValues().toArray());
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			ExceptionSoftener.singleton.factory.getInstance().throwSoftenedException(e);
		}
		return null;
		
		
	}
	
	default void forEach(Consumer c){
		getCachedValues().forEach(c);
	}
	
	default <T extends CachedValues> T filter(Predicate<Tuple2<Integer,Object>> p){
		ClosedVar<Integer> index = new ClosedVar(0);
		val newList = getCachedValues().stream().map(v-> Tuples.tuple(index.set(index.get()+1).get(),v))
						.filter(p).collect(Collectors.toList());
		return (T)new TupleImpl(newList,newList.size());
	}
	default List toList(){
		return getCachedValues();
	}
	default <T extends Stream<?>> T asFlattenedStream(){
		return (T)asStreams().flatMap(s->s);
	}
	default <T extends Stream<?>> Stream<T> asStreams(){
		return (Stream)getCachedValues().stream()
					.filter(o->o!=null)
					.map(o->DynamicInvoker.invokeStream(o.getClass(), o));
	}
	@Override
	default Iterator iterator(){
		return getCachedValues().iterator();
	}
	
	default Stream stream(){
		return getCachedValues().stream();
	}
	
	default <T extends CachedValues> T flatMap(Function<List,T> fn){
		return fn.apply(getCachedValues());
	}
	default <T extends CachedValues> T map(Function<List,List> fn){
		List list = fn.apply(getCachedValues());
		return (T)new TupleImpl(list,list.size());
	}
}
