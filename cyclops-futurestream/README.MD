# Cyclops FutureStream : Streams of asynchronously executing tasks / futures

FutureStream is a powerful lazy, asynchronous, parallel stream particularly well suited to performing blocking I/O operations. Get started by creating and configuring an instance of the Stream builder LazyReact.

e.g. to load, process and save a large number of files in parallel, we can use cyclops-futurestream like so..
```java
 new LazyReact(10,10).of(arrayOfFilenames)
                      .map(this::loadFiles)
                      .map(this::processData)
                      .map(this::saveFiles)
                      .run();
```

Other features in this module include

* StreamSource : for creating 'pushable' JDK Streams - Streams you can push new data into
* SimpleReact : an easy to use, eager FutureStream
* Pipes : for plumbing together your asynchronous parallel futurestreams

## Get cyclops-futurestream


* [![Maven Central : cyclops-futurestream](https://maven-badges.herokuapp.com/maven-central/com.oath.cyclops/cyclops-futurestream/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.oath.cyclops/cyclops-futurestream)   [![javadoc.io](https://javadocio-badges.herokuapp.com/com.oath.cyclops/cyclops-futurestream/badge.svg)](https://javadocio-badges.herokuapp.com/com.oath.cyclops/cyclops-futurestream)
* [Javadoc for cyclops-futurestream](http://www.javadoc.io/doc/com.oath.cyclops/cyclops-futurestream)


# Examples

FutureStream - parallel / async Streaming

```java
new LazyReact(100,100).generate(()->"data") //100 active tasks, 100 threads
                      .map(d->"produced on " + Thread.currentThread().getId())
                      .peek(System.out::println)
                      .map(this::process)
                      .flatMap(e->ReactiveSeq.range(0,e))
                      .run();
```


## cyclops.futurestream

| type | description | characteristics |
|------|-------------|-----------------|
| Pipes    | Event bus for managing data transfer via Adapters to connected data structures           |   Async              |



# More info

## <a name="futureStreams">FutureStreams

* [LazyFutureStream overview](https://github.com/aol/cyclops-react/wiki/LazyFutureStream) : A powerful API for infinite parallel Streaming
* [SimpleReactStream overview](https://github.com/aol/cyclops-react/wiki/simple-react-streams-overview) : an easy to use API for finite eager parellel Streaming
* [Stream type overview](https://github.com/aol/cyclops-react/wiki/simple-react-streams-overview)
* [LazyFutureStream & reactive-streams](https://github.com/aol/cyclops-react/wiki/A-Reactive-Streams-Publisher-or-Subscriber)
* [A simple API (simple-react) and a rich api (LazyFutureStream](https://github.com/aol/cyclops-react/wiki/A-simple-API,-and-a-Rich-API)
* [Asynchronous terminal operations](https://github.com/aol/cyclops-react/wiki/Asynchronous-terminal-operations)

### <a name="fsOperators">Operators

* [Batching, time control, sharding, zipping](https://github.com/aol/cyclops-react/wiki/Batching,-Time-Control,-Sharding-and-Zipping-Operators)
* [onFail](https://github.com/aol/cyclops-react/wiki/Error-handling-with-onFail)
* [Event based : forEachWithError etc](https://github.com/aol/cyclops-react/wiki/Reactive-Tasks-:-reactive-streams-based-operators)
* [For comprehensions](https://github.com/aol/cyclops-react/wiki/for-comprehensions-within-a-Stream)
* [Retry](https://github.com/aol/cyclops-react/wiki/Retry-functionality-in-SimpleReact)
* [Take, Skip and Sample](https://github.com/aol/cyclops-react/wiki/Take,-Skip-and-Sample)
* [Scheduling](https://github.com/aol/cyclops-react/wiki/Scheduling-Streams)

#### <a name="fsTutorial">The tutorial (with videos)

* [getting started](https://medium.com/@johnmcclean/reactive-programming-with-java-8-and-simple-react-getting-started-b2e34a5f80db#.ablu1d3y4)
* [error handling](https://medium.com/@johnmcclean/reactive-programming-with-java-8-and-simple-react-error-handling-b184b2197c7e)
* [filter /map/ reduce /flatMap](https://medium.com/@johnmcclean/reactive-programming-with-java-8-and-simple-react-filter-map-reduce-flatmap-ce5a557ad2d4)
* [choosing a stream type](https://medium.com/@johnmcclean/reactive-programming-with-java-8-and-simple-react-choosing-a-stream-type-c24dc4dab1af)
* [stream creation](https://medium.com/@johnmcclean/reactive-programming-with-java-8-and-simple-react-stream-creation-4f9918e768e5)
* [pooling reactors](https://medium.com/@johnmcclean/reactive-programming-with-java-8-and-simple-react-pooling-reactors-bf6ae2c0a23b)
* [pull / push model](https://medium.com/@johnmcclean/reactive-programming-with-java-8-and-simple-react-pull-push-model-70751d63628f)
* [flow control](https://medium.com/@johnmcclean/reactive-programming-with-java-8-and-simple-react-flow-control-d2e713b843a9)
* [batching and chunking](https://medium.com/@johnmcclean/reactive-programming-with-java-8-and-simple-react-batching-and-chunking-ecac62ce8bec)
* [sharding](https://medium.com/@johnmcclean/reactive-programming-with-java-8-and-simple-react-sharding-c766019153b5)
* [zipping streams](https://medium.com/@johnmcclean/reactive-programming-with-java-8-and-simple-react-zipping-streams-ed6579c5bbf7)
* [firstOf, anyOf, allOf](https://medium.com/@johnmcclean/reactive-programming-with-java-8-and-simple-react-firstof-allof-anyof-293298273364)
* [stream operations](https://medium.com/@johnmcclean/reactive-programming-with-java-8-and-simple-react-stream-operations-4e79df564735#.omuvs8b7d)
* [sequence operations](https://medium.com/@johnmcclean/reactive-programming-with-java-8-and-simple-react-sequence-operations-88e36032245f)

#### <a name="fsExamples">Examples

* [Getting started example](https://github.com/aol/cyclops-react/wiki/Getting-started-with-a-simple-example)
* [Building a non-blocking NIO Rest Client](https://github.com/aol/cyclops-react/wiki/Example-:-Building-a-non-blocking-NIO-rest-client)
* [Bulk loading files](https://github.com/aol/cyclops-react/wiki/Example-:-Bulk-loading-files)
* [Converting examples from RxJava](https://github.com/aol/cyclops-react/wiki/Example-:-Converting-examples-from-RxJava)
* [Implementing a data cache](https://github.com/aol/cyclops-react/wiki/Example-:-Implementing-a-data-cache)
* [Implementing a Quorum](https://github.com/aol/cyclops-react/wiki/Example-:-Implementing-a-Quorum)
* [Reacting to asynchronous events with a Stream of CompletableFutures](https://github.com/aol/cyclops-react/wiki/Example-:-Reacting-to-Asynchronous-Events-with-a-Stream-of-CompletableFutures)
* [Selecting the fastest algorithm](https://github.com/aol/cyclops-react/wiki/Example-:-Selecting-the-fastest-algorithm---result)
* [Asynchronous fun with Vert.x](https://medium.com/@johnmcclean/asynchronous-fun-with-vert-x-and-cyclops-react-6fcc6557fe03#.svs5aai84)
* [JDBC Processing](https://medium.com/@johnmcclean/jdbc-processing-options-with-cyclops-react-49d62b02f775#.9cqwlbzf1)


### <a name="fsConcepts">FutureStream concepts

* [Understanding LazyFutureStreams behavior](https://github.com/aol/cyclops-react/wiki/Understanding-LazyFutureStreams-behaviour)
* [Quick overview of SimpleReactStream](https://github.com/aol/cyclops-react/wiki/What-does-SimpleReact-do%3F)
* [Understanding the push-pull model of FutureStreams](https://github.com/aol/cyclops-react/wiki/Understanding-the-pull---push-model-of-simple-react)
* [Let the illusion die](https://medium.com/@johnmcclean/let-the-illusion-die-ad2318282bf8#.x90xktmqe) Build your own FutureStreams
* [FutureStream comparison matrix](https://github.com/aol/cyclops-react/wiki/Feature-comparison-matrix) : note EagerFutureStream is discontinued

#### <a name="fsPerformance">Performance

* [Automatic optimization](https://github.com/aol/cyclops-react/wiki/Automatic-Optimization-%5BautoOptimize%5D)
* [Async vs sync **future** execution](https://github.com/aol/cyclops-react/wiki/async-and-sync-execution)
* [Automemoize](https://github.com/aol/cyclops-react/wiki/autoMemoize-(automatic-caching))

#### <a name="actingOnFutures">Acting on Futures or Acting on Results

* [Operating on futures](https://github.com/aol/cyclops-react/wiki/LazyFutureStream-operations-on-underlying-futures)
* [Acting on Futures](https://github.com/aol/cyclops-react/wiki/Acting-on-Futures-(actOnFutures-operator))

#### <a name="fsConfiguration">Configuration

* [React pools - elastic thread pools](https://github.com/aol/cyclops-react/wiki/ReactPools)
* [Fine Tuning SimpleReact](https://github.com/aol/cyclops-react/wiki/Fine-tuning-SimpleReact)
* [Sharing a forkJoinPool with Parallel Streams](https://github.com/aol/cyclops-react/wiki/Sharing-a-ForkJoinPool-with-ParallelStreams) - info purposes, don't do this!
* [Separating task executors](https://github.com/aol/cyclops-react/wiki/Separating-Task-Executors)



## <a name="performance">Performance

* [Working with Primitives Directly](https://github.com/aol/cyclops-react/wiki/Working-with-Primitives-in-Streams)
* [Optimizing cyclops-react Streams](https://medium.com/@johnmcclean/optimizing-simple-react-streams-30b6929fafeb#.dfdqwc7tv)
* [Fast Futures and Fast Future Pooling](https://github.com/aol/cyclops-react/wiki/FastFutures-and-FastFuture-Pooling) : Fast Futures ~2.5 faster than CompletableFutures in LazyFutureStreams

## <a name="pushing">Pushing data into Streams

* [Stackoverflow answer showing how to do it with Queues](http://stackoverflow.com/a/28967294)

## <a name="streamSource">StreamSource

* [StreamSource](https://github.com/aol/cyclops-react/wiki/StreamSource) for pushable Streams
* [Pushing data into Java 8 Streams](http://jroller.com/ie/entry/pushing_data_into_java_8) - blog entry

## <a name="streamSource">Pipes

* [Pipes event bus](https://github.com/aol/cyclops-react/wiki/Pipes-:-an-event-bus)



## <a name="plumbing">Plumbing Streams

* [Queues explained](https://github.com/aol/cyclops-react/wiki/Queues-explained)
* [Signals explained](https://github.com/aol/cyclops-react/wiki/Signals-Explained)
* [Topics explained](https://github.com/aol/cyclops-react/wiki/Topics-Explained)
* [Plumbing Streams with Queues, Topics and Signals](https://medium.com/@johnmcclean/plumbing-java-8-streams-with-queues-topics-and-signals-d9a71eafbbcc#.fbwoae34f)
* [Agrona wait free Queues ](https://github.com/aol/cyclops-react/wiki/Agrona-Wait-Free-Queues)
* [Wait strategies for working with Wait Free Queues](https://github.com/aol/cyclops-react/wiki/Wait-Strategies-for-working-with-Wait-Free-Queues)

### <a name="backpressure">Backpressure

* [Applying Backpressure across Streams](https://medium.com/@johnmcclean/applying-back-pressure-across-streams-f8185ad57f3a#.szymzi9nj)


