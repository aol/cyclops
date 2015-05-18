package com.aol.cyclops.lambda.api;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.aol.cyclops.lambda.utils.ExceptionSoftener;

public interface Mappable {
	default Object unwrap(){
		return this;
	}
	default Map<String,?> toMap(){
		try {
			final Object o = unwrap().getClass();
			return ReflectionCache.getFields(o.getClass())
					.stream()
					.collect(Collectors.toMap((Field f)->f.getName(),(Field f) ->{
						try {

							return f.get(o);
						} catch (Exception e) {
							ExceptionSoftener.singleton.factory.getInstance().throwSoftenedException(e);
							return null;
						}
					}));
		} catch (Exception e) {
			ExceptionSoftener.singleton.factory.getInstance()
					.throwSoftenedException(e);
			return null;
		}
	}
}
