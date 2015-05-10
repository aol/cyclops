package com.aol.cyclops.comprehensions.comprehenders;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.pcollections.HashTreePMap;
import org.pcollections.HashTreePSet;
import org.pcollections.PMap;
import org.pcollections.PSet;


import com.aol.cyclops.lambda.api.Comprehender;

import lombok.AllArgsConstructor;
import lombok.val;

@AllArgsConstructor
public class ReflectionComprehender implements Comprehender {

	Optional<Class> type;
	
	private static volatile PMap<Class,PSet<ProxyWrapper>> proxyCache =  HashTreePMap.empty();
	@AllArgsConstructor
	static class ProxyWrapper{
		private final Proxy proxy;
		
		@Override
		public boolean equals(Object o){
			return o==proxy;
		}
		@Override
		public int hashCode(){
			return System.identityHashCode(proxy);
		}
	}
	@Override
	public Object filter(Object t, Predicate p) {
		Method m = Stream.of(t.getClass().getMethods())
				.filter(method -> "filter".equals(method.getName()))
				.filter(method -> method.getParameterCount()==1)
				.findFirst().get();
		Class z = m.getParameterTypes()[0];
		ProxyWrapper proxy = getProxy(z);
		((FunctionExecutionInvocationHandler)Proxy.getInvocationHandler(proxy.proxy)).setFunction(input -> p.test(input));
		

		try {
			return m.invoke(t, proxy.proxy);
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			throw new RuntimeException(e);
		}finally{
			release(z,proxy);
		}

	}

	@Override
	public Object map(Object t, Function fn) {
		Method m = Stream.of(t.getClass().getMethods())
				.filter(method -> "map".equals(method.getName()))
				.filter(method -> method.getParameterCount()==1).findFirst()
				.get();
		Class z = m.getParameterTypes()[0];
		ProxyWrapper proxy = getProxy(z);
		((FunctionExecutionInvocationHandler)Proxy.getInvocationHandler(proxy.proxy)).setFunction(input -> fn.apply(input));
		

		try {
			return m.invoke(t, proxy.proxy);
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}finally{
			release(z,proxy);
		}
	}

	@Override
	public Object flatMap(Object t, Function fn) {
		
		Method m = Stream.of(t.getClass().getMethods())
				.filter(method -> "flatMap".equals(method.getName()) || "bind".equals(method.getName()))
				.filter(method -> method.getParameterCount()==1)
				.findFirst().get();
		
		Class z = m.getParameterTypes()[0];
		ProxyWrapper proxy = getProxy(z);
		((FunctionExecutionInvocationHandler)Proxy.getInvocationHandler(proxy.proxy)).setFunction(input -> fn.apply(input));
		
		

		try {
			return m.invoke(t, proxy.proxy);
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			
			throw new RuntimeException(e);
		}finally{
			release(z,proxy);
		}
	}

	@Override
	public boolean instanceOfT(Object apply) {
		return type.map(t -> apply.getClass().isAssignableFrom(t)).orElse(true);
	}

	@Override
	public Object of(Object o) {
		try {
			return type.get().getMethod("of",o.getClass()).invoke(null,o);
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | NoSuchMethodException
				| SecurityException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Object of() {
		try {
			return type.get().getMethod("of").invoke(null);
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | NoSuchMethodException
				| SecurityException e) {
			throw new RuntimeException(e);
		}
	}
	
	private synchronized <X> ProxyWrapper getProxy(Class<X> type){
		PSet<ProxyWrapper> proxies = removeProxies(type);
		val proxy = proxies.iterator().next();
		val newProxies = proxies.minus(proxy);
		mergeProxies(type,newProxies);
		return proxy;
	}
	private  PSet<ProxyWrapper> removeProxies(Class key){
		val proxies = proxyCache.get(key);
		val proxiesToUse = proxies==null ? HashTreePSet.singleton(new ProxyWrapper((Proxy)Proxy.newProxyInstance(ReflectionComprehender.class.getClassLoader(),
								new Class[]{key},new FunctionExecutionInvocationHandler()))) : proxies; 
		
		if(proxies!=null)
			proxyCache = proxyCache.minus(key);
		return proxiesToUse;
	}
	private void mergeProxies(Class key,PSet<ProxyWrapper> proxies){
		val current = proxyCache.get(key);
		proxyCache.minus(key);
		val newProxies = current==null ? proxies : proxies.plusAll(current);
		proxyCache = proxyCache.plus(key, newProxies);
	}
	
	void release(Class type,ProxyWrapper proxy){
		
		mergeProxies(type,HashTreePSet.singleton(proxy));
	}
}
