package com.aol.cyclops.comprehensions;

import java.lang.reflect.Proxy;

import lombok.AllArgsConstructor;
import lombok.val;

import org.pcollections.HashTreePMap;
import org.pcollections.HashTreePSet;
import org.pcollections.PMap;
import org.pcollections.PSet;


class Proxier {
	
	private static volatile PMap<Class,PSet<ProxyWrapper>> proxyCache =  HashTreePMap.empty();
	
	
	void release(Class type,Object proxy){
		mergeProxies(type,HashTreePSet.singleton(new ProxyWrapper((Proxy)proxy)));
	}
	
	@SuppressWarnings("unchecked")
	<X> X newProxy(Class<X> type, ComprehensionData compData){
		
		
		
		val proxies = removeProxies(type);
		val proxy = proxies.iterator().next();
		val newProxies = proxies.minus(proxy);
		mergeProxies(type,newProxies);
		
		InvocationHandlerProxy handler = (InvocationHandlerProxy)Proxy.getInvocationHandler(proxy.proxy);
		handler.setProxy(proxy.proxy);
		handler.setCompData(compData);
		
	
		return (X)proxy.proxy;
	}
	private <X> X setHandler(X proxy, InvocationHandlerProxy proxyHandler){
		proxyHandler.setProxy(proxy);
		return proxy;
	}
	private synchronized PSet<ProxyWrapper> removeProxies(Class key){
		val proxies = proxyCache.get(key);
		val proxiesToUse = proxies==null ? HashTreePSet.singleton(newProxyInstance(key,new InvocationHandlerProxy(key))) : proxies; 
		
		if(proxies!=null)
			proxyCache = proxyCache.minus(key);
		return proxiesToUse;
	}
	private synchronized void mergeProxies(Class key,PSet<ProxyWrapper> proxies){
		val current = proxyCache.get(key);
		proxyCache.minus(key);
		val newProxies = current==null ? proxies : proxies.plusAll(current);
		proxyCache = proxyCache.plus(key, newProxies);
	}
	
	private ProxyWrapper newProxyInstance(Class type,InvocationHandlerProxy proxyHandler){
		
		return new ProxyWrapper((Proxy)Proxy.newProxyInstance(type
				.getClassLoader(), new Class[]{type}, proxyHandler));
	}
	
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
	
}
