
package com.aol.cyclops.internal.comprehensions.donotation;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.BaseStream;

import org.pcollections.PStack;
import org.reactivestreams.Publisher;

import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.Reader;
import com.aol.cyclops.control.Reader;
import com.aol.cyclops.internal.comprehensions.donotation.DoBuilderModule.Entry;
import com.aol.cyclops.internal.comprehensions.donotation.DoBuilderModule.Guard;
import com.aol.cyclops.internal.monads.MonadWrapper;
import com.aol.cyclops.types.anyM.AnyMSeq;
import com.aol.cyclops.util.function.CurryVariance;
import com.aol.cyclops.util.function.QuadFunction;

public class DoComp4<T1,T2,T3,T4> extends DoComp{
		public DoComp4(PStack<Entry> assigned, Class orgType) {
			super(assigned,orgType);
			
		}
	
		public <T5> DoComp5<T1,T2,T3,T4,T5> reader(Function<? super T1,Function<? super T2,Function<? super T3,Function<? super T4,Reader<?,? extends T5>>>>> f){
			return new DoComp5<>(addToAssigned(f),getOrgType());
			
		}

		
		public <T5> DoComp5<T1,T2,T3,T4,T5> iterable(Function<? super T1,Function<? super T2,Function<? super T3,Function<? super T4,Iterable<T5>>>>> f){
			return new DoComp5<>(addToAssigned(f),getOrgType());
		}
		public <T5> DoComp5<T1,T2,T3,T4,T5> publisher(Function<? super T1,Function<? super T2,Function<? super T3,Function<? super T4,Publisher<T5>>>>> f){
            return new DoComp5<>(addToAssigned(f),getOrgType());
        }
		

		public <T5> DoComp5<T1,T2,T3,T4,T5> stream(Function<? super T1,Function<? super T2,Function<? super T3,Function<? super T4,BaseStream<T5,?>>>>> f){
			return new DoComp5<>(addToAssigned(f),getOrgType());
			
		}
		

		public <T5> DoComp5<T1,T2,T3,T4,T5> optional(Function<? super T1,Function<? super T2,Function<? super T3,Function<? super T4,Optional<T5>>>>> f){
			return new DoComp5<>(addToAssigned(f),getOrgType());
			
		}
		


		public <T5> DoComp5<T1,T2,T3,T4,T5> future(Function<? super T1,Function<? super T2,Function<? super T3,Function<? super T4,CompletableFuture<T5>>>>> f){
			return new DoComp5<>(addToAssigned(f),getOrgType());
			
		}
		


		public <T5> DoComp5<T1,T2,T3,T4,T5> anyM(Function<? super T1,Function<? super T2,Function<? super T3,Function<? super T4,AnyM<T5>>>>> f){
			return new DoComp5<>(addToAssigned(f),getOrgType());
			
		}
		

		
		public <R> AnyMSeq<R> yield(Function<? super T1,Function<? super T2,Function<? super T3,Function<? super T4,? extends R>>>> f){
			if(getOrgType()!=null)
				return new MonadWrapper<>(this.yieldInternal(f),this.getOrgType()).anyMSeq();
			else
				return AnyM.ofSeq(this.yieldInternal(f));
		}
		public <R> AnyMSeq<R> yield4(QuadFunction<? super T1,? super T2,? super T3,? super T4,? extends R> f){
           return this.yield(CurryVariance.curry4(f));
        }
		
		
		public  DoComp4<T1,T2,T3,T4> filter(Function<? super T1,Function<? super T2,Function<? super T3,Function<? super T4,Boolean>>>> f){
			return new DoComp4<>(getAssigned().plus(getAssigned().size(),new Entry("$$internalGUARD"+getAssigned().size(),new Guard(f))),getOrgType());
		}
		
	}

