# Cyclops Pure (λ) : The cyclops module for pure functional programming in Java

Cyclops pure provides support for advanced functional data structures and higher kinded type classes for a wide range of cyclops data types.

* [control](https://github.com/aol/cyclops-react/tree/master/cyclops-pure/src/main/java/cyclops/control) : advanced functional control structure such as
            Reader, Writer, State, ReaderWriterStrate, IO
* [arrow](https://github.com/aol/cyclops-react/tree/master/cyclops-pure/src/main/java/cyclops/arrow) : Kleisli arrow and related types
* [free](https://github.com/aol/cyclops-react/tree/master/cyclops-pure/src/main/java/cyclops/free) : free monad and related types
* [kinds](https://github.com/aol/cyclops-react/tree/master/cyclops-pure/src/main/java/cyclops/kinds) : Higher Kinded proxies for JDK types
* [typeclasses](https://github.com/aol/cyclops-react/tree/master/cyclops-pure/src/main/java/cyclops/typeclasses) :  interface definitions for many popular typeclasses
* [instances](https://github.com/aol/cyclops-react/tree/master/cyclops-pure/src/main/java/cyclops/instances) : typeclass instances for cyclops and JDK data types
* [transformers](https://github.com/aol/cyclops-react/tree/master/cyclops-pure/src/main/java/cyclops/transformers) : monad transformer interface definitions
* [hkt](https://github.com/aol/cyclops-react/tree/master/cyclops-pure/src/main/java/cyclops/hkt) : Manager classes to make working with Higher Kinded typeclasses cleaner in Java

Since cyclops 10.0.0-M5

### cyclops-pure builds on top of cyclops

The core Cyclops module provides a range of general purpose and purely functional data types

* [cyclops control](https://github.com/aol/cyclops-react/tree/master/cyclops/src/main/java/cyclops/control) : Control types such as Option, Either, Try, Future and Lazy analogues such as Maybe, LazyEither. 
* [cyclops data](https://github.com/aol/cyclops-react/tree/master/cyclops/src/main/java/cyclops/data) : Purely functional data types such as Vector (bitmapped array trie), HashMap (hash array mapped trie), Seq (linked list), LazySeq (lazy list), DifferenceList, NonEmptyList, Zipper, HashSet, BankersQueue, Discrete Interval Encoded Trie and more)
* [cyclops reactive](https://github.com/aol/cyclops-react/tree/master/cyclops/src/main/java/cyclops/reactive) : API for both iterative and reactive based streaming

## Get cyclops-pure


* [![Maven Central : cyclops-pure](https://maven-badges.herokuapp.com/maven-central/com.oath.cyclops/cyclops-pure/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.oath.cyclops/cyclops-pure)   [![javadoc.io](https://javadocio-badges.herokuapp.com/com.oath.cyclops/cyclops-pure/badge.svg)](https://javadocio-badges.herokuapp.com/com.oath.cyclops/cyclops-pure)
* [Javadoc for cyclops-pure](http://www.javadoc.io/doc/com.oath.cyclops/cyclops-pure)

# More info

## <a name="pure">Pure

* [Simulating Higher Kinded Types in Java](https://medium.com/@johnmcclean/simulating-higher-kinded-types-in-java-b52a18b72c74) : The approach taken to implement Higher Kinded types in Cyclops Pure
* [DSLs with the Free Monad in Java 8 : Part 1](https://medium.com/modernnerd-code/dsls-with-the-free-monad-in-java-8-part-i-701408e874f8) : Introduction to Free
* [DSLs with the Free Monad in Java 8 : Part 2](https://medium.com/@johnmcclean/dsls-with-the-free-monad-in-java-8-part-ii-f0010f012ae1) : Higher kinded Free in Java as implemented in Cyclops Pure
* [A functional approach to dependency injection in Java](https://hackernoon.com/superkleisliisfantasticframeworksareatrocious-a-functional-approach-to-dependency-injection-in-e7bc8c4993fa) : An introduction to the Reader monad and Kleisli
* [The monad typeclass with Vavr's Future and Try](https://medium.com/@johnmcclean/cyclops-java-the-monad-typeclass-with-vavrs-future-and-try-2732b002c1cd) : How to use the cyclops monad typeclass with Vavr
* [The Active typeclass manager](https://medium.com/@johnmcclean/cyclops-java-the-active-typeclass-manager-c46daa82aea4) : Overview of the Active Typeclass manager which makes using higher kinded typeclasses a bit simpler in Java
* [Monoids for Java Developers](https://medium.com/@johnmcclean/monoids-for-java-developers-98e2ba94f708) : Introduction to monoids, semigroups and groups using cyclops
* [Cyclops Higher Kinded Types](https://github.com/aol/cyclops/wiki/Higher-Kinded-Types) : Higher Kinded Types in Cyclops
* [Applicative Type Class in Cyclops](https://github.com/aol/cyclops/wiki/Type-Classes-:-Applicative) : Applicative type class in Cyclops
