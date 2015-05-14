# Cyclops enable switch

An interface for representing a feature that can be enabled (switched on) or disabled.

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


    if(featureDisabled) 
          return Switch.disable(data);
    else
        return Switch.enable(data);



Now elsewhere you can check if the switch is enabled or disabled


    if(switch.isEnabled()){
          loadDataToDb(switch.get());
    }

</pre>

### More advanced usage
 
Switch can abstract away entirely the logic for managing whether a feature is enabled or disabled. Users can just code the enabled case and Switch will automatically make sure nothing happens when disabled.

The statement above can be rewritten as -


    switch.map(data -> loadDataToTheDb(data));

### Example usage

Creating the Switch 


    public synchronized Switch<Supplier<List<DomainExpression>>> readFile() {
		Supplier<List<DomainExpression>> s = ()->serialisedFileReader.readFileFromDisk(rawDomainRuleFileLocation);
		if (rawDomainEnabled) {
			return new Enabled(s);
		}
		return new Disabled(s);

	}


Using the Switch 


    Switch<Supplier<List<DomainExpression>>> domainExpressions; //lazy load data from db
     ...

    domainExpressions.stream().flatMap(s -> s.get().stream()).forEach(domainExpression->{
		
				definitions.put(domainExpression.getDerivedAttributeId(), domainExpression.getExpression());
				timestamps.put(domainExpression.getDerivedAttributeId(), domainExpression.getTimestamp());
			
		});


