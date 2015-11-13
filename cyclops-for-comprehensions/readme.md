# Cyclops for comprehensions

## Getting cyclops-for-comprehensions

* [![Maven Central : cyclops-for-comprehensions](https://maven-badges.herokuapp.com/maven-central/com.aol.cyclops/cyclops-for-comprehensions/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.aol.cyclops/cyclops-for-comprehensions)


## Gradle

where x.y.z represents the latest version

compile 'com.aol.cyclops:cyclops-for-comprehensions:x.y.z'

## Maven

```xml
<dependency>
    <groupId>com.aol.cyclops</groupId>
    <artifactId>cyclops-for-comprehensions</artifactId>
    <version>x.y.z</version>
</dependency>
```

# Features

Simplify deeply nested looping (over Collections, even  Streams, Optionals and more!).

![for comprehensions](https://cloud.githubusercontent.com/assets/9964792/7887680/c6ac127c-062a-11e5-9ad7-ad4553761e8d.png)

# Docs

* [Javadoc for Cyclops For Comprehensions](http://www.javadoc.io/doc/com.aol.cyclops/cyclops-for-comprehensions/4.0.2)
* [Wiki for Extensible For Comprehensions](https://github.com/aol/cyclops/wiki/Extensible-For-Comprehensions-for-Java-8)

# Overview of Cyclops For Comprehensions

Two supported formats

1. Type Do Notation via Do.add / with
2. Untpyed Do Notation via UntypedDo.add  / with



# Do Notation
```java
	List<Integer> list= Arrays.asList(1,2,3);
	
	List<Integer> list = Do.add(list)
								.yield((Integer i)-> i +2)
								.unwrap();
				
										
		
	assertThat(Arrays.asList(3,4,5),equalTo(list));
```

Yield, Filter and 'and' take curried functions

(That is a chain of single input parameter functions)

```java
		Stream<Integer> stream = Do.add(asList(20,30))
								   .with( i->asList(1,2,3))
								   .yield(i-> j -> i + j+2)
								   .asSequence();
```

Parameters are stack based, the parameter to the first function is an index into the first Collection or Monad, the parameter to the second function is an index into the second Collection or Monad and so on.

The above code could be rewritten as 

```java
		Stream<Integer> stream = Do.add(asList(20,30))
								   .with(any->asList(1,2,3))
								   .yield(x-> y -> x + y+2)
								   .asSequence();
```
And it would work in exactly the same way

```java
		List<Integer> list= Arrays.asList(1,2,3);
		Stream<Integer> stream = Do.add(list)
								.filter(a -> a>2)
								.yield(a-> a +2)
								.asSequence();
				
										
		
		assertThat(Arrays.asList(5),equalTo(stream.collect(Collectors.toList())));
```

# for comprehensions explained

For comprehensions are useful for iterating over nested structures (e.g. collections, Streams, Optionals, CompletableFutures or other Monads).
    
Given a list of Strings 
```java
     List<String> list = Arrays.asList("hello","world","3");
 ```
We can iterate over them using Java 5 'foreach' syntax
     
```java
     for(String element : list){
     	System.out.println(element);
     }
```   

The equivalent for comprehension would be 

 ```java   	
    Do.add(list)
      .yield( element ->  element )
      .forEach(System.out::println);  
 ```
     
 We have simply converted the list to a Stream and are using Stream forEach to iterate over it.                        	  
                                      
But.. if we nest our looping

```java	
	  List<Integer> numbers = Arrays.asList(1,2,3,4);

	  for(String element : list){
	     for(Integer num : numbers){
     		System.out.println(element + num);
     	  }
      }                              
```

Things start to become a little unwieldy, but a little less so with for comprehensions
      
 ```java    
    Do.add(list)
      .with(element -> numbers)
      .yield(  element -> num  -> element + num )
      .unwrap()
      .forEach(System.out::println);
 ```     
                                  
Let's add a third level of nesting

```java
    List<Date> dates = Arrays.asList(new Date(),new Date(0));

    for(String element : list){
	     for(Integer num : numbers){
	    	 for(Date date : dates){
     			System.out.println(element + num + ":" + date);
     	 	 }
     		
     	  }
      }
 ```
    
 And the for comprehension looks like 
   
```java  
    Do.add(list)
      .add(numbers)
      .add(dates)
      .yield( element ->  num ->  date -> element + num+":"+date )
      .unwrap()
      .forEach(System.out::println);
 ```
 
 Stream map
  
```java    
     list.stream()
         .map(element -> element.toUpperCase())
         .collect(Collectors.toList());
 ```        
         
Can be written as

```java
	  ForComprehensions.foreach1(c -> c.mapAs$1(list))
	                                   .yield( (Vars1<String> v) -> c.$1().toUpperCase())
	                    .collect(Collectors.toList());
```    
 ## Mixing types
 
 Running a for comprehension over a list (stream) and an Optional
 
  ```java 		
		List<String> strs = Arrays.asList("hello","world");
		Optional<String> opt = Optional.of("cool");
		
		
		Do.add(strs)
          .add(opt)
          .yield(v1->v2 -> v1 + v2)
          .unwrap()
          .forEach(System.out::println);
```
										 
Outputs : [hellocool, worldcool]


Or the other way around 

```java
      	List<String> strs strs = Arrays.asList("hello","world");
		Optional<String> opt = Optional.of("cool");
		
		Do.add(opt)
		  .add(strs)
		  .yield(v1->v2 -> v1+ v2)
		  .<String>toSequence()
		  .forEach(System.out::println);
		
		assertThat(results.get(),hasItem("coolhello"));
		assertThat(results.get(),hasItem("coolworld"));
```
		
Outputs : [[coolhello],[coolworld]]

**The first type used controls the interaction!**

## Visualisation of CompletableFuture / Stream mixed combinations

**CompletableFuture defined first**
![do - completablefuture and stream](https://cloud.githubusercontent.com/assets/9964792/7887748/42efba28-062b-11e5-911a-5067e9095928.png)


**Stream defined first**
![do - stream and completablefuture](https://cloud.githubusercontent.com/assets/9964792/7887756/53519b2a-062b-11e5-9249-217d6c904a5e.png)

## Filtering

Guards (filter commands) can be placed at any stage of a for comprehension. E.g.
```java
                 Stream<Double> s = Do.with(Arrays.asList(10.00,5.00,100.30))
						.and((Double d)->Arrays.asList(2.0))
						.filter((Double d)-> (Double e) -> e*d>10.00)
						.yield((Double base)->(Double bonus)-> base*(1.0+bonus))
						.asSequence();
		
		double total = s.collect(Collectors.summingDouble(t->t));
		assertThat(total,equalTo(330.9));
```
## Convert any Object to a Monad

### Stream conversions

* Collection to Stream
* Iterable to Stream
* Iterator to Stream
* Array to Stream
* Int to IntStream.range(int)
* File to Stream
* URL to Stream
* BufferedReader to Stream
* InputStream to Stream
* ResultSet to Stream
* Enum to Stream
* String to Stream

* ObjectToStreamConverter

### Optional conversions

* NullToOptionalConverter
* Optional<Primitive> to Optional

### CompletableFuture Conversionss

* Callable to CompletableFuture
* Supplier to CompletableFuture

## Cyclops Monadic For Comprehensions

Cyclops for comphrensions allow deeply nested iterations or monadic operations to be expressed as a simple foreach expression. The implementation is inspired by the rather excellent Groovy implementation by Mark Perry (Functional Groovy)  see [Groovy Null Handling Using Bind, Comprehensions and Lift](https://mperry.github.io/2013/07/28/groovy-null-handling.html). They will work with *any* Monad type (JDK, Functional Java, Javaslang, TotallyLazy, Cyclops etc).

The Cyclops implementation is pure Java however, and although it will revert to dynamic execution when it needs to, reflection can be avoided entirely.

### Custom interfaces

 
* Support for custom interface definition with virtually unlimited nesting
```java
    Stream<Integer> stream = foreachX(Custom.class,  
									c-> c.myVar(list)
										.yield(()->c.myVar()+3)
									);

    Optional<Integer> one = Optional.of(1);
	Optional<Integer> empty = Optional.empty();
	BiFunction<Integer,Integer,Integer> f2 = (a,b) -> a *b; 
```		
	




### For more info

* [Scala Sequence Comprehensions](http://docs.scala-lang.org/tutorials/tour/sequence-comprehensions.html)
* [Scala yield] (http://docs.scala-lang.org/tutorials/FAQ/yield.html)

