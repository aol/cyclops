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


# Related Modules

[Core Pattern Matching Support](https://github.com/aol/cyclops/blob/master/cyclops-pattern-matching)
[Recursive Pattern Matching Support](https://github.com/aol/cyclops/tree/master/cyclops-pattern-matching-recursive)

## The CollectionMatching class


## Operators

At the top level the operators are 

* *whenFromStream* : to a define a new case from a Stream of cases
* *whenIterable* : to specifically handle the case where the Object to match is an iterable

Second level operators are

* *allTrue* : all the predicates must match
* *bothTrue* : both the predicates must match
* *allMatch* : all the hamcrest matchers must match
* *bothTrue* : both the hamcrest matchers must match
* *allHold* : allows mix of predicates, hamcrest matchers and prototype values all of which must hold
* *allValues* : check all values in the supplied array match the first values in the iterable


Streams

* *streamOfResponsibility* : extract the matching cases from a Stream. Useful for introducing selection logic within your own Java 8 Streams

Further Operators 

* *thenApply* : final action to determine result on match
* *thenConsume* : final action with no return result on match
* *thenExtract* : extract a new value to pass to next stage (or as result)





# Examples : 

## bothMatch

```java
CollectionMatcher.whenIterable().bothMatch(samePropertyValuesAs(new Person("bob")),anything())
											.thenExtract(Extractors.<Person>first())
											.thenApply(bob->bob.getId())
				.whenIterable().bothMatch(samePropertyValuesAs(new Person("alice")),"boo hoo!")		
									.thenExtract(Extractors.<Person>first())
											.thenApply(alice->alice.getId())		
											.apply(Two.tuple(new Person("bob"),"boo hoo!"))
											
	//bob's id
```

## allValues 

Match against all values in a collection

```java
	CollectionMatcher.whenIterable().allValues(1, ANY(), 2).thenApply(l -> "case1")
						 .whenIterable().allValues(1, 3, 2).thenApply(l -> "case2")
						 .whenIterable().bothTrue((Integer i) -> i == 1, (String s) -> s.length() > 0).thenExtract(Extractors.<Integer, String> of(0, 1)).thenApply(t -> t.v1 + t.v2)
						 .match(1, "hello", 2);
						 
 //case1						 
```		
		
## Stream of responsibility

Define a Stream of matching cases, use the first matching case found
```java
		Stream<ChainImpl> chain = Stream.of(new LessThanAndMultiply(5,10),new LessThanAndMultiply(7,100));
		int result = CollectionMatcher.whenFromStream().streamOfResponsibility(chain).match(6).get();
		
		assertThat(result,is(600));
		
	@AllArgsConstructor
	  static class LessThanAndMultiply implements ChainOfResponsibility<Integer,Integer>{
		int max;
		int mult;
		@Override
		public boolean test(Integer t) {
			return t<max;
		}

		@Override
		public Integer apply(Integer t) {
			return t*mult;
		}
		
	}
		
```
