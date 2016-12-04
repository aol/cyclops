# [Cyclops & cyclops-react](http://cyclops-react.io)

Cyclops is a platform that aims to bring much of the power of languages such as Scala to Java, while retaining it's ease of use.

As a platform Cylops offers powerful sequential and parallel Streaming with common reactive functionality, fast and powerful colleciton extensions, a large range of extened persistent collections, , straightforward structural pattern matching, true for comprehensions, utilities for working with JDK types, enhanced APIs for working with Futures, an advanced Try implementation, Xor & Ior, PushableStreams, lazy data types (Maybe, Eval, Either1-5) and even compiler enforced Higher Kinded Types and type classes.

* Cyclops is **not** a foundational library, the end goal is the primary focus and the technology used is a means to that end
* Cyclops integrates best of breed libraries for the Java Platform to produce something more powerful than the sum of the parts.
* By leveraging high quality existing technology where possible the breadth, scope and quality of the cyclops platform increases all the time.

# Contributing 

Contributions are very welcome! We make heavy use of Lombok, to cut down Java boilerplate - so please do install the appropriate Lombok plugin to your IDE.


# User Guide

* [User Guide](https://github.com/aol/cyclops-react/wiki)
* [javadoc](http://www.javadoc.io/doc/com.aol.simplereact/cyclops-react/)

# Getting cyclops-react

* [![Maven Central : cyclops-react](https://maven-badges.herokuapp.com/maven-central/com.aol.simplereact/cyclops-react/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.aol.simple-react/cyclops-react)

* Stackoverflow tag [cyclops-react](http://stackoverflow.com/search?q=cyclops-react)

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



# Libraries that make the platform

## Core libraries

* Agrona - for wait-free Queues for cross-thread data transfer
* jOOλ - for base sequential extended Streaming and tuples
* pCollections - defines a core set of interfaces (and implementations) for working with persistent collections. cyclops extends those interfaces (Lazy Extended Collections) and provides complaint implementations from all the major persistent collection libraries 
* Async retry - for asynchronous retries

## Pending Core

* [Pivotal's Reactor libary](https://github.com/aol/cyclops/tree/master/cyclops-react) - Reactor provides push based Streaming API, most cyclops extension modules also now depend on cyclops-reactor which builds additional features on top of Reactor. cyclops Lazy Extended Collections execute chains of functional operations blazingly fast, and as a result cyclops-reactor will be merged into cyclops-react (the core of the platform).

## Extensions

* [cyclops-scala](https://github.com/aol/cyclops/tree/master/cyclops-scala) : Provides a standard Java API to work with Scala's awesome persistent collections as cyclops Lazy Extended Persistent Collections
* [cyclops-javaslang](https://github.com/aol/cyclops/tree/master/cyclops-javaslang) : Provides an API for working with JavaSlang collections as cyclops Lazy Extended Persistent Collections, provides conversions for JavaSlang types, defines Higher Kinded Type encodings for JavaSlang types and type class instances for many JavaSlang collections / data types
* [cyclops-higherkindedtypes](https://github.com/aol/cyclops/tree/master/cyclops-higherkindedtypes) - Defines Higher Kinded Type encodings for JDK and cyclops-react types, compiler enforced by Derive4j HKT
* [cyclops-typeclasses](https://github.com/aol/cyclops/tree/master/cyclops-typeclasses) - Defines type classes (Unit, Functor, Applicative, Monad, CoMaond, Traversable, Foldable) and instances for JDK Types and cyclops-react types
* [cyclops-clojure](https://github.com/aol/cyclops/tree/master/cyclops-clojure) - Provides a standard Java API to work with Clojure's awesome persistent collections as cyclops Lazy Extended Persistent Collections
* [cyclops-functionaljava])https://github.com/aol/cyclops/tree/master/cyclops-functionaljava) - Provides Higher Kinded Type definitions and type class instances for some FJ data types (community contributes welcome). Native for comprehensions for FJ types. Cross library conversions for FJ types.
* [cyclops-dexx](https://github.com/aol/cyclops/tree/master/cyclops-dexx) -  Provides an API for working with Dexx collections as cyclops Lazy Extended Persistent Collections
* [cyclops-sum-types](https://github.com/aol/cyclops/tree/master/cyclops-sum-types) :  Defines totally lazy sum / either types from Either - Either5
* [cyclops-rx](https://github.com/aol/cyclops/tree/master/cyclops-rx) - Provides For Comprehensions for Observables, Monad Transformers for Observables, Higher Kinded Type encodings and type classes for Observables


<img width="820" alt="screen shot 2016-02-22 at 8 44 42 pm" src="https://cloud.githubusercontent.com/assets/9964792/13232030/306b0d50-d9a5-11e5-9706-d44d7731790d.png">


# License

cyclops-react is licensed under the Apache 2.0 license.		

http://www.apache.org/licenses/LICENSE-2.0
