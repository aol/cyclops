package com.aol.cyclops.javaslang.forcomprehensions;

import java.io.BufferedReader;
import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javaslang.Value;

import org.pcollections.ConsPStack;

import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.SequenceM;

public class Do {

	
	
	//${start}
	public static <T1> DoComp1<T1> monad(Value<T1> monad){
		return new DoComp0(ConsPStack.empty()).monad(monad);
	}
	
	public static  DoComp1<Integer> times(int times){
		return new DoComp0(ConsPStack.empty()).times(times);	
	}
	
	
	public static  DoComp1<Character> add(CharSequence seq){
		return new DoComp0(ConsPStack.empty()).add(seq);
	}
	public static <T1> DoComp1<T1> addValues(T1... values){
		return new DoComp0(ConsPStack.empty()).addValues(values);
	}
	/**
	 * Add a Iterable as next nested level in the comprehension
	 * 
	 * 
	 * 
	 * <pre>{@code   Do.add(iterable)
	 				   .filter( -> i1>5)
				  	   .yield( -> );
							
		}</pre>
	 * 
	 * 
	 * @param o Defines next level in comprehension
	 * @return Next stage in for comprehension builder
	 */
	public static <T1> DoComp1<T1> add(Iterable<T1> o){
		return new DoComp0(ConsPStack.empty()).add(o);	
	}
	


	/**
	 * Add a Iterator as next nested level in the comprehension
	 * 
	 * 
	 * 
	 * <pre>{@code   Do.add(iterator)
	 				   .filter( -> i1>5)
				  	   .yield( -> );
							
		}</pre>
	 * 
	 * 
	 * @param o Defines next level in comprehension
	 * @return Next stage in for comprehension builder
	 */
	public static <T1> DoComp1<T1> add(Iterator<T1> o){
		return new DoComp0(ConsPStack.empty()).add(o);		
	}
	


	/**
	 * Add a Stream as next nested level in the comprehension
	 * 
	 * 
	 * 
	 * <pre>{@code   Do.add(stream)
	 				   .filter( -> i1>5)
				  	   .yield( -> );
							
		}</pre>
	 * 
	 * 
	 * @param o Defines next level in comprehension
	 * @return Next stage in for comprehension builder
	 */
	public  static <T1> DoComp1<T1> addStream(Stream<T1> o){
		return new DoComp0(ConsPStack.empty()).addStream(o);		
	}
	
	


	/**
	 * Add a Optional as next nested level in the comprehension
	 * 
	 * 
	 * 
	 * <pre>{@code   Do.add(optional)
	 				   .filter( -> i1>5)
				  	   .yield( -> );
							
		}</pre>
	 * 
	 * 
	 * @param o Defines next level in comprehension
	 * @return Next stage in for comprehension builder
	 */
	public static <T1> DoComp1<T1> add(Optional<T1> o){
		return new DoComp0(ConsPStack.empty()).add(o);	
	}
	


	/**
	 * Add a CompletableFuture as next nested level in the comprehension
	 * 
	 * 
	 * 
	 * <pre>{@code   Do.add(completableFuture)
	 				   .filter( -> i1>5)
				  	   .yield( -> );
							
		}</pre>
	 * 
	 * 
	 * @param o Defines next level in comprehension
	 * @return Next stage in for comprehension builder
	 */
	public static <T1> DoComp1<T1> add(CompletableFuture<T1> o){
		return new DoComp0(ConsPStack.empty()).add(o);	
	}
	


	/**
	 * Add a AnyM as next nested level in the comprehension
	 * 
	 * 
	 * 
	 * <pre>{@code   Do.add(anyM)
	 				   .filter( -> i1>5)
				  	   .yield( -> );
							
		}</pre>
	 * 
	 * 
	 * @param o Defines next level in comprehension
	 * @return Next stage in for comprehension builder
	 */
	public static <T1> DoComp1<T1> add(AnyM<T1> o){
		return new DoComp0(ConsPStack.empty()).add(o);	
	}
	


	/**
	 * Add a TraversableM as next nested level in the comprehension
	 * 
	 * 
	 * 
	 * <pre>{@code   Do.add(traversable)
	 				   .filter( -> i1>5)
				  	   .yield( -> );
							
		}</pre>
	 * 
	 * 
	 * @param o Defines next level in comprehension
	 * @return Next stage in for comprehension builder
	 */
	public static <T1> DoComp1<T1> add(SequenceM<T1> o){
		return new DoComp0(ConsPStack.empty()).add(o);	
	}
	


	/**
	 * Add a Callable as next nested level in the comprehension
	 * 
	 * 
	 * 
	 * <pre>{@code   Do.add(callable)
	 				   .filter( -> i1>5)
				  	   .yield( -> );
							
		}</pre>
	 * 
	 * 
	 * @param o Defines next level in comprehension
	 * @return Next stage in for comprehension builder
	 */
	public static <T1> DoComp1<T1> add(Callable<T1> o){
		return new DoComp0(ConsPStack.empty()).add(o);	
	}
	


	/**
	 * Add a Supplier as next nested level in the comprehension
	 * 
	 * 
	 * 
	 * <pre>{@code   Do
	 				   .filter( -> i1>5)
				  	   .yield( -> );
							
		}</pre>
	 * 
	 * 
	 * @param o Defines next level in comprehension
	 * @return Next stage in for comprehension builder
	 */
	public static <T1> DoComp1<T1> add(Supplier<T1> o){
		return new DoComp0(ConsPStack.empty()).add(o);	
	}
	


	/**
	 * Add a Collection as next nested level in the comprehension
	 * 
	 * 
	 * 
	 * <pre>{@code   Do.add(collection)
	 				   .filter( -> i1>5)
				  	   .yield( -> );
							
		}</pre>
	 * 
	 * 
	 * @param o Defines next level in comprehension
	 * @return Next stage in for comprehension builder
	 */
	public static <T1> DoComp1<T1> add(Collection<T1> o){
		return new DoComp0(ConsPStack.empty()).add(o);	
	}
	


	


	/**
	 * Add a File as next nested level in the comprehension
	 *
	 *
	 *
	 * <pre>{@code   Do.add(file)
						.filter( -> i1>5)
						 .yield( -> );
							
		}</pre>
	 *
	 *
	 * @param o Defines next level in comprehension
	 * @return Next stage in for comprehension builder
	 */
	public static <T1 extends String>  DoComp1<T1> add(File o){
		return new DoComp0(ConsPStack.empty()).add(o);	
	}
	


	/**
	 * Add a URL as next nested level in the comprehension
	 *
	 *
	 *
	 * <pre>{@code   Do.add(url)
						.filter( -> i1>5)
						 .yield( -> );
							
		}</pre>
	 *
	 *
	 * @param o Defines next level in comprehension
	 * @return Next stage in for comprehension builder
	 */
	public static <T1 extends String>  DoComp1<T1> add(URL o){
		return new DoComp0(ConsPStack.empty()).add(o);	
	}
	


	/**
	 * Add a BufferedReader as next nested level in the comprehension
	 *
	 *
	 *
	 * <pre>{@code   Do.add(bufferedReader)
						.filter( -> i1>5)
						 .yield( -> );
							
		}</pre>
	 *
	 *
	 * @param o Defines next level in comprehension
	 * @return Next stage in for comprehension builder
	 */
	public static <T1 extends String>  DoComp1<T1> add(BufferedReader o){
		return new DoComp0(ConsPStack.empty()).add(o);	
	}
	


	


	


	
}
