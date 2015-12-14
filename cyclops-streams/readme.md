# cyclops-streams

cyclops-streams provides a sequential reactive-streams implmentation ( [SequenceM](http://static.javadoc.io/com.aol.cyclops/cyclops-sequence-api/6.2.1/com/aol/cyclops/sequence/SequenceM.html)) that forms the basis of the multi-threaded reactive-streams implementations in [simple-react](https://github.com/aol/simple-react), as such it provides all the advanced streaming functionality you would expect for an FRP library - windowing / batching (by size, time, state), failure handing, zipping, stream manipulation (insertAt, deleteBetween), advanced skip and limit operations (time based, conditional), asynchronous streaming operations (Future based operations and hotstreams).

cyclops-streams is java.util.Streams on steriods! An implementation of the cyclops-sequence-api [SequenceM](http://static.javadoc.io/com.aol.cyclops/cyclops-sequence-api/6.0.1/com/aol/cyclops/sequence/SequenceM.html) for more advanced sequential Streams. Extends [java.util.stream.Stream](https://docs.oracle.com/javase/8/docs/api/java/util/stream/Stream.html) and [jool.Seq](http://www.jooq.org/products/jOO%CE%BB/javadoc/0.9.7/org/jooq/lambda/Seq.html) to add even more functionality. 
Easy to use Reactive Streams support available if simple-react (v0.99.3 and above) is added to the classpath.

Features include

* Failure handling (recover / retry)
* windowing / batching (by time, size, state, predicate)
* zipping
* HotStreams
* reactive-streams
* Asynchronous execution
* Stream manipulation - insert/At, deleteAt
* Frequency management (xPer, onePer, jitter, debounce)
* Efficient reversal
* StreamUtils - static methods for java.util.stream.Streams
* Streamables - efficient / lazy replayable Streams as java.util.stream.Stream or SequenceM

* [See Streaming Examples wiki page for more details & examples](https://github.com/aol/cyclops/wiki/Streaming-examples)
* [Asynchronous operations](https://github.com/aol/cyclops/wiki/cyclops-streams-:-Asynchronous-Terminal-Operations)


## Getting cyclops-streams

* [![Maven Central : cyclops-streams](https://maven-badges.herokuapp.com/maven-central/com.aol.cyclops/cyclops-streams/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.aol.cyclops/cyclops-streams)


## Gradle

where x.y.z represents the latest version

compile 'com.aol.cyclops:cyclops-streams:x.y.z'

## Maven

```xml
<dependency>
    <groupId>com.aol.cyclops</groupId>
    <artifactId>cyclops-streams</artifactId>
    <version>x.y.z</version>
</dependency>
```
# Overview

* Fast sequential Streams that can run asyncrhonously
* Reactive Stream Support
* Efficient Stream reversal and right based operations (foldRight / scanRight etc)
* static Stream Utilities
* SequenceM implementation
* Terminal operations that return a Future to be populated asynchronously
* HotStream support




# Operators

Large number of Stream operators available on SequenceM or as static methods for java.util.Stream (SequenceM extends java.util.Stream)

Javadoc 
 
* [SequenceM](http://static.javadoc.io/com.aol.cyclops/cyclops-sequence-api/6.0.1/com/aol/cyclops/sequence/SequenceM.html)
* [Streamable](http://static.javadoc.io/com.aol.cyclops/cyclops-sequence-api/6.0.1/com/aol/cyclops/sequence/streamable/Streamable.html)
* [Async FutureOperations](http://static.javadoc.io/com.aol.cyclops/cyclops-sequence-api/6.0.1/com/aol/cyclops/sequence/future/FutureOperations.html)
* [Async IntOperators](http://static.javadoc.io/com.aol.cyclops/cyclops-sequence-api/6.0.1/com/aol/cyclops/sequence/future/IntOperators.html)
* [Async DoubleOperators](http://static.javadoc.io/com.aol.cyclops/cyclops-sequence-api/6.0.1/com/aol/cyclops/sequence/future/DoubleOperators.html)
* [Async LongOperators](http://static.javadoc.io/com.aol.cyclops/cyclops-sequence-api/6.0.1/com/aol/cyclops/sequence/future/LongOperators.html)

# Examples

## Creating Streams

SequenceM streams can be created via creational Operators such as

* of
* fromStream
* fromIterable
* fromList
* fromIntStream
* fromLongStream
* fromDoubleStream

### With efficient reversability


range, of(List), of(..values) all result in Sequences that can be efficiently reversed (and used in scanRight, foldRight etc)

```java
SequenceM.range(0,Integer.MAX_VALUE);

List<Intger> list;
SequenceM.fromList(list);

SequenceM.of(1,2,3)
        .reverse()
        .forEach(System.out::println);
```

## recover / Retry

Recover and retry operators allow different strategies for error recovery.

Recover allows a default value to be provided when an exception occurs

```java
SequenceM.of(1,2,3,4)
					.map(u->{throw new RuntimeException();})
					.recover(e->"hello")
					.firstValue()
//hello
```

Recovery can be linked to specific exception types.

```java
SequenceM.of(1,2,3,4)
					.map(i->i+2)
					.map(u->{throw ExceptionSoftener.throwSoftenedException( new IOException());})
					.recover(IOException.class,e->"hello")
					.firstValue()
//hello
```

With retry, a function will be called with an increasing back-off up to 7 times. 

```java
SequenceM.of( 1,  2, 3)
				.retry(this::loadData)
				.firstValue()
```
## FutureOperations

FutureOperations allow a Stream to be executed Asynchronously with the result of a terminal operation captured in a Future.

```java
Executor exec = Executors.newfixedThreadPool(1);
CompletableFuture<List<Integer>> list = SequenceM.of(1,2,3,4,5)
                                                 .map(this:expensiveOperation)
         										 .futureOperations(exec)
        										 .collect(Collectors.toList());
        										 
 // list populates Asynchronously
 
 list.join()
     .forEach(System.out::println);
```



## HotStream 

HotStreams are executed immediately and asynchronously (on the provided Executor - they will make use of a single thread only). They are also connectable (by potentially multiple Streams), the connected Stream receive those elements emitted *after* they connect.

```java
HotStream<Integer> range = SequenceM.range(0,Integer.MAX_VALUE)
									.peek(System.out::println)
									.hotStream(Executors.fixedThreadPool(1));

// will start printing out each value in range									
									
range.connect()
	.limit(100)
	.futureOperations(ForkJoinPool.commonPool())
	.forEach(System.out::println);
	
//will print out the first 100 values it recieves (after joining) on a separate thread	
```

## Reactive Streams

In conjunction with simple-react v0.99.3 and above SequenceM can be turned into a reactive-stream publisher or subscriber.
```java
CyclopsSubscriber<Integer> sub = SequenceM.subscriber();
		SequenceM.of(1,2,3).subscribe(sub);
		sub.sequenceM().toList();
		
//[1,2,3]
```
# Dependencies

* cyclops-invokedynamic
* cyclops-monad-api
* cyclops-sequence-api

## Recommended in conjunction with

simple-react
