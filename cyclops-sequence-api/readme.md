# cyclops-sequence-api

Defines an api for more advanced sequential Streams. Extends java.util.stream.Stream and jool.Seq to add even more functionality. Reactive Streams support available if simple-react added to the classpath.

## Getting cyclops-sequence-api

* [![Maven Central : cyclops-sequence-api](https://maven-badges.herokuapp.com/maven-central/com.aol.cyclops/cyclops-sequence-api/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.aol.cyclops/cyclops-sequence-api)


## Gradle

where x.y.z represents the latest version

compile 'com.aol.cyclops:cyclops-sequence-api:x.y.z'

## Maven

```xml
<dependency>
    <groupId>com.aol.cyclops</groupId>
    <artifactId>cyclops-sequence-api</artifactId>
    <version>x.y.z</version>
</dependency>
```
# Overview

* Advanced & powerful Streaming api
* Reactive Streams support
* Asynchronous single threaded Streaming
* Terminal operations that return a Future to be populated asynchronously
* Reversable Spliterators for efficient Stream reversal and right based operations
* Retry / onFail
* HotStream support

This primarily defines the interfaces to be used for cyclops Streaming, for an implementation see cyclops-stream.

# The Decomposable Interface  / Trait

The Decomposable Interface defines an unapply method that is used to convert the implementing Object into an iterable. This can be used to control how Cyclops performs recursive decomposition.

```java
	public <I extends Iterable<?>> I unapply();
```
	
### Interfaces that extend Decomposable

* ValueObject
* StreamableValue
* CachedValues, PTuple1-8

## Coercing any Object to a Decomposable

```java
    As.asDecomposable(myObject).unapply().forEach(System.out::println);
```

com.aol.cyclops.dynamic.As provides a range of methods to dynamically convert types

# Dependencies

cyclops-invokedynamic

## Recommended in conjunction with

cyclops-streams
cyclops-monad-api
simple-react