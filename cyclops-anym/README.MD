# Cyclops AnyM : A higher kinded monad abstraction

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
## Get cyclops-reactor


* [![Maven Central : cyclops-anym](https://maven-badges.herokuapp.com/maven-central/com.oath.cyclops/cyclops-anym/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.oath.cyclops/cyclops-anym)   [![javadoc.io](https://javadocio-badges.herokuapp.com/com.oath.cyclops/cyclops-anym/badge.svg)](https://javadocio-badges.herokuapp.com/com.oath.cyclops/cyclops-anym)
* [Javadoc for cyclops-anym](http://www.javadoc.io/doc/com.oath.cyclops/cyclops-anym)

## More info

* 
