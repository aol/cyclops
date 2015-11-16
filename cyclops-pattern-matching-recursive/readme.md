# Cyclops pattern matching (recursive)

Powerful Pattern Matching for Java. This module allows recursive mathcing with wildcards over Objects

## Getting cyclops-pattern-matching-recursive

* [![Maven Central : cyclops-pattern-matching-recursive](https://maven-badges.herokuapp.com/maven-central/com.aol.cyclops/cyclops-pattern-matching-recursive/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.aol.cyclops/cyclops-pattern-matching-recursive)


## Gradle

where x.y.z represents the latest version

compile 'com.aol.cyclops:cyclops-pattern-matching-recursive:x.y.z'

## Maven

```xml
<dependency>
    <groupId>com.aol.cyclops</groupId>
    <artifactId>cyclops-pattern-matching-recursive</artifactId>
    <version>x.y.z</version>
</dependency>
```
# Overview

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
Returns the default message "how are you?"	as values 4,2,3 don't match 1,2,3 or 4,5,6

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
