# cyclops-streams

Defines an api for more advanced sequential Streams. Extends java.util.stream.Stream and jool.Seq to add even more functionality. Reactive Streams support available if simple-react added to the classpath.

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