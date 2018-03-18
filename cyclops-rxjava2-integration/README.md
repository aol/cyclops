# RxJava2 Integration


## Get cyclops-rxjava2


* [![Maven Central : cyclops-rxjava2](https://maven-badges.herokuapp.com/maven-central/com.oath.cyclops/cyclops-rxjava2/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.oath.cyclops/cyclops-rxjava2)   
* [Javadoc for cyclops-rxjava2](http://www.javadoc.io/doc/com.aol.cyclops/cyclops-rxjava2)


# cyclops-rxjava2 features include

* Support for Observables, Flowables, Singles and Maybes
* Native For Comprehensions (Obsevables, Flowables, Singles, Maybes)
* Observable & Flowablable based ReactiveSeq implementations
  * Implement an extended Java 8 Stream using Rx Java 2 Observables and Flowables
  * Full async / reactive-streams support (ObservableReactiveSeq / FlowableReactiveSeq)
  * Full integration with cyclops-react Xtended collections (Lazy Reactive Collections backed by RxJava 2 types)
  * Asynchronously populate an Xtended Collection with Rx Observables, materialize / block on first access
* AnyM monad wrapper for Monadic types (with full integration with cyclops-react Monad abstractions such as Kleisli)
    * Monad wrapper uses native Observable operators
    * Xtended Collections backed by Observable / Flowable operate on Observable / Flowable directly
* StreamT monad transformer operates directly with Observable    
* Extension Operators for Observable / Flowable and ReactiveSeq (extend ReactiveSeq with Observable / Flowable and Observable / Flowable with ReactiveSeq)
* Companion classes for Obserbables, Flowables, Singles, Maybes offering :
  * For comprehensions
  * Higher Kinded Typeclasses
  * Helper functions for combining / accumulating and zipping values
  

# Reactive Collections!

In the example below we asynchronously populate an Xtended list using an Rx Java Observable. Additional, reactive operations can be performed on the List asynchronously.
The ListX only blocks on first access to the data.

```java
import static cyclops.collections.mutable.ListX.listX;
import static cyclops.companion.rx2.Flowables.reactiveSeq;
AtomicBoolean complete = new AtomicBoolean(false);


Observable<Integer> async =  Observables.fromStream(Spouts.async(Stream.of(100,200,300), Executors.newFixedThreadPool(1)))
                                                .doOnComplete(()->complete.set(true));

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


```java
import static cyclops.companion.rx2.Observables.*;
 Observable<Integer> result = Observables.forEach(Observable.just(10, 20),
                                                   a -> Observable.<Integer> just(a + 10),
                                                   (a, b) -> a + b);

 result.toList()
       .toBlocking()
        .single()
        //[30, 50]

```
For Maybes

```java
Maybe<Tuple<Integer,Integer>> result = Maybes.forEach(Maybe.just(10),
                                                      a -> Maybe.<Integer> just(a + 10),
                                                     Tuple::tuple);


    
//[10, 20]
```

# ReactiveSeq integration

Use the Observables companion class to create Observable backed ReactiveSeqs

Create an Observable-backed ReactiveSeq directly or from an Observable
```java
ReactiveSeq<Integer> seq = Observables.just(1,2,3);
ReactiveSeq<Integer> seq2 = Observables.reactiveSeq(Observable.just(1,2,3));
```

And for Flowables

```java
ReactiveSeq<Integer> seq = Flowables.just(1,2,3);
ReactiveSeq<Integer> seq2 = Flowables.reactiveSeq(Flowable.just(1,2,3));
```

With an Observable-back ReactiveSeq we can create Reactive Xtended Collections e.g. an extended j.u.List

```java
import static cyclops.collections.mutable.ListX.listX;
import static cyclops.companion.rx2.Observables.reactiveSeq

ListX<Integer> asyncList = listX(reactiveSeq(observable))
                                        .map(i->i+1);
```

Or a reactive Vavr Vector

```java
import static cyclops.collections.vavr.VavrVectorX;
import static cyclops.companion.rx2.Flowables.reactiveSeq;

VectorX<Integer> asyncList = vectorX(reactiveSeq(flowable))
                                        .map(i->i+1);


//vector is asynchronously populated by our Observable
//we can continue processing and block on first access or
//unwrap to raw Vavr vector type

asyncList.get(1); //will bock until data is available

//will also block until data is available
Vector<Integer> raw = asyncList.to(VavrConverters::Vector); 


```



Use the visit method on ReactiveSeq to pattern match over it's reactive nature

1. Synchronous
2. reactive-streams based async backpressure
3. pure asynchronous execution

For ObservableReactiveSeq the visit method always executes the #3 function

```java

ReactiveSeq<Integer> seq = Observables.just(1,2,3);

String type = seq.visit(sync->"synchronous",rs->"reactive-streams",async->"pure async");

//pure async

ReactiveSeq<Integer> rs = Flowables.just(1,2,3);

String type = rs.visit(sync->"synchronous",rs->"reactive-streams",async->"pure async");

//reactive-streams

```


## Extension operators

Use RxJava2 to extend cyclops-react's array of operations across ReactiveSeq, Future, Maybe (and other cyclops-react types), as well as RxJava 2's Observables, Flowables, Singles, Maybes

```java
import static cyclops.streams.Rx2Operators.observable;

ReactiveSeq<List<Integer>> seq = Observables.of(1,2,3)
                                            .map(i->i+1)
                                            .to(observable(o->o.buffer(10)));
```

Use custom Rx Operators

```java
import static cyclops.streams.Rx2Operators.observable;

ReactiveSeq<List<Integer>> seq = Observables.of(1,2,3)
                                            .to(lift(new Observable.Operator<Integer,Integer>(){
                                                    @Override
                                                    public Subscriber<? super Integer> call(Subscriber<? super Integer> subscriber) {
                                                          return subscriber; // operator code
                                                    }
                                               }))
                                            .map(i->i+1)
```

For and from Mono

```java
import static cyclops.streams.Rx2OperatorsR.single;

Future<Integer> future;
Future<Integer> useSingleCacheOp = future.to(single(m->m.cache()));

```

# AnyM monad abstraction

AnyM is a type that can represent any Java Monad (allowing us to write more abstract code). 

There are three types. AnyM abstracts over all monadic types. AnyMValue represents Monad types that resolve to a single scalar value, AnyMSeq represents monad types that are sequences of values (just like Observable)
```

                        AnyM
                         |
                         |
              __________________________
             |                          |
          AnyMValue                  AnyMSeq    
                                                  
```

We can create an AnyM instance for an Observable via Observables (and for Flowable via Flowables)

```java
import static cyclops.monads.Rx2Witness.observable;

Observable<Integer> myObservable;
AnyMSeq<obsvervable,Integer> monad = Observables.anyM(myObsevable);

monad.map(i->i*2)
     .zipWithIndex();
     .filter(t->t._1()<100l);
```

Convert back to Observable via Observables.raw (or RxWitness.observable)

```java
AnyMSeq<obsvervable,Integer> monad;
Observable<Integer> obs = Observables.raw(monad);
```
For Flowable the code is very similar

```java
import static cyclops.monads.Rx2Witness.flowable;

Flowable<Integer> myFlowable;
AnyMSeq<flowable,Integer> monad = Flowables.anyM(myFlowable);

monad.map(i->i*2)
     .zipWithIndex();
     .filter(t->t._1()<100l);
```

Convert back to Flowable via Flowables.raw (or Rx2Witness.flowable)

```java
AnyMSeq<flowable,Integer> monad;
Observable<Integer> flow = Flowable.raw(monad);
```


We can write generic methods that accept any Monad type

```java
public <W extends WitnessType<W>> AnyMSeq<W,Integer> sumAdjacent(AnyMSeq<W,Integer> sequence){
     return sequence.sliding(1)
                    .map(t->t.sum(i->i).get())
}
```

AnyM manages your Observables, they still behave reactively like Observables should!

```java
AtomicBoolean complete = new AtomicBoolean(false);

ReactiveSeq<Integer> asyncSeq = Spouts.async(Stream.of(1, 2, 3), Executors.newFixedThreadPool(1));
Observable<Integer> observableAsync = Observables.observableFrom(asyncSeq);
AnyMSeq<obsvervable,Integer> monad = Observables.anyM(observableAsync);

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

Observables can also be defined as part of the reactiveSeq family of types inside AnyM - ```AnyM<reactiveSeq,Integer>``` 
```java
AnyM<reactiveSeq,Integer> anyM = Observables.just(1,2,3)
                                            .anyM();

ReactiveSeq<Integer> seq = Witness.reactiveSeq(anyM);


```

## AnyM for  Maybes and Singles 

We can create an AnyM instances for a Maybe via Maybes

```java
Maybe<Integer> maybe;
AnyMValue<maybe,Integer> monad = Maybes.anyM(maybe);

monad.map(i->i*2);

```
And Singles via Single
```java
Single<Integer> single;
AnyMValue<single,Integer> monad = Single.anyM(single);

monad.map(i->i*2);

```

# StreamT monad transformer

Monad Transformers allow us to manipulate nested types - for example we could use the StreamT monad Transformer to manipulate a List of Observables or Flowables as if it was a single Obsevable. Or an Observable inside an Optional as if we were operating directly on the Obsevable within.

## Creating StreamT for nested Observables

Via liftM in Observables
```java
ListX<Observable<Integer>> nested = ListX.of(Observable.just(10,20));
StreamT<list,Integer> listOfObservables = Observables.liftM(nested.anyM());
StreamT<list,Integer> doubled = listOfObservables.map(i->i*2);
```

Via Observable backed ReactiveSeq

```java
ReactiveSeq<Integer> reactive = Observables.just(1,2,3);
StreamT<optional,Integer> transformer = reactive.liftM(Witness.optional.INSTANCE);
```

Extacting Observable from StreamT

Use the unwrapTo method in conjunction with Observables::fromStream to get an 
```java 
StreamT<list,Integer> trans = Observables.just(1,2,3).liftM(list.INSTANCE);

AnyM<list,Observable<T>> anyM = trans.unwrapTo(Observables::fromStream);
```

Use Witness.list to convert to a List

```java
StreamT<list,Integer> trans = Observables.just(1,2,3).liftM(list.INSTANCE);

ListX<Observable<Integer>> listObs = Witness.list(trans.unwrapTo(Observables::fromStream));
```

## Creating StreamT for nested Flowables

Via liftM in Flowables
```java
ListX<Flowable<Integer>> nested = ListX.of(Flowable.just(10,20));
StreamT<list,Integer> listOfFlowables = Flowables.liftM(nested.anyM());
StreamT<list,Integer> doubled = listOfFlowables.map(i->i*2);
```

Via Observable backed ReactiveSeq

```java
ReactiveSeq<Integer> reactive = Flowables.just(1,2,3);
StreamT<optional,Integer> transformer = reactive.liftM(Witness.optional.INSTANCE);
```

Extacting Observable from StreamT

Use the unwrapTo method in conjunction with Observables::fromStream to get an 
```java 
StreamT<list,Integer> trans = Flowables.just(1,2,3).liftM(list.INSTANCE);

AnyM<list,Flowable<T>> anyM = trans.unwrapTo(Flowables::fromStream);
```

Use Witness.list to convert to a List

```java
StreamT<list,Integer> trans = Flowables.just(1,2,3).liftM(list.INSTANCE);

ListX<Flowable<Integer>> listObs = Witness.list(trans.unwrapTo(Flowables::fromStream));
```
# SingleT monad transformer

The SingleT monad transformer is a monad transformer for working directly with nested Single types

## Creating SingleT

Via liftM in Singles
```java
ListX<Single<Integer>> nested = ListX.of(Single.just(10));
SingleT<list,Integer> listOfSingles = Singles.liftM(nested.anyM());
SingleT<list,Integer> doubled = listOfSingles.map(i->i*2);
```

or just SingleT of

```java
ListX<Single<Integer>> nested = ListX.of(Single.just(10));
SingleT<list,Integer> listOfSingles = SingleT.of(nested.anyM());
SingleT<list,Integer> doubled = listOfSingles.map(i->i*2);
```

Extracting nested Singles from SingleT

```java
SingleT<list,Integer> trans = Singles.liftM(ListX.of(Single.just(1)).anyM());

AnyM<list,Single<Integer>> anyM = trans.unwrap();
System.out.println(anyM);
```
# MaybeT monad transformer

The MaybeT monad transformer is a monad transformer for working directly with nested Maybe types

## Creating MaybeT

Via liftM in Maybes
```java
ListX<Maybe<Integer>> nested = ListX.of(Maybe.just(10));
MaybeT<list,Integer> listOfMaybes = Maybes.liftM(nested.anyM());
MaybeT<list,Integer> doubled = listOfMaybes.map(i->i*2);
```

or just SingleT of

```java
ListX<Single<Integer>> nested = ListX.of(Maybe.just(10));
MaybeT<list,Integer> listOfMaybes = MaybeT.of(nested.anyM());
MaybeT<list,Integer> doubled = listOfMaybes.map(i->i*2);
```

Extracting nested Singles from SingleT

```java
MaybeT<list,Integer> trans = Maybes.liftM(ListX.of(Maybe.just(1)).anyM());

AnyM<list,Maybe<Integer>> anyM = trans.unwrap();
System.out.println(anyM);
```


