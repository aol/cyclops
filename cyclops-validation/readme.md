# cyclops-validation

Functional style validation

* Cumulative & Sequential Validation


## Examples



```java
ValidationResults results  = CumulativeValidator.validate((User user)->user.age>18, "too young", "age ok")
												.isValid(user->user.email!=null, "user email null","email ok")
												.accumulate(new User(10,"email@email.com"));
	
		assertThat(results.getResults().size(),equalTo(2));
```


# Dependencies

None

## Recommended in conjunction with

FunctionalJava

# Getting cyclops-validation

## Gradle

where x.y.z represents the latest version

compile 'com.aol.cyclops:cyclops-validation:x.y.z'