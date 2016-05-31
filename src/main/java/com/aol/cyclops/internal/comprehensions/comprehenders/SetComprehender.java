package com.aol.cyclops.internal.comprehensions.comprehenders;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.BaseStream;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.aol.cyclops.data.collections.extensions.standard.SetX;
import com.aol.cyclops.types.extensability.Comprehender;

public class SetComprehender implements Comprehender<Set> {
	
    @Override
    public Object resolveForCrossTypeFlatMap(Comprehender comp, Set apply) {
        List list = (List) apply.stream().collect(Collectors.toCollection(MaterializedList::new));
        return list.size()>0 ? comp.of(list) : comp.empty();
    }
    
    public Class getTargetClass(){
		return Set.class;
	}
	@Override
	public Object filter(Set t, Predicate p) {
	    return SetX.fromIterable(t).filter(p);
		
	}

	@Override
	public Object map(Set t, Function fn) {
	    return SetX.fromIterable(t).map(fn);
	}
	
	public Object executeflatMap(Set t, Function fn){
		return flatMap(t,input -> unwrapOtherMonadTypesLC(this,fn.apply(input)));
	}
	@Override
	public Object flatMap(Set t, Function fn) {
	    return SetX.fromIterable((Iterable)t).flatMap(fn);
			
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
	    return SetX.of(o);
		
	}
	public Set fromIterator(Iterator it){
	    return SetX.fromIterable(()->it);
    }
	@Override 
	public Set unwrap(Object o){
		if(o instanceof Set)
			return (Set)o;
		else
			return (Set)((Stream)o).collect(Collectors.toSet());
	}
	static Set unwrapOtherMonadTypesLC(Comprehender comp,Object apply){
		
	    if(apply instanceof Collection){
            return  SetX.fromIterable((Collection)apply);
        }
        if(apply instanceof Iterable){
            
            return  SetX.fromIterable((Iterable)apply);
        }
        
        if(apply instanceof BaseStream){
        
            return  SetX.fromIterable(()->((BaseStream)apply).iterator());
            
        }
        Object o = Comprehender.unwrapOtherMonadTypes(comp,apply);
        if(o instanceof Collection){
            return  SetX.fromIterable((Collection)apply);
        }
        if(o instanceof Iterable){
            return  SetX.fromIterable((Iterable)apply);
        }
        if(apply instanceof BaseStream){
            return  SetX.fromIterable(()->((BaseStream)apply).iterator());
        }
        return (Set)o;
		
	}
	

	

}
