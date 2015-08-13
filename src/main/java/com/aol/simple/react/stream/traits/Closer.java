package com.aol.simple.react.stream.traits;

import lombok.AllArgsConstructor;

import com.aol.simple.react.async.Queue;

@AllArgsConstructor
public class Closer<T> {
	private int times;
	private Queue<T> queue;
	public boolean done(){
		if(--times==0){
			queue.close();
			return true;
		}
		return false;
	}
}
