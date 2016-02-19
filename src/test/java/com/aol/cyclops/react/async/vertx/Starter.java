package com.aol.cyclops.react.async.vertx;

import com.aol.cyclops.control.LazyReact;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;

public class Starter extends AbstractVerticle {
	  @Override
	  public void start() throws Exception {
	    super.start();
	    
	    LazyReact react = new LazyReact(new VertxExecutor(getVertx()));
	    int number = react.of(1, 2, 3).map(i -> i + 1).reduce((a,b) -> a + b).orElse(Integer.MIN_VALUE);
	    System.out.println("sum = " + number); // 2 + 3 + 4 = 9
	    // works with blocking thread pool
	    // does not work with non-blocking event loop
	  }

	  public static void main(String[] args) {
	    Vertx.vertx().deployVerticle(Starter.class.getCanonicalName());
	  }
	}