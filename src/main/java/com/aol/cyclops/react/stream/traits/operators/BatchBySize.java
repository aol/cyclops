package com.aol.cyclops.react.stream.traits.operators;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import lombok.AllArgsConstructor;

import com.aol.cyclops.react.async.Queue;
import com.aol.cyclops.react.async.Queue.ClosedQueueException;
import com.aol.cyclops.react.async.Queue.QueueTimeoutException;
import com.aol.cyclops.react.async.subscription.Continueable;

@AllArgsConstructor
public class BatchBySize<U, C extends Collection<U>> implements Function<Supplier<U>, Supplier<C>>{


	private final int size;
	private final Continueable subscription;
	private final Queue<U> queue;
	private final Supplier<C> factory;
	@Override
	public Supplier<C> apply(Supplier<U> s) {
		
			return () -> {
				C list = factory.get();
				try {
					for (int i = 0; i < size; i++) {
						try{
							list.add(s.get());
							subscription.closeQueueIfFinished(queue);
						}catch(QueueTimeoutException e){
							//retry if queue access timed out but not clsosed
							i--;
						}
					}
				} catch (ClosedQueueException e) {
					if(list.size()>0)
						throw new ClosedQueueException(list);
					else
						throw new ClosedQueueException();
				}
				return list;
			};
		
	}
	public Function<Supplier<U>, Supplier<Optional<C>>> liftOptional(){
		return s->  () -> {
			try {
				return Optional.of(this.apply(s).get());
			} catch (ClosedQueueException e) {
				
				return Optional.ofNullable((C)e.getCurrentData()).
						flatMap(list-> list.isEmpty() ? Optional.empty() : Optional.of(list));
			}
		};
	}

}
