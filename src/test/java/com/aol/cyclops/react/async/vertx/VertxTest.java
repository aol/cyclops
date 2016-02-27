package com.aol.cyclops.react.async.vertx;

import static org.jooq.lambda.tuple.Tuple.tuple;
import  org.jooq.lambda.tuple.Tuple3;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.jooq.lambda.tuple.Tuple;
import org.junit.Test;

import com.aol.cyclops.control.LazyReact;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.SimpleReact;
import com.aol.cyclops.data.async.Queue;
import com.aol.cyclops.data.async.QueueFactories;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerRequest;

public class VertxTest {
	@Test
	public void sum(){
	    Vertx vertx = Vertx.factory.vertx();
	    
		LazyReact react = new LazyReact(c->vertx.runOnContext(v -> c.run()));
		int number = react.of(1, 2, 3).map(i -> i + 1).reduce((a,b) -> a + b).orElse(Integer.MIN_VALUE);
		System.out.println("sum = " + number); // 2 + 3 + 4 = 9
	}
	@Test
	public void httpServer(){
	    Vertx vertx = Vertx.factory.vertx();
	    CompletableFuture<HttpServer> server =new CompletableFuture<>();
	   

	                     
	    Queue<HttpServerRequest> reqs = QueueFactories.<HttpServerRequest>boundedNonBlockingQueue(1000)
	                                                            .build();
	    
	                
	    vertx.createHttpServer(new HttpServerOptions().
	            setPort(8080).
	            setHost("localhost")
	        )
	    .requestHandler(event-> {
	        reqs.add(event);
	        System.out.println(event.absoluteURI());
	    }).listen(e->{
	           if(e.succeeded())
	               server.complete(e.result());
	           else
	               server.completeExceptionally(e.cause());
	    });	    
	    
	    LazyReact react = new LazyReact(c->vertx.runOnContext(v -> c.run()));
	    
	    react.from(reqs.stream())
	         .peek(System.out::println)
	         .grouped(2)
	         .map(list-> tuple(list.get(0).response(),list.get(1).response(),getParam(list.get(0)),getParam(list.get(1))))
	         .peek(t->t.v1.end("adding "+t.v3+t.v4))
	         .peek(t->t.v2.end("multiplying "+t.v3*t.v4))
	         .run();
	      
	    
        new SimpleReact(c->vertx.runOnContext(v -> c.run())).from(server)
                        .then(s->"server started")
                        .onFail(e->"failed to start "+e.getMessage())
                        .peek(System.out::println);	    
	    
	    while(true){
	        
	    }
	    
       
	           
	}
	
	private int getParam(HttpServerRequest req){
	    return Integer.parseInt(req.getParam("num"));
	}
	
	
	
	
	@Test
    public void downloadUrls(){
	    
	    //cyclops-react async.Queues
        Queue<String> downloadQueue = new Queue<String>();
        Queue<String> completedQueue = new Queue<String>();	    
	    
        //vert.x meets cyclops-react
        Vertx vertx = Vertx.factory.vertx();
        LazyReact react = new LazyReact(c->vertx.runOnContext(v -> c.run()));
        

        //populate the download queue asynchronously
        ReactiveSeq.of("www.aol.com","www.rte.ie","www.aol.com")
                   .peek(next->System.out.println("adding to download queue " + next))
                   .futureOperations(c->vertx.runOnContext(v -> c.run()))
                   .forEach(downloadQueue::add);
        
        //download asynchronously : all cyclops-react tasks are passed into vert.x
        react.from(downloadQueue.stream())
             .peek(System.out::println)
             .map(url->vertx.createHttpClient().getNow(url,"",resp->resp.bodyHandler(body-> completedQueue.add(body.getString(0, body.length())))))
             .run();
        
        
        //handle the results
        completedQueue.stream()
                      .peek(next->System.out.println("just downloaded" + next))
                      .forEach(System.out::println);
        
        
        
    }
	
}
