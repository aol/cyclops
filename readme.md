# Cyclops

Super charge your JDK with one view into the functional Java world with

cyclops-converters, cyclops-pattern-matching, cyclops-lambda-utils, cyclops-enable-switch

Still to document - 
    interface changes in cyclops pattern matcher
    functional composition with cyclops pattern matcher
    more advanced scala style parser
    Algebraic Data Types & decomposition
  
Still to do  
     monoids for immutable list collection in cyclops-converters
     support for pCollections in cyclops converters
   

## cyclops-pattern-matching :

Advanced Scala-like pattern matching for Java 8

* Sequential, Parallel and Async execution
* Match by type, value, predicate or Hamcrest Matcher
* Recursively decompose and match against Case classes
* Fluent step builders for common cases
* Fluent, functionally compositional monad-like core Case and Cases classes 
* Support for chain of responsibility pattern within a Stream
* Support hamcrest matchers
* Java 8 predicates for matching.
* Match on first (return Optional)
* Match many (return Stream)
* Strict and lose typing
* Pre & post value extraction per case
* Match using multiple in case expressions via tuples or iterables of predicates / matchers
* Match against streams of data
* Usable within a Stream (strategy pattern)
* Fluent step builders
* Define cases in situ via method chaining or plugin in variables (implement Consumer)
* Match against collections with each element processed independently
* Three case types (standard, atomised, stream) can be mixed within a single Matching test

### Matching with hamcrest

       Matching.newCase().isMatch(hasItem("hello2"))
                         .thenConsume(System.out::println)
	       .match(Arrays.asList("hello","world"));

    //no match



