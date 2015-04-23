package com.aol.cyclops.matcher;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface Decomposable{
	default List<? extends Object> unapply(){
		return Stream.of(this.getClass().getDeclaredFields()).map(f ->{
			try {
				return f.get(this);
			} catch (Exception e) {
				return null;
			}
		}).collect(Collectors.toList());
		
	}
	
}