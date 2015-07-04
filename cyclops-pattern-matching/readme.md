# Cyclops pattern matching

Powerful Pattern Matching for Java. Use lambda's, hamcrest or scala-like wildcards!

![pattern matching](https://cloud.githubusercontent.com/assets/9964792/8334707/3827c1e2-1a91-11e5-87b1-604905a75ecb.png)
  
# How to get Cyclops Pattern Matching 
              


* [![Maven Central : cyclops-for-comprehensions](https://maven-badges.herokuapp.com/maven-central/com.aol.cyclops/cyclops-pattern-matching/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.aol.cyclops/cyclops-pattern-matching)
* [Javadoc for Cyclops Pattern Matching](http://www.javadoc.io/doc/com.aol.cyclops/cyclops-pattern-matching/5.0.0)
* [Pattern Matching Wiki](https://github.com/aol/cyclops/wiki/Pattern-matching-:-Pattern-Matching-for-Java-8)

# Pattern Matching Structure & Examples

## Cyclops Pattern Matching is structured into two packages

1. A core which holds the cases to be executed
2. A set of builders which aims to make building pattern matching expressions simpler

Builders can build ontop of builders. The matchable interface provides the highest level of abstraction and is the recommended starting point.

Conversly Case and Cases provide the lowest level inputs into the pattern matcher.


* Matchable : provides succint pattern matching for some of the most common case types.
* Matching : provides a more powerful & flexible interface at the expense of increased verboseness
* PatternMatcher : provides a more flexible interface at the expense of looser typing
* Cases / Case : low level functional classes for building pattern matching cases

## The Matchable interface / trait


Objects that implement Matchable get a number of Pattern Matching helper methods by default.

match : matches by value
_match : matches by type and value
matchType : matches by type only

### Clean match statements

The cleanest way to use the Matchable instance is to encapsulate your matching logic inside a method with a name that indicates the intention of the matching. E.g.

	double benefits = employee.match(this::calcEmployeeBenefits);
	
	private CheckValues<I,T> calcEmployeeBenefits(CheckValues<I,T> c){
		return c.with(__,Bonus.PAYABLE,__).then(e->e.salary()*e.bonus())
		        .with(__,__,__).then(e->e.salary());
	}
	
* match example


	new MyCase(4,2,3).match(this::message,"how are you?");
	
	private <I,T> CheckValues<Object, T> message(CheckValues<I, T> c) {
		return c.with(1,2,3).then(i->"hello")
				.with(4,5,6).then(i->"goodbye");
	}
	
Returns the default message "how are you?"	as values 4,2,3 don't match 1,2,3 or 4,5,6

* _match example

    new MyCase(4,5,6)._match(c ->c.isType( (MyCase ce)-> "hello").with(1,2,3),"goodbye")
   
Returns "goodbye" as altough the type matches, 1,2,3 doesn't match 4,5,6

* matchType example

	new MyCase(4,5,6).matchType(c ->c.isType((MyCase ce) -> "hello")
	
Returns "hello" as MyCase is an instance of MyCase

### Wildcards

com.aol.cyclops.matcher.Predicates

contains a number of Wildcard Predicates

Predicates.__   (double underscore) indicates a wild card


    new MyCase(4,5,6)._match(c ->c.isType( (MyCase ce)-> "hello").with(___,5,6),"goodbye")

The first value can be a Wildcard, the second and third should be 5 & 6.

Predicates.ANY() can also be used as a Wildcard. ANY() is capitalised to differentiate from Hamcrest Matchers any()

### Recursive matching

It is possible to recursivley match on values. For example if the entity being matched on consists of other entities we can match recurisvely on those.

com.aol.cyclops.matcher.Predicates.with  - facilitates recursive matching

e.g.

    new MyCase(1,new MyEntity(10,11),6)._match(c ->c.isType( (MyCase ce)-> "hello").with(___,with(10,__),6),"goodbye")

or in fully expanded form 

	new MyCase(1,new MyEntity(10,11),6)._match(c ->c.isType( (MyCase ce)-> "hello").with(Predicates.___,Predicates.with(10,Predicates.__),6),"goodbye")

### Interfaces that extend Matchable

* ValueObject
* StreamableValue
* CachedValues, PTuple1-8

## Coercing any Object to a Matchable

    As.asMatchable(myObject).match(this::makeFinancialDecision)

com.aol.cyclops.dynamic.As provides a range of methods to dynamically convert types/

# The Decomposable Interface  / Trait

The Decomposable Interface defines an unapply method that is used to convert the implementing Object into an iterable. This can be used to control how Cyclops performs recursive decomposition.

	public <I extends Iterable<?>> I unapply();
	
### Interfaces that extend Decomposable

* ValueObject
* StreamableValue
* CachedValues, PTuple1-8

## Coercing any Object to a Decomposable

    As.asDecomposable(myObject).unapply().forEach(System.out::println);

com.aol.cyclops.dynamic.As provides a range of methods to dynamically convert types

# Creating Case classes

In Java it is possible to create sealed type hierarchies by reducing the visibilty of constructors. E.g. If the type hierarchies are defined in one file super class constructors can be made private and sub classes made final. This will prevent users from creating new classes externally. 
Lombok provides a number of annotations that make creating case classes simpler.

@Value :  see https://projectlombok.org/features/Value.html

## A sealed type hierarchy

An example sealed hierarchy (ValueObject implies both Matchable and Decomposable)

	@AllArgsConstructor(access=AccessLevel.PRIVATE) 
	public static class CaseClass implements ValueObject { } 
	@Value public static class MyCase1 extends CaseClass { int var1; String var2; }
	@Value public static class MyCase2 extends CaseClass { int var1; String var2; }

    CaseClass result;
    return result.match(this::handleBusinessCases);
    
    
## The Matching class

Matching provides a number of builders for performing advanced pattern matching.

Features available via the Matching class include


* Match by type, value, predicate or Hamcrest Matcher
* Sequential, Parallel and Async execution
* Recursively decompose and match against Case classes
* Fluent step builders for common cases
* Support for chain of responsibility pattern within a Stream
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

At the top level the operators are 

* *when* : to define a new case
* *whenValues*  : to define a new case, potentially recursively matching against the internal values of an Object
* *whenFromStream* : to a define a new case from a Stream of cases
* *whenIterable* : to specifically handle the case where the Object to match is an iterable

Second level operators are

* *isType* : provide a lambda / Function that will be used to both verify the type and provide a return value if triggered
* *isValue* : compare object to match against specified value
* *isTrue* : Use a Predicate to determine if Object matches
* *isMatch* : Use a Hamcrest Matcher to determine if Object matches

Further Operators 

* *thenApply* : final action to determine result on match
* *thenConsume* : final action with no return result on match
* *thenExtract* : extract a new value to pass to next stage (or as result)

Special cases

Iteables 

* *allTrue* : all the predicates must match
* *allMatch* : all the hamcrest matchers must match
* *allHold* : allows mix of predicates, hamcrest matchers and prototype values all of which must hold

Streams

* *streamOfResponsibility* : extract the matching cases from a Stream. Useful for introducing selection logic within your own Java 8 Streams


Examples : 

### With Hamcrest

    Matching.when().isMatch(hasItem("hello2")).thenConsume(System.out::println)
							.match(Arrays.asList("hello","world"))
	
methods xxMatch accept Hamcrest Matchers
							
### Matching multiple

     Stream<Integer> resultStream = Matching.when().isValue(100).thenApply(v-> v+100)
											.when().isType((Integer i) -> i)
											.matchMany(100);
											
Use the matchMany method to instruct cylops to return all results that match

### Inside a Stream

#### flatMap

Use asStreamFunction to Stream multiple results back out of a set of Cases.

     Integer num = Stream.of(1)
							.flatMap(Matching.when().isValue(1).thenApply(i->i+10).asStreamFunction())
							.findFirst()
							.get();							

asStreamFunction converts the MatchingInstance into a function that returns a Stream. Perfect for use within flatMap.

#### map
	
	Integer num = Stream.of(1)
							.map(Matching.when().isValue(1).thenApply(i->i+10))
							.findFirst()
							.get().get();	

Or drop the second get() (which unwraps from an Optional) with


	Integer num = Stream.of(1)
							.map(Matching.when().isValue(1).thenApply(i->i+10).asUnwrappedFunction())
							.findFirst()
							.get();	
							
							
### Async execution	

Use the Async suffix - available on the Cases object, when calling match to run the pattern matching asynchronously, potentially on another thread.

		CompletableFuture<Integer> result =	Matching.when().isValue(100).thenApply(this::expensiveOperation1)
													.when().isType((Integer i) -> this.exepensiveOperation2(i))
													.cases()
													.matchAsync(100)		
## The PatternMatcher class

The PatternMatcher builder is the core builder for Cyclops Cases, that other builder instances leverage to build pattern matching cases. It's API is unsuitable for general use in most applications, but can leveraged to build application specific Matching builders.


The patternMatcher class provides a lot of utility methods that are organisied as follows

* *inCaseOf* : match with a Predicate and return a result
* *caseOf* : match with a Predicate but no result will be returned
* *inMatchOf* : match with a Hamcrest Matcher and return a result
* *matchOf* : match with a Hamcrest Matcher but no result will be returned
