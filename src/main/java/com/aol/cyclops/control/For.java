package com.aol.cyclops.control;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.BaseStream;

import org.pcollections.ConsPStack;
import org.reactivestreams.Publisher;

import com.aol.cyclops.internal.comprehensions.donotation.DoComp0;
import com.aol.cyclops.internal.comprehensions.donotation.DoComp1;

public class For {
    
    public static <T1> DoComp1<T1> reader(Reader<?,T1> reader){
        return new DoComp0(ConsPStack.empty()).reader(reader);
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
	public static <T1> DoComp1<T1> iterable(Iterable<T1> o){
		return new DoComp0(ConsPStack.empty()).iterable(o);	
	}
	public static <T1> DoComp1<T1> publisher(Publisher<T1> o){
        return new DoComp0(ConsPStack.empty()).publisher(o); 
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
	public  static <T1> DoComp1<T1> stream(BaseStream<T1,?> o){
		return new DoComp0(ConsPStack.empty()).stream(o);		
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
	public static <T1> DoComp1<T1> optional(Optional<T1> o){
		return new DoComp0(ConsPStack.empty()).optional(o);	
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
	public static <T1> DoComp1<T1> future(CompletableFuture<T1> o){
		return new DoComp0(ConsPStack.empty()).future(o);	
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
	public static <T1> DoComp1<T1> anyM(AnyM<T1> o){
		return new DoComp0(ConsPStack.empty()).anyM(o);	
	}
	


	

	
}
