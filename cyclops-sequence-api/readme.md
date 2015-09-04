# cyclops-sequence-api

Defines an api for more advanced sequential Streams. Extends java.util.stream.Stream and jool.Seq to add even more functionality. Reactive Streams support available if simple-react added to the classpath.

* Advanced & powerful Streaming api
* Reactive Streams support
* Asynchronous single threaded Streaming
* Terminal operations that return a Future to be populated asynchronously
* Reversable Spliterators for efficient Stream reversal and right based operations
* HotStream support

This primarily defines the interfaces to be used for cyclops Streaming, for an implementation see cyclops-stream.

# Dependencies

cyclops-invokedynamic

## Recommended in conjunction with

cyclops-streams
cyclops-monad-api
simple-react

# Getting cyclops-sequence-api

## Gradle

where x.y.z represents the latest version

compile 'com.aol.cyclops:cyclops-sequence-api:x.y.z'