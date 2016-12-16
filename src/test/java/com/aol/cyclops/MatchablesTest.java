package com.aol.cyclops;

import static com.aol.cyclops.Matchables.optional;
import static com.aol.cyclops.control.Matchable.otherwise;
import static com.aol.cyclops.control.Matchable.then;
import static com.aol.cyclops.control.Matchable.when;
import static com.aol.cyclops.control.Try.success;
import static com.aol.cyclops.util.function.Predicates.__;
import static com.aol.cyclops.util.function.Predicates.any;
import static com.aol.cyclops.util.function.Predicates.eq;
import static com.aol.cyclops.util.function.Predicates.equal;
import static com.aol.cyclops.util.function.Predicates.greaterThan;
import static com.aol.cyclops.util.function.Predicates.in;
import static com.aol.cyclops.util.function.Predicates.instanceOf;
import static com.aol.cyclops.util.function.Predicates.not;
import static com.aol.cyclops.util.function.Predicates.some;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;

import org.agrona.concurrent.ManyToManyConcurrentArrayQueue;
import org.jooq.lambda.tuple.Tuple;
import org.junit.Test;

import com.aol.cyclops.control.Eval;
import com.aol.cyclops.control.FutureW;
import com.aol.cyclops.control.Matchable.AutoCloseableMatchableIterable;
import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.Try;
import com.aol.cyclops.control.Xor;
import com.aol.cyclops.data.async.Adapter;
import com.aol.cyclops.data.async.QueueFactories;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.util.function.Predicates;

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
	                                                         
	        String result =   Matchables.adapter(adapter)
	                                          .visit(queue->"we have a queue",topic->"we have a topic");
	        assertThat(result,equalTo("we have a queue"));
	        
	}

	
	@Test
	public void nonBlocking(){
	    assertThat(Matchables.blocking(new ManyToManyConcurrentArrayQueue(10))
	                                  .visit(c->"blocking", c->"not"),equalTo("not"));
	}
	@Test
    public void blocking(){
        assertThat(Matchables.blocking(new LinkedBlockingQueue(10))
                                      .visit(c->"blocking", c->"not"),equalTo("blocking"));
    }

}
