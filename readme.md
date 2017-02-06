<img width="820" alt="screen shot 2016-02-22 at 8 44 42 pm" src="https://cloud.githubusercontent.com/assets/9964792/13232030/306b0d50-d9a5-11e5-9706-d44d7731790d.png">

Powerful Streams and functional data types for building modern Java 8 applications. We extend JDK interfaces where possible for maximum integration. 

This is the 2.x branch for 1.x click the link below

* [1.x](https://github.com/aol/cyclops-react/tree/1.x)

2.x is namespaced differently so as not to collide with 1.x on the same classpath. cyclops-react 2.x is coming very soon! [Explore the code](https://github.com/aol/cyclops-react/tree/master/src/main/java/cyclops)


# Getting cyclops-react

* [![Maven Central : cyclops-react](https://maven-badges.herokuapp.com/maven-central/com.aol.simplereact/cyclops-react/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.aol.simple-react/cyclops-react)

* Stackoverflow tag [cyclops-react](http://stackoverflow.com/search?q=cyclops-react)

# Documentation

* [User Guide](https://github.com/aol/cyclops-react/wiki) Primarily tailored for 1.x still.
* [javadoc](http://www.javadoc.io/doc/com.aol.simplereact/cyclops-react/)

## Gradle

where x.y.z represents the latest version

```groovy
compile 'com.aol.simplereact:cyclops-react:x.y.z'
```

## Maven

```xml
<dependency>
    <groupId>com.aol.simplereact</groupId>
    <artifactId>cyclops-react</artifactId>
    <version>x.y.z</version>
</dependency>
```
# Features

* Deep consistent and integrated API across a broad range of key functional types
* ReactiveSeq extends JDK Stream and offers multiple modes of execution - synchonous, asynchronous,
   asynchronous with backpressure, parallel and more.
* Fast Lazy Collection types for efficient functional transformations
* FutureStreams for parallel I/O
* Core components for building asynchronous applications
* Advanced functional features such as for comprehensions and pattern matching integrated into the API
* Clean simulated Higher Kinded Types via Witness Types
* Extensions for JDK types : Optional, CompletableFutures and Streams

* Third party integrations via cyclops modules (including Scala, JavasLang, FunctionalJava, Reactor and RxJava)

# Getting started examples

## Streaming

Sequential Streams, with retry and forEach result + error.

```java
ReactiveSeq.range(0,1000)
           .map(this::processNext)
           .retry(this::mightFail)
           .forEach(System.out::println, System.err::println);
```

Mixed Sequential and Parallel Stream

```java
ReactiveSeq.range(0, 1000)
           .parallel(new ForkJoinPool(10),par -> par.map(this::parallelTransform))
           .map(this::sequentialTransform)
           .forEach(System.out::println,System.err::println,this::finished);
``` 

Single-threaded scatter / gather 

```java
ReactiveSeq.of(1,2,3,4)
           .fanOut(s1->s1.filter(i->i%2==0).map(this::group1),
                   s2->s2.filter(i->i%2!=0).map(this::group2))
           .toListX();
 ```
 
Parallel scatter / gather

```java
ReactiveSeq.of(1,2,3,4)
           .parallelFanOutZipIn(s1->s1.filter(i->i%2==0).map(this::group1),
                                s2->s2.filter(i->i%2!=0).map(this::group2),(g1,g2)->process(g1,g2))
           .toListX();
 ```


Replaying Streams

```java
Stream<Integer> stream = ReactiveSeq.range(0,1000)
                                    .map(i->i*2);

stream.forEach(System.out::println);
List<Integer> replayed = stream.collect(Collectors.toList());
stream.map(i->"hello  " + i)
      .forEach(System.out::println);
```

Asynchronous stream execution

```java
//cyclops.async.Future
 Executor ex = Executors.newFixedThreadPool(1)
 Future<Integer> asyncResult = ReactiveSeq.of(1,2,3,4)
                                          .foldFuture(ex,s->s.reduce( 50,(acc,next) -> acc+next));
                                           
 asyncResult.peek(System.out::println)
            .map(this::processResult);
       
```

Lazy execution using extended Collections
```java
Eval<Integer> lazyResult = ListX.of(1,2,3,4)
                                 .map(i->i*10)
                                 .foldLazy(s->s
                                 .reduce( 50,(acc,next) -> acc+next));
```

Lazy / terminating fold

```java
ReactiveSeq.generate(this::process)
           .map(data->data.isSuccess())
           .combine((a,b)-> a ? false : true, (a,b) -> a|b)
           .findFirst(); //terminating reduction on infinite data structure

```

FutureStream - parallel / async Streaming

```java
new LazyReact(100,100).generate(()->"data") //100 active tasks, 100 threads
                      .map(d->"produced on " + Thread.currentThread().getId())
                      .peek(System.out::println)
                      .map(this::process)
                      .flatMap(e->ReactiveSeq.range(0,e))
                      .run();
```

reactive-streams : Event Driven Push based Streams

```java
Executor exec = Executors.newFixedThreadPool(1);
Either<Throwable,Integer> resultOrError =    Spouts.publishOn(ReactiveSeq.of(1,2,3,4,5),exec);
                                                   .combine((a, b) -> a < 5, Semigroups.intSum)
                                                   .findFirstOrError();
```
In the example above a synchronous Stream is executed on the provided Executor and it's output pushed into another reactive-stream that sums numbers so long as the total remains below 5. Once the total exceeds 5 it's is pushed asynchronously into the returned Either type (alternatively an error may be pushed down instead). The Either type can continue the reactive chain. The code above is completely non-blocking.
Streams created using Spouts factory can by default support non-blocking backpressure as defined in the reactive-streams spec.
                               
Backpressure free : Event Driven Push based Streams

```java
 Executor execA = Executors.newFixedThreadPool(1);
 Executor execB = Executors.newFixedThreadPool(1);
 Maybe<Integer> resultOrError =    Spouts.observeOn(ReactiveSeq.of(1,2,3,4,5),execA)
                                         .zipP(Spouts.observeOn(ReactiveSeq.of(100,200),execB, (a,b)->a+b)
                                         .findOne();
```
The Spouts observeOn and async operators create event driven Streams that do not have the overhead of managing backpressure. In the above example the first result is pushed asynchronously into the reactive Maybe type.



# 2.x Type dictionary

## cyclops

| type | description | characteristics |
|------|-------------|-----------------|
| Optionals     | Extension methods, for comprehensions and Higher kinded type classes            | Higher kinded                 |
| CompletableFutures    | Extension methods, for comprehensions and Higher kinded type classes            | Higher kinded                 |
| Streams     | Extension methods, for comprehensions and Higher kinded type classes            | Higher kinded                 |
| Semigroups     | Combiners for a wide range of types           |                  |
| Monoids     | Combiners with an identity value for a wide range of types            |              |


## cyclops.stream

## cyclops.stream.ReactiveSeq

cyclops defines reactive (push/ event drive) and coreactive (iterative / interactive) Streaming capabilities via the interface ReactiveSeq.

There are 4 concrete implementations for this interface :-

| concrete type | factories | description | characteristics |
|------|-------------|-------------|-----------------|
| ReactiveStreamX    | Spouts | Asynchronous push based Streams. Optionally back-pressure aware (via reactive-streams)              | Reactive (push),Lazy, parallel option, integrated primitive support, replayable, Higher kinded                |
| StreamX    | ReactiveSeq | Synchronous sequential stream, extends JDK Stream interface. Custom Stream faster engine. Streams are replayable.              | Coreactive (pull), Lazy, parallel option, integrated primitive support, replayable, Higher kinded, Operator fusion                |
| OneShotStreamX    | Streams | Synchronous sequential stream, extends JDK Stream interface. Custom Stream faster engine. Streams are replayable.              | Lazy, parallel option, integrated primitive support, replayable, Higher kinded, Operator fusion                |
| FutureStream     | LazyReact | Asynchronous and parallel stream             | Lazy, async, parallel, Reactive                 |


Interfaces

| type | description | characteristics |
|------|-------------|-----------------|
| FutureStream     | Asynchronous and parallel stream             | Lazy, async, parallel, Reactive                 |
| Spouts     | Creational factory methods for push based Streams with optional non-blocking back pressure (via reactive-streams).              | Lazy, parallel option, integrated primitive support, replayable, Higher kinded, Operator fusion                |
| ReactiveSeq     | Synchronous sequential stream, extends JDK Stream interface. Custom Stream faster engine. Streams are replayable.              | Lazy, parallel option, integrated primitive support, replayable, Higher kinded, Operator fusion                |
| Streamable     | Capturing and caching replayable Stream type              | Lazy, caching                |
| StreamSource     | Push data asynchronously into synchronous sequential or parallel Streams (e.g. JDK Stream, ReactiveSeq)              |             |

## cyclops.async

| type | description | characteristics |
|------|-------------|-----------------|
| Future     | Potentially asynchronous task that may populate a result in the Future            | Eager async, Higher kinded                 |
| SimpleReact     | Asynchronous bulk operations on Futures            | Eager async                 |
| LazyReact     | Builder for FutureStreams           |                 |
| Adapter     | Interface for data transfer Adapters to connected Streams. Closing the adapter, closes the streams (impls - Queue, Topic, Signal)           |    Async             |
| Queue     | Facilitates asyncrhonous data transfer to mulitiple connected Streams, via any java.util.Queue impl, Continuations toallow consumers to become producers.           |    Async             |
| Topic     | Asynchronous data transfer to multiple connected Streams, all connected Streams recieve each message           |   Async              |
| Signal    | Asynchronous data transfer - changes in data are broadcast to connected Streams           |   Async              |
| Pipes    | Event bus for managing data transfer via Adapters to connected data structures           |   Async              |


## cyclops.control : Sum Types

| type | description | characteristics |
|------|-------------|-----------------|
| Maybe     | Lazy analogue of Optional (Just/None)             |  Optionally Reactive or Coreactive, Lazy, tail recursive,sum type, Higher kinded               |
| Try     | Represents a value or an exception. Only specified Exceptions are caught on creation by default.            | Eager, avoids error hiding                 |
| Ior     | Inclusive Or, maybe one of two values or both            | Eager, sum and product type                 |
| Xor     | Exclusive Or, maybe one of two values, eager analogue of Either            | Eager, sum type                 |
| Either     | Lazy Either type maybe one of two values, lazy analogue of Xor            | Optionally Reactive or Coreactive, Lazy, tail recursive, sum type                 |
| Either3     | Lazy Either type maybe one of three values            | Optionally Reactive or Coreactive, Lazy, tail recursive, sum type                 |
| Either4     | Lazy Either type maybe one of four values            | Optionally Reactive or Coreactive, Lazy, tail recursive, sum type                 |
| Either5     | Lazy Either type maybe one of five values            | Optionally Reactive or Coreactive, Lazy, tail recursive, sum type                 |

## cyclops.collections (mutable / immutable)

| type | description | characteristics |
|------|-------------|-----------------|
| ListX     | Functional extensions for working with Lists            | Optionally Reactive or Coreactive, Lazy, mutable, immutable, 3rd party support, Higher kinded                 |
| DequeX     | Functional extensions for working with Deques            | Optionally Reactive or Coreactive, Lazy, mutable, immutable, 3rd party support, Higher kinded                 |
| QueueX     | Functional extensions for working with Queues            | Optionally Reactive or Coreactive, Lazy, mutable, immutable, 3rd party support, Higher kinded                 |
| SetX     | Functional extensions for working with Sets            | Optionally Reactive or Coreactive, Lazy , mutable, immutable, 3rd party support                |
| SortedSetX     | Functional extensions for working with SortedSets            | Optionally Reactive or Coreactive, Lazy, mutable, immutable                 |
| MapX     | Functional extensions for working with Maps            | Eager, mutable, immutable                 |

## cyclops.collections.persistent

| type | description | characteristics |
|------|-------------|-----------------|
| PStackX     | Functional extensions for working with persistent Lists            | Optionally Reactive or Coreactive, Lazy, persistent, 3rd party support, Higher kinded                 |
| PVectorX     | Functional extensions for working with persistent Vectors            | Optionally Reactive or Coreactive, Lazy, persistent, 3rd party support, Higher kinded                 |
| PSetX     | Functional extensions for working with persistent Sets            | Optionally Reactive or Coreactive, Lazy, persistent, 3rd party support                 |
| POrderedSetX     | Functional extensions for working with persistent Ordered Sets            | Optionally Reactive or Coreactive, Lazy, persistent, 3rd party support                 |
| PQueueX     | Functional extensions for working with persistent Queues           | Optionally Reactive or Coreactive, Lazy, persistent, 3rd party support, Higher kinded                 |
| PBagX     | Functional extensions for working with persistent Bags (set like collections that allow duplicates)          | Optionally Reactive or Coreactive,Lazy, persistent, 3rd party support                 |
| PMapX     | Functional extensions for working with persistent Maps          | Map, persistent, 3rd party support                 |


## cyclops.control + cyclops.free

| type | description | characteristics |
|------|-------------|-----------------|
| Eval     | Lazy evaluation, optional caching            | Optionally Reactive or Coreactive, Lazy, tail recursive, Higher kinded                 |
| Trampoline     | Easy to use trampoline implementations (see also Free using SupplierKind)            | Lazy                |
| Free     | Higher kinded implementation of the Free monad for Java, facilitates functional interpreters. Free + SupplierKind (higher kinded Fn0) = a more advanced Trampoline implementation.            | Lazy                |

## cyclops.box

| type | description | characteristics |
|------|-------------|-----------------|
| LazyImmutable     | Represents a set once only box type            | Eager execution                 |
| Mutable     | A mutable generic box type           | Eager execution                 |
| MutableInt     | A mutable primitive box type for ints          | Eager execution                 |
| MutableLong     | A mutable primitive box type for longs         | Eager execution                 |
| MutableDouble     | A mutable primitive box type for doubles        | Eager execution                 |
| MutableFloat     | A mutable primitive box type for floats        | Eager execution                 |
| MutableChar     | A mutable primitive box type for characters     | Eager execution                 |
| MutableByte     | A mutable primitive box type for bytes        | Eager execution                 |
| MutableBoolean     | A mutable primitive box type for booleans        | Eager execution                 |

## cyclops.functions

| type | description | characteristics |
|------|-------------|-----------------|
| Fn0-Fn8     | Extended Function interfaces supporting map / flatMap / applicative operations, currying, partial application, lifting, composition and more           |                  |
| AnyMFn0-AnyMFn2     | Type aliases for monadically lifted functions          |                  |
| C3-C5     | Additional Consumers           |                  |
| FluentFunctions     | A fluent API for working with Functions - composition, lifting, AOP and more           |                  |
| Lambda    | An API for working with anomyous lambda expressions (type inferencing)          |                  |
| Memoize     | An API for caching pure functions         |                 |
| PartialApplicator     | An API for Partial Application of functions       |                  |
| Curry / CurryConsumer / CurryVariance     | An API for currying functions        |                  |
| Monoid     | A function for combining values of the same type, with an identity value     |                  |
| Semigroup     | A function for combining values of the same type        |                 |
| Reader     | A transformable function : useful to implement dependency injection or Singletons in a functional style            |                 |


## Higher level abstractions

### cyclops.monads

| type | description | characteristics |
|------|-------------|-----------------|
| AnyM     | Type safe monadic wrapper for any monad type            | Higher kinded                 |
| AnyMValue     | Type safe monadic wrapper for any monadic sum type            | Higher kinded                 |
| AnyMSeq     | Type safe monadic wrapper for any monadic non-scalar type            | Higher kinded                 |

#### cyclops.monads.transformers

| type | description | characteristics |
|------|-------------|-----------------|
| ListT     | Type safe list transformer of manipulating lists in a monadic context            | Higher kinded                 |
| StreamT     | Type safe Stream transformer of manipulating Streams in a monadic context            | Higher kinded                 |
| FutureT     | Type safe future transformer of manipulating futures in a monadic context            | Higher kinded                 |
| CompletableFutureT     | Type safe CompletableFuture transformer of manipulating futures in a monadic context     | Higher kinded |
| OptionalT     | Type safe Optional transformer of manipulating optionals in a monadic context          | Higher kinded                |
| MaybeT     | Type safe Maybe transformer of manipulating Maybes in a monadic context            | Higher kinded                 |
| EvalT     | Type safe Eval transformer of manipulating Evals in a monadic context            | Higher kinded                 |


### cyclops.typeclasses

| type | description | characteristics |
|------|-------------|-----------------|
| Pure     | Embed a value inside a type            | Higher kinded                 |
| Functor     | Transform embedded values            | Higher kinded                 |
| Applicative     | Apply a function to embedded values            | Higher kinded                 |
| Monad     | Apply flattening transformations to embedded values            | Higher kinded                 |
| Traverse     | Invert two nested applicative types (e.g. a List of Futures to a Future with a Lists) applying a function in the process            | Higher kinded                 |
| Fold     | Reduce embedded values to a single extracted value            | Higher kinded                 |
| MonadZero     | Filter a monad (e.g. like Optional.filter)            | Higher kinded                 |
| MonadPlus     | Combine two monads            | Higher kinded                 |
| Comonad     | Extract values from a context and extend functions to operat at monadic level            | Higher kinded                 |



# Articles

* [Reactive programming with Java 8 and simple-react: The Tutorial](https://medium.com/@johnmcclean/reactive-programming-with-java-8-and-simple-react-the-tutorial-3634f512eeb1)
* [JDK Collection eXtensions](https://medium.com/@johnmcclean/extending-jdk-8-collections-8ae8d43dd75e#.tn7ctbaks)
* [Awesome Fluent Functions](https://medium.com/@johnmcclean/can-we-make-working-with-functions-easier-in-java-8-81ed9d1050f2#.apum92khr)
* [Articles on medium](https://medium.com/search?q=simplereact)
* [Introducting the Cyclops Monad API](https://medium.com/@johnmcclean/introducing-the-cyclops-monad-api-a7a6b7967f4d)
* [Easier Try with Cyclops](http://rdafbn.blogspot.com/2015/06/java-8-easier-with-cyclops-try.html)
* [4 flavors of Java 8 Functions](https://medium.com/@johnmcclean/4-flavours-of-java-8-functions-6cafbcf5bb4f)
* [Memoise Functions in Java 8](http://rdafbn.blogspot.com/2015/06/memoize-functions-in-java-8.html)
* [Strategy Pattern in Java 8 ](http://rdafbn.blogspot.com/2015/06/startegy-pattern-in-java-8.html)
* [Straightfoward structural Pattern Matching in Java 8](https://medium.com/about-java/straightforward-structural-pattern-matching-d77155bac8da#.ogdrhsyfe)
* [Functional Feature Toggling](https://medium.com/@johnmcclean/feature-toggling-with-cyclops-a29d1eead62c)
* [Dependency injection using the Reader Monad in Java8](https://medium.com/@johnmcclean/dependency-injection-using-the-reader-monad-in-java8-9056d9501c75)
* [Scheduling a Stream](https://medium.com/@johnmcclean/how-to-schedule-emission-from-a-stream-in-java-aa2dafda7c07#.pi12so6zn)
* [Neophytes guide to Java 8 : Welcome to the Future](https://medium.com/@johnmcclean/neophytes-guide-to-java-8-welcome-to-the-future-83f432ce82a9#.jb5s9qop8)
* [JDBC Processing Options with cyclops-react](https://medium.com/@johnmcclean/jdbc-processing-options-with-cyclops-react-49d62b02f775#.1dh1ziaxv)
* [Deterministic and Non-Deterministic Finite State Machines with Cyclops](http://sebastian-millies.blogspot.de/2015/11/deterministic-and-non-deterministic.html)


[OSCON 2016 slides](http://cdn.oreillystatic.com/en/assets/1/event/154/AOL_s%20return%20to%20open%20source_%20An%20overview%20of%20Java%208%20library%20cyclops-react%20Presentation.pdf)

# License

cyclops-react is licensed under the Apache 2.0 license.		

http://www.apache.org/licenses/LICENSE-2.0
