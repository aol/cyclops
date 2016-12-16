package com.aol.cyclops.functions.collections.extensions.persistent;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import org.jooq.lambda.tuple.Tuple2;
import org.junit.Test;

import com.aol.cyclops.control.ReactiveSeq;
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
	
	@Test
    public void coflatMap(){
       assertThat(PStackX.of(1,2,3)
                   .coflatMap(s->s.sumInt(i->i))
                   .single(),equalTo(6));
        
    }
	@Test
    public void onEmptySwitch(){
            assertThat(PStackX.empty().onEmptySwitch(()->PStackX.of(1,2,3)),equalTo(PStackX.of(1,2,3)));
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
	
	 @Override
	    public FluentCollectionX<Integer> range(int start, int end) {
	        return PStackX.range(start, end);
	    }
	    @Override
	    public FluentCollectionX<Long> rangeLong(long start, long end) {
	        return PStackX.rangeLong(start, end);
	    }
	    @Override
	    public <T> FluentCollectionX<T> iterate(int times, T seed, UnaryOperator<T> fn) {
	       return PStackX.iterate(times, seed, fn);
	    }
	    @Override
	    public <T> FluentCollectionX<T> generate(int times,  Supplier<T> fn) {
	       return PStackX.generate(times, fn);
	    }
	    @Override
	    public <U, T> FluentCollectionX<T> unfold(U seed, Function<? super U, Optional<Tuple2<T, U>>> unfolder) {
	       return PStackX.unfold(seed, unfolder);
	    }
}
