package com.aol.cyclops.react.async;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.jooq.lambda.Seq;

import com.aol.cyclops.react.async.Queue.ClosedQueueException;
import com.aol.cyclops.react.stream.traits.Continuation;

public class StreamOfContinuations implements ContinuationStrategy {
	private final Queue<?> queue;
	private  List<Continuation> continuation= new ArrayList<>();
	
	public StreamOfContinuations(Queue<?> queue){
		this.queue = queue;
	}
	
	@Override
	public void addContinuation(Continuation c) {
		this.continuation.add(c);

	}

	@Override
	public void handleContinuation(){
		
			continuation = Seq.seq(continuation)
								.<Optional<Continuation>>map(c -> {
									try{ 
									return Optional.of(c.proceed());
								}catch(ClosedQueueException e){
									
								
									
									return Optional.empty();
								}
									
								})
								.filter(Optional::isPresent)
								.map(Optional::get)
								.toList();
		
		
			if(continuation.size()==0){
			
				queue.close();
				throw new ClosedQueueException();
			}
		}

}
