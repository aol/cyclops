# Cyclops AnyM : A higher kinded monad abstraction

*DEPRECATED in favour of cyclops-pure & cyclops.typeclasses.Do*

AnyM allows developers to abstract, in a type safe manner, over any Java monadic type.

e.g.

```java
Stream<Integer> stream = Stream.of(1,2,3);
AnyMSeq<stream,Integer> monad = AnyM.fromStream(stream);
```

This allows us to write more generic code, that operates on any monad type. For example to sum adjacent pairs of values in any sequence type we could write an ultra-generic method such as

```java
public <W extends WitnessType<W>> AnyMSeq<W,Integer> sumAdjacent(AnyMSeq<W,Integer> sequence){
     return sequence.sliding(1)
                    .map(t->t.sum(i->i).get())
}
```

to call this method for ListX we can use

```java

AnyM<listX,Integer> listX = AnyM.fromListX(ListX.of(1,2,3));

AnyM<listX,Integer> summedListX = sumAdjacent(listX);

ListX<Integer> summedX = Witness.listX(summedListX);

```

similarly for ReactiveSeq


```java
AnyMSeq<reactiveSeq,Integer> rs = AnyM.fromStream(ReactiveSeq.of(1,2,3));

AnyM<reactiveSeq,Integer> summedRs = sumAdjacent(rs);

ReactiveSeq<Integer> summedX = Witness.reactiveSeq(summedRs);

```
## Get cyclops-anym


