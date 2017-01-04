<img width="820" alt="screen shot 2016-02-22 at 8 44 42 pm" src="https://cloud.githubusercontent.com/assets/9964792/13232030/306b0d50-d9a5-11e5-9706-d44d7731790d.png">

Future & functional based programming via JDK compatible extensions for Java 8 and above. 

# Getting cyclops-react

* [![Maven Central : cyclops-react](https://maven-badges.herokuapp.com/maven-central/com.aol.simplereact/cyclops-react/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.aol.simple-react/cyclops-react)

* Stackoverflow tag [cyclops-react](http://stackoverflow.com/search?q=cyclops-react)

# Documentation

* [User Guide](https://github.com/aol/cyclops-react/wiki)
* [javadoc](http://www.javadoc.io/doc/com.aol.simplereact/cyclops-react/)

## Gradle

where x.y.z represents the latest version

```groovy
compile cyclops2
```

## Maven

```xml
<dependency>
    <groupId>com.aol.simplereact</groupId>
    <artifactId>cyclops-react</artifactId>
    <version>x.y.z</version>
</dependency>
```
# 2.x Type dictionary

## Streams

| type | description | characteristics |
|------|-------------|-----------------|
| FutureStream     | Asynchronous and parallel stream             | Lazy, async, parallel                 |
| ReactiveSeq     | Synchronous sequential stream, extends JDK Stream interface. Custom Stream faster engine. Streams are replayable.              | Lazy, parallel option, primitive support, replayable, Higher kinded                |
| Streamable     | Capturing and caching replayable Stream type              | Lazy, caching                |

## Sum Types

| type | description | characteristics |
|------|-------------|-----------------|
| Maybe     | Lazy analogue of Optional (Just/None)             |  Lazy, tail recursive,sum type, Higher kinded               |
| Try     | Represents a value or an exception. Only specified Exceptions are caught on creation by default.            | Eager, avoids error hiding                 |
| Ior     | Inclusive Or, maybe one of two values or both            | Eager, sum and product type                 |
| Xor     | Exclusive Or, maybe one of two values, eager analogue of Either            | Eager, sum type                 |
| Either     | Lazy Either type maybe one of two values, lazy analogue of Xor            | Lazy, tail recursive, sum type                 |
| Either3     | Lazy Either type maybe one of three values            | Lazy, tail recursive, sum type                 |
| Either4     | Lazy Either type maybe one of four values            | Lazy, tail recursive, sum type                 |
| Either5     | Lazy Either type maybe one of five values            | Lazy, tail recursive, sum type                 |

## Collections : mutable / immutable

| type | description | characteristics |
|------|-------------|-----------------|
| ListX     | Functional extensions for working with Lists            | Lazy, mutable, immutable, 3rd party support, Higher kinded                 |
| DequeX     | Functional extensions for working with Deques            | Lazy, mutable, immutable, 3rd party support, Higher kinded                 |
| QueueX     | Functional extensions for working with Queues            | Lazy, mutable, immutable, 3rd party support, Higher kinded                 |
| SetX     | Functional extensions for working with Sets            | Lazy , mutable, immutable, 3rd party support                |
| SortedSetX     | Functional extensions for working with SortedSets            | Lazy, mutable, immutable                 |

## Collections : persistent

| type | description | characteristics |
|------|-------------|-----------------|
| PListX     | Functional extensions for working with persistent Lists            | Lazy, persistent, 3rd party support, Higher kinded                 |
| PVectorX     | Functional extensions for working with persistent Vectors            | Lazy, persistent, 3rd party support, Higher kinded                 |
| PSetX     | Functional extensions for working with persistent Sets            | Lazy, persistent, 3rd party support                 |
| POrderedSetX     | Functional extensions for working with persistent Ordered Sets            | Lazy, persistent, 3rd party support                 |
| PQueueX     | Functional extensions for working with persistent Queues           | Lazy, persistent, 3rd party support, Higher kinded                 |
| PBagX     | Functional extensions for working with persistent Bags (set like collections that allow duplicates)          | Lazy, persistent, 3rd party support                 |

## JDK Support

| type | description | characteristics |
|------|-------------|-----------------|
| Optionals     | Extension methods, for comprehensions and Higher kinded type classes            | Higher kinded                 |
| CompletableFutures    | Extension methods, for comprehensions and Higher kinded type classes            | Higher kinded                 |
| Streams     | Extension methods, for comprehensions and Higher kinded type classes            | Higher kinded                 |

## Async monadic types

| type | description | characteristics |
|------|-------------|-----------------|
| Future     | Potentially asynchronous task that may populate a result in the Future            | Eager async, Higher kinded                 |
| SimpleReact     | Asyncrhonous bulk operations on Futures            | Eager async                 |

## Misc monadic types

| type | description | characteristics |
|------|-------------|-----------------|
| Eval     | Lazy evaluation, optional caching            | Lazy, tail recursive, Higher kinded                 |
| Reader     | A transformable function : useful to implement dependency injection or Singletons in a functional style            | Eager                |
| Trampoline     | Easy to use trampoline implementations (see also Free using SupplierKind)            | Lazy                |
| Free     | Higher kinded implementation of the Free monad for Java, facilitates functional interpreters            | Lazy                |

## Box types

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


## Higher level abstractions

| type | description | characteristics |
|------|-------------|-----------------|
| AnyM     | Type safe monadic wrapper for any monad type            | Higher kinded                 |
| AnyMValue     | Type safe monadic wrapper for any monadic sum type            | Higher kinded                 |
| AnyMSeq     | Type safe monadic wrapper for any monadic non-scalar type            | Higher kinded                 |

## Monad transformers

| type | description | characteristics |
|------|-------------|-----------------|
| ListT     | Type safe list transformer of manipulating lists in a monadic context            | Higher kinded                 |
| FutureT     | Type safe future transformer of manipulating futures in a monadic context            | Higher kinded                 |


## Type classes

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


![cyclops-react-types](https://cloud.githubusercontent.com/assets/9964792/14741656/cf3d8494-088f-11e6-9189-b2bed00365d1.png)

# Features

Used in Aol to build robust, performant & scalable asynchronous systems : features include

* Compatible extensions to JDK interfaces (Collections, Streams, Functions)
* Built with [jOOλ](https://github.com/jOOQ/jOOL), [reactive-streams](http://www.reactive-streams.org/) ,[Agrona](https://github.com/real-logic/Agrona) and [pCollections](https://github.com/hrldcpr/pcollections)
* Extensions for efficient JDK compatible persistent collections ([pCollections](http://pcollections.org/))
* FutureStreams for managing aggregates for Future Tasks (e.g. for multi-threaded execution of large numbers of I/O tasks)
* Single-threaded asynchronous streaming
* Scheduling of data emission
* Powerful extended type hierarchy for aggregrations (Collections & Streams) and single values
* Powerful functional-style control structures, implemented in Java friendly manner (Maybe, Eval, FutureW, Xor, Ior, Try, AnyM, structural & guard based pattern matching, for-comprehensions)
* Execute functions between wrapped values (Optional / CompletableFutre etc) without tedious unwrapping (Java friendly Applicative support).
* Java friendly abstractions for wrapping any Monad type (Stream, CompletableFuture, Optional, cyclops-react types and types from other Java projects too). AnyM and it's two subtypes AnyMSeq for aggregates (Steam, List etc) and AnyMValue for Values
* Monad Transformers - for manipulating nesting monadic types (e.g. Optionals within a Stream as if it were just an Optional)
* Applicatives - apply functions across wrapped types (e.g. Optional / Maybe / Xor) without unwrapping & aggregate without terminate on failure
* Structural pattern matching & pattern matching via Guards (see Matchable and Matchables)
* Full strength for comprehensions / generators (reference elements from other generators)
* Tight integration with reactive-streams : collections, streams and datatypes are publishers & can be generated via subscribers

* Very Extensible & integration with other projects via cyclops-integration modules.


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
