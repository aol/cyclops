package com.aol.cyclops2.internal.stream.spliterators.push.filter;


import com.aol.cyclops2.internal.stream.ReactiveStreamX;
import com.aol.cyclops2.internal.stream.spliterators.push.OperatorToIterable;
import com.aol.cyclops2.util.ExceptionSoftener;
import cyclops.collections.ListX;
import cyclops.stream.ReactiveSeq;
import cyclops.stream.Spouts;
import org.reactivestreams.Publisher;
import org.reactivestreams.tck.PublisherVerification;
import org.reactivestreams.tck.TestEnvironment;
import org.testng.annotations.Test;

@Test
public class FilterOpteratorToIterableTckPublisherTest extends PublisherVerification<Long>{

	public FilterOpteratorToIterableTckPublisherTest(){
		  super(new TestEnvironment(300L));
	}
	

	@Override
	public Publisher<Long> createPublisher(long elements) {
		return ReactiveSeq.fromIterable(new OperatorToIterable<Long, Long>(((ReactiveStreamX<Long>) Spouts.iterate(0l, i -> i + 1l).filter(i -> i % 2 == 0).limit(elements)).getSource(),
                e -> {
                    throw ExceptionSoftener.throwSoftenedException(e);
                }));

		
	}

	@Override
	public Publisher<Long> createFailedPublisher() {
		return null; //not possible to forEachAsync to failed Stream
		
	}
	

}
