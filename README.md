<img width="820" alt="screen shot 2016-02-22 at 8 44 42 pm" src="https://cloud.githubusercontent.com/assets/9964792/13232030/306b0d50-d9a5-11e5-9706-d44d7731790d.png">

Powerful Streams and functional data types for building modern Java 8 applications. We extend JDK interfaces where possible for maximum integration. 

This is the 10.x branch for 2.x branch click the link below

* [2.x](https://github.com/aol/cyclops-react/tree/2.x)
* [1.x](https://github.com/aol/cyclops-react/tree/1.x)


# Getting Cyclops X

* The latest version is [cyclops:10.0.0-M1](http://mvnrepository.com/artifact/com.oath.cyclops/cyclops/10.0.0-M1)
* [![Maven Central : cyclops-react](https://maven-badges.herokuapp.com/maven-central/com.oath.cyclops/cyclops/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.oath.cyclops/cyclops)

* Stackoverflow tag [cyclops-react](http://stackoverflow.com/search?q=cyclops-react)

# What's new in Cyclops X (cyclops 10.0.0)

- Fast purely functional datastructures (Vector, Seq / List, LazySeq / LazyList, NonEmptyList, HashSet, TreeSet, TrieSet, HashMap, LinkedMap, MultiMap, TreeMap, BankersQueue, LazyString, Discrete Interval Encoded Tree, Zipper, Range, Tree, DifferenceList, HList, Dependent Map )
- Structural Pattern Matching API (deconstruct algebraic product and sum types)
- Improved type safety via the removal of unsafe APIs
  -- E.g. Unlike Optional, Option has no get method (which could throw a null pointer)
  -- New data structures do not support operations that would throw exceptions (you can't call head on an empty list for example)
- Eager and Lazy alternatives for most datastructures (Option is eager, Maybe is lazy + reactive)
- Improved naming of types (Function1-8 rather than Fn1-8, Either not Xor)
- Group id is changed to com.oath.cyclops
- Versioning between cyclops-react and cyclops is merged on cyclops versioning scheme (version 10 = Cyclops X)
- Light weight dependencies : reactive-streams API & Agrona


# Documentation

* [User Guide](https://github.com/aol/cyclops-react/wiki) Primarily tailored for 1.x still.
* [javadoc](http://www.javadoc.io/doc/com.aol.simplereact/cyclops-react/)

## Latest Articles

* [DSLs with the Free Monad in Java 8 : Part I](https://medium.com/@johnmcclean/dsls-with-the-free-monad-in-java-8-part-i-701408e874f8)
* [Cross Library Stream Benchmarking : Playing Scrabble with Shakespeare](https://medium.com/@johnmcclean/cross-library-stream-benchmarking-playing-scrabble-with-shakespeare-8dd1d1654717)

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









## Higher level abstractions



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
