package com.aol.cyclops.matcher;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface Decomposable{
	
	
	default List<? extends Object> unapply(){

		try {
			return ReflectionCache.getField(this.getClass()).stream().map(f ->{
				try {
				
					return f.get(this);
				} catch (Exception e) {
					ExceptionSoftener.singleton.factory.getInstance().throwSoftenedException(e);
					return null;
				}
			}).collect(Collectors.toList());
		} catch (Exception e) {
			ExceptionSoftener.singleton.factory.getInstance()
					.throwSoftenedException(e);
			return null;
		}
		
	}
	
	
}
