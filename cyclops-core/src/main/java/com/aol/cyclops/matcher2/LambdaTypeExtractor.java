package com.aol.cyclops.matcher2;



import java.io.Serializable;
import java.lang.invoke.MethodType;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.aol.cyclops.control.ExceptionSoftener;

/**
 * Extract generic type info from Lambda expressions
 * 
 * @see <a href='http://stackoverflow.com/questions/21887358/reflection-type-inference-on-java-8-lambdas'>Discussion on stackoverflow</a>
 * Serialisation approach seems to work more often than ConstantPool approach.
 * 
 * Does not work for MethodReferences.
 * 
 * @author johnmcclean
 *
 */
class LambdaTypeExtractor {
	
	/**
	 * Extract generic type info from a Serializable Lambda expression
	 * 
	 * @param serializable Serializable lambda expression
	 * @return MethodType info
	 */
	public static MethodType extractType(Serializable serializable){
		
		try{
			return extractChecked(serializable);
		}catch(Exception e){
			ExceptionSoftener.throwSoftenedException(e);
			return null;
		}
	}

	private static MethodType extractChecked(Serializable serializable) throws IllegalArgumentException, TypeNotPresentException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, SecurityException{
	 Method method = serializable.getClass().getDeclaredMethod("writeReplace");
     method.setAccessible(true);
     MethodType type = MethodType.fromMethodDescriptorString( ((SerializedLambda) method.invoke(serializable)).getImplMethodSignature(),
    		 serializable.getClass().getClassLoader());
     
     return type;
	}
}
