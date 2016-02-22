<img width="820" alt="screen shot 2016-02-22 at 8 44 42 pm" src="https://cloud.githubusercontent.com/assets/9964792/13232030/306b0d50-d9a5-11e5-9706-d44d7731790d.png">

Future & functional based programming via JDK compatible extensions for Java 8 and above.

# Getting cyclops-react

* [![Maven Central : cyclops-react](https://maven-badges.herokuapp.com/maven-central/com.aol.simplereact/cyclops-react/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.aol.simple-react/cyclops-react)

## Gradle

where x.y.z represents the latest version

compile 'com.aol.simplereact:cyclops-react:x.y.z'

## Maven

```xml
<dependency>
    <groupId>com.aol.simplereact</groupId>
    <artifactId>cyclops-react</artifactId>
    <version>x.y.z</version>
</dependency>

# Features

Used in Aol to build robust, performant & scaleable asynchronous systems features include

* Compatible, extensions to JDK interfaces (Collections & Streams)
* Built with [https://github.com/jOOQ/jOOL](https://github.com/jOOQ/jOOL)
* Extensions for efficient JDK compatible persistent collections ([pCollections](http://pcollections.org/))
* FutureStreams for managing aggregates for Future Tasks (e.g. for multi-threaded execution of large numbers of I/O tasks)
* Single-threaded asynchronous streaming
* Scheduling of data emission
* Powerful extended type hierarchy for aggregrations (Collections & Streams) and single values
* Powerful functional-style control structures, implemented in Java friendly manner (Maybe, Eval, FutureW, Xor, Ior, Try, AnyM, structural & guard based pattern matching, for-comprehensions)
* Execute functions between wrapped values (Optional / CompletableFutre etc) without tedious unwrapping (Java friendly Applicative support).
* Java friendly abstractions for wrapping any Monad type (Stream, CompletableFuture, Optional, cyclops-react types and types from other Java projects too)

* Integration with other projects via cyclops-integration modules.

# Documentation

* [user-guide : work in progress](https://github.com/aol/simple-react/user-guide)
* [simple-react wiki](https://github.com/aol/simple-react/wiki)
* [cyclops wiki](https://github.com/aol/cyclops/wiki)
* [javadoc](http://www.javadoc.io/doc/com.aol.simplereact/cyclops-react/)

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
* [Pattern Matching in Java 8](https://medium.com/@johnmcclean/pattern-matching-in-cyclops-for-java-8-21a4912bfe4d)
* [Functional Feature Toggling](https://medium.com/@johnmcclean/feature-toggling-with-cyclops-a29d1eead62c)
* [Dependency injection using the Reader Monad in Java8](https://medium.com/@johnmcclean/dependency-injection-using-the-reader-monad-in-java8-9056d9501c75)
* [Scheduling a Stream](https://medium.com/@johnmcclean/how-to-schedule-emission-from-a-stream-in-java-aa2dafda7c07#.pi12so6zn)
* [Neophytes guide to Java 8 : Welcome to the Future](https://medium.com/@johnmcclean/neophytes-guide-to-java-8-welcome-to-the-future-83f432ce82a9#.jb5s9qop8)
* [Deterministic and Non-Deterministic Finite State Machines with Cyclops](http://sebastian-millies.blogspot.de/2015/11/deterministic-and-non-deterministic.html)

[Old simple-react landing page](https://github.com/aol/simple-react/wiki/1.-simple-react-overview)


# License

cyclops-react is licensed under the Apache 2.0 license.		

http://www.apache.org/licenses/LICENSE-2.0
