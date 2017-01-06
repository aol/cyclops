package com.aol.cyclops2.react.mixins;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

import cyclops.async.Pipes;
import cyclops.async.Queue;
public class LazyReactiveTest {
	
        Pipes<String,String> pipes = Pipes.of();
		@Test
		public void testNoPipe() {
		    pipes.register("hello", new Queue<String>());
			pipes.clear();
			assertFalse(pipes.size()>0);
		}
		@Test
        public void testOneOrError() {
            pipes.register("hello", new Queue<String>());
           
            pipes.push("hello", "world");
            
            pipes.get("hello").map(a->a.close()).orElse(false);
            pipes.reactiveSeq("hello").get().forEach(System.out::println);
          //  assertThat(pipes.oneOrError("hello"),equalTo(Xor.primary("world")));
            
        }
		
}
		

