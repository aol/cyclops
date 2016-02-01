package com.aol.cyclops.comprehensions.comprehenders;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.BaseStream;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.pcollections.PVector;

import com.aol.cyclops.lambda.api.Comprehender;

public class ListComprehender implements Comprehender {
	public Class getTargetClass(){
		return List.class;
	}
	@Override
	public Object filter(Object t, Predicate p) {
		if(t instanceof List)
			return ((List)t).stream().filter(p);
		else
			return ((Stream)t).filter(p);
	}

	@Override
	public Object map(Object t, Function fn) {
		if(t instanceof List)
			return ((List)t).stream().map(fn);
		else
			return ((Stream)t).map(fn);
	}
	
	public Object executeflatMap(Object t, Function fn){
		return flatMap(t,input -> unwrapOtherMonadTypesLC(this,fn.apply(input)));
	}
	@Override
	public Object flatMap(Object t, Function fn) {
		if(t instanceof List)
			return ((List) t).stream().flatMap(fn);
		else 
			return ((Stream) t).flatMap(fn);
			
	}

	@Override
	public boolean instanceOfT(Object apply) {
		return apply instanceof Stream;
	}
	@Override
	public List empty() {
		return Arrays.asList();
	}
	@Override
	public List of(Object o) {
		return Arrays.asList(o);
	}
	
	@Override 
	public List unwrap(Object o){
		if(o instanceof List)
			return (List)o;
		else
			return (List)((Stream)o).collect(Collectors.toList());
	}
	static Stream unwrapOtherMonadTypesLC(Comprehender comp,Object apply){
		
		
		
		if(apply instanceof Collection){
			return ((Collection)apply).stream();
		}
		if(apply instanceof Iterable){
			 return StreamSupport.stream(((Iterable)apply).spliterator(),
						false);
		}
		if(apply instanceof BaseStream){
			return StreamSupport.stream(Spliterators.spliteratorUnknownSize(((BaseStream)apply).iterator(), Spliterator.ORDERED),
					false);
		}
		Object o = Comprehender.unwrapOtherMonadTypes(comp,apply);
		if(o instanceof Collection){
			return ((Collection)o).stream();
		}
		if(o instanceof Iterable){
			 return StreamSupport.stream(((Iterable)o).spliterator(),
						false);
		}
		if(apply instanceof BaseStream){
			return StreamSupport.stream(Spliterators.spliteratorUnknownSize(((BaseStream)apply).iterator(), Spliterator.ORDERED),
					false);
		}
		return (Stream)o;
		
	}
	

	

}