#### With pre-extraction<

       Matching.newCase().extract(Person::getAge)
                         .isMatch(greaterThan(21)
                         .thenConsume(System.out::println)
                     .match(new Person("bob",24));
                         

    //prints 24 
    //bobs age


Note on Extraction 

If Person implements iterable (returning [name,age] - so age is at(1) )

then this code will also work

       Matching.newCase().extract(Extractors.at(1))
                         .isMatch(greaterThan(21)
                         .thenConsume(System.out::println)
                   .match(new Person("bob",24));




### With Post Extraction

         Matching.newCase().isMatch(is(not(empty())))
                                        .thenExtract(get(1)) //get is faster (than at) lookup for indexed lists
					.thenConsume(System.out::println)
				.match(Arrays.asList(20303,"20303 is passing",true));

    //prints 20303 is passing


### Matching with predicates


    Matching.newCase().isTrue((Integer a)-> a>100)
                      .thenApply(x->x*10)
            .match(101);

    //return Optional[1010]



With PostExtraction 


    Matching.newCase().isTrue((Person person)->person.getAge()>18)
                      .thenExtract(Person::getName)
                      .thenApply(name-> name + " is an adult")
            .match(new Person("rosie",39))

### Reusable cases

Example 

         Matching.newCase(c->c.extract(Person::getName)
                                  .isTrue((String name)->name.length()>5)
                                  .thenApply(name->name+" is too long"))
	        .newCase(c->c.extract(Person::getName)
                                  .isTrue((String name)->name.length()<3)
                                  .thenApply(name->name+" is too short"))				
             .match(new Person("long name",9))




can be refactored to 


    Consumer<AggregatedCase> nameTooLong => c->c.extract(Person::getName)
                                            .isTrue((String name)->name.length()>5) 
                                           .thenApply(name->name+" is too long");

    Consumer<AggregatedCase> nameTooShort => c->c.extract(Person::getName)
                                           .isTrue((String name)->name.length()<3)
                                           .thenApply(name->name+" is too short");

                Matching.newCase(nameTooLong)
                        .newCase(nameTooShort)
                        .match(new Person("long name",9))
### Matching against Tuples or Collections

It is possible to match against user data as a collection with each element verified separately

E.g. For the user input [1,"hello",2] each element can be verified and processed independently

        Matching.atomisedCase().allValues(10,ANY,2).thenApply(l->"case1")

			.atomisedCase().allValues(1,3,8).thenApply(l->"case2")

			.atomisedCase().bothTrue((Integer i)->i==1,(String s)->s.length()>0)
					.thenExtract(Extractors.<Integer,String>toTuple2())
					.thenApply(t->"Integer at pos 0 is " + t.v1+ " + String at pos 1 is " +t.v2)

			.match(1,"hello",2)

### Strategy pattern within a Stream



   		Stream.of(1,2,3,4).map(Matching.newCase().isType((GenericRule rule)> selectRuleBuilder(rule)))
					    .map(o->o.orElse(defaultRuleBuilder())
                                            .map(RuleBuilder::applyRule)
                                            .collect(Collectors.toList());


### Chain of responsibility pattern within a Stream

A chain of responsibility can be implemented by creating a Stream of objects that implement ChainOfResponsibility.

ChainOfResponsibility is an interface that extends both Predicate and Function.

The Matcher will test each member of the chain via it's Predicate.test method with the current value to match against, if it passes, the Matcher will forward
the current value to the apply method on the selected member.

#### Example with default behaviour


         return	Seq.seq(urlListMatchRules)
					.map(Matching.streamCase().streamOfResponsibility(domainPathExpresssionBuilders.stream()))
					.map(o->o.orElse(new Expression()))  //type is Optional<Expression>
					.toList();

    //if there is no match we create a new Expression, otherwise we generate an expression
    // from the first member of the Chain of Responsibility to match


#### Example where no match is unnacceptable


       return	Seq.seq(urlListMatchRules)
					.map(Matching.streamCase().streamOfResponsibility(domainPathExpresssionBuilders.stream()).asUnwrappedFunction())
					.toList(); 

    //throws NoSuchElementException is no match found


#### Example where no match is acceptable


	private final List<ChainOfResponsibility<UrlListMatchRule,Expression>> domainPathExpresssionBuilders;
	
	return	Seq.seq(urlListMatchRules)
					.flatMap(Matching.streamCase().streamOfResponsibility(domainPathExpresssionBuilders.stream()).asStreamFunction())
					.toList();

    //empty list if no match


#### Example where multiple matches are acceptable


	return	Seq.seq(urlListMatchRules)
					.flatMap(Matching.streamCase().selectFromChain(domainPathExpresssionBuilders.stream())::matchMany)
					.toList();

    //in this case each rule can result in multiple Expressions being produced

### Scala parser example

parser.eval(expr, 3) == 19


	public Integer eval(Expression expression, int xValue){

		
		return Matching.newCase().isType( (X x)-> xValue)
				.newCase().isType((Const c) -> c.getValue())
				.newCase().isType((Add a) ->  eval(a.getLeft(),xValue) + eval(a.getRight(),xValue))
				.newCase().isType( (Mult m) -> eval(m.getLeft(),xValue) * eval(m.getRight(),xValue))
				.newCase().isType( (Neg n) ->  -eval(n.getExpr(),xValue))
				.match(expression).orElse(1);
		
		
	}
	
	
	
	static class Expression{ }
	
	static class X extends Expression{ }
	
	@AllArgsConstructor
	@FieldDefaults(makeFinal=true, level=AccessLevel.PRIVATE)
	@Getter
	static class Const extends Expression{
		int value;
		
	}
	@AllArgsConstructor
	@FieldDefaults(makeFinal=true, level=AccessLevel.PRIVATE)
	@Getter
	static class Add extends Expression{
		Expression left;
		Expression right;
		
	}
	
	@AllArgsConstructor
	@FieldDefaults(makeFinal=true, level=AccessLevel.PRIVATE)
	@Getter
	static class Mult extends Expression{
		Expression left;
		Expression right;
		
	}
	@AllArgsConstructor (access=AccessLevel.PROTECTED) 
	@FieldDefaults(makeFinal=true, level=AccessLevel.PRIVATE)
	@Getter
	static class Neg extends Expression{
		Expression expr;
		
		
	}

### Using the PatternMatcher Builder directly

#### Looser typing

Can match one value

    String result = new PatternMatcher()
	        		.inCaseOfValue(5,at(0),r-> "found "+r)
	        		.inCaseOfValue(10,at(0),r-> "found 10")
	        		.inCaseOfType(at(1),(FileNotFoundException e) -> "file not found")
	        		.inCaseOf(at(2),(Integer value)->value>1000,value -> "larger than 1000")
	        		.caseOf(at(2),(Integer value)->value>1000,System.out::println)
	                .<String>match(ImmutableList.of(10,Optional.empty(),999))
	                .orElse("ok");


or many

 
    List data = new PatternMatcher().inCaseOfType((String s) -> s.trim())
				.inCaseOfType((Integer i) -> i+100)
				.inCaseOfValue(100, i->"jackpot")
				.matchMany(100)
				.collect(Collectors.toList());



Match against many from a Stream


    List data = new PatternMatcher().inCaseOfType((String s) -> s.trim())
				.inCaseOfType((Integer i) -> i+100)
				.inCaseOfValue(100, i->"jackpot")
				.matchManyFromStream(Stream.of(100,200,300,100," hello "))
				.collect(Collectors.toList());








## cyclops-lambda-utils

### ImmutableClosedValue

This is a class that helps work around the limitations of Java 8 lambda expressions as closures. In particular the workings of 'effectively final'.

ImmutableClosedValue allows a capture value to be set exactly once

E.g. from cyclops-pattern-matching the code to make an Extracto memoised ->

    public static final <T,R > Extractor<T,R> memoised( Extractor<T,R> extractor){
		final ImmutableClosedValue<R> value = new ImmutableClosedValue<>();
		return input -> {
			return value.getOrSet(()->extractor.apply(input));
				
		};
		
	}

getOrSet is used to extract the value from the ImmutableClosedValue, and takes a Supplier as an argument. The Supplier is only invoked once (the first time).

### ClosedVar

ClosedVar represents a captured variable inside a Java 8 closure. Because of the effectively final rule we can't access variables from within a Closure, but we can mutate the state of captured Objects. ClosedVar holds a value we would like mutate (if really, really, neccessary)

     ClosedVar<Integer> timesCalled = new ClosedVar<>(0);
     Function<String,String> fn = input -> {
     			timesCalled.set(timesCalled.get()+1);
     			return input + timesCalled.get();
     }


## Cyclops enable switch

An interface for representing a feature that can be enabled (switched on) or disabled.

### Rationale

Concrete type that conveys that a feature may be disabled or may be enabled (switchable).

####Features

* Enable / Disable classes (Pattern Match by type)
* convert to Optional or Stream
* standard Java 8 operators (map, flatMap, peek, filter, forEach) + flatten etc
* isEnabled / isDisabled


### Getting started

The most basic way to use it is (if you are used to programming imperatively)


    if(featureDisabled) 
          return Switch.disable(data);
    else
        return Switch.enable(data);



Now elsewhere you can check if the switch is enabled or disabled


    if(switch.isEnabled()){
          loadDataToDb(switch.get());
    }

</pre>

### More advanced usage
 
Switch can abstract away entirely the logic for managing whether a feature is enabled or disabled. Users can just code the enabled case and Switch will automatically make sure nothing happens when disabled.

The statement above can be rewritten as -


    switch.map(data -> loadDataToTheDb(data));

### Example usage

Creating the Switch 


    public synchronized Switch<Supplier<List<DomainExpression>>> readFile() {
		Supplier<List<DomainExpression>> s = ()->serialisedFileReader.readFileFromDisk(rawDomainRuleFileLocation);
		if (rawDomainEnabled) {
			return new Enabled(s);
		}
		return new Disabled(s);

	}


Using the Switch 


    Switch<Supplier<List<DomainExpression>>> domainExpressions; //lazy load data from db
     ...

    domainExpressions.stream().flatMap(s -> s.get().stream()).forEach(domainExpression->{
		
				definitions.put(domainExpression.getDerivedAttributeId(), domainExpression.getExpression());
				timestamps.put(domainExpression.getDerivedAttributeId(), domainExpression.getTimestamp());
			
		});



## cyclops-converters 

* Immutable Java classes
* Immutable Java Collections
* Efficient lazy execution
* Streams and sequences
* Actors
* Safe concurrency
* Reactive programming


Integrates 

* Project Lombok : for immutable builders and withers
* Google Guava : for fast non-modifiable collections
* totallylazy : persistent collections, sequences, monads, actors
* javaslang : immutable collections, streams & sequences, monads, tuples, exception handling
* functionalJava : immutable collections, streams & sequences, monads, actors
* lazySeq : lazy Sequence
* jooλ : sequences, tuples,  exception handling
* simple-react : concurrent streaming library

Features

* Convert iterable sequences (i.e. Streams)
* Convert function types
* Convert tuples
* Convert Optional / Either types
* Syntax sugar over monads (e.g. tailRecursion)


