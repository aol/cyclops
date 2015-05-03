package com.aol.cyclops.comprehensions.comprehenders;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.aol.cyclops.comprehensions.Comprehender;

public class ReflectionComprehender implements Comprehender {

	@Override
	public Object filter(Object t, Predicate p) {
		Method m = Stream.of(t.getClass().getMethods())
				.filter(method -> "filter".equals(method.getName()))
				.findFirst().get();
		Class z = m.getParameters()[0].getClass();
		Object o = Proxy.newProxyInstance(ReflectionComprehender.class
				.getClassLoader(), z.getClass().getInterfaces(), (proxy,
				method, args) -> {
			return p.test(args[0]);
		});

		try {
			return m.invoke(t, o);
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			throw new RuntimeException(e);
		}

	}

	@Override
	public Object map(Object t, Function fn) {
		Method m = Stream.of(t.getClass().getMethods())
				.filter(method -> "map".equals(method.getName())).findFirst()
				.get();
		Class z = m.getParameters()[0].getClass();
		Object o = Proxy.newProxyInstance(ReflectionComprehender.class
				.getClassLoader(), z.getClass().getInterfaces(), (proxy,
				method, args) -> {
			return fn.apply(args[0]);
		});

		try {
			return m.invoke(t, o);
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Object flatMap(Object t, Function fn) {
		Method m = Stream.of(t.getClass().getMethods())
				.filter(method -> "flatMap".equals(method.getName()))
				.findFirst().get();
		Class z = m.getParameters()[0].getClass();
		Object o = Proxy.newProxyInstance(ReflectionComprehender.class
				.getClassLoader(), z.getClass().getInterfaces(), (proxy,
				method, args) -> {
			return fn.apply(args[0]);
		});

		try {
			return m.invoke(t, o);
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

}
