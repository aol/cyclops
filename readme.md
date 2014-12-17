#Simple React : Simple Fluent Api for Functional Reactive Programming with Java 8

Simple React is the new Java 8 based concurrency mechanism for LANA. It is a fluent API built on top of Java 8 CompletableFuture. It provides a focused, simple and limited Reactive API aimed at solving the 90% use case - but without adding complexity.

##Why Simple React

Simple React is built on top of JDK standard libraries and unlike other Reactive implementations for Java does not re-invent Streams, Functional interfaces etc.

##Data flow

Simple react starts with an array of Suppliers which generate data other functions will react to. Each supplier will be passed to an Executor to be executed, potentially on a separate thread. Each additional step defined when calling Simple React will also be added as a linked task, also to be executed, potentially on a separate thread.
##Example 1 : reacting with completablefutures

React **with**

			List<CompletableFuture<Integer>> futures = new SimpleReact()
				.<Integer, Integer> react(() -> 1, () -> 2, () -> 3)
				.with((it) -> it * 100);

In this instance, 3 suppliers generate 3 numbers. These may be executed in parallel, when they complete each number will be multiplied by 100 - as a separate parrellel task (handled by a ForkJoinPool or configurable task executor). A List of Future objects will be returned immediately from Simple React and the tasks will be executed asynchronously.
React with does not block.
##Example 2 : chaining

React **then**

	 	new SimpleReact()
	 			.<Integer, Integer> react(() -> 1, () -> 2, () -> 3)
				.then((it) -> it * 100)
				.then((it) -> "*" + it)


React then allows event reactors to be chained. Unlike React with, which returns a collection of Future references, React then is a fluent interface that returns the React builder - allowing further reactors to be added to the chain.
React then does not block.
React with can be called after React then which gives access to the full CompleteableFuture api. CompleteableFutures can be passed back into SimpleReact via SimpleReact.react(streamOfCompleteableFutures);
See this blog post for examples of what can be achieved via CompleteableFuture :- http://www.nurkiewicz.com/2013/12/promises-and-completablefuture.html

##Example 3: blocking

React and **block**

			List<String> strings = new SimpleReact()
				.<Integer, Integer> react(() -> 1, () -> 2, () -> 3)
				.then((it) -> it * 100)
				.then((it) -> "*" + it)
				.block();
						
In this example, once the current thread of execution meets the React block method, it will block until all tasks have been completed. The result will be returned as a List. The Reactive tasks triggered by the Suppliers are non-blocking, and are not impacted by the block method until they are complete. Block, only blocks the current thread.

##Example 4: breakout

Sometimes you may not need to block until all the work is complete, one result or a subset may be enough. To faciliate this, block can accept a Predicate functional interface that will allow SimpleReact to stop blocking the current thread when the Predicate has been fulfilled. E.g.

			List<String> strings = new SimpleReact()
				.<Integer, Integer> react(() -> 1, () -> 2, () -> 3)
				.then(it -> it * 100)
				.then(it -> "*" + it)
				.block(status -> status.getCompleted()>1);

In this example the current thread will unblock once more than one result has been returned. The available fields on the status object are :-
completed
errors
total
elapsedMillis

##Example 5 : onFail

React **onFail**
onFail allows disaster recovery for each task (a separate onFail should be configured for each react phase that can fail). E.g. if reading data from an external service fails, but default value is acceptable - onFail is a suitable mechanism to set the default value.

			List<String> strings = new SimpleReact()
				.<Integer, Integer> react(() -> 100, () -> 2, () -> 3)
				.then(it -> {
					if (it == 100)
						throw new RuntimeException("boo!");
		
					return it;
				})
				.onFail(e -> 1)
				.then(it -> "*" + it)
				.block();

In this example, should the first "then" phase fail, the default value of 1 will be used instead.

##Example 6: non-blocking

React and **allOf**

allOf is a non-blocking equivalent of block. The current thread is not impacted by the calculations, but the reactive chain does not continue until all currently alloted tasks complete. The allOf task is then provided with a list of the results from the previous tasks in the chain. Any parallelStreams used inside allOf will reuse the SimpleReact ExecutorService - if it is a ForkJoinPool (which it is by default), rather than the Common ForkJoinPool parallelStreams use by default. 

        	boolean blocked[] = {false};
			new SimpleReact()
				.<Integer, Integer> react(() -> 1, () -> 2, () -> 3)	
				.then(it -> {
					try {
						Thread.sleep(50000);
					} catch (Exception e) {
						
					}
					blocked[0] =true;
					return 10;
				})
				.allOf( it -> it.size());

		
			assertThat(blocked[0],is(false));

