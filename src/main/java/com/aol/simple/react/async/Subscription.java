package com.aol.simple.react.async;


import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;

import org.jooq.lambda.Seq;

@Getter
@Setter
public class Subscription implements Continueable{
	private final Map<Queue,AtomicLong> limits = new HashMap<>();
	
	private final Map<Queue,AtomicBoolean> unlimited = new HashMap<>();
	private final Map<Queue,AtomicLong> count = new HashMap<>();
	private final List<Queue> queues = new LinkedList<>();
	
	private final AtomicBoolean closed=  new AtomicBoolean(false);
	
	public void registerSkip(long skip){
		if(queues.size()>0)
			limits.get(queues.get(queues.size()-1)).addAndGet(skip);
	}
	public void registerLimit(long limit){
		
		if(queues.size()>0){
			if(unlimited.get(queues.get(queues.size()-1)).get())
				limits.get(queues.get(queues.size()-1)).set(0);
			
			limits.get(queues.get(queues.size()-1)).addAndGet(limit);
			unlimited.get(queues.get(queues.size()-1)).set(false);
			
			queues.stream().forEach(this::closeQueueIfFinishedStateless);
			
		}
	}
	public void addQueue(Queue q){
	
		queues.add(q);
		limits.put(q, new AtomicLong(Long.MAX_VALUE-1));
		unlimited.put(q, new AtomicBoolean(true));
		count.put(q, new AtomicLong(0l));
		
	}
	
	
	public boolean closeQueueIfFinished(Queue queue){
		
		if(queues.size()==0)
			return true;
		
		long queueCount = count.get(queue).incrementAndGet();
		long limit = valuesToRight(queue).stream().reduce((acc,next)-> Math.min(acc, next)).get();
		
		
		
		if(queueCount>=limit){ //last entry - close THIS queue only!
			
			queue.closeAndClear();
			closed.set(true);
		}
		
	
		return true;
		
	}
	public void closeQueueIfFinishedStateless(Queue queue){
		
		if(queues.size()==0)
			return;
		
		long queueCount = count.get(queue).get();
		long limit = valuesToRight(queue).stream().reduce((acc,next)-> Math.min(acc, next)).get();
		
		
		
		
		if(queueCount>=limit){ //last entry - close THIS queue only!
			
			queue.closeAndClear();
			closed.set(true);
		}
		
	
		
		
	}
	private List<Long> valuesToRight(Queue queue) {
		return Seq.seq(queues.stream()).splitAt(findQueue(queue)).v2.map(limits::get).map(AtomicLong::get).collect(Collectors.toList());
		
	}
	
	private int findQueue(Queue queue){
		for(int i=0;i< queues.size();i++){
			if(queues.get(i) == queue)
				return i;
		}
		return -1;
	}
	@Override
	public void closeAll() {
		closed.set(true);
		queues.stream().forEach(Queue::closeAndClear);
		
	}
	@Override
	public boolean closed() {
		return closed.get();
	}
}
/**
stream.map().iterator().limit(4).flatMap().limit(2).iterator().limit(8)
subscription

stream no limit
	q1:limit (4)
	q2:limit (2)
	q3:limit (8)
	**/

