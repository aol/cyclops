package com.aol.cyclops.matcher;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class ReflectionCache {
	private final static Map<Class,List<Field>> fields = new ConcurrentHashMap<>();

	public static List<Field> getField(
			Class<? extends Decomposable> class1) {
		return fields.computeIfAbsent(class1, cl ->{
			return Stream.of(class1.getDeclaredFields()).peek(f -> f.setAccessible(true)).collect(Collectors.toList());
					});
		
		
	}
}
