package com.oath.cyclops.internal.stream.spliterators.push.flatMap.publisher;


import cyclops.reactive.Spouts;
import org.reactivestreams.Publisher;
import org.testng.annotations.Test;

@Test(enabled =false)
public class PublisherFlatMap2TckPublisherTest{//} extends PublisherVerification<Long>{

	public PublisherFlatMap2TckPublisherTest(){
	//	  super(new TestEnvironment(300L));
	}


	//@Override
	public Publisher<Long> createPublisher(long elements) {
		return Spouts.iterate(0l, i->i+1l).mergeMap(i->Spouts.of(0l,i)).limit(elements);

	}

//	@Override
	public Publisher<Long> createFailedPublisher() {
		return null; //not possible to forEachAsync to failed Stream

	}
//	@Override @Test
	public void optional_spec111_maySupportMultiSubscribe() throws Throwable {

	}



}
