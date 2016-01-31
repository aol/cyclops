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
# Overview

## With the Matchable interface 

```java
Matchable.of(Optional.of(4))
         .mayMatch(
                o-> o.isEmpty().then(i->"empty"),
                o-> o.hasValues(1).then(i->"one"),
                o-> o.hasValues(2).then(i->"two"),
                o-> o.hasValuesWhere(i->i>2).then(i->"many"),
            ).orElse("error")
```

<img width="880" alt="screen shot 2015-07-22 at 10 14 06 pm" src="https://cloud.githubusercontent.com/assets/9964792/8837606/0a2d9368-30bf-11e5-9690-eaa96bb56cc5.png">



![pattern matching](https://cloud.githubusercontent.com/assets/9964792/8334707/3827c1e2-1a91-11e5-87b1-604905a75ecb.png)
  
# Docs
              

* [Javadoc for Cyclops Pattern Matching](http://www.javadoc.io/doc/com.aol.cyclops/cyclops-pattern-matching/5.0.0)
* [Pattern Matching Wiki](https://github.com/aol/cyclops/wiki/Pattern-matching-:-Pattern-Matching-for-Java-8)
* [Pattern Matching in Cyclops](https://medium.com/@johnmcclean/pattern-matching-in-cyclops-for-java-8-21a4912bfe4d)

# Related Modules

[Core Pattern Matching Support](https://github.com/aol/cyclops/blob/master/cyclops-pattern-matching)
[Pattern Matching for Collections](https://github.com/aol/cyclops/tree/master/cyclops-pattern-matching-collections)

## The Matchable interface / trait


Objects that implement Matchable get a number of Pattern Matching helper methods by default.

match : matches by value
_match : matches by type and value
matchType : matches by type only

### Clean match statements

The cleanest way to use the Matchable instance is to encapsulate your matching logic inside a method with a name that indicates the intention of the matching. E.g.

```java
    double benefits = employee.match(this::calcEmployeeBenefits);
    
    private CheckValues<I,T> calcEmployeeBenefits(CheckValues<I,T> c){
        return c.with(__,Bonus.PAYABLE,__).then(e->e.salary()*e.bonus())
                .with(__,__,__).then(e->e.salary());
    }
```
    
* match example

```java
    new MyCase(4,2,3).match(this::message,"how are you?");
    
    private <I,T> CheckValues<Object, T> message(CheckValues<I, T> c) {
        return c.with(1,2,3).then(i->"hello")
                .with(4,5,6).then(i->"goodbye");
    }
``` 
Returns the default message "how are you?"  as values 4,2,3 don't match 1,2,3 or 4,5,6

* _match example

```java
    new MyCase(4,5,6)._match(c ->c.isType( (MyCase ce)-> "hello").with(1,2,3),"goodbye")
``` 
Returns "goodbye" as altough the type matches, 1,2,3 doesn't match 4,5,6

* matchType example

```java
    new MyCase(4,5,6).matchType(c ->c.isType((MyCase ce) -> "hello")
```
Returns "hello" as MyCase is an instance of MyCase

### Wildcards

com.aol.cyclops.matcher.Predicates

contains a number of Wildcard Predicates

Predicates.__   (double underscore) indicates a wild card

```java
    new MyCase(4,5,6)._match(c ->c.isType( (MyCase ce)-> "hello").with(___,5,6),"goodbye")
```

The first value can be a Wildcard, the second and third should be 5 & 6.

Predicates.ANY() can also be used as a Wildcard. ANY() is capitalised to differentiate from Hamcrest Matchers any()

### Recursive matching

It is possible to recursivley match on values. For example if the entity being matched on consists of other entities we can match recurisvely on those.

com.aol.cyclops.matcher.Predicates.with  - facilitates recursive matching

e.g.

```java
    new MyCase(1,new MyEntity(10,11),6)._match(c ->c.isType( (MyCase ce)-> "hello").with(___,with(10,__),6),"goodbye")
```
or in fully expanded form 

```java
    new MyCase(1,new MyEntity(10,11),6)._match(c ->c.isType( (MyCase ce)-> "hello").with(Predicates.___,Predicates.with(10,Predicates.__),6),"goodbye")
```

### Interfaces that extend Matchable

* ValueObject
* StreamableValue
* CachedValues, PTuple1-8

## Coercing any Object to a Matchable

```java
    As.asMatchable(myObject).match(this::makeFinancialDecision)
```

com.aol.cyclops.dynamic.As provides a range of methods to dynamically convert types/

# The Decomposable Interface  / Trait

The Decomposable Interface defines an unapply method that is used to convert the implementing Object into an iterable. This can be used to control how Cyclops performs recursive decomposition.

```java
    public <I extends Iterable<?>> I unapply();
```
    
### Interfaces that extend Decomposable

* ValueObject
* StreamableValue
* CachedValues, PTuple1-8

## Coercing any Object to a Decomposable

```java
    As.asDecomposable(myObject).unapply().forEach(System.out::println);
```

com.aol.cyclops.dynamic.As provides a range of methods to dynamically convert types

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
    
## The RecursiveMatching class

Matching provides a number of builders for performing advanced pattern matching.

Features available via the Matching class include


* Match by type or value recursively
* Sequential, Parallel and Async execution
* Recursively decompose and match against Case classes
* Fluent step builders for common cases
* Match on first (return Optional)
* Match many (return Stream)
* Match against streams of data
* Usable within a Stream (strategy pattern)
* Fluent step builders

## Operators

At the top level the operators are 

* *when* : to define a new case
* *whenIsType*  : define a new case and match by type

Second level operators are

* *isType* : provide a lambda / Function that will be used to both verify the type and provide a return value if triggered


Further Operators 

* *thenApply* : final action to determine result on match
* *thenConsume* : final action with no return result on match
* *thenExtract* : extract a new value to pass to next stage (or as result)

Examples : 

Parser Example 

```java

    @AllArgsConstructor(access=AccessLevel.PRIVATE) static abstract class  Expression implements Decomposable{}
    final static class X extends Expression{ }
    @Value final static class Const extends Expression  { int value; }
    @Value final static class Add<T extends Expression, R extends Expression> extends Expression { T left; R right; }
    @Value final static class Mult<T extends Expression, R extends Expression> extends Expression  { T left; R right; }
    @Value final static class Neg<T extends Expression> extends Expression { T expr; }

public Integer eval(Expression expression, int xValue){
        
        return Matching.whenIsType( (X x)-> xValue)
                .whenIsType((Const c) -> c.getValue())
                .whenIsType((Add a) ->   eval(a.getLeft(),xValue) + eval(a.getRight(),xValue))
                .whenIsType( (Mult m) -> eval(m.getLeft(),xValue) * eval(m.getRight(),xValue))
                .whenIsType( (Neg n) ->  -eval(n.getExpr(),xValue))
                .match(expression).orElse(1);
        
        
        
    }
    
    // 1 + (2 * (X*X))
    Expression expr = new Add(new Const(1), new Mult(new Const(2), new Mult(new X(), new X()))); 
        
    assertThat(parser.eval(expr, 3),is(19));
``` 

