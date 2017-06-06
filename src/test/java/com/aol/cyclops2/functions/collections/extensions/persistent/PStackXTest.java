package com.aol.cyclops2.functions.collections.extensions.persistent;

import com.aol.cyclops2.data.collections.extensions.FluentCollectionX;
import com.aol.cyclops2.functions.collections.extensions.CollectionXTestsWithNulls;
import cyclops.collections.immutable.LinkedListX;
import cyclops.collections.immutable.PersistentSetX;
import cyclops.collections.immutable.VectorX;
import cyclops.collections.mutable.ListX;
import cyclops.stream.ReactiveSeq;
import cyclops.stream.Spouts;
import org.jooq.lambda.tuple.Tuple2;
import org.junit.Test;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

public class PStackXTest extends CollectionXTestsWithNulls{


	@Override
	public <T> FluentCollectionX<T> of(T... values) {
		LinkedListX<T> list = LinkedListX.empty();
		for(T next : values){
			list = list.plus(list.size(),next);
		}
		System.out.println("List " + list);
		return list;
		
	}
    @Test
    public void sliding() {
        ListX<VectorX<Integer>> list = of(1, 2, 3, 4, 5, 6).sliding(2).toListX();

        System.out.println(list);
        assertThat(list.get(0), hasItems(1, 2));
        assertThat(list.get(1), hasItems(2, 3));
    }
	
	@Test
    public void coflatMap(){
       assertThat(LinkedListX.of(1,2,3)
                   .coflatMap(s->s.sumInt(i->i))
                   .singleUnsafe(),equalTo(6));
        
    }
	@Test
    public void onEmptySwitch(){
            assertThat(LinkedListX.empty().onEmptySwitch(()-> LinkedListX.of(1,2,3)),equalTo(LinkedListX.of(1,2,3)));
    }
	/* (non-Javadoc)
	 * @see com.aol.cyclops2.function.collections.extensions.AbstractCollectionXTest#empty()
	 */
	@Override
	public <T> FluentCollectionX<T> empty() {
		return LinkedListX.empty();
	}
	
	@Test
	public void pVectorX(){
	    


		ReactiveSeq<String> seq = Spouts.from(VectorX.of(1, 2, 3, 4)
				.plus(5)
				.map(i -> "connect toNested Akka, RxJava and more with reactive-streams" + i));
	    
	   PersistentSetX<String> setX =  seq.to().futureStream()
	                                   .map(data->"fan out across threads with futureStreams" + data)
	                                   .to().persistentSetX();
	    
	                        
	                             
	    
	    
	}
	
	@Test
	public void remove(){
	    /**
	    LinkedListX.of(1,2,3)
	            .minusAll(PBagX.of(2,3))
                .flatMapP(i->Flux.just(10+i,20+i,30+i));

	    **/
	}
	
	 @Override
	    public FluentCollectionX<Integer> range(int start, int end) {
	        return LinkedListX.range(start, end);
	    }
	    @Override
	    public FluentCollectionX<Long> rangeLong(long start, long end) {
	        return LinkedListX.rangeLong(start, end);
	    }
	    @Override
	    public <T> FluentCollectionX<T> iterate(int times, T seed, UnaryOperator<T> fn) {
	       return LinkedListX.iterate(times, seed, fn);
	    }
	    @Override
	    public <T> FluentCollectionX<T> generate(int times,  Supplier<T> fn) {
	       return LinkedListX.generate(times, fn);
	    }
	    @Override
	    public <U, T> FluentCollectionX<T> unfold(U seed, Function<? super U, Optional<Tuple2<T, U>>> unfolder) {
	       return LinkedListX.unfold(seed, unfolder);
	    }
}
