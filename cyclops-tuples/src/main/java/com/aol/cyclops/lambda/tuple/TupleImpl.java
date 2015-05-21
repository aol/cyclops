package com.aol.cyclops.lambda.tuple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Wither;

import com.aol.cyclops.lambda.api.TupleWrapper;



/**
 * Iteroperability class for different Tuple implementation - wrap in Tuple of appropriate arity
 * 
 * @author johnmcclean
 *
 * @param <T1>
 * @param <T2>
 * @param <T3>
 * @param <T4>
 * @param <T5>
 * @param <T6>
 */

@AllArgsConstructor(access=AccessLevel.PRIVATE)
public class TupleImpl<T1,T2,T3,T4,T5,T6,T7,T8> implements TupleWrapper,  PTuple8<T1,T2,T3,T4,T5,T6,T7,T8>{
	@Getter
	private final Object instance;
	
	
	@Getter
	private final List<Object> cachedValues;
	@Wither
	private final  int arity;
	
	private final List<Object> empty = Arrays.asList();
	public TupleImpl(int arity){
		this.arity = arity;
		this.cachedValues = empty;
		this.instance=null;
	}
	public TupleImpl(Object tuple){
		this(tuple,OptionalInt.empty());
	}
	public TupleImpl(Object tuple,int arity){
		this(tuple,OptionalInt.of(arity));
	}
	private TupleImpl(Object tuple,OptionalInt arity){
		this.instance = tuple;
		if(tuple instanceof Collection)
			cachedValues = new ArrayList( ((List)tuple));
		else if(tuple instanceof Map){
			cachedValues = new ArrayList( ((Map)tuple).entrySet());
		}
		else if(tuple instanceof Stream){
			cachedValues = (List)((Stream)tuple).collect(Collectors.toList());
		}
		else if(tuple instanceof Iterable){
			cachedValues=  loadFromIterable((Iterable)tuple);
		}
		else if(tuple instanceof Iterator){
			cachedValues=  loadFromIterator((Iterator)tuple);
		}
		else if(tuple!=null && tuple.getClass().isArray()){
			cachedValues=  Arrays.asList((Object[]) tuple);
		}
		else
			cachedValues = values();
		this.arity = arity.orElse(cachedValues.size());
	}
	
	
	private List<Object> loadFromIterator(Iterator tuple) {
		List<Object> result = new ArrayList<>();
		while(tuple.hasNext())
			result.add(tuple.next());
		return result;
	}
	private List<Object> loadFromIterable(Iterable tuple) {
		List<Object> result = new ArrayList<>();
		for(Object next : tuple){
			result.add(next);
		}
		return result;
	}
	@Override
	public int arity(){
		return arity;
	}
	@Override
    public String toString() {
		return getCachedValues().toString();
    }
	@Override
	public int hashCode() {
		
		return Objects.hashCode(this.getCachedValues());
	}
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof CachedValues))
			return false;
		if(getCachedValues()==null){
			if(  ( (CachedValues)obj).getCachedValues()==null){
				return true;
			}
		}
		
		return getCachedValues().equals( ((CachedValues)obj).getCachedValues());

	}
	
	
	
}