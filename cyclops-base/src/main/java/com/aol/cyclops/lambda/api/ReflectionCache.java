package com.aol.cyclops.lambda.api;

import static com.aol.cyclops.streams.StreamUtils.reversedStream;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.Value;

import com.nurkiewicz.lazyseq.LazySeq;

public class ReflectionCache {
	private final static Map<Class,List<Field>> fields = new ConcurrentHashMap<>();

	private final static Map<Class,Optional<Method>> unapplyMethods =new ConcurrentHashMap<>();
	
	public static List<Field> getFields(
			Class class1) {
		return getFieldData(class1).stream().collect(Collectors.<Field>toList());
		
	}
	public static Map<String,Field> getFieldMap(
			Class class1) {
		return getFieldData(class1).stream().collect(Collectors.toMap(f->f.getName(),f->f));
		
	}
	private static List<Field> getFieldData(
			Class class1) {
		return fields.computeIfAbsent(class1, cl ->{
			return reversedStream(LazySeq.iterate(class1, c->c.getSuperclass())
						.takeWhile(c->c!=Object.class).toList())
						.flatMap(c->Stream.of(c.getDeclaredFields()))
						.filter(f->!Modifier.isStatic(f.getModifiers()))
						.map(f -> { f.setAccessible(true); return f;})
						.collect(Collectors.toList());
					});
		
	}/**
	@Value
	private static class Two{
		String first;
		Field second;
		public static Two of(Field f){
			return new Two(f.getName(),f);
		}
	}**/
	
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
