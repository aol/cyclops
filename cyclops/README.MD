# Cyclops : Core functional platform for Java 8 and above

Cyclops core consists of the following features

* Persistent / purely functional data types
* Advanced sequential, parallel and asynchronous Streaming
* Functional control structures
* Pattern Matching
* Free monad and applicative
* Advanced function interfaces and algebraic types

Currently part of the core but migrating to their own module as part of Cyclops X

* JDK Companion clasess
* Reactive collections


## Get cyclops


* [![Maven Central : cyclops](https://maven-badges.herokuapp.com/maven-central/com.oath.cyclops/cyclops/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.oath.cyclops/cyclops)
* [Javadoc for cyclops](http://www.javadoc.io/doc/com.oath.cyclops/cyclops)

# Getting started examples

## Persistent (Functional) Data Structures

We can replace verbose and error prone JDK mutable collections with safer, performant functional equivalents

### Replace ArrayList with Vector

```java

List<Integer> arrayList = new ArrayList<>();
arrayList.add(10);
arrayList.add(20);
arrayList.remove(10);

```

```java

Vector<Integer> vector = Vector.of(10,20); //[10,20]
Vector<Integer> removed = vector.removeValue(10); //[20]

```

### Replace LinkedList with Seq

```java

List<Integer> arrayList = new LinkedList<>();
arrayList.add(10);
arrayList.add(20);
arrayList.remove(10);

```

```java

Seq<Integer> functionalLinkedList = Seq.of(10,20); //[10,20]
Seq<Integer> removed = functionalLinkedList.removeValue(10); //[20]

```

### Replace Stream with LazySeq or ReactiveSeq

Java's Stream is lazy but not a data structure. Cyclops' LazySeq is a Lazy LinkedList (like Scala's Stream) and ReactiveSeq is a more powerful Stream implementation (that implements both the Stream interface and the reactive-streams spec for asynchronous streaming with non-blocking back pressure).



```java

Stream<Integer> arrayList = Stream.of(1,2,3)
                                  .map(i->i*2);

```


```java

LazySeq<Integer> arrayList = LazySeq.of(1,2,3)
                                    .map(i->i*2);

ReactiveSeq<Integer> arrayList = ReactiveSeq.of(1,2,3)
                                            .map(i->i*2);
```

### Replace Optional with Option or Maybe

Option and Maybe prevent you from representing illegal states, it isn't possible to call get() on an empty Option or Maybe.
Option is eager and is the simplest and most performant choice for most use cases.
Maybe is lazy and (optionally) 'reactive' that makes it a good use case for functions that may return a value that you may conditionally or asynchronously execute in the Future.

```java

Optional<Integer> opt = Optional.empty();
if(opt.isPresent())
   return opt.get();
else
   return 0;

...
//somewhere else
opt.get(); // Exception!



```

```java


Option<Integer> opt = Option.none();

return opt.getOrElse(0);


Maybe<Integer> lazy = Maybe.fromLazyOption(()->Option.some(10));
//deferred Maybe[10]

Maybe<Integer> lazy = Maybe.fromLazyOption(()->Option.none());
//deferred Maybe[]

//reactive

Maybe<Integer> reactive = Maybe.fromFuture(future);
//async Maybe[future value]
```

Complete / push data into Reactive Sum types (Maybe) via complete method
```java
CompletableMaybe<Integer> async = Maybe.maybe();
async.map(this::doWorkWhenDataArrives);
...
//on a separate thread
async.complete(value)
```


# Replace Exceptions with Try

```java
public String loadData(File f) throws IOException{

}


public Try<String,IOException> loadData(File f);
```

# Replace CompletableFuture with Future

A Future implementation with more standard method names (map / flatMap / zip as opposed to thenApply / thenCompose / then Combine)
```java

CompletableFuture<Result> future;
CompletableFuture<String> result = future.thenApply(this::process)
                                         .thenCompose(this::asyncProces);


Future<Result> future;
Future<String> result = future.map(this::process)
                              .flatMap(this::asyncProces);
```

# Replace basic Suppliers with Eval for Lazy Evaluation

Eval offers a powerful abstraction over laziness with map / flatMap / zip operators, built in tail-recursion and more.

```java
Supplier<Integer> lazy = ()->calculateResult();

Eval<Integer> lazy = Eval.later(()->calculateResult()); //Memoizing / caching lazy call
Eval<String> chained = lazy.map(this::process)
                           .flatMap(this::lazyProcess);

```

# Replace mutable java maps with HashMap

cyclops.data.HashMap is a performant persistent HashMap (implemented via a Hash Array mapped Trie)

```java
Map<String,Integer> map = new j.u.HashMap<>();
map.put("key",10)
map.put("key2",200);
map.remove("key");

HashMap<String,Integer> map = cyclops.data.HashMap.of("key",10,"key2",200);
Immutable<String,Integer> removed = map.remove("key");


```
## Streaming

cyclops-react defines an interface ReactiveSeq for advanced Streaming operations. Multiple implementations are provided for synchronous / asynchronous and sequential / parallel streaming within cyclops-react. Use ReactiveSeq.XXX creational methods to build synchronous Streams, or Spouts.XXX creational methods to build asynchronous Streams. For parallel Streaming consider FutureStream.builder().

For alternative implenations backed by popular 3rd party Streaming libraries (such as RxJava Observables and Reactor Flux) see [cyclops integration modules](https://github.com/aol/cyclops). It is also possible to fluently use operators defined in 3rd party libraries with ReactiveSeq (e.g. to fluently make use of any operator defined on Flux).

Sequential Streams, with retry and forEach result + error.

```java
ReactiveSeq.range(0,1000)
           .map(this::processNext)
           .retry(this::mightFail)
           .forEach(System.out::println, System.err::println);
```

Mixed Sequential and Parallel Stream

```java
ReactiveSeq.range(0, 1000)
           .parallel(new ForkJoinPool(10),par -> par.map(this::parallelTransform))
           .map(this::sequentialTransform)
           .forEach(System.out::println,System.err::println,this::finished);
```

Single-threaded scatter / gather

```java
ReactiveSeq.of(1,2,3,4)
           .fanOut(s1->s1.filter(i->i%2==0).map(this::group1),
                   s2->s2.filter(i->i%2!=0).map(this::group2))
           .toListX();
 ```

Parallel scatter / gather

```java
ReactiveSeq.of(1,2,3,4)
           .parallelFanOutZipIn(s1->s1.filter(i->i%2==0).map(this::group1),
                                s2->s2.filter(i->i%2!=0).map(this::group2),(g1,g2)->process(g1,g2))
           .toListX();
 ```


Replaying Streams

```java
Stream<Integer> stream = ReactiveSeq.range(0,1000)
                                    .map(i->i*2);

stream.forEach(System.out::println);
List<Integer> replayed = stream.collect(Collectors.toList());
stream.map(i->"hello  " + i)
      .forEach(System.out::println);
```

Asynchronous stream execution

```java
//Future
 Executor ex = Executors.newFixedThreadPool(1)
 Future<Integer> asyncResult = ReactiveSeq.of(1,2,3,4)
                                          .foldFuture(ex,s->s.reduce( 50,(acc,next) -> acc+next));

 asyncResult.peek(System.out::println)
            .map(this::processResult);

```

Lazy / terminating fold

With LazySeq
```java
LazySeq.generate(()->true)
       .lazyFoldRight(false,(a, b)->a ? true : b.get());
//true

```

With ReactiveSeq

```java
ReactiveSeq.generate(this::process)
           .map(data->data.isSuccess())
           .combine((a,b)-> a ? false : true, (a,b) -> a|b)
           .findFirst(); //terminating reduction on infinite data structure

```




reactive-streams : Event Driven Push based Streams

```java
Executor exec = Executors.newFixedThreadPool(1);
Either<Throwable,Integer> resultOrError =    Spouts.publishOn(ReactiveSeq.of(1,2,3,4,5),exec);
                                                   .combine((a, b) -> a < 5, Semigroups.intSum)
                                                   .findFirstOrError();
```
In the example above a synchronous Stream is executed on the provided Executor and it's output pushed into another reactive-stream that sums numbers so long as the total remains below 5. Once the total exceeds 5 it's is pushed asynchronously into the returned Either type (alternatively an error may be pushed down instead). The Either type can continue the reactive chain. The code above is completely non-blocking.
Streams created using Spouts factory can by default support non-blocking backpressure as defined in the reactive-streams spec.

Backpressure free : Event Driven Push based Streams

```java
 Executor execA = Executors.newFixedThreadPool(1);
 Executor execB = Executors.newFixedThreadPool(1);
 Maybe<Integer> resultOrError =    Spouts.observeOn(ReactiveSeq.of(1,2,3,4,5),execA)
                                         .zip(Spouts.observeOn(ReactiveSeq.of(100,200),execB, (a,b)->a+b)
                                         .findOne();
```
The Spouts observeOn and async operators create event driven Streams that do not have the overhead of managing backpressure. In the above example the first result is pushed asynchronously into the reactive Maybe type.

## To create a synchronous Stream via Kotlinesque Sequence Generators

[More info](https://github.com/aol/cyclops-react/issues/616)

```java
ReactiveSeq.generate(suspend(times(10),s-> {
            System.out.println("Top level - should repeat after sequence completes!");
            return s.yield(1,
                           () -> s.yield(2),
                           () -> s.yield(3),
                           () -> s.yield(4));
                       }))
           .take(6)
           .printOut();
```
## To make use of Operators from Reactor

[via cyclops-reactor](https://github.com/aol/cyclops/tree/master/cyclops-reactor)

```java
import static cyclops.streams.ReactorOperators.flux;
ReactiveSeq<List<Integer>> seq = Spouts.of(1,2,3)
                                       .map(i->i+1)
                                       .to(flux(o->o.buffer(10)));
```
## To create a ReactiveSeq instance backed by an RxJava Observable

[via cyclops-rx](https://github.com/aol/cyclops/tree/master/cyclops-rx)
```java

ReactiveSeq<Integer> seq = Observables.of(1,2,3)
                                      .to(lift(new Observable.Operator<Integer,Integer>(){
                                              @Override
                                              public Subscriber<? super Integer> call(Subscriber<? super Integer> subscriber) {
                                                     return subscriber; // operator code
                                              }
                                       }))
                                       .map(i->i+1)
```

# Type dictionary

## cyclops.data
| type | description | characteristics |
|------|-------------|-----------------|
| Vector     | A fast persistent analog of ArrayList, implemented in terms of a bit-mapped trie | Strict, Higher kinded               |
| Seq     | A  persistent analog of LinkedList | Strict, Higher kinded               |
| LazySeq     | A  persistent lazy LinkedList  (analog of Scala's Stream type) | Lazy, Higher kinded               |
| IntMap     | A persistent analog of ArrayList | Strict, Higher kinded               |
| DifferenceList     | A  persistent List with efficient appends | Lazy, Higher kinded               |
| HList     | Heterogenous List (type safe List with many different types) | Lazy, Higher kinded               |
| NonEmptyList     | A  persistent List with at least one value - may be strict or lazy | Strict/Lazy, Higher kinded               |
| Zipper     | A  data structure for traversing and modifying a persistent list | Strict/Lazy, Higher kinded               |
| LazyString    | A  lazy String | Lazy, Higher kinded               |
| HashSet     | A  persistent HashSet  (implemented in terms of a Hash Array mapped Trie) | Strict, Higher kinded               |
| TrieSet     | A  persistent Set  (implemented in terms of a Hashed Patricia Trie) | Strict, Higher kinded               |
| Bag     | A  persistent Set like data structure that tracks duplicates   | Strict, Higher kinded               |
| TreeSet     | A  persistent Ordered Set   | Strict, Higher kinded               |
| HashMap     | A  persistent HashMap  (implemented in terms of a Hash Array mapped Trie) | Strict, Higher kinded               |
| TreeMap     | A  persistent TreeMap  (implemented in terms of a RedBlack Tree) | Strict, Higher kinded               |
| TrieMap     | A  persistent Map  (implemented in terms of a Hashed Patricia Trie) | Strict, Higher kinded               |
| LinkedMap     | A  persistent LinkedHashMap  (implemented in terms of a Hash Array mapped Trie) | Strict, Higher kinded               |
| DependentMap     | A  persistent Map that supports multiple key types, and value types are dependent on the key type | Strict, Higher kinded               |
| BankersQueue     | A  persistent Queue  (implemented in terms of the BankersQueue algorithm) | Strict, Higher kinded               |
| DIET     | Discrete interval encoded tree | Lazy, Higher kinded               |
| Range     | Represents a range of values of an arbitrary type | Lazy, Higher kinded               |
| Tree     | A persistent Tree data structure | Strict, Higher kinded               |
| Enumeration     | Represents a sequence of values of an arbitrary type | Strict, Higher kinded               |
| Eq     | Test for equality |  Higher kinded               |
| Ord     |  Order (less, equal, more) |  Higher kinded               |
| NaturalTransformation     | Transform one Higher Kinded type into another |  Higher kinded               |

## cyclops.data.tuple
| type | description | characteristics |
|------|-------------|-----------------|
| Tuple     | Factory methods for Tuples | Strict, Higher kinded               |
| Tuple0-Tuple8     | Tuple implementations | Strict, Higher kinded               |

## cyclops.control

| type | description | characteristics |
|------|-------------|-----------------|
| Option     | Strict Option type, illegal states are unrepresentable (unlike JDK Optional / Vavr Option) | Strict, Higher kinded               |
| Either     | Strict Either type, maybe one of two values, eager analogue of LazyEither. illegal states are unrepresentable            | Strict, sum type, Higher Kinded                 |
| Eval     | Lazy evaluation, optional caching            | Optionally Reactive or Coreactive, Lazy, tail recursive, Higher kinded                 |
| Try     | Represents a value or an exception. Only specified Exceptions are caught on creation by default.            | Eager or Lazy, Optionally reactive, optionally tail recursive, avoids error hiding                 |
| Ior     | Inclusive Or, maybe one of two values or both            | Eager, sum and product type                 |
| LazyEither     | Lazy Either type maybe one of two values, lazy analogue of Xor            | Optionally Reactive or Coreactive, Lazy, tail recursive, sum type                 |
| LazyEither3     | Lazy Either type maybe one of three values            | Optionally Reactive or Coreactive, Lazy, tail recursive, sum type                 |
| LazyEither4     | Lazy Either type maybe one of four values            | Optionally Reactive or Coreactive, Lazy, tail recursive, sum type                 |
| LazyEither5     | Lazy Either type maybe one of five values            | Optionally Reactive or Coreactive, Lazy, tail recursive, sum type                 |
| Trampoline     | Easy to use trampoline implementations (see also Free using SupplierKind)            | Lazy, tail recursive, concurrent                |
| Unrestricted     | "Java Friendly" implementation of the Free monad for Java, facilitates functional interpreters.         | Lazy, concurrent                |
| Future     | Potentially asynchronous task that may populate a result in the Future            | Eager async, Higher kinded                 |

### cyclops.control : Reactive Sum Types


| type | description | characteristics |
|------|-------------|-----------------|
| CompletableMaybe     | Reactive analogue of Optional (Just/None). Create via Maybe.maybe()             |     Reactive, Lazy, tail recursive, sum type         |
| CompletableEither     | Reactive Either type maybe one of two values, reactive analogue of Xor.  Create via Either.either()            | Reactive, Lazy, tail recursive, sum type                 |
| CompletableEither3     | Reactive Either type maybe one of three values.  Create via Either3.either3()            | Reactive, Lazy, tail recursive, sum type                 |
| CompletableEither4     | Reactive Either type maybe one of four values.  Create via Either4.either4()            | Reactive, Lazy, tail recursive, sum type                 |
| CompletableEither5     | Reactive Either type maybe one of five values.  Create via Either5.either5()            | Reactive,Lazy, tail recursive, sum type                 |


## cyclops.companion

| type | description | characteristics |
|------|-------------|-----------------|
| Optionals     | Extension methods, for comprehensions and Higher kinded type classes            | Higher kinded                 |
| CompletableFutures    | Extension methods, for comprehensions and Higher kinded type classes            | Higher kinded                 |
| Streams     | Extension methods, for comprehensions and Higher kinded type classes            | Higher kinded                 |
| Eithers     | Methods to generate an Either based on cyclops Adapter types            |                  |
| Functions     | Useful functions and for comprehenions for functions            |                  |
| BiFunctions     | useful combiners that don't obey the Semigroup laws           |                  |
| Semigroups     | Combiners for a wide range of types           |                  |
| Monoids     | Combiners with an identity value for a wide range of types            |              |
| Groups     | Combiners with an identity value and an inverse for a wide range of types            |              |
| Reducers     | Reducers for cyclops data types for using in cyclops folds          |              |
| Iterables     | Iterable extensions and for comprehensions for Iterable types          |              |



## cyclops.function

| type | description | characteristics |
|------|-------------|-----------------|
| Function1-Function8     | Extended Function interfaces supporting map / flatMap / applicative operations, currying, partial application, lifting, composition and more           |                  |
| Consumer3-Consumer5     | Additional Consumers           |                  |
| checked.Checked***     | Checked equivalents of JDK Function types           |                  |
| Predicate3-Predicate5     | Additional Predicates           |                  |
| Predicates     | Useful Predicates          |                  |
| FluentFunctions     | A fluent API for working with Functions - composition, lifting, AOP and more           |                  |
| PartialFunction |  Function for which a result does not exist for all inputs       |                  |
| Lambda    | An API for working with anomyous lambda expressions (type inferencing)          |                  |
| Effect    | An advanced Runnable that only causes side effects          |                  |
| Ordering    | A more powerful and easier to use Comparator          |                  |
| Memoize     | An API for caching pure functions         |                 |
| PartialApplicator     | An API for Partial Application of functions       |                  |
| Curry / CurryConsumer / CurryVariance     | An API for currying functions        |                  |
| Semigroup     | A function for combining values of the same type        |                 |
| Monoid     | A function for combining values of the same type, with an identity value     |                  |
| Group     | A function for combining values of the same type, with an identity value and an inverse     |                  |
| Reducer     | Accepted by cyclops fold /reduce methods for reducing sequences of values to cyclops data types     |                  |

# cyclops.reactive

| type | description | characteristics |
|------|-------------|-----------------|
| Spouts     | Creational factory methods for push based Streams with optional non-blocking back pressure (via reactive-streams).              | Lazy, parallel option, integrated primitive support, replayable, Higher kinded, Operator fusion                |
| ReactiveSeq     | Synchronous sequential stream, extends JDK Stream interface. Custom Stream faster engine. Streams are replayable.              | Lazy, parallel option, integrated primitive support, replayable, Higher kinded, Operator fusion                |
| Streamable     | Capturing and caching replayable Stream type              | Lazy, caching                |
| Generator     | Kotlinesque Sequence Generator              |             |


## com.oath.cyclops.async

| type | description | characteristics |
|------|-------------|-----------------|
| Adapter     | Interface for data transfer Adapters to connected Streams. Closing the adapter, closes the streams (impls - Queue, Topic, Signal)           |    Async             |
| Queue     | Facilitates asyncrhonous data transfer to mulitiple connected Streams, via any java.util.Queue impl, Continuations toallow consumers to become producers.           |    Async             |
| Topic     | Asynchronous data transfer to multiple connected Streams, all connected Streams recieve each message           |   Async              |
| Signal    | Asynchronous data transfer - changes in data are broadcast to connected Streams           |   Async              |
| QueueFactories    | Factories for generating wait-free and blocking queues           |   Async              |


## com.oath.cyclops2.util.box

| type | description | characteristics |
|------|-------------|-----------------|
| LazyImmutable     | Represents a set once only box type            | Eager execution                 |
| Mutable     | A mutable generic box type           | Eager execution                 |
| MutableInt     | A mutable primitive box type for ints          | Eager execution                 |
| MutableLong     | A mutable primitive box type for longs         | Eager execution                 |
| MutableDouble     | A mutable primitive box type for doubles        | Eager execution                 |
| MutableFloat     | A mutable primitive box type for floats        | Eager execution                 |
| MutableChar     | A mutable primitive box type for characters     | Eager execution                 |
| MutableByte     | A mutable primitive box type for bytes        | Eager execution                 |
| MutableBoolean     | A mutable primitive box type for booleans        | Eager execution                 |

## cyclops.stream

## cyclops.stream.ReactiveSeq

cyclops defines reactive (push/ event drive) and coreactive (iterative / interactive) Streaming capabilities via the interface ReactiveSeq.

There are 4 concrete implementations for this interface included in cyclops-react :-



| concrete type | factories | description | characteristics |
|------|-------------|-------------|-----------------|
| ReactiveStreamX    | Spouts | Asynchronous push based Streams. Optionally back-pressure aware (via reactive-streams)              | Reactive (push),Lazy, parallel option, integrated primitive support, replayable, Higher kinded                |
| StreamX    | ReactiveSeq | Synchronous sequential stream, extends JDK Stream interface. Custom Stream faster engine. Streams are replayable.              | Coreactive (pull), Lazy, parallel option, integrated primitive support, replayable, Higher kinded, Operator fusion                |
| OneShotStreamX    | Streams | Synchronous sequential stream, extends JDK Stream interface. Custom Stream faster engine. Streams are not replayable. Backed by j.u.s.Stream via jool.seq            | Lazy, parallel option, integrated primitive support, Higher kinded               |
| FutureStream     | LazyReact | Asynchronous and parallel stream  (contained in cyclops-futurestream)            | Lazy, async, parallel, Reactive                 |

Additional implementations provided in cyclops integration modules

| concrete type | factories | description | characteristics |
|------|-------------|-------------|-----------------|
| FluxReactiveSeq    | Fluxs | Asynchronous push based Streams, non-blocking back-pressure aware (via reactive-streams)              | Reactive (push),Lazy, parallel option, integrated primitive support, replayable, Higher kinded, Operator Fusion                |
| ObservableReactiveSeq    | Observables |  Asynchronous push based Streams             | Reactive (push),Lazy, parallel option, integrated primitive support, replayable, Higher kinded              |

Additional implementations provided in cyclops integration modules for RxJava 2

| concrete type | factories | description | characteristics |
|------|-------------|-------------|-----------------|
| ObservableReactiveSeq    | Observables |  Asynchronous push based Streams             | Reactive (push),Lazy, parallel option, integrated primitive support, replayable, Higher kinded, Operator Fusion               |
| FlowableReactiveSeq    | Flowables |  Asynchronous push based Streams, non-blocking back-pressure aware (via reactive-streams)             | Reactive (push),Lazy, parallel option, integrated primitive support, replayable, Higher kinded, Operator Fusion                |


Classes / Interfaces that represent the API (cyclops-react)

| type | description | characteristics |
|------|-------------|-----------------|
| FutureStream     | Asynchronous and parallel stream             | Lazy, async, parallel, Reactive                 |
| Spouts     | Creational factory methods for push based Streams with optional non-blocking back pressure (via reactive-streams).              | Lazy, parallel option, integrated primitive support, replayable, Higher kinded, Operator fusion                |
| ReactiveSeq     | Synchronous sequential stream, extends JDK Stream interface. Custom Stream faster engine. Streams are replayable.              | Lazy, parallel option, integrated primitive support, replayable, Higher kinded, Operator fusion                |
| Streamable     | Capturing and caching replayable Stream type              | Lazy, caching                |
| StreamSource     | Push data asynchronously into synchronous sequential or parallel Streams (e.g. JDK Stream, ReactiveSeq)              |             |

# Articles

* [Awesome Fluent Functions](https://medium.com/@johnmcclean/can-we-make-working-with-functions-easier-in-java-8-81ed9d1050f2#.apum92khr)
* [Easier Try with Cyclops](http://rdafbn.blogspot.com/2015/06/java-8-easier-with-cyclops-try.html)
* [4 flavors of Java 8 Functions](https://medium.com/@johnmcclean/4-flavours-of-java-8-functions-6cafbcf5bb4f)
* [Memoise Functions in Java 8](http://rdafbn.blogspot.com/2015/06/memoize-functions-in-java-8.html)
* [Strategy Pattern in Java 8 ](http://rdafbn.blogspot.com/2015/06/startegy-pattern-in-java-8.html)
* [Dependency injection using the Reader Monad in Java8](https://medium.com/@johnmcclean/dependency-injection-using-the-reader-monad-in-java8-9056d9501c75)
* [Scheduling a Stream](https://medium.com/@johnmcclean/how-to-schedule-emission-from-a-stream-in-java-aa2dafda7c07#.pi12so6zn)
* [Neophytes guide to Java 8 : Welcome to the Future](https://medium.com/@johnmcclean/neophytes-guide-to-java-8-welcome-to-the-future-83f432ce82a9#.jb5s9qop8)
* [JDBC Processing Options with cyclops-react](https://medium.com/@johnmcclean/jdbc-processing-options-with-cyclops-react-49d62b02f775#.1dh1ziaxv)
