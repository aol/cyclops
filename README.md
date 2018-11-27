# Getting Cyclops X (10)

* The latest version is [cyclops:10.1.0](http://mvnrepository.com/artifact/com.oath.cyclops/cyclops/10.1.0)


* Stackoverflow tag [cyclops-react](http://stackoverflow.com/search?q=cyclops-react)

* [Documentation](https://github.com/aol/cyclops/wiki) (work in progress for Cyclops X)

* [Integration modules](https://github.com/aol/cyclops-integration)

# Tutorial : 

* [Dysfunctional programming in Java : A functional tutorial](https://medium.com/@johnmcclean/dysfunctional-programming-in-java-a-functional-tutorial-20e0bebd2ec9)
  - [Part 1 : Laziness (Covering Supplier & the Eval monad)](https://medium.com/@johnmcclean/dysfunctional-programming-in-java-i-laziness-cc9c6981de39)
  - [Part 2 : Immutability (Covering Immutable Objects with Lombok, withers)](https://medium.com/@johnmcclean/dysfunctional-programming-in-java-2-immutability-a2cff487c224)
  - [Part 3 : Functional Composition (Covering method chaining, Streams, ReactiveSeq, Flux from Reactor)](https://medium.com/@johnmcclean/dysfunctional-programming-in-java-3-functional-composition-16828f0609c2)
  - [Part 4 : No nulls allowed (Covering class design, Optional, eager / lazy / reactive Option types, Mono from Reactor)](https://blog.usejournal.com/dysfunctional-programming-in-java-4-no-nulls-allowed-88e8735475a)
  - [Part 5 : No exceptions (Covering making illegal states unrepresentable, eager / lazy Either type, Try)](https://medium.com/@johnmcclean/dysfunctional-programming-in-java-5-no-exceptions-5f37ac594323)
  - [Part 6 : No locks or synchronized blocks (Covering Future, Sequence, FutureStreams and reactive-streams)](https://medium.com/@johnmcclean/dysfunctional-programming-in-java-6-no-locks-or-synchronized-blocks-ea5a47644689)
  - [Part 7 : Immutable Collections (Covering Vector, NonEmptyList and reactive processing)](https://medium.com/@johnmcclean/dysfunctional-programming-in-java-7-immutable-collections-83ba78aaf4dd)

# What's new in Cyclops X (cyclops 10)

![cyclops-data-types](https://user-images.githubusercontent.com/9964792/37656704-b4266d7a-2c40-11e8-84d9-23a4a77e0341.jpeg)

- Fast purely functional datastructures (Vector, Seq / List, LazySeq / LazyList, NonEmptyList, HashSet, TreeSet, TrieSet, HashMap, LinkedMap, MultiMap, TreeMap, BankersQueue, LazyString, Discrete Interval Encoded Tree, Zipper, Range, Tree, DifferenceList, HList, Dependent Map )
- Structural Pattern Matching API (deconstruct algebraic product and sum types)
- Improved type safety via the removal of unsafe APIs
  -- E.g. Unlike Optional, Option has no get method (which could throw a null pointer)
  -- New data structures do not support operations that would throw exceptions (you can't call head on an empty list for example)
- Eager and Lazy alternatives for most datastructures (Option is eager, Maybe is lazy + reactive)
- Improved naming of types (Function1-8 rather than Fn1-8, Either not Xor)
- Group id is changed to com.oath.cyclops
- Versioning between cyclops-react and cyclops is merged on cyclops versioning scheme (version 10 = Cyclops X)
- Light weight dependencies : reactive-streams API, KindedJ & Agrona
- JVM Polyglot Higher Kinded Types Support with KindedJ

## Modules

* [cyclops](https://github.com/aol/cyclops-react/tree/master/cyclops) - Persistent data structures and control types
* [cyclops-futurestream](https://github.com/aol/cyclops-react/tree/master/cyclops-futurestream) - Parallel asynchronous streaming
* [cyclops-reactive-collections](https://github.com/aol/cyclops-react/tree/master/cyclops-reactive-collections) - Fast, non-blocking, asynchronous extensions for JDK and Persistent Collections
* [cyclops-anyM](https://github.com/aol/cyclops-react/tree/master/cyclops-anym) - Higher kinded abstractions for working with any Java Monad type.
* [cyclops-pure](https://github.com/aol/cyclops-react/tree/master/cyclops-pure) - Higher kinded type classes for pure functional programming in Java
* [cyclops-reactor-integration](https://github.com/aol/cyclops-react/tree/master/cyclops-reactor-integration) - Reactive collections and AnyM integrations with Reactor.
* [cyclops-rxjava2-integration](https://github.com/aol/cyclops-react/tree/master/cyclops-rxjava2-integration) - Reactive collections and AnyM integrations with Rx Java 2.
* [cyclops-jackson-integration](https://github.com/aol/cyclops-react/tree/master/cyclops-jackson) - Jackson databindings for Cyclops persistent data structures and control types.




## Gradle

where x.y.z represents the latest version

```groovy
compile 'com.oath.cyclops:cyclops:x.y.z'
```

## Maven

```xml
<dependency>
    <groupId>com.oath.cyclops</groupId>
    <artifactId>cyclops</artifactId>
    <version>x.y.z</version>
</dependency>
```

<img width="820" alt="screen shot 2016-02-22 at 8 44 42 pm" src="https://cloud.githubusercontent.com/assets/9964792/13232030/306b0d50-d9a5-11e5-9706-d44d7731790d.png">

Powerful Streams and functional data types for building modern Java 8 applications. We extend JDK interfaces where possible for maximum integration. 

This is the 10.x branch for 2.x branch click the link below

* [2.x](https://github.com/aol/cyclops-react/tree/2.x)
* [1.x](https://github.com/aol/cyclops-react/tree/1.x)

# License

cyclops is licensed under the Apache 2.0 license.		

http://www.apache.org/licenses/LICENSE-2.0
