package com.oath.cyclops.internal.stream.spliterators.push.map;

import com.oath.cyclops.internal.stream.spliterators.push.MapOperator;
import com.oath.cyclops.internal.stream.spliterators.push.SubscriberSource;
import org.reactivestreams.Subscriber;
import org.reactivestreams.tck.SubscriberBlackboxVerification;
import org.reactivestreams.tck.TestEnvironment;
import org.testng.annotations.Test;

@Test
public class MapTckBlackBoxSubscriberTest extends SubscriberBlackboxVerification<Long>{
	public MapTckBlackBoxSubscriberTest() {
        super(new TestEnvironment(300L));
    }

	@Override
	public Subscriber<Long> createSubscriber() {
		SubscriberSource<Long> sub = new SubscriberSource<Long>();

		new MapOperator<Long,Long>(sub, i->i*2).subscribe(System.out::println,System.err::println,()->{});
		return sub;

	}

	@Override
	public Long createElement(int element) {
		return new Long(element);
	}


}
