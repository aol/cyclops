package com.aol.simple.react.stream.traits;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.LockSupport;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;

import com.aol.cyclops.sequence.SequenceM;
import com.aol.simple.react.async.Queue;
import com.aol.simple.react.async.Queue.QueueReader;

public class EagerFutureStreamFunctions {
	/**
	 * Close all queues except the active one
	 * 
	 * @param active Queue not to close
	 * @param all All queues potentially including the active queue
	 */
	static void closeOthers(Queue active, List<Queue> all){
		all.stream().filter(next -> next!=active).forEach(Queue::closeAndClear);
		
	}
	/**
	 * Close all streams except the active one
	 * 
	 * @param active Stream not to close
	 * @param all  All streams potentially including the active stream
	 */
	static void closeOthers(EagerSimpleReactStream active, List<EagerSimpleReactStream> all){
		all.stream().filter(next -> next!=active).filter(s -> s instanceof SimpleReactStream).forEach(EagerSimpleReactStream::cancel);
		
	}
	/**
	 * Zip two streams into one. Uses the latest values from each rather than waiting for both
	 * 
	 */
	static <T1, T2> Seq<Tuple2<T1, T2>> combineLatest(EagerSimpleReactStream<T1> left,
			EagerSimpleReactStream<T2> right) {
		return combineLatest(left, right, Tuple::tuple);
	}
	/**
	 * Return first Stream out of provided Streams that starts emitted results 
	 * 
	 * @param futureStreams Streams to race
	 * @return First Stream to start emitting values
	 */
	@SafeVarargs
	public static <U> EagerSimpleReactStream<U> firstOf(EagerSimpleReactStream<U>... futureStreams) {
		List<Tuple2<EagerSimpleReactStream<U>, QueueReader>> racers = Stream
				.of(futureStreams)
				.map(s -> Tuple.tuple(s,new Queue.QueueReader(s.toQueue(),null))).collect(Collectors.toList());
		while(true){
		for(Tuple2<EagerSimpleReactStream<U>,Queue.QueueReader> q: racers){
			if(q.v2.notEmpty()){
				EagerFutureStreamFunctions.closeOthers(q.v2.getQueue(),racers.stream().map(t -> t.v2.getQueue()).collect(Collectors.toList()));
				EagerFutureStreamFunctions.closeOthers(q.v1,racers.stream().map(t -> t.v1).collect(Collectors.toList()));
				return q.v1.fromStream(q.v2.getQueue().stream(q.v1.getSubscription()));
			}
				
		}
		LockSupport.parkNanos(1l);
		}

		

	}

	
	

	/**
	 * Zip two streams into one using a {@link BiFunction} to produce resulting. 
	 * values. Uses the latest values from each rather than waiting for both.
	 * 
	 */
	static <T1, T2, R> Seq<R> combineLatest(EagerSimpleReactStream<T1> left,
			EagerSimpleReactStream<T2> right, BiFunction<T1, T2, R> zipper) {
		
		Queue q = left.then(it->new Val(Val.Pos.left,it)).merge(right.then(it->new Val(Val.Pos.right,it))).toQueue();
		final Iterator<Val> it = q.stream(left.getSubscription()).iterator();
		

		class Zip implements Iterator<R> {
			T1 lastLeft = null;
			T2 lastRight = null;
			@Override
			public boolean hasNext() {

				return it.hasNext();
			}

			@Override
			public R next() {
				Val v =it.next();
				if(v.pos== Val.Pos.left)
					lastLeft = (T1)v.val;
				else
					lastRight = (T2)v.val;
			
				return zipper.apply(lastLeft, lastRight);
				

			}
		}

		return Seq.seq(new Zip());
	}
	
	/**
	 * Zip two streams into one. Uses the latest values from each rather than waiting for both
	 * 
	 */
	static <T1, T2> Seq<Tuple2<T1, T2>> withLatest(EagerSimpleReactStream<T1> left,
			EagerSimpleReactStream<T2> right) {
		return withLatest(left, right, Tuple::tuple);
	}
	/**
	 * Zip two streams into one using a {@link BiFunction} to produce resulting. 
	 * values. Uses the latest values from each rather than waiting for both.
	 * 
	 */
	static <T1, T2, R> Seq<R> withLatest(EagerSimpleReactStream<T1> left,
			EagerSimpleReactStream<T2> right, BiFunction<T1, T2, R> zipper) {
		
		Queue q = left.then(it->new Val(Val.Pos.left,it)).merge(right.then(it->new Val(Val.Pos.right,it))).toQueue();
		final Iterator<Val> it = q.stream(left.getSubscription()).iterator();
		

		class Zip implements Iterator<R> {
			T1 lastLeft = null;
			T2 lastRight = null;
			@Override
			public boolean hasNext() {

				return it.hasNext();
			}

			@Override
			public R next() {
				Val v =it.next();
				if(v.pos== Val.Pos.left){
					lastLeft = (T1)v.val;
					return zipper.apply(lastLeft, lastRight);
				}
				else
					lastRight = (T2)v.val;
			
				return (R)Optional.empty();
				

			}
		}

		return Seq.seq(new Zip()).filter(next->!(next instanceof Optional));
	}
	
	
	static <T1, T2> SequenceM<T1> skipUntil(EagerSimpleReactStream<T1> left,
			EagerSimpleReactStream<T2> right) {
		
		Queue q = left.then(it->new Val(Val.Pos.left,it)).merge(right.then(it->new Val(Val.Pos.right,it))).toQueue();
		final Iterator<Val> it = q.stream(left.getSubscription()).iterator();
		
		final Object missingValue = new Object();
		class Zip implements Iterator<T1> {
			Optional<T1> lastLeft = Optional.empty();
			Optional<T2> lastRight = Optional.empty();
			@Override
			public boolean hasNext() {

				return it.hasNext();
			}

			@Override
			public T1 next() {
				Val v =it.next();
				if(v.pos== Val.Pos.left){
					if(lastRight.isPresent())
						lastLeft = Optional.of((T1)v.val);
				}
				else
					lastRight = Optional.of((T2)v.val);
				if(!lastRight.isPresent())
					return (T1)Optional.empty();
				if(lastLeft.isPresent())
					return lastLeft.get();
				else
					return (T1)Optional.empty();
				
				

			}
		}
		
		return SequenceM.fromIterable(()->new Zip()).filter(next->!(next instanceof Optional));
	}
	static <T1, T2> SequenceM<T1> takeUntil(EagerSimpleReactStream<T1> left,
			EagerSimpleReactStream<T2> right) {
		
		Queue q = left.then(it->new Val(Val.Pos.left,it)).merge(right.then(it->new Val(Val.Pos.right,it))).toQueue();
		final Iterator<Val> it = q.stream(left.getSubscription()).iterator();
		
		final Object missingValue = new Object();
		class Zip implements Iterator<T1> {
			Optional<T1> lastLeft = Optional.empty();
			Optional<T2> lastRight = Optional.empty();
			boolean closed= false;
			@Override
			public boolean hasNext() {
				
				return !closed && it.hasNext();
			}

			@Override
			public T1 next() {
				Val v =it.next();
				if(v.pos== Val.Pos.left)
					lastLeft = Optional.of((T1)v.val);
				else
					lastRight = Optional.of((T2)v.val);
				
				if(!lastRight.isPresent() && lastLeft.isPresent())
					return lastLeft.get();
				else{
					closed= true;
					return (T1)Optional.empty();
				}
				
				

			}
		}

		return SequenceM.fromIterable(()->new Zip()).filter(next->!(next instanceof Optional));
	}

}
