package com.aol.cyclops.lambda.tuple;

import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;


import com.aol.cyclops.lambda.utils.ExceptionSoftener;

public class DynamicInvoker {

	private static final Map<Method,CallSite> callSites = new ConcurrentHashMap<>();
	private static final  Map<Class,Method> streamMethod = new ConcurrentHashMap<>();
	public static Stream invokeStream(Class clazz, Object o){
		Method m = streamMethod.computeIfAbsent(clazz, c->Stream.of(c.getMethods())
				.filter(method -> "stream".equals(method.getName()))
				.filter(method -> method.getParameterCount()==0).findFirst()
				.map(m2->{ m2.setAccessible(true); return m2;})
				.get());
		return (Stream)new Invoker().executeMethod(o,m,clazz);
	}
	
	static class Invoker{
		private Object executeMethod(Object t, Method m, Class z) {
			try {
				
				
				

				return callSites.computeIfAbsent(m, (m2) ->  {
					try {
						return new ConstantCallSite(MethodHandles.publicLookup().unreflect(m2));
					} catch (Exception e) {
						ExceptionSoftener.singleton.factory.getInstance().throwSoftenedException(e);
					}
					return null;
				}).dynamicInvoker().invoke(t);
			
			} catch (Throwable e) {
				ExceptionSoftener.singleton.factory.getInstance().throwSoftenedException(e);
			}
			return null;
		}
	}
}
