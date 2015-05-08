package com.aol.cyclops.lambda.api;

import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import net.sf.cglib.proxy.Mixin;
import net.sf.cglib.proxy.Mixin.Generator;

import com.aol.cyclops.lambda.utils.ExceptionSoftener;

public class CoerceToDecomposable {
	
	/**public static <T> T coerceToDecomposableKeepType(T toCoerce){
		//	Generator gen = new Generator();
     //   gen.setDelegates(new Object[]{toCoerce, new CoercedDecomposable(toCoerce)});
      //  gen.setClassLoader(cl);
       // return (Decomposable)gen.create();
		return (T)Mixin.create(new Object[]{ toCoerce, new CoercedDecomposable(toCoerce) });
	}
	**/
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
	}
}
