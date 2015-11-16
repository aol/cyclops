# Cyclops pattern matching

Powerful Pattern Matching for Java. Use lambda's, hamcrest or scala-like wildcards!

See also cyclops-pattern-matching-recursive and cyclops-pattern-matching-collections

## Getting cyclops-pattern-matching

* [![Maven Central : cyclops-pattern-matching](https://maven-badges.herokuapp.com/maven-central/com.aol.cyclops/cyclops-pattern-matching/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.aol.cyclops/cyclops-pattern-matching)


## Gradle

where x.y.z represents the latest version

compile 'com.aol.cyclops:cyclops-pattern-matching:x.y.z'

## Maven

```xml
<dependency>
    <groupId>com.aol.cyclops</groupId>
    <artifactId>cyclops-pattern-matching</artifactId>
    <version>x.y.z</version>
</dependency>
```
# Overview

<img width="880" alt="screen shot 2015-07-22 at 10 14 06 pm" src="https://cloud.githubusercontent.com/assets/9964792/8837606/0a2d9368-30bf-11e5-9690-eaa96bb56cc5.png">



![pattern matching](https://cloud.githubusercontent.com/assets/9964792/8334707/3827c1e2-1a91-11e5-87b1-604905a75ecb.png)
  
# Docs
              
* [Javadoc for Cyclops Pattern Matching](http://www.javadoc.io/doc/com.aol.cyclops/cyclops-pattern-matching/5.0.0)
* [Pattern Matching Wiki](https://github.com/aol/cyclops/wiki/Pattern-matching-:-Pattern-Matching-for-Java-8)

# Related Modules

[Recursive Pattern Matching Support](https://github.com/aol/cyclops/tree/master/cyclops-pattern-matching-recursive)
[Pattern Matching for Collections](https://github.com/aol/cyclops/tree/master/cyclops-pattern-matching-collections)

# Pattern Matching Structure & Examples

## Cyclops Pattern Matching is structured into two packages

1. A core which holds the cases to be executed
2. A set of builders which aims to make building pattern matching expressions simpler

Builders can build ontop of builders. The matchable interface provides the highest level of abstraction and is the recommended starting point.

Conversely Case and Cases provide the lowest level inputs into the pattern matcher.


* Matching : provides a more powerful & flexible interface at the expense of increased verboseness
* PatternMatcher : provides a more flexible interface at the expense of looser typing
* Cases / Case : low level functional classes for building pattern matching cases




    
    
## The Matching class

Matching provides a number of builders for performing advanced pattern matching.

Features available via the Matching class include


* Match by type, value, predicate or Hamcrest Matcher
* Sequential, Parallel and Async execution
* Fluent step builders for common cases
* Support hamcrest matchers
* Java 8 predicates for matching.
* Match on first (return Optional)
* Match many (return Stream)
* Pre & post value extraction per case
* Match using multiple in case expressions via tuples or iterables of predicates / matchers
* Match against streams of data
* Usable within a Stream (strategy pattern)
* Fluent step builders
* Define cases in situ via method chaining or plugin in variables (implement Consumer)
* Match against collections with each element processed independently
* Three case types (standard, atomised, stream) can be mixed within a single Matching test

## Operators

At the **top level** the operators are 

* *when()* : to define a new case

A step builder / wizard will then be provided with isType, isValue, allMatch, noneMatch, anyMatch options. These can also be called directly on Matching via

Composite **top level** operators are 

* *whenIsType()*  : provide a lambda / Function that will be used to both verify the type and provide a return value if triggered
* *whenIsValue()*  : compare object to match against specified value
* *whenAllMatch()*  : when an array of Hamcrest matchers all hold
* *whenAnyMatch()* : when at least one of an array of Hamcrest matchers hold
* *whenNoneMatch()* : : when none of an array of Hamcrest matchers hold
* *whenIsTrue()*  : Use a Predicate to determine if Object matches
* *whenIsMatch()* : Use a Hamcrest Matcher to determine if Object matches

**Second level** operators are

* *isType* : provide a lambda / Function that will be used to both verify the type and provide a return value if triggered
* *isValue* : compare object to match against specified value
* *isTrue* : Use a Predicate to determine if Object matches
* *isMatch* : Use a Hamcrest Matcher to determine if Object matches
* *allMatch()*  : when an array of Hamcrest matchers all hold
* *anyMatch()* : when at least one of an array of Hamcrest matchers hold
* *noneMatch()* : : when none of an array of Hamcrest matchers hold

Further Operators 

* *thenApply* : final action to determine result on match
* *thenConsume* : final action with no return result on match
* *thenExtract* : extract a new value to pass to next stage (or as result)



Examples : 

### With Hamcrest
```java
    Matching.when().isMatch(hasItem("hello2")).thenConsume(System.out::println)
							.match(Arrays.asList("hello","world"))
```	
methods xxMatch accept Hamcrest Matchers
							
### Matching multiple
```java
     Stream<Integer> resultStream = Matching.when().isValue(100).thenApply(v-> v+100)
											.when().isType((Integer i) -> i)
											.matchMany(100);
```											
Use the matchMany method to instruct cylops to return all results that match

### Inside a Stream

#### flatMap

Use asStreamFunction to Stream multiple results back out of a set of Cases.
```java
     Integer num = Stream.of(1)
							.flatMap(Matching.when().isValue(1).thenApply(i->i+10).asStreamFunction())
							.findFirst()
							.get();							
```
asStreamFunction converts the MatchingInstance into a function that returns a Stream. Perfect for use within flatMap.

#### map
```java	
	Integer num = Stream.of(1)
							.map(Matching.when().isValue(1).thenApply(i->i+10))
							.findFirst()
							.get().get();	
```
Or drop the second get() (which unwraps from an Optional) with

```java
	Integer num = Stream.of(1)
							.map(Matching.when().isValue(1).thenApply(i->i+10).asUnwrappedFunction())
							.findFirst()
							.get();	
```							
							
### Async execution	

Use the Async suffix - available on the Cases object, when calling match to run the pattern matching asynchronously, potentially on another thread.
```java
		CompletableFuture<Integer> result =	Matching.when().isValue(100).thenApply(this::expensiveOperation1)
													.when().isType((Integer i) -> this.exepensiveOperation2(i))
													.cases()
													.matchAsync(100)
													
													
```
# Creating Case classes
In Java it is possible to create sealed type hierarchies by reducing the visibilty of constructors. E.g. If the type hierarchies are defined in one file super class constructors can be made private and sub classes made final. This will prevent users from creating new classes externally. 
Lombok provides a number of annotations that make creating case classes simpler.

@Value :  see https://projectlombok.org/features/Value.html

## A sealed type hierarchy

An example sealed hierarchy (ValueObject implies both Matchable and Decomposable)
```java
	@AllArgsConstructor(access=AccessLevel.PRIVATE) 
	public static class CaseClass implements ValueObject { } 
	@Value public static class MyCase1 extends CaseClass { int var1; String var2; }
	@Value public static class MyCase2 extends CaseClass { int var1; String var2; }

    CaseClass result;
    return result.match(this::handleBusinessCases);
```														
## The PatternMatcher class

The PatternMatcher builder is the core builder for Cyclops Cases, that other builder instances leverage to build pattern matching cases. It's API is unsuitable for general use in most applications, but can leveraged to build application specific Matching builders.


The patternMatcher class provides a lot of utility methods that are organisied as follows

* *inCaseOf* : match with a Predicate and return a result
* *caseOf* : match with a Predicate but no result will be returned
* *inMatchOf* : match with a Hamcrest Matcher and return a result
* *matchOf* : match with a Hamcrest Matcher but no result will be returned
