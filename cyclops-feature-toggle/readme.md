# Cyclops Production Feature Toggle


## Getting cyclops-feature-toggle

* [![Maven Central : cyclops-feature-toggle](https://maven-badges.herokuapp.com/maven-central/com.aol.cyclops/cyclops-feature-toggle/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.aol.cyclops/cyclops-feature-toggle)


## Gradle

where x.y.z represents the latest version

compile 'com.aol.cyclops:cyclops-feature-toggle:x.y.z'
```xml
<dependency>
    <groupId>com.aol.cyclops</groupId>
    <artifactId>cyclops-feature-toggle</artifactId>
    <version>x.y.z</version>
</dependency>
```

#Features 

Cyclops Feature Toggle makes delivering on CI/CD easy by making it very simple to turn production features on & off!



### Rationale

Concrete type that conveys that a feature may be disabled or may be enabled (switchable).

####Features

* Enable / Disable classes (Pattern Match by type)
* convert to Optional or Stream
* standard Java 8 operators (map, flatMap, peek, filter, forEach) + flatten etc
* isEnabled / isDisabled
* Biased towards enabled (right biased).


### Getting started

The most basic way to use it is (if you are used to programming imperatively)

```java
    if(featureDisabled) 
          return FeatureToggle.disable(data);
    else
        return FeatureToggle.enable(data);

```

Now elsewhere you can check if the switch is enabled or disabled

```java
    FeatureToggle<Data> toggle;
    if(toggle.isEnabled()){
          loadDataToDb(toggle.get());
    }

```

### More advanced usage
 
FeatureToggle  can abstract away entirely the logic for managing whether a feature is enabled or disabled. Users can just code the enabled case and FeatureToggle  will automatically make sure nothing happens when disabled.

The statement above can be rewritten as -
```java

    toggle.map(data -> loadDataToTheDb(data));
```
### Example usage

Creating the FeatureToggle 

```java
    public synchronized FeatureToggle<Supplier<List<DomainExpression>>> readFile() {
		Supplier<List<DomainExpression>> s = ()->serialisedFileReader.readFileFromDisk(rawDomainRuleFileLocation);
		if (rawDomainEnabled) {
			return new Enabled(s);
		}
		return new Disabled(s);

	}
```

Using the Switch 

```java
    FeatureToggle<Supplier<List<DomainExpression>>> domainExpressions; //lazy load data from db
     ...

    domainExpressions.stream().flatMap(s -> s.get().stream()).forEach(domainExpression->{
		
				definitions.put(domainExpression.getDerivedAttributeId(), domainExpression.getExpression());
				timestamps.put(domainExpression.getDerivedAttributeId(), domainExpression.getTimestamp());
			
		});

```
