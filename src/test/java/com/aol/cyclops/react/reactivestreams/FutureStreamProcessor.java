package com.aol.cyclops.react.reactivestreams;

import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Function;

import lombok.AllArgsConstructor;

import org.reactivestreams.Processor;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import com.aol.cyclops.react.async.Queue;
import com.aol.cyclops.react.stream.traits.LazyFutureStream;

public class FutureStreamProcessor<T,R> extends FutureStreamSubscriber<T> implements Processor<T,R>{
	volatile Subscriber<? super R> publisherSubscription;
	volatile boolean subscribeCalled = false;
	Function<LazyFutureStream<T>,LazyFutureStream<R>> fn = lf -> (LazyFutureStream)lf;
	ConcurrentLinkedQueue<Long> requests = new ConcurrentLinkedQueue<Long>();
	
	
	@Override
	public void subscribe(Subscriber<? super R> sr) {
		Objects.requireNonNull(sr);
		if(this.subscribeCalled)
			return;
		System.out.println("Subscriber " + System.identityHashCode(sr));
		Subscriber toUse = new Subscriber<R>(){

			volatile boolean errorState = false;
			@Override
			public void onSubscribe(Subscription s) {
				System.out.println("Subscriber " + System.identityHashCode(sr) + " : subscription "  + System.identityHashCode(s));
				if(!errorState){
					sr.onSubscribe(new SubProxy(s,FutureStreamProcessor.this));
				}
			}

			@Override
			public void onNext(R t) {
				if(!errorState){
					System.out.println("on next " + t);
					sr.onNext(t);
				}
				
			}

			@Override
			public void onError(Throwable t) {
				if(!errorState){
					sr.onError(t);
				}
				errorState =true;
				
			}

			@Override
			public void onComplete() {
				if(!errorState){
					sr.onComplete();
				}
				
			}
			
		};
		
		if(getStream()!=null)
			getStream().subscribe(toUse);
		else{
			this.publisherSubscription=toUse;
		}
		subscribeCalled =true;
		
	}
//	@AllArgsConstructor
	private static class SubProxy implements Subscription{
		Subscription publishers;
		//Subscription subscribers;
		FutureStreamProcessor proc;
		public SubProxy(Subscription publishers, FutureStreamProcessor proc){
			this.publishers = publishers;
			this.proc = proc;
			System.out.println("Subscription " + System.identityHashCode(publishers) + ": proxy : subscription "  + System.identityHashCode(this));
		}
		@Override
		public void request(long n) {
			if(n<1){
				proc.onError(new IllegalArgumentException("3.9 While the Subscription is not cancelled, Subscription.request(long n) MUST throw a java.lang.IllegalArgumentException if the argument is <= 0."));
			}
		//	proc.getSubscription().request(n);
			proc.requests.add(n);
			publishers.request(n);
			
		}

		@Override
		public void cancel() {
			proc.getSubscription().cancel();
			System.out.println("Cancel subscribers queue");
			
			publishers.cancel();
			System.out.println("Cancel publishers queue");
		
			
			proc.cancel();
		}
	}
	public void onError(Throwable t){
		super.onError(t);
		if(this.publisherSubscription!=null)
			this.publisherSubscription.onError(t);
	}
	public void onSubscribe(final Subscription s) {
		Objects.nonNull(s);
		if(this.subscription!=null){
			s.cancel();
			return;
		}
	
		internalSubscribe(s);
		if(this.publisherSubscription!=null)
			getStream().subscribe(this.publisherSubscription);
		
		//stream = (LazyFutureStream)getStream().withReactiveStreamsSubscription(s);
	}
	private void internalSubscribe(final Subscription s){
	
		queue = new Queue<T>(){
			public T get(){
				System.out.println(requests);
				if(requests.size()==0)
					s.request(1);
				else
					s.request(requests.poll());
				return super.get();
			}
		};

		this.subscription= s;
		stream = stream();
		System.out.println("initial demand");
		s.request(1);
	}
	public void cancel() {
		this.publisherSubscription=null;
	}

}
