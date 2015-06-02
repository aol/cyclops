package com.aol.cyclops.comprehensions.comprehenders;

import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import lombok.AllArgsConstructor;
import lombok.val;

import org.pcollections.HashTreePMap;
import org.pcollections.HashTreePSet;
import org.pcollections.PMap;
import org.pcollections.PSet;

import com.aol.cyclops.lambda.api.Comprehender;
import com.aol.cyclops.lambda.api.InvokeDynamic;
import com.aol.cyclops.lambda.utils.ExceptionSoftener;

@AllArgsConstructor
public class InvokeDynamicComprehender implements Comprehender {
	public Class getTargetClass(){
		return null;
	}
	Optional<Class> type;
	
	private static volatile PMap<Class,PSet<ProxyWrapper>> proxyCache =  HashTreePMap.empty();
	private static volatile Map<Method,CallSite> callSites = new ConcurrentHashMap<>();
	private static volatile Map<Class,Method> mapMethod = new ConcurrentHashMap<>();
	private static volatile Map<Class,Method> flatMapMethod = new ConcurrentHashMap<>();
	private static volatile Map<Class,Method> filterMethod = new ConcurrentHashMap<>();
	private static volatile Map<Class,Method> ofMethod = new ConcurrentHashMap<>();
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
		Class clazz = t.getClass();
		Method m = filterMethod.computeIfAbsent(clazz, c->Stream.of(c.getMethods())
				.filter(method -> "filter".equals(method.getName()))
				.filter(method -> method.getParameterCount()==1).findFirst()
				.map(m2->{ m2.setAccessible(true); return m2;})
				.get());
		
		Class z = m.getParameterTypes()[0];
		ProxyWrapper proxy = getProxy(z);
		((FunctionExecutionInvocationHandler)Proxy.getInvocationHandler(proxy.proxy)).setFunction(input -> p.test(input));
		

		return executeMethod(t, m, z, proxy);

	}

	@Override
	public Object map(Object t, Function fn) {
		
		Class clazz = t.getClass();
	
		
		Method m = mapMethod.computeIfAbsent(clazz, c->Stream.of(c.getMethods())
				.filter(method -> "map".equals(method.getName()))
				.filter(method -> method.getParameterCount()==1).findFirst()
				.map(m2->{ m2.setAccessible(true); return m2;})
				.get());
		
		Class z = m.getParameterTypes()[0];
		ProxyWrapper proxy = getProxy(z);
		((FunctionExecutionInvocationHandler)Proxy.getInvocationHandler(proxy.proxy)).setFunction(input -> fn.apply(input));
		

		
		
		return executeMethod(t, m, z, proxy);
		
		
	}
	
	private Object executeMethod(Object t, Method m, Class z, ProxyWrapper proxy) {
		try {
			
			
			

			return this.callSites.computeIfAbsent(m, (m2) ->  {
				try {
					return new ConstantCallSite(MethodHandles.publicLookup().unreflect(m2));
				} catch (Exception e) {
					ExceptionSoftener.singleton.factory.getInstance().throwSoftenedException(e);
				}
				return null;
			}).dynamicInvoker().invoke(t,proxy.proxy);
		
		} catch (Throwable e) {
			ExceptionSoftener.singleton.factory.getInstance().throwSoftenedException(e);
		}finally{
			release(z,proxy);
		}
		return null;
	}


	@Override
	public Object flatMap(Object t, Function fn) {
		Class clazz = t.getClass();
		Method m = flatMapMethod.computeIfAbsent(clazz, c->Stream.of(c.getMethods())
				.filter(method -> "flatMap".equals(method.getName()) || "bind".equals(method.getName()))
				.filter(method -> method.getParameterCount()==1).findFirst()
				.map(m2->{ m2.setAccessible(true); return m2;})
				.get());
		
		
		Class z = m.getParameterTypes()[0];
		ProxyWrapper proxy = getProxy(z);
		((FunctionExecutionInvocationHandler)Proxy.getInvocationHandler(proxy.proxy)).setFunction(input -> fn.apply(input));
		
		

		return executeMethod(t, m, z, proxy);
	}

	private boolean isAssignableFrom(Class t,Object apply){
		if(apply.getClass().isAssignableFrom(t))
			return true;
		
		if(apply.getClass().getSuperclass()==Object.class)
			return false;
		
		return apply.getClass().getSuperclass().isAssignableFrom(t);
	}
	@Override
	public boolean instanceOfT(Object apply) {
		return type.map(t -> isAssignableFrom(t,apply)).orElse(true);
	}

	@Override
	public Object of(Object o) {
		
		InvokeDynamic dyn = new InvokeDynamic();
		return dyn.execute(Arrays.asList("of","singleton","some","right","success"),type,o);

	}

	@Override
	public Object empty() {
		
		InvokeDynamic dyn = new InvokeDynamic();
		return dyn.execute(Arrays.asList("of","empty","cons","none","nil","left","failure"),type);
		
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
		val proxiesToUse = proxies==null ? HashTreePSet.singleton(new ProxyWrapper((Proxy)Proxy.newProxyInstance(InvokeDynamicComprehender.class.getClassLoader(),
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
