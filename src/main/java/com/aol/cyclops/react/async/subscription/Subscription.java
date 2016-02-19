package com.aol.cyclops.react.async.subscription;


import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import org.jooq.lambda.Seq;

import com.aol.cyclops.data.async.Queue;

@Getter
@Setter
public class Subscription implements Continueable{
	private final Map<Queue,AtomicLong> limits = new HashMap<>();
	
	private final Map<Queue,AtomicBoolean> unlimited = new HashMap<>();
	private final Map<Queue,AtomicLong> count = new HashMap<>();
	private final List<Queue> queues = new LinkedList<>();
	
	private final AtomicBoolean closed=  new AtomicBoolean(false);
	
	private final AtomicLong timeLimitNanos = new AtomicLong(-1);
	
	public long timeLimit(){
		return timeLimitNanos.get();
	}
	public void registerSkip(long skip){
		if(queues.size()>0)
			limits.get(currentQueue()).addAndGet(skip);
	}
	public void registerTimeLimit(long nanos){
		if(timeLimitNanos.get()==-1 || timeLimitNanos.get()>nanos)
			timeLimitNanos.set(nanos);
	}
	public void registerLimit(long limit){
		
		if(queues.size()>0){
			if(unlimited.get(currentQueue()).get())
				limits.get(currentQueue()).set(0);
			
			limits.get(currentQueue()).addAndGet(limit);
			unlimited.get(currentQueue()).set(false);
			
			queues.stream().forEach(this::closeQueueIfFinishedStateless);
			
		}
	}
	private Queue currentQueue() {
		return queues.get(queues.size()-1);
	}
	public void addQueue(Queue q){
	
		queues.add(q);
		limits.put(q, new AtomicLong(Long.MAX_VALUE-1));
		unlimited.put(q, new AtomicBoolean(true));
		count.put(q, new AtomicLong(0l));
		
	}
	
	
	public void closeQueueIfFinished(Queue queue){
		
		closeQueueIfFinished(queue,AtomicLong::incrementAndGet);
		
	}
	private void closeQueueIfFinished(Queue queue, Function<AtomicLong,Long> fn){
		
		if(queues.size()==0)
			return;
		
		long queueCount = fn.apply(count.get(queue));
		long limit = valuesToRight(queue).stream().reduce((acc,next)-> Math.min(acc, next)).get();
		
		
		
		
		if(queueCount>=limit){ //last entry - close THIS queue only!
			
			queue.closeAndClear();
			closed.set(true);
		}
		
		
	}
	public void closeQueueIfFinishedStateless(Queue queue){
	
		closeQueueIfFinished(queue,AtomicLong::get);
			
	}
	private List<Long> valuesToRight(Queue queue) {
		return Seq.seq(queues.stream()).splitAt(findQueue(queue)).v2.map(limits::get).map(AtomicLong::get).collect(Collectors.toList());
		
	}
	private Seq<Queue> queuesToLeft(Queue queue) {
		return Seq.seq(queues.stream()).splitAt(findQueue(queue)).v1;
		
	}
	
	private int findQueue(Queue queue){
		for(int i=0;i< queues.size();i++){
			if(queues.get(i) == queue)
				return i;
		}
		return -1;
	}
	@Override
	public void closeAll(Queue queue) {
		
	
		closed.set(true);
		queue.closeAndClear();
		queuesToLeft(queue).forEach(Queue::closeAndClear);
	
	}
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
stream.map().iterator().limit(4).flatMap(..).limit(2).map(..).limit(8)
subscription

stream no limit
	q1:limit (4)
	q2:limit (2)
	q3:limit (8)
	**/

