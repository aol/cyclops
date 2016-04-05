
package com.aol.cyclops.internal.comprehensions.donotation;
import com.aol.cyclops.control.Reader;

import com.aol.cyclops.internal.comprehensions.donotation.DoBuilderModule.Assignment;
import com.aol.cyclops.internal.comprehensions.donotation.DoBuilderModule.Entry;
import com.aol.cyclops.internal.comprehensions.donotation.DoBuilderModule.Guard;

import java.io.BufferedReader;
import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.BaseStream;
import java.util.stream.Stream;

import org.pcollections.PStack;
import org.reactivestreams.Publisher;

import com.aol.cyclops.internal.monads.MonadWrapper;
import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.types.anyM.*;
import com.aol.cyclops.control.ReactiveSeq;
	
public class DoComp1<T1> extends DoComp{
		public DoComp1(PStack<Entry> assigned, Class orgType) {
			super(assigned,orgType);
			
		}

	
		
		public <T2> DoComp2<T1,T2> reader(Function<? super T1,Reader<?,? extends T2>> f){
			return new DoComp2<>(addToAssigned(f),getOrgType());
			
		}

		
		public <T2> DoComp2<T1,T2> iterable(Function<? super T1,Iterable<T2>> f){
			return new DoComp2<>(addToAssigned(f),getOrgType());
			
		}
		public <T2> DoComp2<T1,T2> publisher(Function<? super T1,Publisher<T2>> f){
            return new DoComp2<>(addToAssigned(f),getOrgType());   
        }
		
		

		
		
		public <T2> DoComp2<T1,T2> stream(Function<? super T1,BaseStream<T2,?>> f){
			return new DoComp2<>(addToAssigned(f),getOrgType());
			
		}


		
		public <T2> DoComp2<T1,T2> optional(Function<? super T1,Optional<T2>> f){
			return new DoComp2<>(addToAssigned(f),getOrgType());
			
		}
		


		
		public <T2> DoComp2<T1,T2> future(Function<? super T1,CompletableFuture<T2>> f){
			return new DoComp2<>(addToAssigned(f),getOrgType());
			
		}
		


		
		public <T2> DoComp2<T1,T2> anyM(Function<? super T1,AnyM<T2>> f){
			return new DoComp2<>(addToAssigned(f),getOrgType());
			
		}
		


		
		/**
		 * Execute and Yield a result from this for comprehension using the supplied function
		 * 
		 * e.g. sum every element across nested structures
		 * 
		 * <pre>{@code   Do.iterable(list1)
					  	   .yield(i -> i+10);
								
			}</pre>
		 * 
		 * 
		 * @param f To be applied to every element in the for comprehension
		 * @return For comprehension result
		 */
		public <R> AnyMSeq<R> yield(Function<? super T1,? extends R> f){
			if(getOrgType()!=null)
				return new MonadWrapper<>(this.yieldInternal(f),this.getOrgType()).anyMSeq();
			else
				return AnyM.ofSeq(this.yieldInternal(f));
		}
		
		
		/**
		 * Filter data
		 * 
		 * 
		 * 
		 * <pre>{@code   Do.iterable(list1)
		 				   .filter(i -> i>5)
					  	   .yield(i-> i+10);
								
			}</pre>
		 * 
		 * 
		 * @param f To be applied to every element in the for comprehension
		 * @return Current stage  guard / filter applied
		 */
		public  DoComp1<T1> filter(Predicate<? super T1> f){
            return new DoComp1<>(getAssigned().plus(getAssigned().size(),new Entry("$$internalGUARD"+getAssigned().size(),new Guard(f))),getOrgType());
        }
		
		
		
       
		
	}

