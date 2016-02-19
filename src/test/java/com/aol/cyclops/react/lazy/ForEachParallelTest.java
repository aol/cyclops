package com.aol.cyclops.react.lazy;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.aol.cyclops.react.base.BaseSequentialSQLTest.X;
import com.aol.cyclops.types.futurestream.LazyFutureStream;

public class ForEachParallelTest {

	@Test
	 public void testOnEmptyThrows(){
		LazyFutureStream.parallel(1,2,3,4)
						.peek(i-> System.out.println("A"+Thread.currentThread().getId()))
						.peekSync(i->sleep(i*100)).forEach(i-> System.out.println(Thread.currentThread().getId()));
	    	
	  
	  }

	private Object sleep(int i) {
		try {
			Thread.sleep(i);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return i;
	}
}
