OptionalT
========

OptionalT class allow you to convert AnyM monad to its Optional (like in Java) analog.

Creating OptionalT
------------------
You can build OptionalT from AnyM using methods `fromAnyM` or `of`.
```java
OptionalT<Integer> optionalInteger = OptionalT.of(AnyM.<Integer> ofNullable(4).map(Optional::of));
OptionalT<Integer> optionalInteger2 = OptionalT.fromAnyM(AnyM.<Integer> ofNullable(4));
```


Transforming OptionalT
--------------
Main feature of OptionalT is ability to perform data transformation using it. You can use `map` method

```java
OptionalT<Integer> optionalInteger2 = OptionalT.fromAnyM(AnyM.<Double> ofNullable(4)).map(s -> s.intValue());
```

in case you need to use partial function you can use `flatMap`

```java
private OptionalT<Integer> fromDouble(Double d) {
//either return something or nothing
}
OptionalT<Integer> optionalInteger = OptionalT.fromAnyM(AnyM.<Double> ofNullable(4)).flatMap(this::fromDouble);
```

OptionalT also support lifting. So if you can convert you `A -> B` function to `OptionalT<A> -> OptionalT<B>` 
function using lift method
```java
Function<OptionalT<String>, OptionalT<Integer>> f = OptionalT.lift((String string) -> string.length());
```
In case you need two argument function you can use lift
```java
BiFunction<OptionalT<String>, OptionalT<String>, OptionalT<Integer>> f = OptionalT
				.lift2((String s1, String s2) -> s1.length() * s2.length());
```
