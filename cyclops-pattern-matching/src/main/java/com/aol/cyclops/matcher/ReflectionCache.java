package com.aol.cyclops.matcher;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jooq.lambda.Unchecked;
import org.jooq.lambda.UncheckedException;

class ReflectionCache {
	private final static Map<Class,List<Field>> fields = new ConcurrentHashMap<>();

	private final static Map<Class,Optional<Method>> unapplyMethods =new ConcurrentHashMap<>();
	public static List<Field> getField(
			Class<? extends Decomposable> class1) {
		return fields.computeIfAbsent(class1, cl ->{
			return Stream.of(class1.getDeclaredFields()).peek(f -> f.setAccessible(true)).collect(Collectors.toList());
					});
		
	}
	
	public static Optional<Method> getUnapplyMethod(Class c) {
	
			return unapplyMethods.computeIfAbsent(c, Unchecked.function(cl -> {
				try{
					return Optional.of(cl.getMethod("unapply"));
				}catch(NoSuchMethodException e){
					return Optional.empty();
				}
			}));	
		
	}
}
