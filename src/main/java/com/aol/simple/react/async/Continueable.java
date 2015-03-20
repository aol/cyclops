package com.aol.simple.react.async;

public interface Continueable {

	public void closeQueueIfFinished(Queue queue);

	public void addQueue(Queue queue);
	public void registerSkip(long skip);
	public void registerLimit(long limit);
	public void closeAll();

	public boolean closed();

	public void closeQueueIfFinishedStateless(Queue queue);
}
