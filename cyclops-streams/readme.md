# cyclops-streams

Defines an api for more advanced sequential Streams. Extends java.util.stream.Stream and jool.Seq to add even more functionality. Reactive Streams support available if simple-react added to the classpath.

* Fast sequential Streams that can run asyncrhonously
* Reactive Stream Support
* Efficient Stream reversal
* static Stream Utilities
* SequenceM implementation
* Terminal operations that return a Future to be populated asynchronously
* HotStream support


# Dependencies

cyclops-invokedynamic
cyclops-monad-api
cyclops-sequence-api

## Recommended in conjunction with

simple-react

# Getting cyclops-sequence-api

## Gradle

where x.y.z represents the latest version

compile 'com.aol.cyclops:cyclops-streams:x.y.z'

# StreamUtils

## Multiple simultanous reduction with Monoids

    Monoid<String> concat = Monoid.of("",(a,b)->a+b);
	Monoid<String> join = Monoid.of("",(a,b)->a+","+b);


	StreamUtils.reduce(Stream.of("hello", "world", "woo!"),Stream.of(concat,join));

Results in ["helloworldwoo!",",hello,world,woo!"]

See also Monoid.reduce(Stream s)


## Cycle 

    StreamUtils.cycle(Stream.of(1,2,3)).limit(6).collect(Collectors.toList())
 
 Results in [1,2,3,1,2,3]
 
## Reverse

    StreamUtils.reverse(Stream.of(1,2,3)).collect(Collectors.toList())
   
Results in [3,2,1]  

## Stream creation from Iterable and Iterator

From Iterable

    StreamUtils.stream(Arrays.asList(1,2,3)).collect(Collectors.toList())

From Iterator

	StreamUtils.stream(Arrays.asList(1,2,3).iterator()).collect(Collectors.toList())    
	
## Reverse a Stream
 
 
     ReversedIterator.reversedStream(LazySeq.iterate(class1, c->c.getSuperclass())
						.takeWhile(c->c!=Object.class).toList());