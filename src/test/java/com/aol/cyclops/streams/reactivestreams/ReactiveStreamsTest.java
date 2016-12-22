package com.aol.cyclops.streams.reactivestreams;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;

import org.junit.Test;

import cyclops.async.LazyReact;
import cyclops.stream.ReactiveSeq;
import cyclops.async.SimpleReact;
import cyclops.stream.Streamable;
import cyclops.collections.immutable.PBagX;
import cyclops.collections.immutable.POrderedSetX;
import cyclops.collections.immutable.PSetX;
import cyclops.collections.immutable.PStackX;
import cyclops.collections.immutable.PVectorX;
import cyclops.collections.DequeX;
import cyclops.collections.ListX;
import cyclops.collections.QueueX;
import cyclops.collections.SetX;
import cyclops.collections.SortedSetX;
import com.aol.cyclops.types.stream.reactive.SeqSubscriber;

import reactor.core.publisher.Flux;

public class ReactiveStreamsTest {

    @Test
    public void subscribeToEmpty(){
        SeqSubscriber<Integer> sub = ReactiveSeq.subscriber();
        ReactiveSeq.<Integer>empty().subscribe(sub);
        
        sub.forEach(System.out::println);
        
    }
    @Test
    public void subscribeToFlux(){
        SeqSubscriber<Integer> sub = ReactiveSeq.subscriber();
        Flux.just(1,2,3).subscribe(sub);
        assertThat(sub.stream().toList(),equalTo(
                Arrays.asList(1,2,3)));
    }
    @Test
    public void fromFluxReactiveSeq(){
        assertThat( ReactiveSeq.fromPublisher(Flux.just(1,2,3)).toList(),equalTo(
                Arrays.asList(1,2,3)));
    }
    @Test
    public void fromFluxListX(){
        assertThat( ListX.fromPublisher(Flux.just(1,2,3)).toList(),equalTo(
                Arrays.asList(1,2,3)));
    }
    @Test
    public void fromFluxSetX(){
        assertThat( SetX.fromPublisher(Flux.just(1,2,3)).toList(),equalTo(
                Arrays.asList(1,2,3)));
    }
    @Test
    public void fromFluxQueueX(){
        assertThat( QueueX.fromPublisher(Flux.just(1,2,3)).toList(),equalTo(
                Arrays.asList(1,2,3)));
    }
    @Test
    public void fromFluxDequeX(){
        assertThat( DequeX.fromPublisher(Flux.just(1,2,3)).toList(),equalTo(
                Arrays.asList(1,2,3)));
    }
    @Test
    public void fromFluxSortedSetX(){
        assertThat( SortedSetX.fromPublisher(Flux.just(1,2,3)).toList(),equalTo(
                Arrays.asList(1,2,3)));
    }
    @Test
    public void fromFluxPSetX(){
        assertThat( PSetX.fromPublisher(Flux.just(1,2,3)).toList(),equalTo(
                Arrays.asList(1,2,3)));
    }
    @Test
    public void fromFluxPOrderedSetX(){
        assertThat( POrderedSetX.fromPublisher(Flux.just(1,2,3)).toList(),equalTo(
                Arrays.asList(1,2,3)));
    }
    @Test
    public void fromFluxPStackX(){
        assertThat( PStackX.fromPublisher(Flux.just(1,2,3)).toList(),equalTo(
                Arrays.asList(3,2,1)));
    }
    @Test
    public void fromFluxPVectorX(){
        assertThat( PVectorX.fromPublisher(Flux.just(1,2,3)).toList(),equalTo(
                Arrays.asList(1,2,3)));
    }
    @Test
    public void fromFluxPBagX(){
        assertThat( PBagX.fromPublisher(Flux.just(1,2,3)).toList(),equalTo(
                Arrays.asList(1,2,3)));
    }
    @Test
    public void fromFluxStreamableX(){
        assertThat( Streamable.fromPublisher(Flux.just(1,2,3)).toList(),equalTo(
                Arrays.asList(1,2,3)));
    }
    @Test
    public void fromFluxLazyFutureStream(){
        assertThat( new LazyReact().fromPublisher(Flux.just(1,2,3)).toList(),equalTo(
                Arrays.asList(1,2,3)));
    }
    @Test
    public void fromFluxSimpleReactStream(){
        assertThat( new SimpleReact().fromPublisher(Flux.just(1,2,3)).block(),equalTo(
                Arrays.asList(1,2,3)));
    }
	@Test
	public void publishAndSubscribe(){
		SeqSubscriber<Integer> sub = ReactiveSeq.subscriber();
		ReactiveSeq.of(1,2,3).subscribe(sub);
		assertThat(sub.stream().toList(),equalTo(
				Arrays.asList(1,2,3)));
	}
	@Test
	public void publishAndSubscribeEmpty(){
		SeqSubscriber<Integer> sub = ReactiveSeq.subscriber();
		ReactiveSeq.<Integer>of().subscribe(sub);
		assertThat(sub.stream().toList(),equalTo(
				Arrays.asList()));
	}
	@Test
    public void subscribeToFluxIterator(){
        SeqSubscriber<Integer> sub = ReactiveSeq.subscriber();
        Flux.just(1,2,3).subscribe(sub);
        assertThat(ReactiveSeq.fromIterator(sub.iterator()).toList(),equalTo(
                Arrays.asList(1,2,3)));
    }
    @Test
    public void publishAndSubscribeIterator(){
        SeqSubscriber<Integer> sub = ReactiveSeq.subscriber();
        ReactiveSeq.of(1,2,3).subscribe(sub);
        assertThat(ReactiveSeq.fromIterator(sub.iterator()).toList(),equalTo(
                Arrays.asList(1,2,3)));
    }
    @Test
    public void publishAndSubscribeEmptyIterator(){
        SeqSubscriber<Integer> sub = ReactiveSeq.subscriber();
        ReactiveSeq.<Integer>of().subscribe(sub);
        assertThat(ReactiveSeq.fromIterator(sub.iterator()).toList(),equalTo(
                Arrays.asList()));
    }
}
