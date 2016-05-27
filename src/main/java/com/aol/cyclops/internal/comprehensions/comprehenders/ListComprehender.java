package com.aol.cyclops.internal.comprehensions.comprehenders;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.BaseStream;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.types.extensability.Comprehender;

public class ListComprehender implements Comprehender {
	public Class getTargetClass(){
		return List.class;
	}
	@Override
	public Object filter(Object t, Predicate p) {
	    return ListX.fromIterable((Iterable)t).filter(p);
		
	}

	@Override
	public Object map(Object t, Function fn) {
	    return ListX.fromIterable((Iterable)t).map(fn);
	
	}
	
	public Object executeflatMap(Object t, Function fn){
		return flatMap(t,input -> unwrapOtherMonadTypesLC(this,fn.apply(input)));
	}
	@Override
	public Object flatMap(Object t, Function fn) {
	    return ListX.fromIterable((Iterable)t).flatMap(fn);
	  
	}

	@Override
	public boolean instanceOfT(Object apply) {
		return apply instanceof List;
	}
	@Override
	public List empty() {
		return Arrays.asList();
	}
	@Override
	public List of(Object o) {
		return Arrays.asList(o);
	}
	public List fromIterator(Iterator it){
        List list= new ArrayList();
        for(Object next : (Iterable)()-> it){
            list.add(next);
        }
        return Collections.unmodifiableList(list);
    }
	
	@Override 
	public List unwrap(Object o){
		if(o instanceof List)
			return (List)o;
		else
			return (List)((Stream)o).collect(Collectors.toList());
	}
	static List unwrapOtherMonadTypesLC(Comprehender comp,Object apply){
	
		
		if(apply instanceof Collection){
			return  ListX.fromIterable((Collection)apply);
		}
		if(apply instanceof Iterable){
			
		    return  ListX.fromIterable((Iterable)apply);
		}
		
		if(apply instanceof BaseStream){
		
		    return  ListX.fromIterable(()->((BaseStream)apply).iterator());
		    
		}
		Object o = Comprehender.unwrapOtherMonadTypes(comp,apply);
		if(o instanceof Collection){
		    return  ListX.fromIterable((Collection)apply);
		}
		if(o instanceof Iterable){
		    return  ListX.fromIterable((Iterable)apply);
		}
		if(apply instanceof BaseStream){
		    return  ListX.fromIterable(()->((BaseStream)apply).iterator());
		}
		return (List)o;
		
		
	}
	

	

}
