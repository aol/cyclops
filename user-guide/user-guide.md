# Getting Started

* [Adding cyclops-react dependency](https://github.com/aol/cyclops-react/wiki/Adding-cyclops-react-as-a-dependency)
* [Adding the legacy cyclops-modules](https://github.com/aol/cyclops/wiki/A-guide-to-using-cyclops-as-independent-modules)

# Some common features
* [custom conversions and operators](https://github.com/aol/cyclops-react/wiki/Custom-operators-and-type-conversions) Available on all types.

# Collections

* [Working with persistent and mutable collections](http://gist.asciidoctor.org/?github-aol/simple-react//user-guide/Collections.adoc)
* [Lazy Extended Collections](https://medium.com/@johnmcclean/faster-lazy-extended-powerful-jdk-collections-5a519ab582ae#.m6cbebjs5) - Fast and powerful
* [Extending JDK 8 Collections](https://medium.com/@johnmcclean/extending-jdk-8-collections-8ae8d43dd75e#.3grjedpnb)

# Functions

* [Working with Lambdas](http://gist.asciidoctor.org/?github-aol/simple-react//user-guide/lambdas.adoc)
* [Fluent Functions in cyclops](https://medium.com/@johnmcclean/can-we-make-working-with-functions-easier-in-java-8-81ed9d1050f2#.ebtvdy31s) - blog post
* [Convertable mixin](https://github.com/aol/cyclops/wiki/cyclops-closures-:-Convertable-mixin)
* [Mutable boxes](https://github.com/aol/cyclops/wiki/cyclops-closures-%3A-Mutable--for--managing-and---mutating-mutable-variables/_edit)
* [LazyImmutable set once box](https://github.com/aol/cyclops/wiki/cyclops-closures-:-Lazy-Immutable---a-wrapping-class-for-setOnce-values)
* [Memoization](https://github.com/aol/cyclops/wiki/Caching-method-calls)
* [Partial Application](https://github.com/aol/cyclops/wiki/cyclops-functions-:-Partial-Application)
* [Exception Softener](https://github.com/aol/cyclops/wiki/cyclops-invoke-dynamic-:-ExceptionSoftener)
* [Utilities for working with Functions](https://github.com/aol/cyclops/wiki/Utilities-for-working-with-Java-8-Functions)
* [Memoization, Currying, Uncurrying and Type inferencing](https://github.com/aol/cyclops/wiki/Memoisation,-Currying,-Uncurrying-and-Type-Inferencing)
* [4 flavours of Java 8 Functons](https://medium.com/@johnmcclean/4-flavours-of-java-8-functions-6cafbcf5bb4f#.h29gipajc) Blog post

# Pattern matching and the visitor pattern

* [Built-in Visitor pattern](http://gist.asciidoctor.org/?github-aol/simple-react//user-guide/visitor-pattern.adoc)
* [Structural Pattern matching](http://gist.asciidoctor.org/?github-aol/simple-react//user-guide/pattern-matching.adoc)
* [Matchables pre-canned matching](https://github.com/aol/cyclops-react/wiki/Matchables)
* [Straightforward Structural Pattern Matching](https://medium.com/about-java/straightforward-structural-pattern-matching-d77155bac8da#.ibyghe671) - blog post

# Control Structures

## Combiners

* [Monoids & Semigroups](https://github.com/aol/cyclops-react/wiki/Monoids-&-Semigroups)

## Asynchronous execution

* [FutureW - a better CompletableFuture API](https://github.com/aol/cyclops-react/wiki/FutureW)

## Lazy Evaluation

* [Eval - monadic, tail recursive, lazy evaluation with caching](https://github.com/aol/cyclops-react/wiki/Eval)
* [Brief introduction to Eval](https://docs.google.com/presentation/d/1vqaangBCl9ibzewE7KXzMhevjmMWBrAgJixi5cOqb_A/edit?usp=sharing)

## Recursion

* [Trampoline](https://github.com/aol/cyclops/wiki/Trampoline-:-Stackless-Recursion-for-Java-8)
* [Trampolining: a practical guide for awesome Java Developers](https://medium.com/@johnmcclean/trampolining-a-practical-guide-for-awesome-java-developers-4b657d9c3076#.ecg7agr07)

## Sum Types (Try, Xor, Maybe, FeatureToggle]

* [Try functional exception handling for Java 8](https://github.com/aol/cyclops/wiki/Try-:-functional-exception-handling-for-Java-8)
* [Why cyclops-react Try](http://softwareengineering.stackexchange.com/a/319440/229756)
* [Try examples](https://github.com/aol/cyclops/wiki/Try-examples)
* [When Functional Try outperforms Try / Catch](https://medium.com/@johnmcclean/when-functional-try-outperforms-try-catch-c44e83ec7939#.mkmc0ihgq)
* [Feature Toggling](https://github.com/aol/cyclops/wiki/Enable-and-disable-production-features)
* [Feature Toggling with cyclops](https://medium.com/@johnmcclean/feature-toggling-with-cyclops-a29d1eead62c#.gqc0z6b2h) Blog post
* [Maybe: A lazy tail-recursive version of Optional](https://medium.com/@johnmcclean/future-java-today-9eef0e4dd126#.7274sd23t)
* [Validator : lazy active validator, works with Xor](https://github.com/aol/cyclops-react/wiki/Validator)
* [Xor : exclusive Or, a right biased Either type](https://github.com/aol/cyclops-react/wiki/Xor)

## Product types

* [Tuples from jOOλ](http://www.jooq.org/products/jOO%CE%BB/javadoc/0.9.11/org/jooq/lambda/tuple/package-frame.html): Javadoc link. cyclops-react extends jooλ
* [Power Tuples independent cyclops module](https://github.com/aol/cyclops/wiki/Power-Tuples)

## Product & Sum types

* [Ior](https://github.com/aol/cyclops-react/wiki/Ior)

# Streaming

* [Streaming overview](https://github.com/aol/cyclops/wiki/Streams-in-cyclops-overview) : ReactiveSeq, Streamable and more
* [A rational : Java 8 Streams 10 missing features](https://medium.com/@johnmcclean/java-8-streams-10-missing-features-ec82ee90b6c0)

## Performance

* [Optimizing cyclops-react Streams](https://medium.com/@johnmcclean/optimizing-simple-react-streams-30b6929fafeb#.dfdqwc7tv)
* [Fast Futures and Fast Future Pooling](https://github.com/aol/cyclops-react/wiki/FastFutures-and-FastFuture-Pooling) : Fast Futures ~2.5 faster than CompletableFutures in LazyFutureStreams

## Pushing data into Streams

* [StreamSource](https://github.com/aol/cyclops-react/wiki/StreamSource) for pushable Streams
* [Pipes event bus](https://github.com/aol/cyclops-react/wiki/Pipes-:-an-event-bus)
* [Pushing data into Java 8 Streams](http://jroller.com/ie/entry/pushing_data_into_java_8) - blog entry
* [Stackoverflow answer showing how to do it with Queues](http://stackoverflow.com/a/28967294)

## Repeatable Streams (Streamable)

* [Streamable](https://github.com/aol/cyclops/wiki/cyclops-streams-:-Streamable)
* [Streamable as a mixin](https://github.com/aol/cyclops/wiki/Mixins-:-Streamable)

## Plumbing Streams

* [Queues explained](https://github.com/aol/cyclops-react/wiki/Queues-explained)
* [Signals explained](https://github.com/aol/cyclops-react/wiki/Signals-Explained)
* [Topics explained](https://github.com/aol/cyclops-react/wiki/Topics-Explained)
* [Plumbing Streams with Queues, Topics and Signals](https://medium.com/@johnmcclean/plumbing-java-8-streams-with-queues-topics-and-signals-d9a71eafbbcc#.fbwoae34f)
* [Agrona wait free Queues ](https://github.com/aol/cyclops-react/wiki/Agrona-Wait-Free-Queues)
* [Wait strategies for working with Wait Free Queues](https://github.com/aol/cyclops-react/wiki/Wait-Strategies-for-working-with-Wait-Free-Queues)

### Backpressure

* [Applying Backpressure across Streams](https://medium.com/@johnmcclean/applying-back-pressure-across-streams-f8185ad57f3a#.szymzi9nj)

## ReactiveSeq (powerful sequential Streaming)

* [Scheduling Streams](https://github.com/aol/cyclops/wiki/cyclops-streams-:-Scheduling-Streams-(ReactiveSeq,--jOO%CE%BB--Javaslang-JDK))
* [Scheduling Streams example](https://medium.com/@johnmcclean/how-to-schedule-emission-from-a-stream-in-java-aa2dafda7c07#.6se0q2fpw) blog post
* [Asynchronous execution](https://github.com/aol/cyclops/wiki/cyclops-streams-:-Asynchronous-Terminal-Operations)
* [For comprehensions](https://github.com/aol/cyclops/wiki/cyclops-streams---ReactiveSeq---for-comprehension-operators-(forEach2,-forEach3))
* [ReactiveSeq examples](https://github.com/aol/cyclops/wiki/cyclops-streams-%3A-Streaming-examples/_edit)

## FutureStreams

* [LazyFutureStream overview](https://github.com/aol/cyclops-react/wiki/LazyFutureStream) : A powerful API for infinite parallel Streaming
* [SimpleReactStream overview](https://github.com/aol/cyclops-react/wiki/1.-simple-react-overview) : an easy to use API for finite eager parellel Streaming
* [LazyFutureStream & reactive-streams](https://github.com/aol/cyclops-react/wiki/A-Reactive-Streams-Publisher-or-Subscriber)
* [A simple API (simple-react) and a rich api (LazyFutureStream](https://github.com/aol/cyclops-react/wiki/A-simple-API,-and-a-Rich-API)
* [Asynchronous terminal operations](https://github.com/aol/cyclops-react/wiki/Asynchronous-terminal-operations)

### Operators

* [Batching, time control, sharding, zipping](https://github.com/aol/cyclops-react/wiki/Batching,-Time-Control,-Sharding-and-Zipping-Operators)
* [onFail](https://github.com/aol/cyclops-react/wiki/Error-handling-with-onFail)
* [Event based : forEachWithError etc](https://github.com/aol/cyclops-react/wiki/Reactive-Tasks-:-reactive-streams-based-operators)
* [For comprehensions](https://github.com/aol/cyclops-react/wiki/for-comprehensions-within-a-Stream)
* [Retry](https://github.com/aol/cyclops-react/wiki/Retry-functionality-in-SimpleReact)
* [Take, Skip and Sample](https://github.com/aol/cyclops-react/wiki/Take,-Skip-and-Sample)
* [Scheduling](https://github.com/aol/cyclops-react/wiki/Scheduling-Streams)

#### The tutorial (with videos)

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

#### Examples

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


### FutureStream concepts

* [Understanding LazyFutureStreams behavior](https://github.com/aol/cyclops-react/wiki/Understanding-LazyFutureStreams-behaviour)
* [Quick overview of SimpleReactStream](https://github.com/aol/cyclops-react/wiki/What-does-SimpleReact-do%3F)
* [Understanding the push-pull model of FutureStreams](https://github.com/aol/cyclops-react/wiki/Understanding-the-pull---push-model-of-simple-react)
* [Let the illusion die](https://medium.com/@johnmcclean/let-the-illusion-die-ad2318282bf8#.x90xktmqe) Build your own FutureStreams
* [FutureStream comparison matrix](https://github.com/aol/cyclops-react/wiki/Feature-comparison-matrix) : note EagerFutureStream is discontinued

#### Performance

* [Automatic optimization](https://github.com/aol/cyclops-react/wiki/Automatic-Optimization-%5BautoOptimize%5D)
* [Async vs sync **future** execution](https://github.com/aol/cyclops-react/wiki/async-and-sync-execution)
* [Automemoize](https://github.com/aol/cyclops-react/wiki/autoMemoize-(automatic-caching))

#### Acting on Futures or Acting on Results

* [Operating on futures](https://github.com/aol/cyclops-react/wiki/LazyFutureStream-operations-on-underlying-futures)
* [Acting on Futures](https://github.com/aol/cyclops-react/wiki/Acting-on-Futures-(actOnFutures-operator))

#### Configuration

* [React pools - elastic thread pools](https://github.com/aol/cyclops-react/wiki/ReactPools)
* [Fine Tuning SimpleReact](https://github.com/aol/cyclops-react/wiki/Fine-tuning-SimpleReact)
* [Sharing a forkJoinPool with Parallel Streams](https://github.com/aol/cyclops-react/wiki/Sharing-a-ForkJoinPool-with-ParallelStreams) - info purposes, don't do this!
* [Separating task executors](https://github.com/aol/cyclops-react/wiki/Separating-Task-Executors)


# AnyM - a functor for Monads

AnyMValue a monad for monadic values. AnyMSeq a monad for non-scalar monads.

* [AnyM intro](https://github.com/aol/cyclops-react/wiki/AnyM)
* [AnyM creational methods](https://github.com/aol/cyclops/wiki/cyclops-moand-api-:-Creating-an-instanceof-AnyM)
* [AnyM for comprehensions](http://gist.asciidoctor.org/?github-aol/simple-react//user-guide/visitor-pattern.adoc)
* [Introduction to the cyclops-monad API](https://medium.com/@johnmcclean/introducing-the-cyclops-monad-api-a7a6b7967f4d#.7r6hyotds)

#  Monad transformers via AnyM

* [OptionalT example](https://github.com/aol/cyclops/wiki/cyclops-monad-api-:-Monad-Transformer-OptionalT-overview-and-examples)

# For Comprehensions

Using the cyclops-react Do builder (prefer control.For) to the lower level Do.

* [Extensible for comprehensions](https://github.com/aol/cyclops/wiki/Extensible-For-Comprehensions-for-Java-8) : used to build type specific For Comprehensions elsewhere
* [For Comprehensions explained](https://github.com/aol/cyclops/wiki/For-Comprehensions-Explained)
* [The neophytes guide to Java 8 : Welcome to the Future](https://medium.com/@johnmcclean/neophytes-guide-to-java-8-welcome-to-the-future-83f432ce82a9#.imr0kl369) - the syntax is better today
* [Dependency injection with the Reader monad](https://medium.com/@johnmcclean/dependency-injection-using-the-reader-monad-in-java8-9056d9501c75#.gx6jrizbx) - cyclops now has it's own Reader monad.

# Higher Kinded Types

* [Higher Kinded Types in cyclops](https://github.com/aol/cyclops/wiki/Higher-Kinded-Types)

# Type classes 

[monad,applicative, functor, unit, monadPlus based on HKT]

* [Type classes](https://github.com/aol/cyclops/wiki/Type-classes)

# Integrations

Integrations include

1. AnyM support
2. For comprehension support
3. Higher Kinded Type encodings for 3rd party libraries
4. Lazy / faster collections by taking advantage of strengths of each library

* [Javaslang](https://github.com/aol/cyclops/blob/master/cyclops-javaslang/readme.md)
* [Guava](https://github.com/aol/cyclops/blob/master/cyclops-guava/readme.md)
* [Functional Java](https://github.com/aol/cyclops/blob/master/cyclops-functionaljava/readme.md)
* [RxJava](https://github.com/aol/cyclops/blob/master/cyclops-rx/readme.md)
* [Reactor](https://github.com/aol/cyclops/blob/master/cyclops-reactor/readme.md)

# Functional & Reactive Microservices

* [Microserver : micro-reactive](https://github.com/aol/micro-server/blob/master/micro-reactive/readme.md)
