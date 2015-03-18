package com.aol.simple.react.async;

public class AlwaysContinue implements Continueable{

	public boolean closeQueueIfFinished(Queue queue){
		return true;
	}

	@Override
	public void addQueue(Queue queue) {
		
		
	}
	public void registerSkip(long skip){
		
	}
	public void registerLimit(long limit){
		
	}

	@Override
	public void closeAll() {
		
		
	}

	@Override
	public boolean closed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void closeQueueIfFinishedStateless(Queue queue) {
		// TODO Auto-generated method stub
		
	}
}
