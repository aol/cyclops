package com.aol.cyclops.lambda.api;

import static com.aol.cyclops.streams.ReversedIterator.reversedStream;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.aol.cyclops.streams.ReversedIterator;
import com.nurkiewicz.lazyseq.LazySeq;

public class ReflectionCache {
	private final static Map<Class,List<Field>> fields = new ConcurrentHashMap<>();

	private final static Map<Class,Optional<Method>> unapplyMethods =new ConcurrentHashMap<>();
	public static List<Field> getField(
			Class class1) {
		return fields.computeIfAbsent(class1, cl ->{
			return reversedStream(LazySeq.iterate(class1, c->c.getSuperclass())
						.takeWhile(c->c!=Object.class).toList())
						.flatMap(c->Stream.of(c.getDeclaredFields()))
						.filter(f->!Modifier.isStatic(f.getModifiers()))
						.map(f -> { f.setAccessible(true); return f;})
						.collect(Collectors.toList());
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
