# Javaslang Integration

v5.0.0 of cyclops-javaslang requires v1.2.2 of Javaslang.



Use Javaslang.anyM to create wrapped Javaslang Monads.

Pacakage com.aol.cyclops.javaslang contains converters for types from various functional libraries for Java

* JDK
* Guava
* Functional Java
* jooλ
* simple-react

## Example flatMap a Javaslang Try, returning an JDK Optional

    assertThat(Javaslang.anyM(Try.of(this::success))
			.map(String::toUpperCase)
			.flatMapOptional(Optional::of)
			.toSequence()
			.toList(),equalTo(Arrays.asList("HELLO WORLD")));
			
## Get cyclops-javaslang


* [![Maven Central : cyclops-for-comprehensions](https://maven-badges.herokuapp.com/maven-central/com.aol.cyclops/cyclops-javaslang/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.aol.cyclops/cyclops-javaslang)
* [Javadoc for Cyclops Pattern Matching](http://www.javadoc.io/doc/com.aol.cyclops/cyclops-javaslang/5.0.0)
