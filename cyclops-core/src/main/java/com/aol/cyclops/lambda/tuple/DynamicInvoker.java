package com.aol.cyclops.lambda.tuple;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.aol.cyclops.invokedynamic.ExceptionSoftener;

public class DynamicInvoker {

	private static final Map<Method,CallSite> callSites = new ConcurrentHashMap<>();
	private static final  Map<Class,Optional<Method>> streamMethod = new ConcurrentHashMap<>();
	
	public static Stream invokeStream(Class clazz, Object o){
		if(o instanceof Stream)
			return (Stream)o;
		else if(o instanceof Collection)
			return ((Collection)o).stream();
		else if(o instanceof Iterable)
			return StreamSupport.stream(((Iterable)o).spliterator(), false);
		else if(o instanceof CharSequence)
			return ((CharSequence)o).chars().boxed().map(i ->Character.toChars(i)[0]);
		else if(o instanceof BufferedReader)
			return ((BufferedReader)o).lines();
		else if(o instanceof File){
			try {
				return Files.lines(Paths.get( ((File)o).getAbsolutePath()));
			} catch (IOException e) {
				ExceptionSoftener.throwSoftenedException(e);
			}
		}
		else if(o instanceof URL){
			try {
				BufferedReader in = new BufferedReader(
				        new InputStreamReader(
				        ((URL)o).openStream()));
				return ((BufferedReader)o).lines();
			} catch (IOException e) {
				ExceptionSoftener.throwSoftenedException(e);
			}
				
		}else if(o!=null && o.getClass().isArray())
			return Stream.of((Object[])o);
		Optional<Method> m = streamMethod.computeIfAbsent(clazz, c->Stream.of(c.getMethods())
				.filter(method -> "stream".equals(method.getName()))
				.filter(method -> method.getParameterCount()==0).findFirst()
				.map(m2->{ m2.setAccessible(true); return m2;}));
		return m.map(mt -> (Stream)new Invoker().executeMethod(o,mt,clazz)).orElse(Stream.of(o));
		
	}
	
	static class Invoker{
		private Object executeMethod(Object t, Method m, Class z) {
			try {
				return callSites.computeIfAbsent(m, (m2) ->  {
					try {
						return new ConstantCallSite(MethodHandles.publicLookup().unreflect(m2));
					} catch (Exception e) {
						ExceptionSoftener.throwSoftenedException(e);
					}
					return null;
				}).dynamicInvoker().invoke(t);
			
			} catch (Throwable e) {
				ExceptionSoftener.throwSoftenedException(e);
			}
			return null;
		}
	}
}
