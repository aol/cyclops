package com.aol.cyclops.comprehensions;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.pcollections.HashTreePMap;
import org.pcollections.HashTreePSet;
import org.pcollections.PMap;
import org.pcollections.PSet;

import com.aol.cyclops.comprehensions.ComprehensionsModule.ComprehensionData;

import lombok.AllArgsConstructor;
import lombok.val;


class Proxier {
	
	private static volatile PMap<Class,PSet<ProxyWrapper>> proxyCache =  HashTreePMap.empty();
	
	
	void release(Class type,List<Proxy> proxies){
		
		mergeProxies(type,proxies.stream().map(ProxyWrapper::new)
				.map(HashTreePSet::singleton)
				.reduce(HashTreePSet.empty(),(acc,next) -> acc.plusAll(next)));
	}
	
	@SuppressWarnings("unchecked")
	<X> X newProxy(Class<X> type, ComprehensionData compData, ThreadLocal<Map<Class,List>> activeProxyStore){
		
		
		
		val proxy = getProxy(type);
		
		InvocationHandlerProxy handler = (InvocationHandlerProxy)Proxy.getInvocationHandler(proxy.proxy);
		handler.setProxy(proxy.proxy);
		handler.setCompData(compData);
		handler.setActiveProxyStore(activeProxyStore);
		
		val list = activeProxyStore.get().computeIfAbsent(type,c->new ArrayList());
		list.add(proxy.proxy);
	
		return (X)proxy.proxy;
	}
	private <X> X setHandler(X proxy, InvocationHandlerProxy proxyHandler){
		proxyHandler.setProxy(proxy);
		return proxy;
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
