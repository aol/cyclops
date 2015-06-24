# Cyclops for comprehensions

Simplify deeply nested looping (over Collections, even  Streams, Optionals and more!).

![for comprehensions](https://cloud.githubusercontent.com/assets/9964792/7887680/c6ac127c-062a-11e5-9ad7-ad4553761e8d.png)

# Getting Cyclops For Comprehensions

* [![Maven Central : cyclops-for-comprehensions](https://maven-badges.herokuapp.com/maven-central/com.aol.cyclops/cyclops-for-comprehensions/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.aol.cyclops/cyclops-for-comprehensions)
* [Javadoc for Cyclops For Comprehensions](http://www.javadoc.io/doc/com.aol.cyclops/cyclops-for-comprehensions/4.0.2)
* [Wiki for Extensible For Comprehensions](https://github.com/aol/cyclops/wiki/Extensible-For-Comprehensions-for-Java-8)

# Overview of Cyclops For Comprehensions

Two supported formats

1. do nototation
2. scala like syntax



# Do Notation

	List<Integer> list= Arrays.asList(1,2,3);
	
	Stream<Integer> stream = Do.with(list)
								.yield((Integer i)-> i +2);
				
										
		
	assertThat(Arrays.asList(3,4,5),equalTo(stream.collect(Collectors.toList())));


Yield, Filter and 'and' take curried functions

(That is a chain of single input parameter functions)

		Stream<Integer> stream = Do.with(asList(20,30))
								   .and((Integer i)->asList(1,2,3))
								   .yield((Integer i)-> (Integer j) -> i + j+2);

Parameters are stack based, the parameter to the first function is an index into the first Collection or Monad, the parameter to the second function is an index into the second Collection or Monad and so on.

The above code could be rewritten as 

		Stream<Integer> stream = Do.with(asList(20,30))
								   .and((Integer any)->asList(1,2,3))
								   .yield((Integer x)-> (Integer y) -> x + y+2);

And it would work in exactly the same way

		List<Integer> list= Arrays.asList(1,2,3);
		Stream<Integer> stream = Do.with(list)
								.filter((Integer a) -> a>2)
								.yield((Integer a)-> a +2);
				
										
		
		assertThat(Arrays.asList(5),equalTo(stream.collect(Collectors.toList())));


# for comprehensions explained

For comprehensions are useful for iterating over nested structures (e.g. collections, Streams, Optionals, CompletableFutures or other Monads).
    
Given a list of Strings 

     List<String> list = Arrays.asList("hello","world","3");
     
We can iterate over them using Java 5 'foreach' syntax
     
     for(String element : list){
     	System.out.println(element);
     }
     

The equivalent for comprehension would be 

     ForComprehensions.foreach1(c -> c.mapAs$1(list)
                                	  .run( (Vars1<String> v) -> System.out.println(v.$1())
                                	  
 or with Do Notation
 	
    Do.with(list)
      .yield( (String element) -> {System.out.println(element); return null; } );                            	  
                                      
If we nest our looping
	
	  List<Integer> numbers = Arrays.asList(1,2,3,4);

	  for(String element : list){
	     for(Integer num : numbers){
     		System.out.println(element + num);
     	  }
      }                              

Things start to become a little unwieldy, but a little less so with for comprehensions
      
     ForComprehensions.foreach2(c -> c.flatMapAs$1(list)
                                      .mapAs$2((Vars2<String,Integer> v)->numbers)                                                    
                                      .run(v -> System.out.println(v.$1()+v.$2())

With Do notation

    Do.with(list)
      .and((String element) -> numbers)
      .yield( (String element) -> (Integer num) -> {System.out.println(element + num); return null; } );
      
                                  
Let's add a third level of nesting

    List<Date> dates = Arrays.asList(new Date(),new Date(0));

    for(String element : list){
	     for(Integer num : numbers){
	    	 for(Date date : dates){
     			System.out.println(element + num + ":" + date);
     	 	 }
     		
     	  }
      }
    
 And the for comprehension looks like 
   
     ForComprehensions.foreach3(c -> c.flatMapAs$1(list)
                                      .flatMapAs$2((Vars<String,Integer,Date> v) -> numbers)
                                      .mapAs$2(v -> dates)                                                    
                                      .run( v-> System.out.println(v.$1()+v.$2()+v.$3())
 
 
 With Do notation

    Do.with(list)
      .andJustAdd(numbers)
      .andJustAdd(dates)
      .yield( (String element) -> (Integer num) -> (Date date) -> {System.out.println(element + num+":"+date) ; return null; } );
 
 
 Stream map
      
     list.stream()
         .map(element -> element.toUpperCase())
         .collect(Collectors.toList());
         
         
Can be written as

	  ForComprehensions.foreach1(c -> c.mapAs$1(list))
	                                   .yield( (Vars1<String> v) -> c.$1().toUpperCase())
	                    .collect(Collectors.toList());
     
 ## Mixing types
 
 Running a for comprehension over a list (stream) and an Optional
 
     val strs = Arrays.asList("hello","world");  //using Lombok val
	 val opt = Optional.of("cool");
		
		
	  Seq<String> results = ForComprehensions.foreach2( c-> c.flatMapAs$1(strs)
										 .mapAs$2((Vars2<String,String> v) -> opt)
										 .yield( v -> v.$1() + v.$2()));
										 
Outputs : [hellocool, worldcool]


Or the other way around 


        val strs = Arrays.asList("hello","world");
		val opt = Optional.of("cool");
		
		
		Optional<List<String>> results = ForComprehensions.foreach2( c-> c.flatMapAs$1(opt)
										 .mapAs$2( (Vars2<String,String> v) -> strs)
										 .yield( v -> v.<String>$1() + v.$2()));
		
		assertThat(results.get(),hasItem("coolhello"));
		assertThat(results.get(),hasItem("coolworld"));
		
Outputs : [[coolhello],[coolworld]]

## Filtering

## Convert any Object to a Monad

### Stream conversions

Collection to Stream
Iterable to Stream
Iterator to Stream
Array to Stream
Int to IntStream.range(int)
File to Stream
URL to Stream
BufferedReader to Stream
InputStream to Stream
ResultSet to Stream
Enum to Stream
String to Stream

ObjectToStreamConverter

### Optional conversions

NullToOptionalConverter
Optional<Primitive> to Optional

### CompletableFuture Conversionss

Callable to CompletableFuture
Supplier to CompletableFuture

## Dynamic Proxy Caching

To support Monads that use non standard Functional interfaces, Cyclops will create / cache and reuse dynamic proxies that wrap JDK8 Functional interfaces in suitable wrappers.



## Cyclops Monadic For Comprehensions

Cyclops for comphrensions allow deeply nested iterations or monadic operations to be expressed as a simple foreach expression. The implementation is inspired by the rather excellent Groovy implementation by Mark Perry (Functional Groovy)  see [Groovy Null Handling Using Bind, Comprehensions and Lift](https://mperry.github.io/2013/07/28/groovy-null-handling.html). They will work with *any* Monad type (JDK, Functional Java, Javaslang, TotallyLazy, Cyclops etc).

The Cyclops implementation is pure Java however, and although it will revert to dynamic execution when it needs to, reflection can be avoided entirely.

### Features

 
1. Nested iteration over Collections & Maps
* Nested iteration over JDK 8 Monads - Stream, Optional, CompletableFuture
* Nested iteration over any external Monad e.g. Functional Java, Javaslang, TotallyLazy (by reflection, or register a Comprehender)
* Strict and looser typing    
* Fluent step builder interfaces with semantic naming
* Built in support for 3 levels of nesting
    
    stream.flatMap ( s1 -> stream2.flatMap( s2 -> stream3.map (s3 -> s3+s2+s1)));
    
    foreach3 (c -> c.flatMapAs$1(stream)
                   .flatMapAs$2(stream2)
                   .mapAs$3(stream3)
                   .yield(()->$3()+$2()+$1());

* Support for custom interface definition with virtually unlimited nesting

    Stream<Integer> stream = foreachX(Custom.class,  
									c-> c.myVar(list)
										.yield(()->c.myVar()+3)
									);

    Optional<Integer> one = Optional.of(1);
	Optional<Integer> empty = Optional.empty();
	BiFunction<Integer,Integer,Integer> f2 = (a,b) -> a *b; 
		
	Object result =  foreach2(c -> c.flatMapAs$1(one)
									.mapAs$2(v->empty)
									.yield((Vars2<Integer,Integer> v)->{return f2.apply(v.$1(), v.$2());}));

Each call to $ results in flatMap call apart from the last one which results in map. guard can be used for filtering.
The c.$1() and c.$2() calls capture the result of the operations at c.$1(_) and c.$2(_).

### There are 4 For Comphrension classes

* ForComprehensions :- static foreach methods with looser typing, and provides entry point to custom For Comprehensions
* ForComprehension1 :- Stricter typing, offers $1 operations only
* ForComprehension2 :- Stricter typing, offer $1 and $2 operations
* ForComprehension3 :- Stricter typing, offer $1, $2 and $3 operations

### Auto-Seq upscaling


### For more info

* [Scala Sequence Comprehensions](http://docs.scala-lang.org/tutorials/tour/sequence-comprehensions.html)
* [Scala yield] (http://docs.scala-lang.org/tutorials/FAQ/yield.html)

