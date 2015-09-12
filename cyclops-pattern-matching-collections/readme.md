# Cyclops pattern matching (collections)

Powerful Pattern Matching for Java. This module provides an API for matching on Collections, aswell as on and within Streams!

## Getting cyclops-pattern-matching

* [![Maven Central : cyclops-pattern-matching-collections](https://maven-badges.herokuapp.com/maven-central/com.aol.cyclops/cyclops-pattern-matching-collections/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.aol.cyclops/cyclops-pattern-matching-collections)


## Gradle

where x.y.z represents the latest version

compile 'com.aol.cyclops:cyclops-pattern-matching-collections:x.y.z'

## Maven

```xml
<dependency>
    <groupId>com.aol.cyclops</groupId>
    <artifactId>cyclops-pattern-matching-collections</artifactId>
    <version>x.y.z</version>
</dependency>
```
# Overview

<img width="880" alt="screen shot 2015-07-22 at 10 14 06 pm" src="https://cloud.githubusercontent.com/assets/9964792/8837606/0a2d9368-30bf-11e5-9690-eaa96bb56cc5.png">



![pattern matching](https://cloud.githubusercontent.com/assets/9964792/8334707/3827c1e2-1a91-11e5-87b1-604905a75ecb.png)
  
# Docs 
              

* [Javadoc for Cyclops Pattern Matching](http://www.javadoc.io/doc/com.aol.cyclops/cyclops-pattern-matching-collections/6.0.0)
* [Pattern Matching Wiki](https://github.com/aol/cyclops/wiki/Pattern-matching-:-Pattern-Matching-for-Java-8)

# Pattern Matching Structure & Examples


## The CollectionMatching class


## Operators

At the top level the operators are 

* *whenFromStream* : to a define a new case from a Stream of cases
* *whenIterable* : to specifically handle the case where the Object to match is an iterable

Second level operators are

Iteables 

* *allTrue* : all the predicates must match
* *bothTrue* : both the predicates must match
* *allMatch* : all the hamcrest matchers must match
* *allHold* : allows mix of predicates, hamcrest matchers and prototype values all of which must hold

Streams

* *streamOfResponsibility* : extract the matching cases from a Stream. Useful for introducing selection logic within your own Java 8 Streams

Further Operators 

* *thenApply* : final action to determine result on match
* *thenConsume* : final action with no return result on match
* *thenExtract* : extract a new value to pass to next stage (or as result)





Examples : 

