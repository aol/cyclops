package com.oath.cyclops.internal.stream.spliterators.push.grouping;


import cyclops.reactive.Spouts;
import org.reactivestreams.Publisher;
import org.reactivestreams.tck.PublisherVerification;
import org.reactivestreams.tck.TestEnvironment;
import org.testng.annotations.Test;

@Test
public class GroupedTckPublisherTest extends PublisherVerification<Long>{

	public GroupedTckPublisherTest(){
		  super(new TestEnvironment(300L));
	}


	@Override
	public Publisher<Long> createPublisher(long elements) {
		return Spouts.iterate(0l, i->i+1l).grouped(1).map(l->l.getOrElse(0,-1l)).limit(elements);

	}

	@Override
	public Publisher<Long> createFailedPublisher() {
		return null; //not possible to forEachAsync to failed Stream

	}


}
