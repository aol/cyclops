# Guava Integration

## Getting cyclops-guava

* [![Maven Central : cyclops-guava](https://maven-badges.herokuapp.com/maven-central/com.aol.cyclops/cyclops-guava/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.aol.cyclops/cyclops-guava)


## Gradle

where x.y.z represents the latest version

compile 'com.aol.cyclops:cyclops-guava:x.y.z'

## Maven

```xml
<dependency>
    <groupId>com.aol.cyclops</groupId>
    <artifactId>cyclops-guava</artifactId>
    <version>x.y.z</version>
</dependency>
```

# Features

Currently requires Guava 18.0 or above

Use Guava.anyM to create wrapped Guava Monads.

Pacakage com.aol.cyclops.guava contains converters for types from various functional libraries for Java

* JDK
* Javaslang
* Functional Java
* jooλ
* simple-react

Supported Guava Monads include

* FluentIterable
* Optional

These are available in Cyclops Comprehensions, or via Cyclops AnyM.

