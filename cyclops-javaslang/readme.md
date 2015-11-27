# Javaslang Integration

v6.2.0 of cyclops-javaslang requires v2.0.0 of Javaslang.

# Features

* AnyM / For Comprehension support for Javaslang Monads
* reactive-streams implementation for Javaslang Traversables
* conversion between Javaslang and other types
* Memoize javaslang functions with a configurable Cache (support LRU, or TTL)
* Javaslang Stream extensions (future operations, hot streams, stream manipulation)

# Details & Examples


## AnyM

Use Javaslang.anyM to create wrapped Javaslang Monads.

```java	
assertThat(Javaslang.anyM(Try.of(this::success))
			.map(String::toUpperCase)
			.flatMapOptional(Optional::of)
			.toSequence()
			.toList(),equalTo(Arrays.asList("HELLO WORLD")));
```

## For Comprehensions

Javaslang specific for-comprehensions

```java
    @Test
	public void futureTest(){
		
		Try<String> result = 	Do.monad(grind("arabica beans"))
					  .monad(heatWater(new Water(25)))
					  .withMonad(ground -> water -> brew(ground,water))
					  .monad(frothMilk("milk"))
					  .yield(ground ->water -> espresso->foam-> combine(espresso,foam))
					  .unwrap();
		
		System.out.println(result.get());
	}
	
	
	
	Try<String> grind(String beans) {
		 return Try.of(()->"ground coffee of "+ beans);
	}

	Try<Water> heatWater(Water water){
		 return Try.of(()->water.withTemperature(85));
		  
	}

	Try<String> frothMilk(String milk) {
		 return Try.of(()->"frothed " + milk);
	}

	Try<String>	brew(String coffee, Water heatedWater){
		  return Try.of(()->"espresso");
	}
	String combine(String espresso ,String frothedMilk) {
		return "cappuccino";
	}
```

## reactive-streams

cyclops-javaslang provides a reactive-stream publisher and reactive-streams subscriber implementation for Javaslang Traversables.

### Subscribe to a javaslang Stream

```java	
CyclopsSubscriber<Integer> subscriber =SequenceM.subscriber();
		
Stream<Integer> stream = Stream.of(1,2,3);
		
JavaslangReactiveStreamsPublisher.ofSync(stream)
				 .subscribe(subscriber);
		
subscriber.sequenceM()
	 .forEach(System.out::println);
```

### Publish to a javaslang Stream

```java	
SequenceM<Integer> publisher =SequenceM.of(1,2,3);
		
JavaslangReactiveStreamsSubscriber<Integer> subscriber = new JavaslangReactiveStreamsSubscriber<>();
publisher.subscribe(subscriber);
		
Stream<Integer> stream = subscriber.getStream();
		
		
stream.forEach(System.out::println);
```

Pacakage com.aol.cyclops.javaslang contains converters for types from various functional libraries for Java

* JDK
* Guava
* Functional Java
* jooλ
* simple-react

Supported Javaslang Monads include

* Try
* Either
* Option
* Stream
* Future
* Lazy
* List
* Array
* Stack
* Queue
* Vector
* HashSet


These are available in Cyclops Comprehensions, or via Cyclops AnyM.

## Example flatMap a Javaslang Try, returning an JDK Optional

```java	
    assertThat(Javaslang.anyM(Try.of(this::success))
			.map(String::toUpperCase)
			.flatMapOptional(Optional::of)
			.toSequence()
			.toList(),equalTo(Arrays.asList("HELLO WORLD")));
```	


## HotStream example 

Use StreamUtils to start a Stream emitting on another thread

```java	
volatile Object value;

	public void hotStream() throws InterruptedException{
		value= null;
		CountDownLatch latch = new CountDownLatch(1);
		StreamUtils.hotStream(Stream.ofAll(1,2,3)
				.peek(v->value=v)
				.peek(v->latch.countDown())
				,exec);
		
		latch.await();
		assertTrue(value!=null);
	}
		
```

Connect to a HotStream that is already emitting data

```java
StreamUtils.hotStream(Stream.range(0,Integer.MAX_VALUE)
					.take(100)
					.peek(v->value=v)
					.peek(v->latch.countDown())
					.peek(System.out::println)
					,exec)
					.connect()
					.take(100)
					.forEach(System.out::println);
```

## Augmenting existing Stream classes with Lombok ExtensionMethod

```java
@ExtensionMethod(StreamUtils.class)
public  class FutureTest {


	public void aysncStream(){ 
	
	  CompletableFuture<Integer> total =  Stream.of(1,2,3,4,5)
	         									.map(it -> it*100)
	        									.futureOperations(exec)
			 									.reduce( (acc,next) -> acc+next);
	
	    //1500
	}


}
```

## Memoization with a Guava cache

Example configuration for Memoization with a Guava cache with TTL of 10 minutes after writing

```java
		Cache<Object, Integer> cache = CacheBuilder.newBuilder()
			       .maximumSize(1000)
			       .expireAfterWrite(10, TimeUnit.MINUTES)
			       .build();
	
		Cacheable<Integer> cacheable = (key,fn)->  { 
					try {
						return cache.get(key,()->fn.apply(key));
					} catch (ExecutionException e) {
						 throw ExceptionSoftener.throwSoftenedException(e);
					}
		};
		
		Function2<Integer,Integer,Integer> s = memoizeBiFunction( (a,b)->a + ++called,
										cacheable);
		assertThat(s.apply(0,1),equalTo(1));
		assertThat(s.apply(0,1),equalTo(1));
		assertThat(s.apply(0,1),equalTo(1));
		assertThat(s.apply(1,1),equalTo(3));
		assertThat(s.apply(1,1),equalTo(3));
```

## Get cyclops-javaslang


* [![Maven Central : cyclops-for-comprehensions](https://maven-badges.herokuapp.com/maven-central/com.aol.cyclops/cyclops-javaslang/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.aol.cyclops/cyclops-javaslang)
* [Javadoc for Cyclops Javaslang](http://www.javadoc.io/doc/com.aol.cyclops/cyclops-javaslang/6.1.0)
