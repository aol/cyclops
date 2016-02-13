package com.aol.cyclops.types.mixins;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import lombok.Value;

public class AsSupplier {
	/**
	 * Create a Duck typing  based Supplier
	 * 
	 * 
	 * 
	 * @param toCoerce Object to convert into a Supplier, 
	 * 		must have a non-void get() method
	 * @return Supplier that delegates to the supplied object
	 */
	public static <T>  Supplier<T> asSupplier(Object toCoerce){
		return new CoercedSupplier(toCoerce,Arrays.asList("get","call"));
	}
	/**
	 * Create a Duck typing  based Supplier
	 * That returns the result of a call to the supplied method name
	 * 
	 * @param toCoerce Object to convert into a supplier
	 * @param method Method to call when Supplier.get() called
	 * @return Supplier that delegates to supplied object
	 */
	public static <T>  Supplier<T> asSupplier(Object toCoerce, String method){
		return new CoercedSupplier(toCoerce,Arrays.asList(method));
	}
	@Value
	public static class CoercedSupplier<T> implements Gettable<T>{
		private final Object dValue;
		private final List<String> supplierMethodNames;
		public Object unwrap(){
			return dValue;
		}
		
		
	}
}
