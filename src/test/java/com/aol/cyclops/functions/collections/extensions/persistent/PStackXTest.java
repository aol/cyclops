package com.aol.cyclops.functions.collections.extensions.persistent;

import org.junit.Test;

import com.aol.cyclops.data.collections.extensions.FluentCollectionX;
import com.aol.cyclops.data.collections.extensions.persistent.PBagX;
import com.aol.cyclops.data.collections.extensions.persistent.PSetX;
import com.aol.cyclops.data.collections.extensions.persistent.PStackX;
import com.aol.cyclops.data.collections.extensions.persistent.PVectorX;
import com.aol.cyclops.functions.collections.extensions.CollectionXTestsWithNulls;
import com.aol.cyclops.types.stream.reactive.SeqSubscriber;

import reactor.core.publisher.Flux;

public class PStackXTest extends CollectionXTestsWithNulls{

	@Override
	public <T> FluentCollectionX<T> of(T... values) {
		PStackX<T> list = PStackX.empty();
		for(T next : values){
			list = list.plus(list.size(),next);
		}
		System.out.println("List " + list);
		return list.efficientOpsOff();
		
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.functions.collections.extensions.AbstractCollectionXTest#empty()
	 */
	@Override
	public <T> FluentCollectionX<T> empty() {
		return PStackX.empty();
	}
	
	@Test
	public void pVectorX(){
	    
	    SeqSubscriber<String> subscriber = SeqSubscriber.subscriber();
	    
	    PVectorX.of(1,2,3,4)
	            .plus(5)
	            .map(i->"connect to Akka, RxJava and more with reactive-streams"+i)
	            .subscribe(subscriber);
	    
	   PSetX<String> setX =  subscriber.toFutureStream()
	                                   .map(data->"fan out across threads with futureStreams" + data)
	                                   .toPSetX();
	    
	                        
	                             
	    
	    
	}
	
	@Test
	public void remove(){
	    
	    PStackX.of(1,2,3)
	            .minusAll(PBagX.of(2,3))
                .flatMapPublisher(i->Flux.just(10+i,20+i,30+i));

	    
	}
}
