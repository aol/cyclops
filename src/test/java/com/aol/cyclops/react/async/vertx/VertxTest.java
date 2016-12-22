package com.aol.cyclops.react.async.vertx;

import static org.hamcrest.Matchers.equalTo;
import static org.jooq.lambda.tuple.Tuple.tuple;
import static org.junit.Assert.assertThat;

import java.util.concurrent.CompletableFuture;

import org.junit.Ignore;
import org.junit.Test;

import cyclops.async.LazyReact;
import cyclops.stream.ReactiveSeq;
import cyclops.async.SimpleReact;
import cyclops.async.Queue;
import cyclops.async.QueueFactories;
import cyclops.async.wait.WaitStrategy;

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
		
		assertThat(number,equalTo(9));
	}
	@Test @Ignore
	public void httpServer(){
	   
	    Vertx vertx = Vertx.factory.vertx();
	    CompletableFuture<HttpServer> server =new CompletableFuture<>();
	   

	                     
	    Queue<HttpServerRequest> reqs = QueueFactories.<HttpServerRequest>boundedNonBlockingQueue(1000, 
	                                                        WaitStrategy.spinWait())
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
	    
	    react.fromStream(reqs.stream())
	         .filter(req->req.getParam("num")!=null)
	         .peek(i->System.out.println("grouping " + i))
	         .grouped(2)
	         .map(list-> tuple(list.get(0).response(),list.get(1).response(),getParam(list.get(0)),getParam(list.get(1))))
	         .peek(i->System.out.println("peeking + "+i))
	         .peek(t->t.v1.end("adding "+(t.v3+t.v4)))
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
	
	
	
	
	@Test @Ignore
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
                   .forEachWithError(downloadQueue::add,System.err::println);
        
        //download asynchronously : all cyclops-react tasks are passed into vert.x
        react.fromStream(downloadQueue.stream())
             .peek(System.out::println)
             .map(url->vertx.createHttpClient().getNow(url,"",resp->resp.bodyHandler(body-> completedQueue.add(body.getString(0, body.length())))))
             .run();
        
        
        //handle the results
        completedQueue.stream()
                      .peek(next->System.out.println("just downloaded" + next))
                      .forEach(System.out::println);
        
        
        
    }
	
}
