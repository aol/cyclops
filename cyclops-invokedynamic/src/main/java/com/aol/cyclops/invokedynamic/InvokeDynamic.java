package com.aol.cyclops.invokedynamic;

import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class InvokeDynamic {
	private static volatile Map<Method, CallSite> callSites = new ConcurrentHashMap<>();
	private static volatile Map<Class, Optional<Method>> streamMethod = new ConcurrentHashMap<>();
	private static volatile Map<Class, Optional<Method>> supplierMethod = new ConcurrentHashMap<>();
	private static volatile Map<String,Map<Class, List<Method>>> generalMethods = new ConcurrentHashMap<>();
	public Optional<Stream> stream(Object t) {

		Class clazz = t.getClass();

		Optional<Method> om = streamMethod.computeIfAbsent(
				clazz,
				c -> Stream.of(c.getMethods())
						.filter(method -> "stream".equals(method.getName()) || "toStream".equals(method.getName()))
						.filter(method -> method.getParameterCount() == 0)
						.findFirst().map(m2 -> {
							m2.setAccessible(true);
							return m2;
						}));
		if (!om.isPresent())
			return Optional.empty();
		Method m = om.get();

		return Optional.of((Stream) executeMethod( m,t));

	}
	public <T> Optional<T> execute(List<String> methodNames, Object obj, Object... args){
		return (Optional)methodNames.stream().map(s -> execute(s,obj,args)).filter(Optional::isPresent).findFirst().get();
	}
	public <T> Optional<T> execute(String methodName,Object obj,Object... args) {
		Class clazz = obj.getClass();
		Map<Class, List<Method>> methods = generalMethods.computeIfAbsent(methodName, k->new ConcurrentHashMap<>());
		List<Method> om = methods.computeIfAbsent(
				clazz,
				c -> Stream.of(c.getMethods())
						.filter(method -> methodName.equals(method.getName()))
						.filter(method -> method.getParameterCount() == args.length)
						.map(m2 -> {
							m2.setAccessible(true);
							return m2;
						}).collect(Collectors.toList()));
		
		if(om.size()>0){
			return Optional.of((T)executeMethod( om.get(0),obj,args));
		}
		return Optional.empty();
	}
	public <T> Optional<T> supplier(Object t,List<String> methodNames) {

		Class clazz = t.getClass();

		Optional<Method> om = streamMethod.computeIfAbsent(
				clazz,
				c -> Stream.of(c.getMethods())
						.filter(method -> methodNames.contains(method.getName()))
						.filter(method -> method.getParameterCount() == 0)
						.findFirst().map(m2 -> {
							m2.setAccessible(true);
							return m2;
						}));
		if (!om.isPresent())
			return Optional.empty();
		Method m = om.get();

		return Optional.of( (T)executeMethod( m,t));

	}

	private Object executeMethod(Method m, Object obj,Object... args) {
		try {

			
			MethodHandle mh = this.callSites
					.computeIfAbsent(
							m,
							(m2) -> {
								try {
									return new ConstantCallSite(MethodHandles
											.publicLookup().unreflect(m2));
								} catch (Exception e) {
									throw (RuntimeException)e;
								}
								
							}).dynamicInvoker();
			if(args.length==0)
				return mh.invoke(obj);
			if(args.length==1)
				return mh.invoke(obj,args[0]);
			if(args.length==2)
				return mh.invoke(obj,args[0],args[1]);

		} catch (Throwable e) {
			throw (RuntimeException)e;
		} finally {

		}
		return null;
	}
	
}
