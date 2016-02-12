package com.aol.cyclops.internal.comprehensions.comprehenders;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.BaseStream;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.aol.cyclops.lambda.api.Comprehender;

public class SetComprehender implements Comprehender<Object> {
	public Class getTargetClass(){
		return Set.class;
	}
	@Override
	public Object filter(Object t, Predicate p) {
		if(t instanceof Set)
			return ((Set)t).stream().filter(p);
		else
			return ((Stream)t).filter(p);
	}

	@Override
	public Object map(Object t, Function fn) {
		if(t instanceof Set)
			return ((Set)t).stream().map(fn);
		else
			return ((Stream)t).map(fn);
	}
	
	public Object executeflatMap(Object t, Function fn){
		return flatMap(t,input -> unwrapOtherMonadTypesLC(this,fn.apply(input)));
	}
	@Override
	public Object flatMap(Object t, Function fn) {
		if(t instanceof Set)
			return ((Set) t).stream().flatMap(fn);
		else 
			return ((Stream) t).flatMap(fn);
			
	}
	@Override
	public boolean instanceOfT(Object apply) {
		return apply instanceof Stream;
	}
	@Override
	public Set empty() {
		return Collections.unmodifiableSet(new HashSet());
	}
	
	@Override
	public Set of(Object o) {
		Set set= new HashSet();
		set.add(o);
		return Collections.unmodifiableSet(set);
	}
	@Override 
	public Set unwrap(Object o){
		if(o instanceof Set)
			return (Set)o;
		else
			return (Set)((Stream)o).collect(Collectors.toSet());
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
