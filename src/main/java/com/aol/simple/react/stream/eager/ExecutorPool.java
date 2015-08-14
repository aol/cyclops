package com.aol.simple.react.stream.eager;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class ExecutorPool {
	
	public static ExecutorPool pool = new ExecutorPool();
		private BlockingQueue<Executor> list = new LinkedBlockingQueue<>();
		public Executor next()  {
			System.out.println("Size " + list.size());
			Executor exec = list.poll();
			if(exec!=null){
				System.out.println("found!");
				return exec;
			}
			System.out.println("new thread!");
			return Executors.newSingleThreadExecutor();
		}
		public void populate(Executor exec){
			System.out.println("returning");
			list.offer(exec);
			System.out.println("Size " + list.size());
		}
	
}
