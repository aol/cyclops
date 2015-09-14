# cyclops-validation

Functional style validation

## Getting cyclops-validation

* [![Maven Central : cyclops-validation](https://maven-badges.herokuapp.com/maven-central/com.aol.cyclops/cyclops-validation/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.aol.cyclops/cyclops-validation)


## Gradle

where x.y.z represents the latest version

compile 'com.aol.cyclops:cyclops-validation:x.y.z'

## Maven

```xml
<dependency>
    <groupId>com.aol.cyclops</groupId>
    <artifactId>cyclops-validation</artifactId>
    <version>x.y.z</version>
</dependency>
```

# Features

* Cumulative Validation


## Examples

Accumulate 

```java
ValidationResults results  = CumulativeValidator.of((User user)->user.age>18, "too young", "age ok")
												.isValid(user->user.email!=null, "user email null","email ok")
												.accumulate(new User(10,"email@email.com"));
	
		assertThat(results.getResults().size(),equalTo(2));
```

Accumulate until fail

```java
	ValidationResults<String,String> results  = CumulativeValidator.of((User user)->user.age>18, "too young", "age ok")
												.add(Validator.of((User user)->user.email!=null, "user email null","email ok"))
												.accumulateUntilFail(new User(10,"email@email.com"));
	
		assertThat(results.getResults().size(),equalTo(1));
```


# Dependencies

None

## Recommended in conjunction with

Can be used with FunctionalJava Validation

