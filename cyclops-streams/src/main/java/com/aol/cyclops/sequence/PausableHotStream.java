package com.aol.cyclops.sequence;


public interface PausableHotStream<T> extends HotStream<T>{
	void unpause();
	void pause();
}
