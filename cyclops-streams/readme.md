# cyclops-streams

java.util.Streams on steriods! An implementation of the cyclops-sequence-api for more advanced sequential Streams. Extends java.util.stream.Stream and jool.Seq to add even more functionality. 
Easy to use Reactive Streams support available if simple-react is added to the classpath.

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
* Efficient Stream reversal
* static Stream Utilities
* SequenceM implementation
* Terminal operations that return a Future to be populated asynchronously
* HotStream support


# Dependencies

cyclops-invokedynamic
cyclops-monad-api
cyclops-sequence-api

## Recommended in conjunction with

simple-react

# Getting cyclops-sequence-api

## Gradle

where x.y.z represents the latest version

compile 'com.aol.cyclops:cyclops-streams:x.y.z'

# Operators

Large number of Stream operators available on SequenceM or as static methods for java.util.Stream (SequenceM extends java.util.Stream)

* Reactive Streams support
* Connectable Hot Streams 
* [Core Operators](https://github.com/aol/cyclops/blob/master/cyclops-sequence-api/src/main/java/com/aol/cyclops/sequence/SequenceM.java)
* [Async Operators](https://github.com/aol/cyclops/blob/master/cyclops-sequence-api/src/main/java/com/aol/cyclops/sequence/future/FutureOperations.java)
* [Async Operators ints](https://github.com/aol/cyclops/blob/master/cyclops-sequence-api/src/main/java/com/aol/cyclops/sequence/future/IntOperators.java)
* [Async Operators doubles](https://github.com/aol/cyclops/blob/master/cyclops-sequence-api/src/main/java/com/aol/cyclops/sequence/future/DoubleOperators.java)
* [Async Operators longss](https://github.com/aol/cyclops/blob/master/cyclops-sequence-api/src/main/java/com/aol/cyclops/sequence/future/LongOperators.java)
* [Available as static methods](https://github.com/aol/cyclops/blob/master/cyclops-streams/src/main/java/com/aol/cyclops/streams/StreamUtils.java)


## StreamUtils

## Multiple simultanous reduction with Monoids

    Monoid<String> concat = Monoid.of("",(a,b)->a+b);
	Monoid<String> join = Monoid.of("",(a,b)->a+","+b);


	StreamUtils.reduce(Stream.of("hello", "world", "woo!"),Stream.of(concat,join));

Results in ["helloworldwoo!",",hello,world,woo!"]

See also Monoid.reduce(Stream s)


## Cycle 

    StreamUtils.cycle(Stream.of(1,2,3)).limit(6).collect(Collectors.toList())
 
 Results in [1,2,3,1,2,3]
 
## Reverse

    StreamUtils.reverse(Stream.of(1,2,3)).collect(Collectors.toList())
   
Results in [3,2,1]  

## Stream creation from Iterable and Iterator

From Iterable

    StreamUtils.stream(Arrays.asList(1,2,3)).collect(Collectors.toList())

From Iterator

	StreamUtils.stream(Arrays.asList(1,2,3).iterator()).collect(Collectors.toList())    
	
## Reverse a Stream
 
 
     ReversedIterator.reversedStream(LazySeq.iterate(class1, c->c.getSuperclass())
						.takeWhile(c->c!=Object.class).toList());