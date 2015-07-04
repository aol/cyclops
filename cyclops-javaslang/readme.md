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