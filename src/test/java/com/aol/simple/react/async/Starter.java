package com.aol.simple.react.async;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;

import com.aol.simple.react.stream.lazy.LazyReact;

public class Starter extends AbstractVerticle {
	  @Override
	  public void start() throws Exception {
	    super.start();
	    
	    LazyReact react = new LazyReact().withAsync(false);
	    int number = react.of(1, 2, 3).map(i -> i + 1).reduce((a,b) -> a + b).orElse(Integer.MIN_VALUE);
	    System.out.println("sum = " + number); // 2 + 3 + 4 = 9
	    //works with vert.x event loop
	  }

	  public static void main(String[] args) {
	    Vertx.vertx().deployVerticle(Starter.class.getCanonicalName());
	  }
	}
