package com.aol.simple.react.async.vertx;

import io.vertx.core.Vertx;

import org.junit.Test;

import com.aol.simple.react.stream.lazy.LazyReact;

public class VertxTest {
	@Test
	public void sum(){
		LazyReact react = new LazyReact(new VertxExecutor(Vertx.factory.vertx()));
		int number = react.of(1, 2, 3).map(i -> i + 1).reduce((a,b) -> a + b).orElse(Integer.MIN_VALUE);
		System.out.println("sum = " + number); // 2 + 3 + 4 = 9
	}
}
