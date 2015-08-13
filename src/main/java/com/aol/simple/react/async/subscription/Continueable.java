package com.aol.simple.react.async.subscription;

import com.aol.simple.react.async.Queue;

public interface Continueable {

	public void closeQueueIfFinished(Queue queue);

	public void addQueue(Queue queue);
	public void registerSkip(long skip);
	public void registerLimit(long limit);
	public void closeAll(Queue q);

	public boolean closed();

	public void closeQueueIfFinishedStateless(Queue queue);

	public void closeAll();
}