* [![Maven Central : cyclops-anym](https://maven-badges.herokuapp.com/maven-central/com.oath.cyclops/cyclops-anym/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.oath.cyclops/cyclops-anym)   [![javadoc.io](https://javadocio-badges.herokuapp.com/com.oath.cyclops/cyclops-anym/badge.svg)](https://javadocio-badges.herokuapp.com/com.oath.cyclops/cyclops-anym)
* [Javadoc for cyclops-anym](http://www.javadoc.io/doc/com.oath.cyclops/cyclops-anym)

## More info

* [Blog post on using AnyM with Vavr](https://medium.com/@johnmcclean/a-type-safe-java-monad-api-part-i-with-vavrs-future-and-try-7beaea28d351)


## Using AnyM with 3rd party types

To make use of AnyM with currently unsupported 3rd party types, implement the MonadAdapter interface for that type, delegating to the appropriate map / filter / flatMap methods on the 3rd party type.


```
com.oath.anym.extensability.MonadAdapter
```

Define an enum which implements cyclops.monads.WitnessType

e.g. our implementation for the JDK's Optional looks like this

```java

public static enum optional implements WitnessType<optional>{
        INSTANCE;

        @Override
        public MonadAdapter<optional> adapter() {
            return OptionalAdapter.optional;
        }

    }
```

Then to embed Optional inside AnyM we provide a static utility method

```java

 public static <T> AnyMValue<optional,T> fromOptional(final Optional<T> opt) {
        Objects.requireNonNull(opt);
        return AnyM.fromValue(opt, optional.INSTANCE);

 }

```

And call it

```java
Optional<Integer> opt = Optional.empty();
AnyMValue<optional,Integer> anyM = fromOptional(opt);
```
The MonadAdapter implementation for Optional looks like this

```java
public class OptionalAdapter extends AbstractFunctionalAdapter<optional> implements ValueAdapter<optional> {

    private final Supplier<Optional<?>> empty;
    private final Function<?,Optional<?>> unit;


    public final static OptionalAdapter optional = new OptionalAdapter(()->Optional.empty(),t->Optional.of(t));



  private <U> Supplier<Optional<U>> getEmpty(){
        return (Supplier)empty;
    }
    private <U> Function<U,Optional<U>>  getUnit(){
        return (Function)unit;
    }
    private <U> Function<Iterator<U>,Optional<U>>  getUnitIterator(){
        return  it->it.hasNext() ? this.<U>getUnit().apply(it.next()) : this.<U>getEmpty().get();
    }
    public <T> Option<T> get(AnyMValue<optional,T> t){
        return Option.fromOptional((Optional<T>)t.unwrap());
    }

    @Override
    public <T> Iterable<T> toIterable(AnyM<optional, T> t) {
        return Maybe.fromOptional(optional(t));
    }


    @Override
    public <T> AnyM<optional, T> filter(AnyM<optional, T> t, Predicate<? super T> fn) {
        return fromOptional(optional(t).filter(fn));
    }


    @Override
    public <T> AnyM<optional, T> empty() {
        return fromOptional(this.<T>getEmpty().get());
    }


    @Override
    public <T, R> AnyM<optional, R> ap(AnyM<optional, ? extends Function<? super T,? extends R>> fn, AnyM<optional, T> apply) {
         return fromOptional(Optionals.zip(optional(apply), optional(fn),(a,b)->b.apply(a)));
    }

    @Override
    public <T, R> AnyM<optional, R> flatMap(AnyM<optional, T> t,
            Function<? super T, ? extends AnyM<optional, ? extends R>> fn) {
        return fromOptional(optional(t).<R>flatMap(fn.andThen(Witness::optional).andThen(Optionals::narrow)));
    }

    @Override
    public <T> AnyM<optional, T> unitIterable(Iterable<T> it)  {
       return fromOptional(this.<T>getUnitIterator().apply(it.iterator()));
    }

    @Override
    public <T> AnyM<optional, T> unit(T o) {
        return fromOptional(this.<T>getUnit().apply(o));
    }

    @Override
    public <T, R> AnyM<optional, R> map(AnyM<optional, T> t, Function<? super T, ? extends R> fn) {
        return fromOptional(optional(t).<R>map(fn));
    }
}
```

The process to support java.util.stream.Stream was very similar. Although the goal is to have create an AnyMSeq rather than AnyMValue.

We define the Witness enum
```java
 public static enum stream implements StreamWitness<stream>{
        INSTANCE;

        @Override
        public MonadAdapter<stream> adapter() {
            return StreamAdapter.stream;
        }

    }
```

And to create an AnyMSeq use AnyM.ofSeq rather than ofValue

```java

 public static <T> AnyMSeq<stream,T> fromStream(final Stream<T> s) {
        Objects.requireNonNull(s);
        return AnyM.fromSeq(s, stream.INSTANCE);

 }

```


# Type dictionary

### cyclops.monads

| type | description | characteristics |
|------|-------------|-----------------|
| AnyM     | Type safe monadic wrapper for any monad type            | Higher kinded                 |
| AnyMValue     | Type safe monadic wrapper for any monadic sum type            | Higher kinded                 |
| AnyMSeq     | Type safe monadic wrapper for any monadic non-scalar type            | Higher kinded                 |
| KleisliM    | A functional interface that represents a manipulatable function that takes a  value and returns a monad            | Higher kinded                 |
| CokleisliM    | A functional interface that represents a manipulatable function that takes a monad and returns a value           | Higher kinded                 |

#### cyclops.monads.function

| type | description | characteristics |
|------|-------------|-----------------|
| AnyMFunction0-AnyMFunction2     | Type aliases for monadically lifted functions          |                  |
| BinaryAnyMFuntion    | Type aliases for monadically lifted functions          |                  |
| UnaryAnyMFuntion    | Type aliases for monadically lifted functions          |                  |

#### cyclops.monads.transformers

| type | description | characteristics |
|------|-------------|-----------------|
| ListT     | Type safe list transformer for manipulating lists in a monadic context            | Higher kinded                 |
| SeqT     | Type safe list transformer for manipulating cyclops Seq's (persistent linked lists) in a monadic context            | Higher kinded                 |
| VectorT     | Type safe list transformer for manipulating cyclops Vector's (performant persistent Vectors) in a monadic context            | Higher kinded                 |
| LazySeqT     | Type safe list transformer for manipulating cyclops LazySeq's in a monadic context            | Higher kinded                 |
| StreamT     | Type safe Stream transformer for manipulating Streams in a monadic context            | Higher kinded                 |
| FutureT     | Type safe future transformer for manipulating futures in a monadic context            | Higher kinded                 |
| CompletableFutureT     | Type safe CompletableFuture transformer for manipulating futures in a monadic context     | Higher kinded |
| OptionalT     | Type safe Optional transformer for manipulating optionals in a monadic context          | Higher kinded                |
| XorT     | Type safe Monad transformer for manipulating Xors & Eithers in a monadic context            | Higher kinded                 |
| MaybeT     | Type safe Maybe transformer for manipulating lazy / reactive Maybes in a monadic context            | Higher kinded                 |
| OptionT     | Type safe Maybe transformer for manipulating strict Options in a monadic context            | Higher kinded                 |
| EvalT     | Type safe Eval transformer of manipulating Evals in a monadic context            | Higher kinded                 |

