package com.aol.simple.react.stream.api;

import com.aol.simple.react.async.Queue;

public interface ToQueue <U>{
	abstract  Queue<U> toQueue(); 
}
