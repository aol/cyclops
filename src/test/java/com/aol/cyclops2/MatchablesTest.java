package com.aol.cyclops2;

import cyclops.companion.Xors;
import cyclops.async.Adapter;
import cyclops.async.QueueFactories;
import org.agrona.concurrent.ManyToManyConcurrentArrayQueue;
import org.junit.Test;

import java.util.concurrent.LinkedBlockingQueue;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class MatchablesTest {

    
    private String loadData(){
        return "";
    }
    private boolean validData(String data){
        return true;
    }
    private int save(String data){
        return 1;
    }
    private final  int IO_ERROR = -1;
    private final  int UNEXPECTED_RESULT = 0;
    private final  int SUCCESS = 1;

	@Test
	public void adapter(){
	    Adapter<Integer> adapter = QueueFactories.<Integer>unboundedQueue()
	                                                        .build();
	                                                         
	        String result =   Xors.adapter(adapter)
	                                          .visit(queue->"we have a queue",topic->"we have a topic");
	        assertThat(result,equalTo("we have a queue"));
	        
	}

	
	@Test
	public void nonBlocking(){
	    assertThat(Xors.blocking(new ManyToManyConcurrentArrayQueue(10))
	                                  .visit(c->"blocking", c->"not"),equalTo("not"));
	}
	@Test
    public void blocking(){
        assertThat(Xors.blocking(new LinkedBlockingQueue(10))
                                      .visit(c->"blocking", c->"not"),equalTo("blocking"));
    }

}
