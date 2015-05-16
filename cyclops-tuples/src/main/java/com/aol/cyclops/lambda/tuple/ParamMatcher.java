package com.aol.cyclops.lambda.tuple;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.aol.cyclops.lambda.utils.ExceptionSoftener;

class ParamMatcher {

	public  <T> T create(Class<T> to, int arity, List<Object> values, Throwable t){
		List<Class> classes = Stream.of(values).map(v->v==null? null : v.getClass())
							.collect(Collectors.toList());
		
		List<Constructor<?>> cons = Stream.of(to.getConstructors())
				.filter(c -> c.getParameterCount()==arity)
				.collect(Collectors.toList());
		
		for(Constructor<?> c : cons){
			if(match(c,classes))
				try {
					//constructors from successful conversions should be cached
					return (T)c.newInstance(order(values,c).toArray());
				} catch (InstantiationException | IllegalAccessException
						| IllegalArgumentException | InvocationTargetException e) {
					
				}
		}
		ExceptionSoftener.singleton.factory.getInstance().throwSoftenedException(t);
		return null;
	}
	 private List<Object> order(List<Object> values,Constructor c) {
		Class[] order = c.getParameterTypes();
		List<Object> result = new ArrayList<>();
		for(Class next : order){
			result.add(find(next,values));
		}
		return result;
		
	}
	private Object find(Class next, List<Object> values) {
		return values.stream().filter(v->v!=null)
					.filter(v->v.getClass().isAssignableFrom(next))
					.findFirst().orElse(null);
	}
	boolean match(Constructor c, List<Class> values){
		return values.stream()
		 	.filter(v->v!=null)
		 	.map(v->matchFor(v,c))
		 	.allMatch(t->t);
		
	}
	private boolean matchFor(Class v, Constructor c) {
		return Stream.of(c.getParameterTypes()).anyMatch(ct->v.isAssignableFrom(ct));
	}
}
