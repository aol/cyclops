# Project Reactor Integration


## Get cyclops-reactor


* [![Maven Central : cyclops-reactor](https://maven-badges.herokuapp.com/maven-central/com.oath.cyclops/cyclops-reactor-integration/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.oath.cyclops/cyclops-reactor-integration)  
* [Javadoc for Cyclops-Reactor](http://www.javadoc.io/doc/com.oath.cyclops/cyclops-reactor)

	
# cyclops-reactor features include

* Native For Comprehensions for Flux and Monos
* Flux based ReactiveSeq implementation
  * Implement an extended Java 8 Stream using Reactor Flux
  * Full integration with cyclops-react Xtended collections
  * Asynchronously populate an Xtended Collection with Reactor Fluxs, materialize / block on first access
* AnyM monad wrapper for Monadic types (with full integration with cyclops-react Monad abstractions such as Kleisli)
    * Monad wrapper uses native Flux and Mono operators
    * Xtended Collections backed by Flux operate on Flux directly
* StreamT monad transformer operates directly with Flux
* MonoT monad transformer operates directly on Mono  
* Extension Operators for Flux and ReactiveSeq (extend ReactiveSeq with Flux and Flux with ReactiveSeq)
* Companion classes for Fluxs and Monos offering :
  * For comprehensions
   * Helper functions for combining / accumulating and zipping values	

# Reactive Collections!

In the example below we asynchronously populate an Xtended list using an Reactor Flux. Additional, reactive operations can be performed on the List asynchronously.
The ListX only blocks on first access to the data.

```java
import static cyclops.collections.mutable.ListX.listX;
import static cyclops.companion.rx.ObservableReactiveSeq.reactiveSeq;
AtomicBoolean complete = new AtomicBoolean(false);


Flux<Integer> async =  Flux.just(1,2,3)
                           .subscribeOn(Schedulers.fromExecutor(ForkJoinPool.commonPool());

ListX<Integer> asyncList = listX(reactiveSeq(async))
                                      .map(i->i+1);

System.out.println("Blocked? " + complete.get());

System.out.println("First value is "  + asyncList.get(0));

System.out.println("Completed? " + complete.get());
```
Which will print

```
Blocked? false
First value is 101
Completed? true
```

# For Comprehensions

For Fluxs

```java
Flux<Integer> result = Fluxs.forEach(Flux.just(10, 20),
                                     a -> Flux.<Integer> just(a + 10),
                                     (a, b) -> a + b);

result.collect(Collectors.toList())
      .block();
    
//[30, 50]
```

For Monos

```java
Mono<Tuple<Integer,Integer>> result = Monos.forEach(Mono.just(10),
                                                    a -> Mono.<Integer> just(a + 10),
                                                    Tuple::tuple);


    
//[10, 20]
```
# ReactiveSeq integration

Use the Fluxs companion class to create Flux backed ReactiveSeqs

Create an Flux-backed ReactiveSeq directly or from an Observable
```java
ReactiveSeq<Integer> seq = FluxReactiveSeq.just(1,2,3);
ReactiveSeq<Integer> seq2 = FluxReactiveSeq.reactiveSeq(FluxReactiveSeq.just(1,2,3));
```

With an Flux-back ReactiveSeq we can create Reactive Xtended Collections e.g. an extended j.u.List

```java
import static cyclops.collections.mutable.ListX.listX;
import static cyclops.companion.reactor.FluxReactiveSeq.reactiveSeq;

ListX<Integer> asyncList = listX(reactiveSeq(flux))
                                        .map(i->i+1);
```

Or a reactive Cyclops Vector

```java

import static cyclops.companion.reactor.FluxReactiveSeq.reactiveSeq;

VectorX<Integer> asyncList = VectorX.vectorX(reactiveSeq(flux))
                                    .map(i->i+1);


//vector is asynchronously populated by our Flux
//we can continue processing and block on first access or
//unwrap to raw vector type

asyncList.get(1); //will bock until data is available

//will also block until data is available
Vector<Integer> raw = asyncList.to(VavrConverters::Vector); 


```

Use the visit method on ReactiveSeq to pattern match over it's reactive nature

1. Synchronous
2. reactive-streams based async backpressure
3. pure asynchronous execution

For FluxReactiveSeq the visit method always executes the #2 function

```java

ReactiveSeq<Integer> seq = FluxReactiveSeq.just(1,2,3);

String type = seq.fold(sync->"synchronous",rs->"reactive-streams",async->"pure async");
//"reactive-streams"

```

## Extension operators

Use Reactor to extend cyclops-react's array of operations for and from Flux

```java
import static cyclops.streams.ReactorOperators.flux;

ReactiveSeq<List<Integer>> seq = FluxReactiveSeq.of(1,2,3)
                                      .map(i->i+1)
                                      .to(flux(o->o.buffer(10)));
```

For and from Mono

```java
import static cyclops.streams.ReactorOperators.mono;

Future<Integer> future;
Future<Integer> useMonoLogOp = future.to(mono(m->m.log()));

```

Also use cyclops-react operators in your Flux and Mono computations

# AnyM monad abstraction

AnyM is a type that can represent any Java Monad (allowing us to write more abstract code). 

There are three types. AnyM abstracts over all monadic types. AnyMValue represents Monad types that resolve to a single scalar value (like Mono), AnyMSeq represents monad types that are sequences of values (just like Flux)
```

                        AnyM
                         |
                         |
              __________________________
             |                          |
          AnyMValue                  AnyMSeq    
                                                  
```

We can create an AnyM instance for an Flux via Fluxs

```java
Flux<Integer> myFlux;
AnyMSeq<flux,Integer> monad = Fluxs.anyM(myFlux);

monad.map(i->i*2)
     .zipWithIndex();
     .filter(t->t._1()<100l);
```

We can create an AnyM instance for an Mono via Monos

```java
Mono<Integer> mono;
AnyMValue<mono,Integer> monad = Monos.anyM(mono);

monad.map(i->i*2);

```

Convert back to Flux via Fluxs.raw (or ReactorWitness.flux) and to Mono via Monos.raw (or ReactorWitness.mono)

```java
AnyMSeq<flux,Integer> monad;
Flux<Integer> obs = Fluxs.raw(monad);
```


We can write generic methods that accept any Monad type

```java
public <W extends WitnessType<W>> AnyMSeq<W,Integer> sumAdjacent(AnyMSeq<W,Integer> sequence){
     return sequence.sliding(1)
                    .map(t->t.sum(i->i).get())
}
```

AnyM manages your Fluxs, they still behave reactively like Fluxs should!

```java
AtomicBoolean complete = new AtomicBoolean(false);

Flux<Integer> async =  Flux.just(1,2,3)
                           .subscribeOn(Schedulers.fromExecutor(ForkJoinPool.commonPool());
AnyMSeq<flux,Integer> monad = Fluxs.anyM(async);

monad.map(i->i*2)
     .forEach(System.out::println,System.err::println,()->complete.set(true));

System.out.println("Blocked? " + complete.get());
while(!complete.get()){
        Thread.yield();
}
```

```
Blocked? false
2
4
6
```

Fluxs can also be defined as part of the reactiveSeq family of types inside AnyM - ```AnyM<reactiveSeq,Integer>``` 
```java
AnyM<reactiveSeq,Integer> anyM = FluxReactiveSeq.just(1,2,3)
                                            .anyM();

ReactiveSeq<Integer> seq = Witness.reactiveSeq(anyM);


```

# StreamT monad transformer

Monad Transformers allow us to manipulate nested types - for example we could use the StreamT monad Transformer to manipulate a List of Fluxs as if it was a single Flux. Or an Flux inside an Mono as if we were operating directly on the Flux within.

## Creating StreamT

Via liftM in Fluxs
```java
ListX<Flux<Integer>> nested = ListX.of(Flux.just(10));
StreamT<list,Integer> listOfFluxs = Fluxs.liftM(nested.anyM());
StreamT<list,Integer> doubled = listOfFluxs.map(i->i*2);
```

Via Flux backed ReactiveSeq

```java
ReactiveSeq<Integer> reactive = FluxReactiveSeq.just(1,2,3);
StreamT<optional,Integer> transformer = reactive.liftM(Witness.optional.INSTANCE);
```

Extacting Flux from StreamT

Use the unwrapTo method in conjunction with Fluxs::fromStream to get an 
```java 
StreamT<list,Integer> trans = FluxReactiveSeq.just(1,2,3).liftM(list.INSTANCE);

AnyM<list,Flux<T>> anyM = trans.unwrapTo(Fluxs::fromStream);
```

Use Witness.list to convert to a List

```java
StreamT<list,Integer> trans = FluxReactiveSeq.just(1,2,3).liftM(list.INSTANCE);

ListX<Flux<Integer>> listObs = Witness.list(trans.unwrapTo(Fluxs::fromStream));
```

# MonoT monad transformer

The MonoT monad transformer is a monad transformer for working directly with nested Mono types

## Creating MonoT

Via liftM in Monos
```java
ListX<Mono<Integer>> nested = ListX.of(Mono.just(10));
MonoT<list,Integer> listOfMonos = Monos.liftM(nested.anyM());
MonoT<list,Integer> doubled = listOfMonos.map(i->i*2);
```

or just MonoT of

```java
ListX<Mono<Integer>> nested = ListX.of(Mono.just(10));
MonoT<list,Integer> listOfMonos = MonoT.of(nested.anyM());
MonoT<list,Integer> doubled = listOfMonos.map(i->i*2);
```

Extracting nested Monos from MonoT

```java
 MonoT<list,Integer> trans = Monos.liftM(ListX.of(Mono.just(1)).anyM());

AnyM<list,Mono<Integer>> anyM = trans.unwrap();
System.out.println(anyM);
```


# Kotlin style sequence generators

```java

import static cyclops.stream.Generator.suspend;
import static cyclops.stream.Generator.times;

i = 100;
k = 9999;

Flux<Integer> fi = Flux.fromIterable(suspend((Integer i) -> i != 4, s -> {

                         Generator<Integer> gen1 = suspend(times(2), s2 -> s2.yield(i++));
                         Generator<Integer> gen2 = suspend(times(2), s2 -> s2.yield(k--));

                         return s.yieldAll(gen1.stream(), gen2.stream());
                  }
               ));


//Flux(100, 101, 9999, 9998, 102)
```
