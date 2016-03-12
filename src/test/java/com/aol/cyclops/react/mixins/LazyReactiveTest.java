package com.aol.cyclops.react.mixins;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.aol.cyclops.control.Pipes;
import com.aol.cyclops.control.Xor;
import com.aol.cyclops.data.async.Queue;
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
		

