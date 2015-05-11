package com.aol.cyclops.lambda.api;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class ReflectionCache {
	private final static Map<Class,List<Field>> fields = new ConcurrentHashMap<>();

	private final static Map<Class,Optional<Method>> unapplyMethods =new ConcurrentHashMap<>();
	public static List<Field> getField(
			Class class1) {
		return fields.computeIfAbsent(class1, cl ->{
			return Stream.of(class1.getDeclaredFields()).filter(f->!Modifier.isStatic(f.getModifiers())).peek(f -> f.setAccessible(true)).collect(Collectors.toList());
					});
		
	}
	
	public static Optional<Method> getUnapplyMethod(Class c) {
	
			return unapplyMethods.computeIfAbsent(c, cl -> {
				try{
					return Optional.of(cl.getMethod("unapply"));
				}catch(NoSuchMethodException e){
					return Optional.empty();
				}
			});	
		
	}
	
	
}
