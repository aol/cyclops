package com.aol.cyclops.lambda.tuple;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.EqualsAndHashCode;
import lombok.Getter;

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
@EqualsAndHashCode
class TupleImpl<T1,T2,T3,T4,T5,T6,T7,T8> implements TupleWrapper,  Tuple8<T1,T2,T3,T4,T5,T6,T7,T8>
															{
	@Getter
	private final Object instance;
	@Getter
	private final List<Object> cachedValues;
	
	private final  int arity;
	
	public TupleImpl(Object tuple,int arity){
		this.instance = tuple;
		if(tuple instanceof List)
			cachedValues = ((List)tuple);
		else if(tuple instanceof Stream){
			cachedValues = (List)((Stream)tuple).collect(Collectors.toList());
		}
		else
			cachedValues = values();
		this.arity = arity;
	}
	@Override
	public int arity(){
		return arity;
	}
	@Override
    public String toString() {
        return String.format(asStringFormat(arity).orElse(""), _1());
    }
	
	
	
}