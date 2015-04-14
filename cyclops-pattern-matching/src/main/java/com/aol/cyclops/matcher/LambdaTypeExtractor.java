package com.aol.cyclops.matcher;



import java.io.Serializable;
import java.lang.invoke.MethodType;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.stream.Stream;

import net.jodah.typetools.TypeResolver;

public class LambdaTypeExtractor {
	private static final ExceptionSoftener softener = ExceptionSoftener.singleton.factory.getInstance();
	
	public static MethodType extractType(Serializable serializable){
		
		try{
			return extractChecked(serializable);
		}catch(Exception e){
			softener.throwSoftenedException(e);
			return null;
		}
	}

	private static MethodType extractChecked(Serializable serializable) throws IllegalArgumentException, TypeNotPresentException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, SecurityException{
	 Method method = serializable.getClass().getDeclaredMethod("writeReplace");
     method.setAccessible(true);
     MethodType type = MethodType.fromMethodDescriptorString( ((SerializedLambda) method.invoke(serializable)).getImplMethodSignature(),
    		 serializable.getClass().getClassLoader());
     if(type.parameterCount()==0) {
		Class[] classes = TypeResolver.resolveRawArguments(serializable.getClass().getSuperclass(), serializable.getClass());
		 System.out.println("type resolver" + classes.length);
		 Stream.of(classes).forEach(System.out::println);
	 }
		return type;
	}
}
