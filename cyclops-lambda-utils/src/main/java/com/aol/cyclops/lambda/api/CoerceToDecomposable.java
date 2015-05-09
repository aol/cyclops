package com.aol.cyclops.lambda.api;

import java.util.Objects;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;

import com.aol.cyclops.lambda.utils.ExceptionSoftener;

public class CoerceToDecomposable {
	
	
	
	public static Decomposable coerceToDecomposable(Object toCoerce){
		return new CoercedDecomposable(toCoerce);
	}
	@AllArgsConstructor
	public static class CoercedDecomposable implements Decomposable{
		private final Object coerced;
		
		public <T extends Iterable<? extends Object>> T unapply(){
			try {
				return (T)ReflectionCache.getField((Class)coerced.getClass()).stream().map(f ->{
					try {
					
						return f.get(coerced);
					} catch (Exception e) {
						ExceptionSoftener.singleton.factory.getInstance().throwSoftenedException(e);
						return null;
					}
				}).collect(Collectors.toList());
			} catch (Exception e) {
				ExceptionSoftener.singleton.factory.getInstance()
						.throwSoftenedException(e);
				return (T)null;
			}
			
		}

		@Override
		public int hashCode() {
			return coerced.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			return Objects.equals(coerced, obj);
			
		}
	}
}
