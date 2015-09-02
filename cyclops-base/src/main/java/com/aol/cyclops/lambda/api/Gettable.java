package com.aol.cyclops.lambda.api;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

import com.aol.cyclops.invokedynamic.InvokeDynamic;
import com.aol.cyclops.lambda.utils.ExceptionSoftener;

public interface Gettable<T> extends Supplier<T> {
	default Object unwrap(){
		return this;
	}
	default List<String> getSupplierMethodNames(){
		return Arrays.asList("get","call");
	}
	default T get(){
		Object gettable = unwrap();
		if(gettable instanceof Supplier)
			return ((Supplier<T>)gettable).get();
		if(gettable instanceof Callable){
			try {
				return ((Callable<T>)gettable).call();
			} catch (Exception e) {
				ExceptionSoftener.singleton.factory.getInstance().throwSoftenedException(e);
			}
		}
		return  new InvokeDynamic().<T>supplier(gettable,getSupplierMethodNames()).get();
	}
}
