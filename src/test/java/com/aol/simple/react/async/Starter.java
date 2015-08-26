package com.aol.simple.react.async;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;

import com.aol.simple.react.stream.lazy.LazyReact;

public class Starter extends AbstractVerticle {
	  @Override
	  public void start() throws Exception {
	    super.start();
	    
	    LazyReact react = new LazyReact(new VertxExecutor(getVertx()));
	    react.of(1, 2, 3)
	    			.map(i -> i + 1).futureOperations()
	    			.reduce((a,b) -> a + b)
	    			.thenApply(opt-> opt.orElse(Integer.MIN_VALUE))
	    			.thenAccept(number->System.out.println(number))
	    		;
	  //  System.out.println("sum = " + number); // 2 + 3 + 4 = 9
	    // works with blocking thread pool
	    // does not work with non-blocking event loop
	  }

	  public static void main(String[] args) {
	    Vertx.vertx().deployVerticle(Starter.class.getCanonicalName());
	  }
	}