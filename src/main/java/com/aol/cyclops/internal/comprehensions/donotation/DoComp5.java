
package com.aol.cyclops.internal.comprehensions.donotation;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.BaseStream;

import org.pcollections.PStack;
import org.reactivestreams.Publisher;

import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.Reader;
import com.aol.cyclops.internal.comprehensions.donotation.DoBuilderModule.Entry;
import com.aol.cyclops.internal.comprehensions.donotation.DoBuilderModule.Guard;
import com.aol.cyclops.internal.monads.MonadWrapper;
import com.aol.cyclops.types.anyM.AnyMSeq;
import com.aol.cyclops.util.function.CurryVariance;
import com.aol.cyclops.util.function.QuintFunction;
public class DoComp5<T1,T2,T3,T4,T5> extends DoComp{
		public DoComp5(PStack<Entry> assigned, Class orgType) {
			super(assigned,orgType);
			
		}
		

		public <T6> DoComp6<T1,T2,T3,T4,T5,T6> reader(Function<? super T1,Function<? super T2,Function<? super T3,Function<T4,Function<? super T5,Reader<?,? super T6>>>>>> f){
			return new DoComp6<>(addToAssigned(f),getOrgType());
			
		}
		
		public <T6> DoComp6<T1,T2,T3,T4,T5,T6> iterable(Function<? super T1,Function<? super T2,Function<? super T3,Function<T4,Function<? super T5,Iterable<T6>>>>>> f){
			return new DoComp6<>(addToAssigned(f),getOrgType());
			
		}
		public <T6> DoComp6<T1,T2,T3,T4,T5,T6> publisher(Function<? super T1,Function<? super T2,Function<? super T3,Function<T4,Function<? super T5,Publisher<T6>>>>>> f){
            return new DoComp6<>(addToAssigned(f),getOrgType());
            
        }
		

		public <T6> DoComp6<T1,T2,T3,T4,T5,T6> stream(Function<? super T1,Function<? super T2,Function<? super T3,Function<T4,Function<? super T5,BaseStream<T6,?>>>>>> f){
			return new DoComp6<>(addToAssigned(f),getOrgType());
			
		}
		


		public <T6> DoComp6<T1,T2,T3,T4,T5,T6> optional(Function<? super T1,Function<? super T2,Function<? super T3,Function<T4,Function<? super T5,Optional<T6>>>>>> f){
			return new DoComp6<>(addToAssigned(f),getOrgType());
			
		}
		

		public <T6> DoComp6<T1,T2,T3,T4,T5,T6> future(Function<? super T1,Function<? super T2,Function<? super T3,Function<T4,Function<? super T5,CompletableFuture<T6>>>>>> f){
			return new DoComp6<>(addToAssigned(f),getOrgType());
			
		}
		


		public <T6> DoComp6<T1,T2,T3,T4,T5,T6> anyM(Function<? super T1,Function<? super T2,Function<? super T3,Function<T4,Function<? super T5,AnyM<T6>>>>>> f){
			return new DoComp6<>(addToAssigned(f),getOrgType());
			
		}
		

	
		public <R> AnyMSeq<R> yield(Function<? super T1,Function<? super T2,Function<? super T3,Function<? super T4,Function<? super T5,? extends R>>>>> f){
			if(getOrgType()!=null)
				return new MonadWrapper<>(this.yieldInternal(f),this.getOrgType()).anyMSeq();
			else
				return AnyM.ofSeq(this.yieldInternal(f));
		}
		public <R> AnyMSeq<R> yield(QuintFunction<? super T1,? super T2,? super T3,? super T4,? super T5,? extends R> f){
            return this.yield(CurryVariance.curry5(f));
        }
		
		
		public  DoComp5<T1,T2,T3,T4,T5> filter(Function<? super T1,Function<? super T2,Function<? super T3,Function<T4,Function<? super T5,Boolean>>>>> f){
			return new DoComp5<>(getAssigned().plus(getAssigned().size(),new Entry("$$internalGUARD"+getAssigned().size(),new Guard(f))),getOrgType());
		}
		
	}

