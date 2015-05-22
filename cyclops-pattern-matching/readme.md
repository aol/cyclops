# Cyclops pattern matching

Cyclops Pattern Matching is structured into two packages

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
		        .with(__,__,__).then(e->e.salary())
	}

### Interfaces that extends Matchable

* ValueObject
* StreamableValue
* CachedValues, PTuple1-8

## Coercing any Object to a Matchable

    As.asMatchable(myObject).match(this::makeFinancialDecision)

com.aol.cyclops.dynamic.As provides a range of methods to dynamically convert types/

# Creating Case classes

In Java it is possible to create sealed type hierarchies by reducing the visibilty of constructors. E.g. If the type hierarchies are defined in one file super class constructors can be made private and sub classes made final. This will prevent users from creating new classes externally. 
Lombok provides a number of annotations that make creating case classes simpler.

@Value :  see https://projectlombok.org/features/Value.html

## A sealed type hierarchy

An example sealed hierarchy

	@AllArgsConstructor(access=AccessLevel.PRIVATE) 
	public static class CaseClass implements Matchable { } 
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

Examples : 

### With Hamcrest

    Matching.newCase().isMatch(hasItem("hello2")).thenConsume(System.out::println)
							.match(Arrays.asList("hello","world"))
	
methods xxMatch accept Hamcrest Matchers
							
### Matching multiple

     Stream<Integer> resultStream = Matching.newCase().isValue(100).thenApply(v-> v+100)
											.newCase().isType((Integer i) -> i)
											.matchMany(100);
											
Use the matchMany method to instruct cylops to return all results that match

### Inside a Stream

#### flatMap

Use asStreamFunction to Stream multiple results back out of a set of Cases.

     Integer num = Stream.of(1)
							.flatMap(Matching.newCase().isValue(1).thenApply(i->i+10).asStreamFunction())
							.findFirst()
							.get();							

asStreamFunction converts the MatchingInstance into a function that returns a Stream. Perfect for use within flatMap.

#### map
	
	Integer num = Stream.of(1)
							.map(Matching.newCase().isValue(1).thenApply(i->i+10))
							.findFirst()
							.get().get();	

Or drop the second get() (which unwraps from an Optional) with


	Integer num = Stream.of(1)
							.map(Matching.newCase().isValue(1).thenApply(i->i+10).asUnwrappedFunction())
							.findFirst()
							.get();	
							
							
### Async execution	

Use the Async suffix - available on the Cases object, when calling match to run the pattern matching asynchronously, potentially on another thread.

		CompletableFuture<Integer> result =	Matching.newCase().isValue(100).thenApply(this::expensiveOperation1)
													.newCase().isType((Integer i) -> this.exepensiveOperation2(i))
													.cases()
													.matchAsync(100)		
## The PatternMatcher class

The PatternMatcher builder is the core builder for Cyclops