In this example, the current thread will continue and assert that it is not blocked, allOf could continue and be executed in a separate thread.

first() is a useful method to extract a single value from a dataflow that ends in allOf. E.g. 


        	boolean blocked[] = {false};
			int size = new SimpleReact()
				.<Integer, Integer> react(() -> 1, () -> 2, () -> 3)	
				.then(it -> {
					try {
						Thread.sleep(50000);
					} catch (Exception e) {
						
					}
					blocked[0] =true;
					return 10;
				})
				.allOf( it -> it.size()).first();

		
			assertThat(blocked[0],is(false));

##Example 7: non-blocking with the Stream api

             List<Integer> result =new SimpleReact()
             	.<Integer, Integer> react(() -> 1, () -> 2, () -> 3)
				.then(it -> {
					return it*200;
				})
				.allOf( (it )->{
					return it.parallelStream()
					.filter( f -> f>300)
					.map(m -> m-5)
					.reduce(0, (acc,next) -> acc+next); 
				}).block();

		
			assertThat(result.size(),is(1));
			assertThat(result.get(0),is(990));


In this example we block the current thread to get the final result, the allOf task uses the Streams api to setup another FRP chain that takes the inputs from our initial parellel jobs ([1,2,3] -> [200,400,600]), and does a filter / map/ reduce on them in parallel.

##Example 6 : capturing exceptions

React *capture*

onFail is used for disaster recovery (when it is possible to recover) - capture is used to capture those occasions where the full pipeline has failed and is unrecoverable.

			List<String> strings = new SimpleReact()
				.<Integer, Integer> react(() -> 1, () -> 2, () -> 3)
				.then(it -> it * 100)
				.then(it -> {
					if (it == 100)
						throw new RuntimeException("boo!");
		
					return it;
				})
				.onFail(e -> 1)
				.then(it -> "*" + it)
				.then(it -> {
					
					if ("*200".equals(it))
						throw new RuntimeException("boo!");
		
					return it;
				})
				.capture(e -> logger.error(e.getMessage(),e))
				.block();


In this case, strings will only contain the two successful results (for ()->1 and ()->3), an exception for the chain starting from Supplier ()->2 will be logged by capture. Capture will not capture the exception thrown when an Integer value of 100 is found, but will catch the exception when the String value "*200" is passed along the chain.

##Example 7 : using the Streams Api

React and the *Streams Api*

It is possible to reuse the internal SimpleReact ExecutorService for JDK 8 parallelStreams. SimpleReact uses a ForkJoinPool as the ExecutorService by default, and to reuse the ExecutorService with parallelStreams it must be a ForkJoinPool - so if you want to supply your own make sure it is also a ForkJoinPool. 

A mechamism to share ExecutorServices is provided via the *collectResults* method, this collects the results from the current active tasks, and clients are given the full range of SimpleReact blocking options.  The results will then be provided to a function provided by client code when the *submit* is called. A way to merge all these steps into a single method is also provided (submitAndBlock). The easiest way to do this is via the submitAndBlock method 

Example :

		 Integer result = new SimpleReact()
				.<Integer, Integer> react(() -> 1, () -> 2, () -> 3)
				.then(it -> it * 200)
				.<List<Integer>,Integer>submitAndblock(
						it -> it.parallelStream()
								.filter(f -> f > 300)
								.map(m -> m - 5)
								.reduce(0, (acc, next) -> acc + next));
								
To use a different ExecutorService than SimpleReact's internal ExecutorService leverae parallelStream directly from block() 

			ImmutableMap<String,Integer> dataSizes = new SimpleReact()
				.<Integer, Integer> react(() -> 1, () -> 2, () -> 30,()->400)
				.then(it -> it * 100)
				.then(it -> "*" + it)
				.<String>block()
				.parallelStream()
				.filter( it -> it.length()>3)
				.map(it -> ImmutableMap.of(it,it.length()))
				.reduce(ImmutableMap.of(),  (acc, next) -> ImmutableMap.<String, Integer>builder()
					      .putAll(acc)
					      .putAll(next)
					      .build());

In this example the converted Strings are filtered by length and an ImmutableMap created using the Java 8 Streams Api.