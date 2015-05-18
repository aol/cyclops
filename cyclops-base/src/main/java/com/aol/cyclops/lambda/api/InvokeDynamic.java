package com.aol.cyclops.lambda.api;

import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import com.aol.cyclops.lambda.utils.ExceptionSoftener;


class InvokeDynamic {
	private static volatile Map<Method, CallSite> callSites = new ConcurrentHashMap<>();
	private static volatile Map<Class, Optional<Method>> streamMethod = new ConcurrentHashMap<>();
	private static volatile Map<Class, Optional<Method>> supplierMethod = new ConcurrentHashMap<>();
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

		return Optional.of((Stream) executeMethod(t, m));

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

		return Optional.of( (T)executeMethod(t, m));

	}

	private Object executeMethod(Object t, Method m) {
		try {

			return this.callSites
					.computeIfAbsent(
							m,
							(m2) -> {
								try {
									return new ConstantCallSite(MethodHandles
											.publicLookup().unreflect(m2));
								} catch (Exception e) {
									ExceptionSoftener.singleton.factory
											.getInstance()
											.throwSoftenedException(e);
								}
								return null;
							}).dynamicInvoker().invoke(t);

		} catch (Throwable e) {
			ExceptionSoftener.singleton.factory.getInstance()
					.throwSoftenedException(e);
		} finally {

		}
		return null;
	}
}
