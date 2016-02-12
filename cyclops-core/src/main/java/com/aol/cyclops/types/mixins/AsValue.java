package com.aol.cyclops.types.mixins;

import com.aol.cyclops.types.ValueObject;

public class AsValue {
	
	/**
	 * Coerce an Object to implement the ValueObject interface
	 * Adds pattern matching and decomposability functionality
	 * 
	 * e.g. 
	 * 
	 * <pre>{@code
	 *  int result = AsValue.asValue(new Child(10,20))._match(aCase-> 
									aCase.isType( (Child child) -> child.val).with(10,20))
		
		//is child.val (10)
		\@AllArgsConstructor static class Parent{ private final int val; }
	    \@Value static class Child extends Parent{ int nextVal;
			public Child(int val,int nextVal) { super(val); this.nextVal = nextVal;}
		}
			
	 * }</pre>
	 * @param toCoerce Object to coerce
	 * @return ValueObject that delegates calls to the supplied object
	 */
	public static ValueObject asValue(Object toCoerce){
		return new CoercedValue(toCoerce);
	}
	
	@lombok.Value
	public static class CoercedValue implements ValueObject{
		private final Object value;
		public  Object unwrap(){
			return value;
		}
		
		public Object getMatchable(){
			return value;
		}
			
	}
}
