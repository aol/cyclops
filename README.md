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


# License

cyclops is licensed under the Apache 2.0 license.		

http://www.apache.org/licenses/LICENSE-2.0
